# StruKit Revision 7.27 A — Android

Changes
- Expanded profile property panels follow the full table width.
- Comparison pins always occupy one of three equal-width slots. Single tap opens a unified blue property dialog; double tap removes the pin. Material record dialogs use the same background without nested panel borders.
- Right-hand tool rails extend upward by another 96 dp, bounded by the available screen space.
- Building drafter searches the section catalogue for column and rafter candidates using geometry, grade and editable gravity/wind loads. Suggested member names and mass update with inputs; manual sections remain available.
- Standards menu uses the available space below its button, with a 20 dp downward gap, scrolling and existing colour groups.
- PDF search and page-number entry/Go are below the document. Page numbers are physical PDF pages, including covers, not necessarily printed page labels. Search still navigates matching pages.
- Forces includes a wind surface-pressure and force calculator, with mean-speed/turbulence or direct peak-pressure input.
- Converter notes are above the bottom input panel. Unit rows align right and Swap is on the left.
- Fits include a dimensioned section and scaled deviation-zone plot, tolerance widths, radial clearance and optional measured-part checks. H8/f7 and H7/n6 added through 120 mm; existing selected fits retained through 500 mm.
- Material library back link matches the adjacent 19 sp title.

Calculation scope
The drafter is a preliminary screening tool, not a complete portal-frame solver or a certified structural design. It uses independent simply-supported roof-beam and fixed-base cantilever-column models; it does not resolve joint compatibility or portal forces. Fully restrained rafter compression flanges are assumed. Geometry range is limited to span 40 m, highest level 12 m and roof slope 30 degrees. No passing candidate gives no automatic schedule. Manual sections are not checked.

The model includes section self-weight, elastic bending, an axial/bending screening sum, weak-axis buckling reduction and trial deflection limits. It does not establish lateral-torsional restraint, second-order response, connections, foundations or the complete load combinations required for a building. The in-app calculation basis exposes assumptions and formulas. The archived drawings include different structural systems and are used as reference context, not a universal size-to-area rule.

Wind results are one unfactored surface load case. Coefficients and site conditions are user inputs; default values are examples. External and internal reference pressures are assumed equal. It does not automatically apply regional National Annex maps, zones or dynamic factors.

Sources consulted
- SCI P397, portal frames: https://www.steelconstruction.info/images/c/c5/SCI_P397.pdf
- JRC Eurocode wind worked examples: https://eurocodes.jrc.ec.europa.eu/sites/default/files/2022-06/Bridge_Design-Eurocodes-Worked_examples.pdf
- JRC steel buckling workshop: https://eurocodes.jrc.ec.europa.eu/sites/default/files/2022-06/05_Eurocodes_Steel_Workshop_SIMOES.pdf
- Schaeffler limits and fits tables: https://www.schaeffler-publications.com/pdf/HG1_en-GB.pdf
- Supplied building drawings retained in work/rev725a/reference-archive/draft-projects outside the APK.

Validation
- Pure Kotlin checks: wind magnitude/sign/invalid inputs; ISO fit limits and diameter boundaries; section geometry; member response to span, height and snow load; unsupported and empty-catalogue cases.
- Native phone interaction has not been verified in this environment. Review layout and gestures on the phone after installation.
- All existing material and standards assets are retained. ISO 14120 original PDF is still unavailable; its existing guide remains labelled accordingly.

Previous revisions and their APKs are unchanged.
