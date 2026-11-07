# Historical reference notes — Revision 0.5

These notes describe the earlier release. For current Revision 0.71 coverage and build verification, see README.md and the accompanying checks file.

Native Kotlin + Jetpack Compose, offline-first. No account or internet permission needed by the installed app. Source project only; **the APK has not been compiled/tested in this environment**.

## Tool 1 — steel profiles (redesigned)

- 13 right-side profile tabs: RHS, SHS, CHS, RSA, IPE, IPN, HEA, HEB, HEM, UPN, UPE, T, PLATE. **UB and UC removed** at the user's request.
- Fixed schematic for each profile family, irrespective of the selected size. Outlined, never solid-filled; grey dimension lines use **letters**, not selected numeric values.
- **v0.5:** enlarged sketches: scale is computed from the actual pixel canvas and density-aware 50 dp left / right dimension-callout margins, not a hard cap of 1.45 screen pixels. Drawing card is taller. Dimension labels stay outside the steel outline.
- Root fillets drawn for open sections and RSA. Tapered inside flange faces and toe corner rounds drawn schematically for IPN and UPN.
- Upper drawing is fixed; size table underneath scrolls horizontally and vertically. No search bar, website selector or supplier controls.
- Main table for rolled I/H sections and U channels: dimensions only (h, b, tw, tf, r1, r2 where applicable, d where applicable). Tap a row to expand it and see section area and mass per metre.
- Main table for SHS, RHS, CHS and RSA: dimensions plus mass per metre. No area in the main table, and no expandable property panel.
- Main table for plate: thickness only. Tap thickness to expand calculated mass per square metre.
- Some rolled tee references are **idealised** profiles, not a certified product series; tapping shows approximate mass and area.
- 231 built-in profile/plate records in editable `app/src/main/assets/profiles.csv`. Empty r1/r2/d means no reliable dimension was entered; the UI displays “—”, not a guessed dimension.

### Dimensions in the tables

`h` overall height; `b` overall width; `tw` web thickness; `tf` flange thickness; `r1` web-to-flange **root radius** (or RSA internal corner); `r2` **toe radius** at tapered flanges or angles; `d` clear depth between root fillets, calculated for *parallel* flange shapes only (`h − 2(tf + r1)`). For RHS/SHS/CHS, `t` is wall thickness and `D` is CHS diameter. For RSA, `h/b` are leg sizes.

**Important:** These are nominal catalogue/reference values, not as-rolled guarantees. EN 10365:2026 notes that the root and toe radii vary between manufacturers and are not generally mandated by that standard. The r1/r2 fields use reference manufacturers' schedules where available. Fillet/taper geometry on static sketches is illustrative, **not to scale** and not CAD geometry. Always check manufacturer-specific details before fabrication, modelling connections, or design sign-off.

### Data accuracy / caveats

- IPE/HEA/HEB/HEM/IPN/UPN/UPE: published nominal section depths, widths, wall/flange thicknesses, mass/area. r1/r2 filled where referenced; unknown radius cells intentionally blank.
- RSA: F.H. Brundle-style nominal leg/thickness combinations, with published radii on listed standard equal angles where available. Some masses use published nominal values; other rows' mass is marked **≈** in the table because sharp-corner calculations omit real rolled fillets.
- RHS, SHS and CHS: listed dimensions are nominal reference sizes. Mass marked **≈** because idealised corner geometry is used for the current build, **not** the EN 10210/10219 certified section area. Exact rounded-corner design properties will require a manufacturer/standard table in a later version.
- T: simple idealised tee geometry; properties are provisional, not an established rolled range.
- Plate: listed thickness is reference-only, not stock availability. Mass in kg/m² calculated using 7,850 kg/m³ steel density. All plate sizes shown are thick sheets/plates as labelled; sheet dimensions are not represented.

### Non-live source references

