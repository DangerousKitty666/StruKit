export const componentNames={R:'Resistor',V:'Voltage source',I:'Current source',C:'Capacitor',L:'Inductor',S:'Switch',D:'Diode',LED:'LED',W:'Wire link'};
export const symbols={
R:'<path d="M0 0h12m20 0h12"/><rect x="12" y="-6" width="20" height="12"/>',
V:'<path d="M0 0h12m20 0h12m-27 0h10m-5-5v10"/><circle cx="22" cy="0" r="10"/>',
I:'<path d="M0 0h12m20 0h12m-28 0h12l-4-4m4 4-4 4"/><circle cx="22" cy="0" r="10"/>',
C:'<path d="M0 0h18m0-9v18m8-18v18m0-9h18"/>',L:'<path d="M0 0h6c0-12 8-12 8 0c0-12 8-12 8 0c0-12 8-12 8 0c0-12 8-12 8 0h6"/>',
S:'<path d="M0 0h12l19-10m1 10h12"/><circle cx="12" cy="0" r="2"/><circle cx="32" cy="0" r="2"/>',W:'<path d="M0 0h44"/>',
D:'<path d="M0 0H14M14-9 28 0 14 9ZM28-9V9M28 0H44"/>',
LED:'<path d="M0 0H14M14-9 28 0 14 9ZM28-9V9M28 0H44M30-8l7-7m-4 0h4v4m0 6 7-7m-4 0h4v4"/>',
Ground:'<path d="M22-12v10m-15 0h30m-25 5h20m-15 5h10"/>',Earth:'<path d="M22-12v12H7m15 0h15M12 0l-6 10m16-10-6 10m16-10-6 10"/>',
Fuse:'<path d="M0 0h44"/><rect x="10" y="-6" width="24" height="12"/>',Lamp:'<path d="M0 0h12m20 0h12m-29-7 14 14m-14 0 14-14"/><circle cx="22" cy="0" r="10"/>',
Motor:'<path d="M0 0h10m24 0h10m-28 5v-10l6 6 6-6v10"/><circle cx="22" cy="0" r="12"/>',
Transformer:'<path d="M4-12h7c10 0 10 8 0 8c10 0 10 8 0 8c10 0 10 8 0 8H4M40-12h-7c-10 0-10 8 0 8c-10 0-10 8 0 8c-10 0-10 8 0 8h7M21-14v28m3-28v28"/>',
Ammeter:'<path d="M0 0h10m24 0h10m-28 6 6-12 6 12m-9-4h6"/><circle cx="22" cy="0" r="12"/>',
Voltmeter:'<path d="M0 0h10m24 0h10m-28-6 6 12 6-12"/><circle cx="22" cy="0" r="12"/>',
Battery:'<path d="M0 0h14m0-12v24m7-18v12m7-18v24m7-18v12m0-6h9"/>'};
export function symbolLibrary(){return '<section class="card"><h2>Symbols</h2>'+Object.entries(symbols).map(([key,svg])=>'<div class="symbol"><svg viewBox="-3 -19 54 38" aria-label="'+key+'">'+svg+'</svg><div>'+ (componentNames[key]||key)+'<small>'+(componentNames[key]?'Available in builder':key==='Ground'?'Set a reference terminal in builder':'Reference symbol only')+'</small></div></div>').join('')+'</section>'}
