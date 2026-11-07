# Revision 0.71 — 3 November 2026

See README.md for the current layout, reference library, material data and drill-size changes. The APK builds successfully; phone interaction testing remains outstanding.

The entries below are historical release notes.

# v0.5 — More legible sketches, thumb-side converter, fastener expansion

- Steel profile sketches use actual canvas-pixel/density scaling and increased drawing area. Removed the previous 1.45 px scaling cap; letter callouts retain margins. Sketch still never changes on size-row selection.
- Unit converter has the Tool 1 layout: eleven categories as a right-hand thumb rail, with an independently scrolling input/output panel and matching black-grey-accent colours. Added FROM/TO swap.
- Expanded M6–M30 hex bolt rows with coarse pitch, head height, ISO/DIN A/F and common nominal full-thread lengths. New length cells are reference options, **not verified supplier stock**.
- New illustrated washer catalogue: ISO 7089, ISO 7093-1, ISO 7090, DIN 127 (legacy), DIN 6798 tooth, and bonded EPDM sealing. Includes application/caution notes plus selectable standard dimensional values for normal/large flat washers.
- Retained Tools 3 and 4 and supplier listing links from v0.4.
- Source only; Android device compilation/testing still required.

# StructKit — changelog

## v0.4 — Engineering libraries (1 Nov 2026)

- Tool 3: offline ISO 286 hole/shaft fit limit calculator for H7/g6, H7/h6, H7/k6, H7/m6 and H7/p6. Select type of fit, enter Ø0–500 mm, view upper/lower deviations and exact dimensional limits. Calculation reports both minimum and maximum signed clearance; negative clearance = interference. Includes illustrative fit sketch.
- Tool 3: DIN 6885-1 parallel key selection for shaft diameters Ø6–230 mm. Displays standard key b×h, shaft seat depth t₁, hub depth t₂, depth tolerances and width-zone designation. Includes illustrative keyseat sketch.
- Tool 4: eight I/H steel connection detail concepts with non-scale sketches, dimension-letter checklist and engineering design checks. Does not calculate capacities or certify joint details.
- Tool 5: bolt pitch and across-flat metric dimension table for M6–M30 (ISO vs legacy DIN). Self-drilling screw common diameters and UK/LT supplier *published product examples*. External product links can open if network available; the app's other features are offline.
- New tools use the established graphite UI and blue-grey labels.
- Tool 1 and Tool 2 remain as in v0.3, except that the home screen now scrolls for five tool cards.
- Reference-data boundary tests pass (see QA section in README). This source has not been built with an Android SDK on this host.

## v0.3 — Steel profiles refinements

- Static outline sketches with lettered dimensions and radius annotations.
- Expanded section properties; UB/UC removed.

## v0.2 — steel browser

- Right-hand profile tabs and horizontally scrolling size tables.