- EN 10365:2026 scope and radii limits: https://www.iteh.us/catalog/standards/sist/76766186-b391-474f-ad88-71e0ca898f74/sist-en-10365-2026
- IPE, HEA, HEB and UPN dimensions including nominal root radii: https://spawacz.waw.pl/en/reference-tables/steel-section-weight-table/
- IPN root/toe radii: https://ozkansteel.com/en/product/energy-and-structural-profiles/ipn-profiles
- UPN root/toe radii: https://www.johnchorley.co.uk/steel-buyers-guide/european-upn-channels/
- UPE root radii: https://www.sulehri.ae/en/knowledge/steel-channel-angle-tables
- HEM root radii: https://www.sulehri.ae/en/knowledge/steel-beam-tables
- Angle dimensions and radii: https://rsjcentre.com/products-services/equal-angles and https://www.roymech.co.uk/Useful_Tables/Sections/U_AngleA_dim_prop.html
- F.H. Brundle stock size references (non-live): https://www.fhbrundle.co.uk/steel-sections/angles and https://www.fhbrundle.co.uk/steel-sections/square-hollow-section and https://www.fhbrundle.co.uk/steel-sections/rectangular-hollow-section

## Tool 2 — unit converter

**v0.5 redesign:** same black / slate / muted-blue palette as Tool 1. All 11 categories now appear on vertically scrolling **right-hand thumb tabs**, with a scrollable converter panel at left. Category names are shortened on tabs (e.g. PRESS, LINE, SURF); the full category name is shown under the page title. Input value, FROM/TO unit pills, swap button, and result use custom dark cards rather than Material default FilterChips. Categories: length, area, volume, force, stress/pressure, mass, moment/torque, distributed load, surface load, angle and temperature.

## Tool 3 — tolerances

**Shaft / hole fits:** input nominal diameter 0 < d ≤ 500 mm, select clearance, transition or interference grouping and a specific hole-basis fit. Contains H7/g6, H7/h6, H7/k6, H7/m6 and H7/p6. The UI displays hole/shaft upper/lower deviations (µm), exact min/max sizes (mm), minimum/maximum **signed clearance** (mm), size-step and whether the calculated limits permit clearance or interference. Values are tabulated from ISO 286-1/2 reference charts and independent chart cross-checks. NTS outline schematic is illustrative; the classification is computed from numeric limits and can differ from the group label near boundaries.

**Shaft keys:** input nominal shaft diameter 6–230 mm to select standard parallel key b × h, standard shaft/hub seat depths t1/t2 with depth tolerance, and keyway **width zone designations** P9 (shaft, tight) and P9 / JS9 (hub by design). Width *numerical deviations are not calculated* in this version, so do not infer exact keyway width limit from the letter code. Verify the drawing specification and the relevant DIN 6885 series against the shaft/key manufacturer. The source key reference has varying naming conventions for hub/shaft slots and keyseat depth; app uses the familiar shaft t1 and hub t2 distinction from SKF's general engineering chart.

Sources:
- H7/g6 through H7/p6 charts: https://cncvia.com/en/tools/fits/ and https://mlc.org.uk/guides/iso-tolerance-fits-chart
- ISO 286 diameter-step guide: https://nxtolerance.com/tools/h7-tolerance-chart/
- SKF couplings keyway DIN 6885 reference: https://docs.rs-online.com/6e11/A700000012599053.pdf
- Berger Tools DIN 6885 keyway reference: https://www.berger-tools.co.uk/pdfs/6885-1.pdf

## Tool 4 — common I/H beam connections

Includes fin plate, flexible end plate, double angle cleats, flush end plate, extended end plate, beam splice, seated beam and column base plate. Each has an **indicative geometry drawing**, common drawing variables, arrangement notes and *checks to perform*. This is a reference to detail types, not a design tool. No bolt group resistance, weld sizing, load capacity, rotation capacity or EC3 verification is calculated; sketch layouts and bolt numbers are **not** recommended construction details. For actual structural engineering, refer to BS EN 1993-1-8, National Annex, and validated detailing/design guidance such as SCI P358 and P398.

## Tool 5 — fixing library

