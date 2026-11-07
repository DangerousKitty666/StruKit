# Revision 7.3 A UI follow-up — 7 November 2026

- Removed the redundant profile family/size-count line. Steel, timber and purlin diagram cards share the same starting position and 265 dp height.
- Timber and purlins use matching table headings, units, row heights, selection colours and full-width blue expanded-property panels.
- Purlin mass moved into the expanded panel with indicative LT/UK budget prices. Generic steel rates are editable; forming and galvanising exclusions are stated. Purlin properties can be pinned.
- Timber dimension lines are offset 24 dp from the outline. PLATE displays as Plates, preserving the internal data key.
- Pinned-property dialogs have larger text, tighter padding and a slightly transparent blue surface in both libraries.
- Standards and steel connections share a dropdown: 300 dp width, available-height limit, 20 dp downward offset, blue outline/text and 90% opaque black background. Topic colour groups and connection thumbnails remain.
- Converter result and input form one bottom-aligned scrollable stack with a 4 dp bottom gap. The activity explicitly uses resize keyboard handling; Compose handles system and keyboard insets once at the root, including keyboard clearance for other tools.
- Keyboard guidance: https://developer.android.com/develop/ui/compose/system/insets-ui
- About credit reads “by Dangerous Kitty”. Say thanks opens https://ko-fi.com/strukit externally; the retained thanks page also links there instead of showing the PayPal QR.

Validation: Kotlin compilation only. No APK assembled or released. Phone keyboard behaviour and final visual spacing still require on-device review. GitHub copy untouched.
