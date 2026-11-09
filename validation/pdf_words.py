"""Create word rectangles and Contents targets without changing source PDFs."""
import json,re
from pathlib import Path
import pdfplumber
root=Path(__file__).resolve().parents[1]/'app/src/main/assets/standards'
for name in ['access','dw144','htm0301']:
 pages=[];contents=None
 with pdfplumber.open(root/(name+'.pdf')) as pdf:
  for i,p in enumerate(pdf.pages):
   p=p.dedupe_chars();text=p.extract_text() or ''
   if contents is None and i<30 and any(re.fullmatch(r'(?:table of )?contents',line.strip(),re.I) for line in text.splitlines()):contents=i
   pages.append([[w['text'],*[round(v,6) for v in (w['x0']/p.width,w['top']/p.height,w['x1']/p.width,w['bottom']/p.height)]] for w in p.extract_words()])
 (root/(name+'.words.json')).write_text(json.dumps({'contents':contents,'pages':pages},ensure_ascii=False,separators=(',',':')),encoding='utf-8')
 print(name,'pages',len(pages),'contents PDF page',None if contents is None else contents+1,flush=True)
