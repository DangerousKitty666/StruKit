# StruKit Revision 7.31 A

Android versionCode 14. Update APK: StruKit-7.31-A-vc14.apk.

- Both viewers now fill the available height. A defined HTML/body height and explicit full-size Android WebView replace the unreliable viewport-height sizing. The app content has a bounded remaining-height container.
- Viewer title and file controls share a compact row; the filename stays immediately below. Removed the persistent 2D Preview line; limitations remain available under Info.
- Landscape hides the app header and system bars, expands the canvas to the available screen, and overlays the controls. Swipe from the edge to reveal Android navigation. Display cutouts remain protected.
- Orientation/screen-size changes are handled without recreating the activity/WebView, retaining the loaded model, camera and visibility state during rotation. This does not preserve a model after Android kills the app process.
- 3D view buttons no longer shrink vertically or clip. A horizontally scrollable row keeps all controls accessible on narrow displays.

Validation: DXF and A007 STEP loaded in local Chromium. Checked 390×680, 780×360 and 320×640 layouts and rotation back to portrait. Canvas exceeds 60% of the portrait viewer surface and fills landscape; title/buttons share a row; buttons are not vertically clipped; loaded-file identity and layer/part visibility survive resizing. No browser JavaScript errors. Android APK build and signature verification passed; final phone rotation/installation check remains for user testing.

GitHub release: tag v7.31-A, title StruKit Revision 7.31 A, attach StruKit-7.31-A-vc14.apk, leave Pre-release unchecked and select Set as latest release. Existing 7.3 A updater will recognise versionCode 14 after publication.

Android references: https://developer.android.com/develop/adaptive-apps/cookbook/webview-state and https://developer.android.com/develop/ui/views/layout/immersive
