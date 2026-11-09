"""Index a bundled, unchanged PDF: search text and verified printed-page labels.
Usage: python pdf_index.py path/to/document.pdf
Requires pypdf and pdfplumber. Never edits the PDF itself.
"""
import argparse
import collections
import json
import re
from pathlib import Path
import pdfplumber
from pypdf import PdfReader


def footer_label(page):
    # Only the page margins, not numbers in the body or contents table.
    candidates = []
    for box in [(0, 0, page.width, page.height * .09),
                (0, page.height * .91, page.width, page.height)]:
        text = page.crop(box).extract_text() or ''
        for line in text.splitlines():
            m = re.fullmatch(r'\s*(?:Page\s+)?([0-9]{1,4}|[ivxlcdm]{1,8})(?:\s+of\s+\d+)?\s*', line, re.I)
            if m:
                label = m[1].lower()
                roman_pattern = r'm{0,3}(cm|cd|d?c{0,3})(xc|xl|l?x{0,3})(ix|iv|v?i{0,3})'
                if not label.isdecimal() and not re.fullmatch(roman_pattern, label):
                    if len(label) % 2 == 0 and all(label[j] == label[j+1] for j in range(0, len(label), 2)):
                        collapsed = label[::2]
                        if re.fullmatch(roman_pattern, collapsed):
                            label = collapsed
                candidates.append(label)
    return list(dict.fromkeys(candidates))


def infer_labels(candidates, metadata):
    n = len(candidates)
    # Strong evidence: a repeated physical-page/printed-page offset.
    votes = collections.Counter(i + 1 - int(label)
                                for i, row in enumerate(candidates)
                                for label in row if label.isdecimal())
    offset, count = votes.most_common(1)[0] if votes else (0, 0)
    supported = count >= max(3, n // 5)
    labels = [''] * n
    if supported:
        for i, row in enumerate(candidates):
            proposed = str(i + 1 - offset)
            if i + 1 > offset and (not row or proposed in row):
                labels[i] = proposed
    for i, row in enumerate(candidates):
        roman = [s for s in row if re.fullmatch('[ivxlcdm]+', s)]
        if len(roman) == 1 and (not labels[i] or i + 1 <= offset):
            labels[i] = roman[0]
    if metadata and metadata != [str(i + 1) for i in range(n)]:
        for i, label in enumerate(metadata):
            # Visible, unambiguous footer overrides conflicting metadata.
            if len(candidates[i]) == 1:
                labels[i] = candidates[i][0]
            elif not labels[i]:
                labels[i] = label
    return labels


def index_pdf(path):
    path = Path(path)
    reader = PdfReader(path)
    with pdfplumber.open(path) as pdf:
        clean_pages = [p.dedupe_chars() for p in pdf.pages]
        text = [p.extract_text() or '' for p in clean_pages]
        candidates = [footer_label(p) for p in clean_pages]
    labels = infer_labels(candidates, reader.page_labels)
    base = path.with_suffix('')
    Path(str(base) + '.search.json').write_text(json.dumps(text, ensure_ascii=False), encoding='utf-8')
    Path(str(base) + '.labels.json').write_text(json.dumps({
        'labels': labels,
        'method': 'Visible margin labels and consistent offsets, with PDF page-label metadata fallback. Blank labels use physical page navigation.',
        'unresolved': [i + 1 for i, v in enumerate(labels) if not v],
    }, ensure_ascii=False, indent=2), encoding='utf-8')
    return labels


if __name__ == '__main__':
    parser = argparse.ArgumentParser()
    parser.add_argument('pdf')
    args = parser.parse_args()
    labels = index_pdf(args.pdf)
    print(f'{len(labels)} pages indexed; {sum(bool(x) for x in labels)} printed labels found.')
