# StruKit Revision 0.711 — Android Metals and alloys pilot

This pilot replaces the Material library landing screen with the supplied Level 2 Metals and alloys hierarchy: Ferrous and Non-ferrous, with the same alloy-family subfolders and 28 material records. Search matches material and folder names. Tap a material to open expandable property sections and its available original technical diagram. Pinch to zoom the page.

The Source PDF button opens each complete original datasheet inside the app. Previous/Next selects a page; pinch to zoom. Android Back first closes the PDF, then returns to the material tree. The header Home arrow returns directly to the home screen.

## Data and images

- All 28 supplied PDFs are included byte-for-byte, with all original descriptions, supporting information, images, credits and qualifications.
- 3,340 property rows are imported into phone-friendly sections. Numeric ranges, unit notation and estimate markers are preserved. Source yes/no symbols are retained as original image assets.
- 19 technical diagram images are extracted directly from the source PDFs without redrawing, rescaling the stored image, recolouring or recompression. Display size changes proportionally to fit the screen. Nine records have no technical diagram in their supplied PDF; none is invented.
- New short overview/use summaries introduce each record. Full supporting notes remain in the original datasheet; these are not claimed to be fully rewritten.
- Application photographs are omitted from the new material pages in this pilot. They remain unchanged inside the original PDFs. Replacement photographs are not included yet.
- Values are source reference data, not live market prices. Asterisked data are source estimates. Material-family ranges are not certified grade-specific design values.

## Install and build

The APK is signed with the same local Android debug certificate as Revision 0.71, with a higher version code. It can update that build. This is the Android edition only.

For source builds, use JDK 17 or 21, Android SDK 35 and the Gradle wrapper. Create local.properties for your Android SDK and run `gradlew.bat assembleDebug`. Default debug signing uses the developer's local Android debug key.

## Verification

The Android build and APK signature verification passed. All 28 material HTML pages were checked at 320, 360 and 420 px widths for overflow and missing images. Original PDFs and extracted technical images were verified against the supplied files. The pilot has not been interactively tested on a physical phone.
