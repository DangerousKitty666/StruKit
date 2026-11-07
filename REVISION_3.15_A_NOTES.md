# StruKit Revision 3.15 A

User-requested display revision 3.15 A; Android versionCode 15 so it updates 7.31 A (code 14). APK: StruKit-3.15-A-vc15.apk. GitHub tag: v3.15-A. Explicitly select Set as latest release: the display number is lower than 7.31, but Android/update checks use the increasing versionCode.

## Viewer changes

- Removed landscape header/background strips and the strip behind bottom-left controls. Normal landscape status/gesture/selection text below the filename is hidden. Errors remain visible in a separate alert.
- Open file label uses centred flex alignment. The native app header is 16 dp shorter for viewer screens, and viewer header spacing is reduced.
- Removed Up axis. The existing file-type default remains internal (STEP/OBJ: Y up; STL: Z up).
- Translucent on-canvas compass/cube replaces the Iso/Top/Front/Right button row in both orientations. Cube faces select top/front/right; arrows orbit; curved arrows roll by 15 degrees; Home restores isometric. 2D has a matching compass with 15-degree rotation and Home reset.
- Bottom tools group Hide part / Show all / Measure / Parts in 3D, and Measure / Layers in 2D.
- Measurement picks two points, draws endpoints and a connecting line, and reports distance and axis differences. Clear starts again; Close exits measurement mode. Navigation gestures are disabled during point picking; the navigator remains available.
- 3D uses ray intersections with the visible mesh and snaps to a vertex of the hit triangle when within 8 screen pixels for pen or 12 for finger/mouse. Results are approximate mesh distances, not exact CAD dimensions. STEP display tessellation remains 0.5 mm; the mesh itself limits measurement accuracy.
- 2D maps taps into drawing-plane coordinates, including a rotated view. It does not snap to entities. Default units are drawing units; users can explicitly select mm/cm/m/inch scaling to millimetres.
- STEP assembly-tree names now identify unnamed mesh bodies. Example: BEAM2_V1 / Body 1, with the original assembly path in properties. When no name exists, a fallback remains. The importer does not provide material grade or density.
- Selecting a 3D mesh body shows a translucent top-left panel: name, assembly path, axis-aligned X/Y/Z dimensions, triangulated surface area and triangle count. These describe the selected body, not an inferred entire assembly or structural member.
- Standard Pointer Events support pen taps, hover target and an optional Pen only measurement setting. Recent pen events suppress touch point-picks to reduce palm interference. No Samsung Bluetooth Air Actions/side-button integration is claimed.

## Validation

Browser checks loaded a known 100 × 50 mm OBJ panel: bounding dimensions 100 × 50 × 0, area 5000 mm², snapped edge distance 100 mm. Unit conversion to inches-as-input produced 2540 mm. Pen-only rejected mouse input and accepted simulated pen point picks. Real S Pen hardware testing remains pending.

The supplied A007 STEP exposed BEAM2_V1 and other component names through its hierarchy; no generic Part N labels remained in that sample. Supplied DXF tests checked point distance, unit conversion and the expected 15-degree coordinate rotation. Portrait/landscape layouts and navigation were exercised without JavaScript errors. Android build/signature checks are recorded with the delivered APK. Phone runtime review remains necessary.
