# StruKit Revision 0.785 A

Android versionCode 17. User authorised APK release. Existing signing key retained; GitHub publication remains a manual step.

## Changes
- Home footer: Check for updates sits beside Say thanks with matching typography.
- Home sketches now map to tool names rather than list positions, correcting all reordered tiles.
- Building drafter shows sloping roof area below footprint, excluding overhangs: footprint / cos(roof slope), for both mono- and duo-pitch layouts.
- Isometric dimensions use longer offsets and increased drawing margins to remain away from the frame and within the panel.
- Material grade comparisons are on the title row.
- Z purlin drawing shifted right with a wider drawing coordinate frame to keep dimensions clear of the buttons and within bounds.
- HAT purlin drawing centred and enlarged 15%.
- PDF document title enlarged 15%, closer to Standards. Page counter moved into the bottom page-navigation column.
- PDF controls overlay the viewer on a slightly translucent strip with a fading top edge. Bottom content inset prevents the last page being hidden behind the controls.
- PDF sun button opens a brightness slider (25–100%). Initial brightness is 80%; the chosen value persists across documents and restarts. It dims the PDF, including zoom view, without changing phone brightness or the underlying PDF.
- Back gesture from an embedded standards PDF returns to the Standards overview. Zoom dialogs still close first.
- Removed development from the visible revision label.

## Validation and limits
Android APK build, package version, signing-certificate and packaged-asset checks performed for release. No CAD viewer engine changes; previous geometry/browser checks remain applicable. Native device layout, gesture and brightness interaction still need user testing on the phone. Roof area is geometry only and excludes overhangs; structural screening assumptions are unchanged.

## Publishing
APK: StruKit-0.785-A-vc17.apk
Tag: v0.785-A
Title: StruKit Revision 0.785 A
Set as latest release for the in-app updater. Next internal versionCode must be at least 18. Flag unexpected display revision numbering before editing.
