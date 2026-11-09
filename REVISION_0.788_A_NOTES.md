# StruKit Revision 0.788 A

Android versionCode 21. Local APK delivery only; GitHub publication is manual.

## Requested changes

1. Profile price tables keep S275 as the carbon-steel grade, plus SS 304/316 for hollow sections. The note below the table is two lines. Corrected square-section dimensional matching, reversed rectangular dimensions and round-tube OD parsing. Expanded the bundled exact-size stainless catalogue observations and added explicit metals4U catalogue pagination and Linoksa per-metre parsing. Existing installations merge updated bundled observations with newer cached prices.
2. Standards search now indexes visible document text rather than raw HTML/JSON, so stylesheet and script text cannot produce suggested-topic matches. Highlighting uses consistent case-insensitive whole-word/phrase matching for titles, reference pages and PDF words. Search punctuation is escaped; previous/next moves through actual highlights.
3. Electricity and HVAC category rails are anchored to the bottom. Calculate buttons align right. Revised schematics separate labels from lines and keep geometry inside the diagram area.
4. Circuit builder starts empty. Add component tiles by tapping or holding/dragging from the palette. Hold and drag a placed tile to reposition it; tap to edit properties. Tap two terminals to connect, and use Reference to choose the ground terminal. Moving tiles preserves wiring. Disconnect, flip and delete are in the properties menu; Clear circuit asks for confirmation. Diodes and LEDs are included. The symbol library contains 18 symbols, distinguishing buildable components from reference-only symbols.
5. Removed the HVAC DW tab. Ducts, Heat transfer, Airflow and Canopy use two-column inputs; Air moisture remains single-column. Reduced input spacing.

## Validation

- Android assembly completed successfully.
- Production Kotlin checks cover price matching, missing-size handling, VAT normalization, supplier averaging, round OD parsing, Linoksa per-metre parsing and PDF phrase/word boundaries.
- Browser interaction checks at 360 px width cover all Electricity/HVAC tabs, overflow, two-column forms, right-aligned Calculate, blank circuits, component addition, terminal wiring, property editing, dragging, tab-return preservation and clearing.
- Numerical checks cover diode forward/reverse bias, LED forward drop, DC power balance, wiring graphs, retained AC resistor/inductor/capacitor networks, three-phase power, psychrometrics and duct geometry.
- Search checks cover whole words, phrases, punctuation, navigation, clearing highlights and restoration of collapsed sections.
- HVAC, circuit and symbol-library screenshots were visually inspected.
- No physical Android phone was connected for this build. Installation and touch behaviour should still be checked on the user's phone.

## Scope and data limits

Stainless estimates use matching listed dimensions and grades; no nearby wall thickness is substituted. Missing verified prices remain “Not available”. Available listings are indicative catalogue observations, not guaranteed quotations or a complete market survey. Some suppliers block automated requests; refresh retains the last verified data when that happens. VAT and delivery treatment is shown in the app.

Diodes/LEDs use a DC piecewise forward-drop model with 1 Ω forward resistance and 1 GΩ reverse resistance. Reverse breakdown, switching transients and nonlinear AC diode simulation are not included. The existing linear AC/RMS circuit mode remains available for R/L/C circuits. Reference-only symbols do not imply a simulated device model.

## Price sources checked

- [metals4U stainless box sections](https://www.metals4u.co.uk/materials/stainless-steel/stainless-steel-box-section), including pages 2 and 3.
- [metals4U stainless tubes](https://www.metals4u.co.uk/materials/stainless-steel/stainless-steel-tube), including pages 3 and 4.
- [Linoksa stainless profile tubes](https://linoksa.com/lt/82-profiliniai-vamzdziai).
- [Linoksa stainless round tubes](https://linoksa.com/lt/81-apvalus-vamzdziai).

Public catalogue observations retrieved 9 November 2026. Each bundled observation retains its supplier, grade, finish, unit, VAT basis and source URL.
