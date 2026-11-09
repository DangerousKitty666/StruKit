// Original explanatory schematics; no external image dependencies.
export function toolDiagram(tool,tab){
const head='<svg class="tool-diagram" role="img" aria-label="'+tab+' schematic" viewBox="0 0 300 170">',end='<text x="12" y="158">'+tab+' · schematic</text></svg>';
if(['Circuit builder','Symbols'].includes(tab))return '';
const circuit='<path d="M45 62V35H115m45 0h95v70H45V98"/><circle cx="45" cy="80" r="18"/><path d="M34 80h22m-11-11v22"/><rect x="115" y="26" width="45" height="18"/><text x="128" y="20">R</text><text x="14" y="55">V</text><path d="M195 35h30m-8-5 8 5-8 5"/><text x="200" y="22">I →</text>';
const maps={
'Calculator':circuit,'Equation check':circuit,
'AC / mains':'<path d="M20 70H280M40 35V110"/><path class="cool" d="M40 65Q60 5 80 65T120 65T160 65T200 65T240 65"/><path class="warm" d="M60 65Q80 25 100 65T140 65T180 65T220 65T260 65"/><text x="55" y="16">Voltage</text><text x="160" y="16">Current · phase φ</text><text x="270" y="83">t</text>',
'Voltage drop':'<path d="M45 35H95m100 0h60v70h-60m-100 0H45Z"/><rect x="95" y="28" width="100" height="14"/><rect x="95" y="98" width="100" height="14"/><text x="110" y="20">Cable · L, A</text><text x="112" y="76">ΔV = I Rloop</text>',
'Energy':'<rect x="35" y="25" width="75" height="80"/><circle cx="73" cy="65" r="25"/><path d="M73 45v20l16 10"/><text x="140" y="55">Power × time</text><text x="140" y="85">E = P t</text>',
'RC timing':'<path d="M35 20V110H275"/><path class="cool" d="M35 110C75 35 130 30 270 30"/><path stroke-dasharray="4 4" d="M35 59H105V110"/><text x="125" y="85">τ = RC</text><text x="8" y="20">Vc</text><text x="278" y="114">t</text>',
'Ducts':'<path d="M35 50h150v55H35ZM35 50l55-28h150l-55 28m0 55 55-28V22"/><path class="cool" d="M70 77h75m-10-7 10 7-10 7"/><text x="100" y="65">Q</text><text x="90" y="122">width a</text><text x="12" y="84">b</text>',
'Air moisture':'<path d="M40 25v65a15 15 0 1 0 20 0V25a10 10 0 0 0-20 0"/><path class="warm" d="M50 42v58"/><path class="cool" d="M145 25C140 40 120 58 120 76a25 25 0 0 0 50 0c0-18-20-36-25-51Z"/><text x="185" y="55">T dry bulb</text><text x="185" y="80">RH · water</text>',
'Heat transfer':'<rect x="120" y="20" width="55" height="90"/><path class="warm" d="M25 65h85m-10-8 10 8-10 8"/><path class="cool" d="M185 65h85m-10-8 10 8-10 8"/><text x="28" y="42">T hot</text><text x="210" y="42">T cold</text><text x="132" y="72">k</text><text x="140" y="130">L</text>',
'Airflow':'<path d="M40 50h180v60H40Zm0 0 50-25h180l-50 25m0 60 50-25V25"/><path class="cool" d="M70 80h100m-12-7 12 7-12 7"/><text x="82" y="14">Q = room V × ACH</text>',
'Canopy':'<path d="M60 75 90 35h120l30 40ZM140 35V15h20v20M70 110h160"/><path class="warm" d="M100 106V85m-5 5 5-5 5 5m40 16V85m-5 5 5-5 5 5m40 16V85m-5 5 5-5 5 5"/><text x="97" y="65">Capture airflow</text>',
'DW/144':'<path d="M40 50h170v55H40ZM40 50l40-25h170l-40 25m0 55 40-25V25M120 50v55m0-55 40-25"/><text x="60" y="80">Duct</text><text x="137" y="80">Joint</text>'};
return head+(maps[tab]||circuit)+end;
}

