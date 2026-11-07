import {DxfViewer} from 'dxf-viewer';
import {navigation,measurement as createMeasurement} from './interaction.js';
import {dxfFeatures,nearestFeature} from './features.js';
import {Color} from 'three';
import {$,state,status,busy,fail,configure,validate,ready,reset} from './common.js';
if(new URLSearchParams(location.search).get('mode')==='3d'){import('./three.js');}else{
 configure('2D viewer','.dwg,.dxf','2D DWG / ASCII DXF model-space preview using LibreDWG and dxf-viewer. DWG is converted locally in memory. Layouts, external references, custom/proxy objects and some dimensions/hatches are not fully supported. Fonts may be substituted; line patterns and widths can differ. Do not use the preview as a fabrication or dimensional verification drawing.','Drag to pan · pinch to zoom · Fit resets the view');
 let viewer,worker,timer,sequence=0,rows=[],messages=[],measurement=null,angle=0,features=[];
 function clear(){features=[];measurement?.reset();measurement=null;$('measurement-panel')?.remove();$('measurement-overlay')?.remove();angle=0;worker?.terminate();worker=null;clearTimeout(timer);viewer?.Destroy();viewer=null;}
 function fit(){const b=viewer?.GetBounds(),o=viewer?.GetOrigin();if(!b)return;angle=0;viewer.GetCamera().up.set(0,1,0);viewer.FitView(b.minX-o.x,b.maxX-o.x,b.minY-o.y,b.maxY-o.y);rebase();}
 // dxf-viewer 1.0.50 pins the control up-vector when constructed. Recreate only its controls after roll.
 function rebase(){viewer._CreateControls();viewer.controls.enabled=!measurement?.active;viewer.Render();$('angle').textContent=Math.round(angle)+'°';state.rotation=angle;measurement?.draw();}
 function rotate(degrees){if(!viewer)return;angle=(angle+degrees+360)%360;const c=viewer.GetCamera(),rad=angle*Math.PI/180;c.up.set(-Math.sin(rad),Math.cos(rad),0);c.lookAt(c.position.x,c.position.y,0);rebase();}
 navigation(false,fit,rotate,()=>{});
 $('unitsRow').hidden=false;$('units').insertAdjacentHTML('afterbegin','<option value="raw">drawing units</option>');$('units').value='raw';$('units').onchange=()=>measurement?.refresh();
 function layers(){const q=$('filter').value.toLowerCase();$('items').replaceChildren();for(const row of rows.filter(x=>x.name.toLowerCase().includes(q))){const label=document.createElement('label');label.className='item';const check=document.createElement('input');check.type='checkbox';check.checked=row.visible;check.onchange=()=>{row.visible=check.checked;viewer.ShowLayer(row.name,check.checked)};const text=document.createElement('span');text.textContent=row.name;label.append(check,text);$('items').append(label)}}
 $('fit').onclick=fit;$('filter').oninput=layers;
 $('cancel').onclick=()=>{sequence++;clear();reset();busy(null);status('Loading cancelled. Choose another file.');};
 async function open(file){let url;const id=++sequence;clear();reset();messages=[];try{
  validate(file,['dwg','dxf']);$('filename').textContent=file.name;busy('Reading drawing…');let buffer=await file.arrayBuffer();if(id!==sequence)return;
  if(new TextDecoder().decode(buffer.slice(0,22)).startsWith('AutoCAD Binary DXF'))throw Error('Binary DXF is not supported in this prototype. Save as ASCII DXF.');
  if(file.name.toLowerCase().endsWith('.dwg')){busy('Decoding DWG on your device…');buffer=await new Promise((resolve,reject)=>{worker=new Worker('dwg-worker.js',{type:'module'});timer=setTimeout(()=>{worker?.terminate();reject(Error('DWG import exceeded 120 seconds. Try a smaller drawing.'))},120000);worker.onmessage=({data})=>{clearTimeout(timer);worker.terminate();worker=null;if(data.error)reject(Error(data.error));else{messages=data.warnings;resolve(data.buffer)}};worker.onerror=e=>{clearTimeout(timer);worker?.terminate();reject(Error(e.message||'DWG reader failed.'))};worker.postMessage(buffer,[buffer]);});}
  if(id!==sequence)return;
  url=URL.createObjectURL(new Blob([buffer]));buffer=null;
  viewer=new DxfViewer($('viewport'),{autoResize:true,clearColor:new Color('#10151b'),colorCorrection:true,antialias:true,retainParsedDxf:true});
  viewer.Subscribe('message',e=>{messages.push(e.detail?.message||'Drawing contains unsupported content.');});
  await Promise.race([viewer.Load({url,fonts:[new URL('vendor/NotoSans-Regular.ttf',location.href).href],workerFactory:()=>new Worker('dxf-worker.js'),progressCbk:phase=>busy(phase==='prepare'?'Preparing drawing geometry…':'Reading drawing…')}),new Promise((_,reject)=>{timer=setTimeout(()=>reject(Error('Drawing preparation exceeded 120 seconds.')),120000)})]);clearTimeout(timer);
  if(id!==sequence)return;
  if(!viewer.GetBounds())throw Error('No visible model-space geometry.');
  const featureData=dxfFeatures(viewer.GetDxf(),viewer.GetOrigin());features=featureData.features;state.featureIndex={count:features.length,skipped:featureData.skipped,truncated:featureData.truncated};viewer.parsedDxf=null;
  measurement=createMeasurement({canvas:viewer.GetCanvas(),is3d:false,featurePick:(e,filter)=>{const rect=viewer.GetCanvas().getBoundingClientRect(),f=nearestFeature(features,{x:e.clientX-rect.left,y:e.clientY-rect.top},p=>viewer.SceneToCanvasCoord(p.x,p.y),filter,e.pointerType==='pen'?10:18,f=>f.layers.every(layer=>rows.find(r=>r.name===layer)?.visible!==false));if(f?.kind==='point'){const o=viewer.GetOrigin();return {...f,coordinates:{x:f.points[0].x+o.x,y:f.points[0].y+o.y,z:0}}}return f},enabled:v=>{viewer.controls.enabled=v},unit:()=>({scale:$('units').value==='raw'?1:Number($('units').value),label:$('units').value==='raw'?'drawing units':'mm'}),pick:e=>{const rect=viewer.GetCanvas().getBoundingClientRect();return viewer.CanvasToSceneCoord(e.clientX-rect.left,e.clientY-rect.top)},project:p=>viewer.SceneToCanvasCoord(p.x,p.y)});
  viewer.Subscribe('viewChanged',()=>measurement?.draw());rows=[...viewer.GetLayers()];layers();ready(file);state.layers=rows.length;state.bounds=viewer.GetBounds();state.warnings=messages;status(`${rows.length} layers · model space only${messages.length?' · '+messages.length+' reader warnings':''}`);$('selection').textContent=messages.length?messages.slice(0,2).join(' · '):'';
 }catch(e){if(id===sequence){clear();reset();fail(e);}}finally{if(url)URL.revokeObjectURL(url);}}
 $('file').onchange=e=>{const f=e.target.files[0];e.target.value='';if(f)open(f)};window.openViewerFile=open;window.addEventListener('pagehide',clear);
}