- Metric coarse-pitch hex-head fasteners nominal M6 to M30: ISO 4014/4017 across-flats and older DIN 931/933 across-flats, pitch, nominal size. **v0.5:** tap a bolt diameter to expand nominal head height k and a panel of *common* full-thread ISO 4017 / DIN 933 lengths. Lengths are representative catalogue selection options within the usual published size intervals, NOT manufacturer live stock confirmation. Nominal bolt length L is measured from beneath the head. Part-threaded ISO 4014 lengths and corresponding thread lengths must be checked for the selected product.
- **v0.5 washer reference:** six distinct illustrated washer forms and their uses: plain/normal ISO 7089, large-series ISO 7093-1, chamfered ISO 7090, legacy split spring DIN 127, toothed DIN 6798 family, bonded sealing / EPDM. Each expands to show application and caution; the first two also show M6–M30 nominal inside/outside diameters and thicknesses. The types whose dimensions vary by manufacturer do NOT show misleading flat-washer sizes. Plain, large and structural hardened washers are not automatically interchangeable. Split spring washers should not be treated as reliable vibration locks.
- Self-drilling steel screws: nominal common diameter families 4.2, 4.8, 5.5 and 6.3 mm. Drill-tip number, capacity, pullout, drive and washer need supplier-specific confirmation.
- **Published catalogue examples**, not live stock levels: UK F.H. Brundle and Orbital Fasteners; Lithuanian Varžtų Pasaulis and Senukai. Tap an item to open the product page. Links are optional; the reference app works without an internet connection.
- Not every nominal bolt/screw diameter **or newly listed common length** is claimed as stocked at every supplier. UK/LT examples are independent listings and not a recommendation to substitute standard machine bolts for verified EN 14399/EN 15048 structural bolt assemblies.

Sources and cross-checks:
- Nominal ISO 4017 length ranges and head dimensions: https://www.cvtcivata.com/en-US/din_en_iso_4017/457/463/527/0/0
- Plain washer ISO 7089 normal series: https://www.nab-sab-fasteners.co.uk/nab-sab-washers
- Large-series ISO 7093-1 dimensions (including M30 33 mm bore): https://www.pts-uk.com/products/washers/large-series-flat-washers-200hv-iso7093/metric-a2/A709330
- F.H. Brundle M12 × 30 hex bolt: https://www.fhbrundle.co.uk/products/5002HS12X30__M12_x_30mm_Hexagon_Bolt_Galvanised_packed_in_100%27s__%28sold_each%29
- F.H. Brundle screw listings: https://www.fhbrundle.co.uk/fixings-and-fastenings/screws
- F.H. Brundle TEK No.5 5.5 × 55: https://www.fhbrundle.co.uk/products/5002TEK55555__TEK_5.5_x_55_Hex_No.5_S/Drill_Screw_Pack_Qty_100
- Orbital Fasteners M12 DIN 933: https://www.orbitalfasteners.co.uk/categories/m12-hexagon-head-set-screws
- Varžtų Pasaulis bolts, 2022 product guide: https://www.varztupasaulis.lt/static/media/VP_katalogas.pdf
- Senukai DIN 933 M12 × 40: https://www.senukai.lt/p/varztas-din-933-haushalt-12-mm-x-40-mm-100-vnt/dwh2
- Senukai DIN 7504K 5.5 × 25: https://www.senukai.lt/p/savisriegis-haushalt-din-7504k-5-5-mm-x-25-mm-20-vnt/a9nj

## QA and limitations

Pure Kotlin engineering data was compiled and checked on this host for basic bounds, metric boundary handling, min/max consistency for all five fits and diameters 1–500 mm, DIN 6885 key sizes, and presence of UK/LT examples. Android/Compose files **have not been compiled or run in Android Studio here** (no Android SDK present). Please build the source in Android Studio and report compile errors, if any. The project remains a reference aid, not a replacement for current standards, manufacturer product certificates, or a signed engineering design.

## Build with Android Studio

1. Extract the ZIP and use **Open** to select the nested **StructKit** folder (the one containing `settings.gradle.kts`). Do not create a new project.
2. Allow Gradle sync and install any missing Android SDK 35 components if prompted.
3. Choose **Build > Build Bundle(s) / APK(s) > Build APK(s)**.
4. Install the file at `StructKit/app/build/outputs/apk/debug/app-debug.apk` on the phone.

The app package remains `com.structkit.app`. Debug APKs can be installed over older builds signed with the same debug keystore; otherwise uninstall the old build first (which clears local app data).

## Where to add new tools

Add a new card in `Home` and a new branch in `when(screen)` in `StructKit` (`MainActivity.kt`). Data is in a plain, offline `profiles.csv` (12 pipe-delimited columns).
