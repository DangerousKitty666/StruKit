import {featureDistance} from './measure-geometry.js';
import {$,state} from './common.js';
const ns='http://www.w3.org/2000/svg';
export function navigation(is3d,view,rotate,orbit){
 const el=document.createElement('div');el.id='navigator';el.setAttribute('aria-label',is3d?'3D view navigation':'2D view rotation');
 el.innerHTML=`<div class="nav-tools"><button title="Home view" aria-label="Home view" data-nav="home">⌂</button><button title="Rotate left 15°" aria-label="Rotate left 15 degrees" data-nav="ccw">↶</button><button title="Rotate right 15°" aria-label="Rotate right 15 degrees" data-nav="cw">↷</button></div><div class="nav-compass"><button class="nav-n" data-nav="up" aria-label="Tilt up">▲</button><button class="nav-w" data-nav="left" aria-label="Turn left">◀</button><button class="nav-e" data-nav="right" aria-label="Turn right">▶</button><button class="nav-s" data-nav="down" aria-label="Tilt down">▼</button>${is3d?'<svg viewBox="0 0 80 80" class="nav-cube"><g tabindex="0" role="button" aria-label="Top view" data-face="top"><path d="M40 5 72 21 40 38 8 21Z"/><text x="40" y="25">TOP</text></g><g tabindex="0" role="button" aria-label="Front view" data-face="front"><path d="M8 21 40 38 40 74 8 56Z"/><text x="24" y="49">FR</text></g><g tabindex="0" role="button" aria-label="Right view" data-face="right"><path d="M40 38 72 21 72 56 40 74Z"/><text x="56" y="49">R</text></g></svg>':'<button class="nav-plan" data-nav="home" title="Reset rotation">TOP<br><small id="angle">0°</small></button>'}</div>`;
 $('viewport').append(el);
 el.querySelectorAll('[data-nav]').forEach(b=>b.onclick=()=>{if(!state.ready)return;const v=b.dataset.nav;if(v==='home')view('iso');else if(v==='cw'||v==='ccw')rotate(v==='cw'?15:-15);else if(is3d)orbit(v==='left'?-15:v==='right'?15:0,v==='up'?15:v==='down'?-15:0);else rotate(v==='left'||v==='down'?-15:15)});
 el.querySelectorAll('[data-face]').forEach(b=>{b.onclick=()=>state.ready&&view(b.dataset.face);b.onkeydown=e=>{if(e.key==='Enter'||e.key===' '){e.preventDefault();b.onclick()}}});
}

