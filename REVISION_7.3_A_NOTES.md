# Revision 7.3 A — CAD viewer work in progress

No APK has been assembled or released for this revision. Previous releases remain unchanged.

## Added tools

**2D viewer** opens DWG and ASCII DXF through the Android system file picker. It offers touch pan/pinch, Fit and a searchable layer visibility list. DWG is decoded to temporary DXF entirely on the device with LibreDWG, then rendered with dxf-viewer. It is a model-space preview, not an AutoCAD-equivalent renderer. Layouts, external references, custom objects, dimension styles, line patterns and widths have limitations. Noto Sans substitutes drawing fonts. Persistent preview limitations are visible below the drawing; the Info panel explains them.

**3D viewer** opens STEP/STP, OBJ and STL. It offers touch orbit/pan/pinch, Fit, isometric/top/front/right views, a searchable part list, selection highlighting, hide selected and show all. STEP import preserves the returned part meshes and names; the importer hierarchy is retained internally but the initial parts interface is a flat list. Per-face STEP colours are retained in mesh vertex colours. OBJ groups become parts; MTL/texture sidecars are not loaded. STL has no part hierarchy. Mesh units default to mm with cm/m/inch choices; STEP is converted to mm. Y-up is the initial STEP/OBJ view, Z-up for STL, with a visible axis selector to correct other export conventions.

STEP geometry is triangulated at 0.5 mm linear deflection and 0.5 rad angular deflection for display. Bounds are mesh extents, not certified dimensions. This first viewer does not provide precision measurement or CAD editing.

## Implementation

- Separate home tiles, both styled in the app's dark/blue palette.
- Android WebView with a local intercepted HTTPS asset origin; no cloud conversion or document upload. Internet permission is now used by the separate GitHub updater; CAD requests remain restricted to local assets.
- Android system document picker grants access to the selected file only. No broad storage permission.
- Parsing uses terminable Web Workers. Cancel and subsequent reopen are supported. Maximum input 50 MiB and mesh output 2 million triangles; STEP timeout 5 minutes, DWG/DXF 2 minutes, mesh import 1 minute.
- Parser workers terminate after import to release their WASM memory. Renderer resources are disposed on replacement/exit.
- LibreDWG 0.8.6's published WASM has a 1 GiB initial linear-memory reservation (maximum 4 GiB). Earlier investigated 0.7.11 and 0.8.0 packages have the same reservation, so no downgrade was made. This is a significant phone-memory validation item, not a guarantee that every phone can open DWG.
- STEP WASM starts at approximately 28.6 MiB and can grow to 2 GiB. The larger assembly also needs substantial conversion and rendering memory.
- Current Android System WebView with WebGL2, WebAssembly and module-worker support is required. Real-device testing is pending.

## Sample validation

All samples stayed local and are not bundled in the application or copied into the GitHub repository.

| Supplied file | Result in local desktop Chromium at 390 × 680 |
|---|---|
| 2.16 - P01-2mm-QTY2.dxf | Rendered, 3 layers; layer visibility and Fit checked |
| HVAC - Overall_rev2.dwg | Decoded with return code 0; rendered, 59 layers; no reported parser warnings |
| A007 - GA_final.stp | Rendered, 246 meshes, 126,348 triangles; approximately 6–7 s import on this PC |
| A014 - FRAME - GA_final.stp | Rendered, 3,062 meshes, 1,227,692 triangles; approximately 80 s import on this PC |

These results demonstrate import and rendering, not an entity-by-entity comparison against the originating CAD application. Phone speed will differ.

Generated fixtures additionally cover OBJ groups, ASCII and binary STL, mesh-unit conversion, part selection/hide/show, axis/view changes, malformed and empty inputs, STEP cancellation and successful subsequent loading. Browser tests block off-origin requests. Android Kotlin compilation is checked separately; no APK/native phone runtime test has been performed.

## Rebuilding the viewer assets

Authored web source and locked npm dependencies are in `cad-viewer-src`. Run `npm ci --ignore-scripts`, then `node build.cjs`. The build writes bundled scripts into `app/src/main/assets/viewer`. Engine WASM and licence files are also bundled locally. The existing Android signing setup is unchanged.

## Third-party components

- LibreDWG wrapper: @mlightcad/libredwg-web 0.8.6, GPL-3.0 — https://github.com/mlightcad/libredwg-web
- DXF viewer: dxf-viewer 1.0.50, MPL-2.0 — https://github.com/vagran/dxf-viewer
- STEP importer: occt-import-js 0.0.23, LGPL-2.1; includes OpenCASCADE — https://github.com/kovacsv/occt-import-js
- Three.js 0.187.0 (3D), and 0.186.x resolved by dxf-viewer, MIT — https://github.com/mrdoob/three.js
- Noto Sans, SIL Open Font License — https://github.com/notofonts/noto-fonts

Bundled licence texts and package lock record dependencies. No vendor code/WASM was modified. If distributing this combined application later, review GPL/LGPL/MPL source and notice requirements; avoiding an app store does not itself remove distribution obligations.
