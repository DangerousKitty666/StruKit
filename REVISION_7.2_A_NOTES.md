# Revision 7.2 A — Android working draft

APK build and delivery authorised on 6 November 2026 after the final branding changes. Windows is unchanged. About, thanks and researched grade comparisons are now implemented.

## Latest additions (6 November 2026)

- Single-tap About and Say thanks footer actions. About uses the supplied text and original animated 20-frame GIF. Thanks displays the unchanged PayPal QR image.
- Steel profiles & other includes Timber: 13 rectangular sizes, C16/C24 mass and section properties, pinning, and disclosed LT/UK volume-based budget price estimates.
- Material library expanded from 28 to 100 records, including ceramics/concrete, composites, polymers/elastomers and wood. Original PDFs retained byte-for-byte; numeric ranges and source estimate markers preserved.
- Steel, concrete and timber grade-comparison tables with EU reference names, multiple properties, units, scope and source links.
- Corrected extraction column boundaries for the new PDF layouts. New wood records retain grain/board orientation in their titles.

## Implemented

- Steel-profile rows all expand. UK and LT indicative costs use disclosed, editable reference kg rates and show the source/date/tax basis. These are extrapolated estimates, not supplier quotations for every section. Plate prices remain per square metre.
- Double-tap mass, area or price to pin up to three comparison tiles beside the title. Double-tap removes; single-tap reveals Remove. Pins live in an activity ViewModel, survive navigation/configuration changes, and are cleared when the activity is finished or the process ends.
- Purlins are inside Steel profiles. C/Z/HAT selectors sit in the diagram card.
- Forces calculator replaces Rain calculator. Rain separates collected storm volume from retained-water gravity load. Slope converts roof surface to horizontal catchment. Snow provides LT ground-zone references and a balanced-roof shape-factor calculation, plus a measured roof snow option. Scope, assumptions and omitted load cases are visible.
- Building drafter generates a conceptual arrangement, assembly count, four views and a main-member take-off. All 16 supplied drawing PDFs are indexed, with project-specific observations. Trial sections are user selectable; the tool does not infer structural capacity or perform structural member sizing. Dimensions are model lines, not guaranteed clear space. Weight excludes secondary members, connections and finishes.
- ISO 14120 guide now includes an ISO 13857 upper-limb opening-distance table and original explanatory reach diagrams, with the standards' roles distinguished. Reach-over/under/around are not reduced to the reach-through table.
- ISO 14122 source content through Clause 8 is retained with larger type and paragraph spacing; the complete supplied PDF is available for original formulas and figures.
- DW/144 (180 PDF pages) and HTM 03-01 Part A (213 PDF pages) are included in full, with searchable page indexes, direct page links and bottom navigation. Multi-column extracted text is used for search, not presented as unreliable reflowed prose.
- Fuel-consumption conversions distinguish UK and US mpg/gallons. Volume also includes both gallon definitions.
- Tolerances → Holes contains common drill and hole-saw diameters, working/overall dimensions for the cited drill series, and shaded dimensioned illustrations.
- Standards and connection dropdowns are taller, subject to available screen space.
- Header logo reduced by 15%.
- Materials: General and Mechanical properties initially expanded; source PDF is a plain footer link; page navigation is below the source page; duplicate breadcrumb spacing removed. Existing original technical images and PDFs are unchanged.

## Validation

- Android `compileDebugKotlin`: successful.
- Pure Kotlin checks: reciprocal fuel conversions and UK/US gallon distinctions; roof catchment projection; retained-water force; snow slope boundaries and obstruction; frame counts, spacings, member lengths and invalid inputs.
- 327 HTML checks across 320, 360 and 420 px widths: no horizontal overflow or broken images; all 100 materials expose the source PDF and initially expand the two requested properties sections.
- Selected document pages visually inspected.
- No physical-device/gesture test or APK installation performed. App interaction/layout still requires review on a phone before release.

## Engineering scope

The Building drafter is a planning/take-off prototype, not the automatic structural sizing system originally envisaged. Project drawings show differing spans, supports, bracing, floors and roof systems; geometry and prior BOMs alone cannot establish member adequacy. A structural solver, site actions and verified design rules would be needed for automatic sizing. The source projects remain available for comparison without claiming that their member sizes are safe for a different building.

Snow output is a basic unfactored balanced case, not a complete EN 1991-1-3 design envelope. Guarding coverage is a guide, not the complete unsupplied ISO 14120/13857 publications. Source references and assumptions are shown in the app.

## Final branding and APK

- Original LOGO2.png used unchanged in the 48 dp header; by removed.
- Adaptive icon uses the same square original with equal 23% insets and dark background.
- Original 6000 × 2496 banner copied unchanged; 2 dp side margins instead of 20 dp.
- assembleDebug succeeded; signature verified, package com.structkit.app, versionCode 10, versionName 7.2 A. Signed with the existing personal-install debug key.
- APK delivered as outputs/StruKit_Revision_7.2_A.apk. No connected-phone runtime test performed.