export function measurement({canvas,pick,project,enabled,unit,is3d,featurePick=()=>null}){
 const host=$('viewport'),svg=document.createElementNS(ns,'svg');svg.id='measurement-overlay';host.append(svg);
 const box=document.createElement('div');box.id='measurement-panel';box.hidden=true;
 box.innerHTML=`<div class="measure-head"><strong>Measure</strong><button id="pin-measure">Pin dimension</button><button id="close-measure" aria-label="Close measurement">×</button></div><label>Mode <select id="measure-mode"><option value="feature">Feature</option><option value="between">Feature to feature</option><option value="points">Two points</option></select></label><label id="feature-kind-row">Feature <select id="feature-kind"><option value="auto">Auto</option><option value="line">Line / edge</option><option value="point">Point</option><option value="hole">Circle / hole rim</option>${is3d?'<option value="surface">Surface</option>':''}</select></label><div id="measure-result"></div><small id="measure-note"></small><label>Input <select id="measure-input"><option value="all">Finger / pen</option><option value="pen">Pen only</option></select></label><section id="pinned-list"><strong>Pinned dimensions</strong><div id="pin-items"></div></section><div class="measure-bottom"><button id="clear-measure">Clear</button></div>`;
 host.append(box);let active=false,points=[],features=[],down=null,hover=null,hoverFeature=null,lastHover=0,penSeen=false,pins=[],current=null,nextPin=1;
 const mode=()=>$('measure-mode').value,penOnly=()=>$('measure-input').value==='pen';
 const syncControls=()=>enabled(!active||penOnly());
 function el(tag,attrs={},text){const n=document.createElementNS(ns,tag);for(const [k,v]of Object.entries(attrs))n.setAttribute(k,v);if(text!=null)n.textContent=text;svg.append(n);return n}
 function line(a,b,dashed=true){if(a&&b)el('line',{x1:a.x,y1:a.y,x2:b.x,y2:b.y,'stroke-dasharray':dashed?'5 3':'none'})}
 function paintFeature(f,cls){if(!f)return;const ps=f.points.map(project).filter(Boolean);if(!ps.length)return;
  if(f.kind==='surface'){const triangles=f.triangles||[];let path='';for(const t of triangles){const q=t.map(project);if(q.every(Boolean))path+=`M${q[0].x},${q[0].y}L${q[1].x},${q[1].y}L${q[2].x},${q[2].y}Z`;}el('path',{d:path,class:cls+' surface-feature'});}
  else if(f.kind==='point')el('circle',{cx:ps[0].x,cy:ps[0].y,r:6,class:cls});else el('polyline',{points:ps.map(p=>`${p.x},${p.y}`).join(' '),class:cls});
 }
 function anchor(record){if(record.points?.length)return record.points[Math.floor(record.points.length/2)];const f=record.features?.[0];return f?.center||f?.points[0]}
 const draw=()=>{svg.replaceChildren();for(const pin of pins){pin.features.forEach(f=>paintFeature(f,'selected-feature'));const ps=pin.points.map(project).filter(Boolean);if(ps.length===2)line(...ps);const a=anchor(pin),p=a&&project(a);if(p)el('text',{x:p.x+8,y:p.y-10,class:'pinned-label'},'#'+pin.id+' '+describe(pin).text);}
  if(!active)return;features.forEach(f=>paintFeature(f,'selected-feature'));paintFeature(hoverFeature,'hover-feature');const ps=points.map(project).filter(Boolean);if(ps.length===2)line(...ps);
  ps.forEach((p,i)=>{el('circle',{cx:p.x,cy:p.y,r:5});el('text',{x:p.x+9,y:p.y-8},i?'B':'A')});
  if(hover){const r=canvas.getBoundingClientRect(),h={x:hover.x-r.left,y:hover.y-r.top};el('circle',{cx:h.x,cy:h.y,r:9,class:'pen-target'});
   const a=mode()==='between'&&features.length===1?(features[0].center||features[0].points[0]):mode()==='points'&&points.length===1?points[0]:null;if(a)line(project(a),h);
  }
 };
 function describe(rec){const u=unit(),fmt=n=>(n*u.scale).toLocaleString(undefined,{maximumFractionDigits:3}),approx=rec.approximate?'≈ ':'';
  if(rec.mode==='feature'){const f=rec.features[0];if(f.kind==='point'){const p=f.coordinates||f.points[0];return {text:`X ${fmt(p.x)} · Y ${fmt(p.y)}${is3d?' · Z '+fmt(p.z||0):''} ${u.label}`,value:[p.x,p.y,p.z||0].map(n=>n*u.scale)}}
   if(f.kind==='circle')return {text:`${approx}Ø ${fmt(f.radius*2)} ${u.label}`,value:f.radius*2*u.scale};
   if(f.kind==='surface')return {text:`${approx}Surface area ${(f.area*u.scale*u.scale).toLocaleString(undefined,{maximumFractionDigits:2})} ${u.label}²`,value:f.area*u.scale*u.scale};
   return {text:`${approx}${f.kind==='arc'?'Arc':'Edge'} ${fmt(f.length)} ${u.label}`,value:f.length*u.scale};
  }return {text:`${approx}${fmt(rec.distance)} ${u.label}`,value:rec.distance*u.scale};
 }
 function refresh(){current=null;try{
  if(mode()==='feature'&&features.length)current={mode:'feature',features:[features.at(-1)],points:[],approximate:!!features.at(-1).approximate};
  else if(mode()==='between'&&features.length===2)current={mode:'between',features:[...features],...featureDistance(...features)};
  else if(mode()==='points'&&points.length===2)current={mode:'points',features:[],points:[...points],distance:Math.hypot(points[1].x-points[0].x,points[1].y-points[0].y,(points[1].z||0)-(points[0].z||0)),approximate:is3d};
  if(current){if(current.mode==='between')points=current.points;const d=describe(current);$('measure-result').textContent=d.text;state.measurement={mode:current.mode,kind:current.features.at(-1)?.kind,value:d.value,distance:current.distance==null?undefined:current.distance*unit().scale,unit:unit().label,approximate:current.approximate,points:current.points.map(p=>({x:p.x,y:p.y,z:p.z||0}))};}
  else{$('measure-result').textContent=mode()==='feature'?'Select a feature':(features.length===1||points.length===1?'Choose B':'Choose A');delete state.measurement}
  $('measure-note').textContent=current?.basis||(is3d?'Mesh estimates. Circles use centre-to-centre distances; surfaces use finite mesh geometry.':'Drawing units must be confirmed. Circles use their centres in Feature to feature.');
 }catch(e){$('measure-result').textContent=e.message;delete state.measurement}
 $('pin-measure').disabled=!current;state.pinnedDimensions=pins.length;draw();}
 function pinList(){$('pin-items').replaceChildren();for(const pin of pins){const row=document.createElement('div');row.textContent='#'+pin.id+' '+describe(pin).text+' ';const b=document.createElement('button');b.textContent='Delete';b.onclick=()=>{pins=pins.filter(p=>p!==pin);pinList();refresh()};row.append(b);$('pin-items').append(row)}}
 function clear(){points=[];features=[];hoverFeature=null;hover=null;refresh()}
 function setActive(v){if(v)document.dispatchEvent(new Event('viewer-measure-open'));active=v;state.measuring=v;box.hidden=!v;$('measure').classList.toggle('active',v);$('measure').setAttribute('aria-pressed',String(v));clear();syncControls()}
 $('measure').onclick=()=>state.ready&&setActive(!active);$('close-measure').onclick=()=>setActive(false);$('clear-measure').onclick=clear;
 $('pin-measure').onclick=()=>{if(current){if(pins.length>=30){$('measure-result').textContent='30 pinned dimensions. Delete one first.';return}pins.push({...current,id:nextPin++});pinList();clear()}};
 $('measure-mode').onchange=()=>{$('feature-kind-row').hidden=mode()==='points';clear()};$('feature-kind').onchange=clear;$('measure-input').onchange=syncControls;
 const acceptable=e=>e.isPrimary!==false&&(!penOnly()||e.pointerType==='pen');
 const autoPen=()=>{if(!state.ready)return; $('measure-input').value='pen';if(!active)setActive(true);syncControls()};window.activatePenMeasure=autoPen;
 canvas.addEventListener('pointerdown',e=>{if(!active||!acceptable(e))return;e.stopImmediatePropagation();e.preventDefault();down={id:e.pointerId,x:e.clientX,y:e.clientY};canvas.setPointerCapture(e.pointerId)},true);
 canvas.addEventListener('pointermove',e=>{if(e.pointerType==='pen'){if(!penSeen){penSeen=true;autoPen();}if(active){hover={x:e.clientX,y:e.clientY};if(mode()!=='points'&&performance.now()-lastHover>70){lastHover=performance.now();hoverFeature=featurePick(e,$('feature-kind').value);state.hoverFeature=hoverFeature?{kind:hoverFeature.kind}:null;}draw()}}if(down?.id===e.pointerId)e.stopImmediatePropagation()},true);
 canvas.addEventListener('pointerup',e=>{if(!active||!down||down.id!==e.pointerId)return;e.stopImmediatePropagation();const start=down;down=null;if(Math.hypot(start.x-e.clientX,start.y-e.clientY)>10)return;
  if(mode()==='points'){const p=pick(e);if(p){if(points.length===2)points=[];points.push(p)}}else{const f=featurePick(e,$('feature-kind').value);if(!f){$('measure-result').textContent='No supported feature. Try a feature filter or Two points.';return}if(mode()==='feature'||features.length===2){features=[];points=[]}features.push(f)}hoverFeature=null;refresh();},true);
 canvas.addEventListener('pointercancel',()=>down=null);canvas.addEventListener('pointerleave',()=>{hover=null;hoverFeature=null;delete state.hoverFeature;draw()});refresh();
 return {draw,refresh:()=>{pinList();refresh()},reset:()=>{penSeen=false;pins=[];pinList();setActive(false)},get active(){return active},get navigationEnabled(){return !active||penOnly()}};
}

