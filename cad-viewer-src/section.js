import * as THREE from 'three';
export function createSection({host,renderer,button,toggle,render,state,scale}){
 const panel=document.createElement('div');panel.id='section-panel';panel.className='viewer-options';panel.hidden=true;
 panel.innerHTML='<div class="option-heading"><strong>Section view</strong><button id="close-section" aria-label="Close section settings">×</button></div><label><input id="section-enabled" type="checkbox"> Enable cut</label><div class="section-axes" role="group" aria-label="Cutting plane"><button data-axis="x">X</button><button data-axis="y">Y</button><button data-axis="z">Z</button></div><label class="section-position">Position from model centre · mm<input id="section-position" type="number" step="any"></label><input id="section-slider" aria-label="Section position" type="range" min="0" max="100" step="0.1" value="50"><div class="section-actions"><button id="section-flip">Flip side</button><button id="section-centre">Centre</button><button id="section-off">Clear cut</button></div><small>Visual clipping only · cut faces are open, not capped.</small>';
 host.append(panel);const find=id=>panel.querySelector('#'+id),plane=new THREE.Plane(),bounds=new THREE.Box3();let enabled=false,axis='x',fraction=.5,sign=1,loaded=false;
 const position=()=>bounds.isEmpty()?0:bounds.min[axis]+fraction*(bounds.max[axis]-bounds.min[axis]);
 function sync(){const p=position();plane.normal.set(axis==='x'?sign:0,axis==='y'?sign:0,axis==='z'?sign:0);plane.constant=-sign*p;renderer.clippingPlanes=enabled&&loaded?[plane]:[];
  find('section-enabled').checked=enabled;find('section-position').value=Number((p*scale()).toFixed(4));find('section-slider').value=fraction*100;
  panel.querySelectorAll('[data-axis]').forEach(b=>b.classList.toggle('active',b.dataset.axis===axis));button.classList.toggle('active',enabled);state.section={enabled,axis,position:p*scale(),fraction,side:sign,constant:plane.constant,normal:plane.normal.toArray()};render();
 }
 find('close-section').onclick=()=>panel.hidden=true;
 button.onclick=()=>{if(loaded)toggle(panel)};button.disabled=true;
 find('section-enabled').onchange=e=>{enabled=e.target.checked;sync()};
 panel.querySelectorAll('[data-axis]').forEach(b=>b.onclick=()=>{axis=b.dataset.axis;fraction=.5;sync()});
 find('section-slider').oninput=e=>{fraction=Number(e.target.value)/100;sync()};
 find('section-position').onchange=e=>{const n=Number(e.target.value)/scale(),span=bounds.max[axis]-bounds.min[axis];if(!e.target.value.trim()||!Number.isFinite(n)){sync();return}fraction=span>0?THREE.MathUtils.clamp((n-bounds.min[axis])/span,0,1):.5;sync()};
 find('section-flip').onclick=()=>{sign*=-1;sync()};find('section-centre').onclick=()=>{fraction=.5;sync()};find('section-off').onclick=()=>{enabled=false;sync()};
 return {panel,visible:p=>!enabled||plane.distanceToPoint(p)>=-1e-7,load:group=>{bounds.setFromObject(group);loaded=!bounds.isEmpty();button.disabled=!loaded;enabled=false;axis='x';fraction=.5;sign=1;sync()},reset:()=>{loaded=false;enabled=false;button.disabled=true;panel.hidden=true;bounds.makeEmpty();sync()},updateUnits:sync};
}
