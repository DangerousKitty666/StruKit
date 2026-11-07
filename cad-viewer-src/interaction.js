import {$,state} from './common.js';
const ns='http://www.w3.org/2000/svg';
export function navigation(is3d,view,rotate,orbit){
 const el=document.createElement('div');el.id='navigator';el.setAttribute('aria-label',is3d?'3D view navigation':'2D view rotation');
 el.innerHTML=`<div class="nav-tools"><button title="Home view" aria-label="Home view" data-nav="home">⌂</button><button title="Rotate left 15°" aria-label="Rotate left 15 degrees" data-nav="ccw">↶</button><button title="Rotate right 15°" aria-label="Rotate right 15 degrees" data-nav="cw">↷</button></div><div class="nav-compass"><button class="nav-n" data-nav="up" aria-label="Tilt up">▲</button><button class="nav-w" data-nav="left" aria-label="Turn left">◀</button><button class="nav-e" data-nav="right" aria-label="Turn right">▶</button><button class="nav-s" data-nav="down" aria-label="Tilt down">▼</button>${is3d?'<svg viewBox="0 0 80 80" class="nav-cube"><g tabindex="0" role="button" aria-label="Top view" data-face="top"><path d="M40 5 72 21 40 38 8 21Z"/><text x="40" y="25">TOP</text></g><g tabindex="0" role="button" aria-label="Front view" data-face="front"><path d="M8 21 40 38 40 74 8 56Z"/><text x="24" y="49">FRONT</text></g><g tabindex="0" role="button" aria-label="Right view" data-face="right"><path d="M40 38 72 21 72 56 40 74Z"/><text x="56" y="49">RIGHT</text></g></svg>':'<button class="nav-plan" data-nav="home" title="Reset rotation">TOP<br><small id="angle">0°</small></button>'}</div>`;
 $('viewport').append(el);
 el.querySelectorAll('[data-nav]').forEach(b=>b.onclick=()=>{if(!state.ready)return;const v=b.dataset.nav;if(v==='home')view('iso');else if(v==='cw'||v==='ccw')rotate(v==='cw'?15:-15);else if(is3d)orbit(v==='left'?-15:v==='right'?15:0,v==='up'?15:v==='down'?-15:0);else rotate(v==='left'||v==='down'?-15:15)});
 el.querySelectorAll('[data-face]').forEach(b=>{b.onclick=()=>state.ready&&view(b.dataset.face);b.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();b.onclick()}}});
}
export function measurement({canvas,pick,project,enabled,unit,is3d}){
 const host=$('viewport'),svg=document.createElementNS(ns,'svg');svg.id='measurement-overlay';host.append(svg);
 const box=document.createElement('div');box.id='measurement-panel';box.hidden=true;
 box.innerHTML='<strong>Measure</strong><button id="clear-measure" title="Clear measurement">Clear</button><button id="close-measure" aria-label="Close measurement">×</button><div id="measure-result">Choose first point</div><small id="measure-note"></small><label>Input <select id="measure-input"><option value="all">Finger / pen</option><option value="pen">Pen only</option></select></label>';
 host.append(box);let active=false,points=[],down=null,penAt=-Infinity,hover=null;
 const draw=()=>{svg.replaceChildren();if(!active)return;const ps=points.map(p=>project(p));
  if(ps.length===2){const line=document.createElementNS(ns,'line');line.setAttribute('x1',ps[0].x);line.setAttribute('y1',ps[0].y);line.setAttribute('x2',ps[1].x);line.setAttribute('y2',ps[1].y);svg.append(line)}
  ps.forEach((p,i)=>{const c=document.createElementNS(ns,'circle');c.setAttribute('cx',p.x);c.setAttribute('cy',p.y);c.setAttribute('r','5');svg.append(c);const t=document.createElementNS(ns,'text');t.setAttribute('x',p.x+9);t.setAttribute('y',p.y-8);t.textContent=i?'B':'A';svg.append(t)});
  if(hover){const r=canvas.getBoundingClientRect();const c=document.createElementNS(ns,'circle');c.setAttribute('cx',hover.x-r.left);c.setAttribute('cy',hover.y-r.top);c.setAttribute('r','9');c.classList.add('pen-target');svg.append(c)}
 };
 const refresh=()=>{const u=unit();if(points.length===2){const a=points[0],b=points[1],dx=(b.x-a.x)*u.scale,dy=(b.y-a.y)*u.scale,dz=((b.z||0)-(a.z||0))*u.scale;const distance=Math.hypot(dx,dy,dz);$('measure-result').textContent=`${is3d?'≈ ':''}${distance.toLocaleString(undefined,{maximumFractionDigits:3})} ${u.label} · ΔX ${Math.abs(dx).toFixed(3)} · ΔY ${Math.abs(dy).toFixed(3)}${is3d?' · ΔZ '+Math.abs(dz).toFixed(3):''}`;state.measurement={distance,unit:u.label,points:points.map(p=>({x:p.x,y:p.y,z:p.z||0})),approximate:is3d};}else{$('measure-result').textContent=points.length?'Choose second point':'Choose first point';delete state.measurement}
 $('measure-note').textContent=is3d?'Mesh point distance · nearby triangle vertices snap. STEP display tolerance 0.5 mm; not an exact CAD dimension.':'Point distance in the drawing plane · no geometry snapping. Confirm drawing units.';draw();};
 const setActive=v=>{active=v;state.measuring=v;points=[];hover=null;box.hidden=!v;$('measure').classList.toggle('active',v);$('measure').setAttribute('aria-pressed',String(v));enabled(!v);refresh()};
 $('measure').onclick=()=>state.ready&&setActive(!active);$('close-measure').onclick=()=>setActive(false);$('clear-measure').onclick=()=>{points=[];refresh()};
 const acceptable=e=>e.isPrimary!==false&&(e.pointerType==='pen'||($('measure-input').value!=='pen'&&performance.now()-penAt>900));
 canvas.addEventListener('pointerdown',e=>{if(e.pointerType==='pen')penAt=performance.now();if(!active||!acceptable(e))return;e.stopImmediatePropagation();e.preventDefault();down={id:e.pointerId,x:e.clientX,y:e.clientY};canvas.setPointerCapture(e.pointerId)},true);
 canvas.addEventListener('pointermove',e=>{if(e.pointerType==='pen'){penAt=performance.now();if(active){hover={x:e.clientX,y:e.clientY};draw()}}if(active&&down?.id===e.pointerId)e.stopImmediatePropagation()},true);
 canvas.addEventListener('pointerup',e=>{if(!active||!down||down.id!==e.pointerId)return;e.stopImmediatePropagation();const start=down;down=null;if(Math.hypot(start.x-e.clientX,start.y-e.clientY)>10)return;const p=pick(e);if(!p){$('measure-result').textContent='No visible surface here. Tap on the model.';return}if(points.length===2)points=[];points.push(p);refresh()},true);
 canvas.addEventListener('pointercancel',()=>{down=null});canvas.addEventListener('pointerleave',()=>{hover=null;draw()});
 return {draw,refresh,reset:()=>setActive(false),get active(){return active}};
}

