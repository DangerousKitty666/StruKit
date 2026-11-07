# Revision 7.25 A — Android

Implemented:
- Profiles: full available table width, larger row type, reduced sketch height, full-width three-pin strip above the right rail. Tap a pin for its available full property panel; double-tap removes it.
- Timber: rounded dimensioned diagram with integrated C16/C24 selector, table with expandable properties, comparison pins.
- Right rails extend up by up to 96 dp while retaining bottom alignment and fades. Holes renamed Drill sizes; rivnut diagram uses the shared rounded panel.
- Building drafter: Projects tab and rail removed. Project PDFs preserved outside the APK in ../reference-archive/draft-projects. Trial section sizes sit beside the Columns/Rafters controls. Top view rotated, all drawings centred, amber numeric dimensions distinct from blue frame lines.
- Standards: colour-grouped menu; ISO/HTM/DW at bottom. Opening position unchanged, as clarified by user. Original ISO 14122, DW144 and HTM03-01 PDFs shown as continuous scroll with search by page and tap-to-zoom. PDFs unchanged; searchable text is a separate index.
- Colour library: 216 RAL Classic, 118 BS4800 and 91 BS381C digital swatches, identifier guide, search, double-tap full-screen colour and double-tap exit. Published supplier approximations; original RAL/BS physical colour samples remain definitive. Sources stored per record in colours.json. Menu tints use the same RAL approximations at 40% opacity.
- Material library: tile-style grade menu; up to three session pins via property double-tap, single tap opens the full record, double tap removes. Session ViewModel preserves pins across tools and configuration changes.
- Converter: results at top and scrollable input controls anchored at bottom, keyboard-aware.
- Home: original banner displayed 20% larger with side crop, reduced lower gap, scroll area begins closer to header.

Scope / remaining source dependency:
- ISO 14120 original PDF was never supplied. Its entry explicitly says this and retains the existing guide; it is not presented as an original standard.
- Building member choices remain editable trial sections. This update does not add automatic structural sizing or certify recommended sizes.

Validation:
- Android compilation and APK assembly checked.
- All 100 original material PDFs and the 3 original standards/publication PDFs byte-identical to 7.2 A.
- 423 publication pages indexed; text coverage checked. 425 unique colour records with valid RGB hex values.
- Property double-click extraction verified in browser for metal, ceramic, polymer and wood records.
- No phone installation or native UI runtime validation performed in this environment.
