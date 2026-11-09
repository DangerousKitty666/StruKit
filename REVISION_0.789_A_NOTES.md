# StruKit Revision 0.789 A

Android versionCode 22. APK delivered locally; the user publishes on GitHub.

## Changes

1. **I/H-section pricing:** audited all 73 IPE, IPN, HEA, HEB and HEM records. The calculation is kg per metre × currency per kg, not a stock-length price. Beam estimates no longer inherit rates inferred from small hollow sections. Matching, explicitly per-metre supplier observations still take precedence. When these are absent, the previous budget assumptions (£1.55/kg UK; €1.40/kg LT) are retained and clearly labelled as budgets, not verified current market averages. Prices exclude VAT and delivery. The two-line explanatory note is retained.
2. **HVAC/Electricity menus:** replaced the HTML side rail with the same native ToolTabRail used elsewhere in the app. The native rail sits beside a bounded WebView, rather than positioning itself against the HTML page's height. This also keeps the rail independent of content scrolling.
3. **Diagrams:** increased separation between captions and diagrams; moved the duct-flow label, heat-thickness label and RC label away from lines. Corrected source-symbol lead endpoints.
4. **Circuit properties:** Apply and Close are kept in a sticky top action row. Apply saves changes and closes the dialog. Tap outside to dismiss without applying unsaved input. Android Back closes an open HTML dialog before leaving the tool.
5. **3D menus:** one View tile contains Light, Camera and Mode submenus. Only one settings panel is open at a time. Outside taps and Escape dismiss settings; opening Parts, Measure or Section closes competing panels. Pen-triggered measurement also dismisses settings. Fixed a pre-existing camera-position error when switching from Projected back to Normal.
6. **3D Section:** bottom-left Section tile, X/Y/Z clipping, slider and numeric position from the model centre, Flip side, Centre and Clear cut. The cut remains when its settings panel closes, and resets when the file closes or changes. Picking ignores clipped-away geometry. Partially clipped whole-feature measurements are not offered as complete features. This is a visual mesh section: cut faces are open, not capped, and newly exposed cut contours are not new exact CAD measurement entities.
7. **Both measurement tools:** result text enlarged to 20px with stronger weight; pinned result rows enlarged to 13px.
8. **2D layout:** status line moved to the right of Measure/Layers, below the drawing. Removed the old status row from the footer so the drawing gains height. Layers uses the same left-aligned translucent panel style as Parts and supports outside-tap dismissal.
9. **Building Drafter cost:** UK and LT estimates for the selected columns and rafters, adjustable rates per kg, editable purchase/cutting allowance (initially 5%), purchase mass and cost per square metre of footprint. Invalid rates/allowances do not produce a result. This is a main-member material budget only, excluding VAT, secondary steel, connections, fabrication, coating, transport, erection, cladding and foundations.

## Checks

- All 73 I/H records checked for per-metre arithmetic and independence from hollow-section price observations.
- Existing price normalization, exact-size matching and PDF search checks passed.
- Cost calculation tested at 0% and 5% allowances, both currencies, and invalid/non-finite inputs.
- Engineering-page browser checks passed for tab switching, narrow layouts, Apply, outside dismissal, dialog-back behaviour and short viewports.
- Viewer checks passed in portrait and landscape: View submenus, exclusive settings, outside taps, section axes/offset/side/reset, camera switching, Parts/Measure transitions, 2D status placement and larger results.
- Section screenshots and the revised diagrams were visually inspected.
- Android assembly and APK signature/package verification are recorded with the delivered build.
- No physical phone was connected for installation or native-rail visual verification. Browser tests exercise the production bundled viewer/tool assets; the native wrapper was compiled.

## Pricing review references and limits

The supplier review did not establish a complete like-for-like S275 I/H price series for both countries. It would be misleading to present the fallback rates as newly verified market averages or force them to agree numerically (UK uses GBP, LT uses EUR).

- [Lankava IPE 160](https://www.lankava.lt/sija-ipe-160): explicitly distinguishes a per-metre price from a six-metre stock length; no usable in-stock price was returned.
- [Ajero IPE catalogue](https://ajerometalai.lt/e-katalogas/35-dvitejines-sijos-ipe): prices are labelled per piece, so they must not be imported as per-metre observations.
- [The Metal Store UB 203×102×23](https://www.themetalstore.co.uk/products/universal-beam-203-x-102-x-23kg-m): separate one-, one-and-a-half- and two-metre products illustrate stock-length normalization. These UB sections are not substituted for IPE/HE sections.
- [F.H. Brundle UB 203×102×23](https://www.fhbrundle.co.uk/products/301120310223__Price_Per_Metre_-_Grade_S355): per-metre listing is S355, not an exact S275 IPE match.

Checked 9 November 2026. Obtain a supplier quotation for purchasing or project costing.
