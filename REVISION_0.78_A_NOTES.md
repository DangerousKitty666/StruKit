# Revision 0.78 A — Android

User authorised APK release on 7 November 2026 after the additional PDF and purlin changes. GitHub publication is a separate manual step.

## Changes
- Shorter top viewer buttons in portrait and landscape; centred labels retained.
- 3D Light button with Studio, Soft, Headlight, Raking and Contrast modes.
- Adaptive zoom/orbit speed, much closer minimum zoom, adaptive near clipping and logarithmic depth buffer. Double-tap a surface to focus the orbit target; selected-part popup has Focus to frame that component.
- Both viewers: Two points and Feature measurement modes, feature filter and pen-hover highlights. Existing unit conversion and pen-only input retained.
- 2D: analytic line lengths, vertices, circles, arcs, polyline bulges and transformed block geometry. Hidden layers excluded. DWG measurements use the locally converted DXF geometry.
- 3D: welded mesh boundary/sharp edges, edge vertices and fitted circular rims. STEP face boundaries retained for edge extraction; triangulation diagonals excluded. Approximate results labelled explicitly.
- Home tool order: 2D viewer, 3D viewer, Steel profiles & other, Unit converter, Tolerances, Fixing library, Building drafter, Forces calculator, Standards, Material library, Steel connections.
- Display revision 0.78 A; internal versionCode 16 continues upward for Android updating.

## Validation
- Android compileDebugKotlin passed offline (existing deprecation warnings). No assemble/package task run.
- Browser checks: original two-point measurement and units, simulated pen-only input, portrait/landscape state, camera navigation and STEP component names.
- New browser checks: 100-unit feature edge, pen hover, all five lighting modes, adaptive zoom speed/near plane, analytic 20-unit DXF circle.
- Geometry checks: known DXF line/circle/bulge arc/transformed block; extruded plate straight edges and both 20 mm hole rims.
- Screenshots and reports in the parent work/rev078a folder. Actual Android phone, S Pen hover and touch sensitivity need device testing after a future authorised APK build.

## Feature measurement limits
3D measurements are display-mesh estimates, not exact BREP dimensions. A fitted circular rim may be a hole or an outside round feature; the app does not assert which. Faceted or incomplete circles may not be detected. Use Two points when no supported feature is found. Extraction is lazy per body and capped at 400,000 triangles per body. 2D indexing is capped at 100,000 features; unsupported entities, tilted extrusion geometry and non-uniformly scaled circles are excluded. Confirm OBJ/STL and drawing units. No geometry is uploaded.

## Next work / release policy
Continue in this source folder. Keep previous revisions and release outputs intact. Release authorised on 7 November 2026. If a future revision label appears out of sequence, ask first. When eventually releasing, use the existing signing key and a monotonically increasing versionCode; do not infer update order from the corrected display number. Keep later work separate from this release.

## Additional layout changes (7 November 2026)
- Standards PDFs: document title and live page counter share one header row. Reduced heading spacing, removed duplicate per-page counters and idle footer text, compact 36 dp search/page entry fields. Search placeholder uses the user's literal wording “search in off”. Search results and validation messages appear only when needed.
- C and Z purlins: profile controls sit in the upper-left of the diagram area, with taller profiles on the right; height, flange, lip and thickness dimensions are spaced outside outlines. Uniform scaling keeps the sketches from stretching. HAT layout retained.
- Included in the authorised APK. Native phone layout review remains pending.
