import {DxfViewer} from 'dxf-viewer';
import {Color} from 'three';
import {$,state,status,busy,fail,configure,validate,ready,reset} from './common.js';
if(new URLSearchParams(location.search).get('mode')==='3d'){import('./three.js');}else{
 configure('2D viewer','.dwg,.dxf','2D DWG / ASCII DXF model-space preview using LibreDWG and dxf-viewer. DWG is converted locally in memory. Layouts, external references, custom/proxy objects and some dimensions/hatches are not fully supported. Fonts may be substituted; line patterns and widths can differ. Do not use the preview as a fabrication or dimensional verification drawing.','Drag to pan · pinch to zoom · Fit resets the view');
 let viewer,worker,timer,sequence=0,rows=[],messages=[];
 function clear(){worker?.terminate();worker=null;clearTimeout(timer);viewer?.Destroy();viewer=null;}
 function fit(){const b=viewer?.GetBounds(),o=viewer?.GetOrigin();if(b)viewer.FitView(b.minX-o.x,b.maxX-o.x,b.minY-o.y,b.maxY-o.y);}
 function layers(){const q=$('filter').value.toLowerCase();$('items').replaceChildren();for(const row of rows.filter(x=>x.name.toLowerCase().includes(q))){const label=document.createElement('label');label.className='item';const check=document.createElement('input');check.type='checkbox';check.checked=row.visible;check.onchange=()=>{row.visible=check.checked;viewer.ShowLayer(row.name,check.checked)};const text=document.createElement('span');text.textContent=row.name;label.append(check,text);$('items').append(label)}}
 $('fit').onclick=fit;$('filter').oninput=layers;
 $('cancel').onclick=()=>{sequence++;clear();reset();busy(null);status('Loading cancelled. Choose another file.');};
 async function open(file){let url;const id=++sequence;clear();reset();messages=[];try{
  validate(file,['dwg','dxf']);$('filename').textContent=file.name;busy('Reading drawing…');let buffer=await file.arrayBuffer();if(id!==sequence)return;
  if(new TextDecoder().decode(buffer.slice(0,22)).startsWith('AutoCAD Binary DXF'))throw Error('Binary DXF is not supported in this prototype. Save as ASCII DXF.');
  if(file.name.toLowerCase().endsWith('.dwg')){busy('Decoding DWG on your device…');buffer=await new Promise((resolve,reject)=>{worker=new Worker('dwg-worker.js',{type:'module'});timer=setTimeout(()=>{worker?.terminate();reject(Error('DWG import exceeded 120 seconds. Try a smaller drawing.'))},120000);worker.onmessage=({data})=>{clearTimeout(timer);worker.terminate();worker=null;if(data.error)reject(Error(data.error));else{messages=data.warnings;resolve(data.buffer)}};worker.onerror=e=>{clearTimeout(timer);worker?.terminate();reject(Error(e.message||'DWG reader failed.'))};worker.postMessage(buffer,[buffer]);});}
  if(id!==sequence)return;
  url=URL.createObjectURL(new Blob([buffer]));buffer=null;
  viewer=new DxfViewer($('viewport'),{autoResize:true,clearColor:new Color('#10151b'),colorCorrection:true,antialias:true,retainParsedDxf:false});
  viewer.Subscribe('message',e=>{messages.push(e.detail?.message||'Drawing contains unsupported content.');});
  await Promise.race([viewer.Load({url,fonts:[new URL('vendor/NotoSans-Regular.ttf',location.href).href],workerFactory:()=>new Worker('dxf-worker.js'),progressCbk:phase=>busy(phase==='prepare'?'Preparing drawing geometry…':'Reading drawing…')}),new Promise((_,reject)=>{timer=setTimeout(()=>reject(Error('Drawing preparation exceeded 120 seconds.')),120000)})]);clearTimeout(timer);
  if(id!==sequence)return;
  if(!viewer.GetBounds())throw Error('No visible model-space geometry.');
  rows=[...viewer.GetLayers()];layers();ready(file);state.layers=rows.length;state.bounds=viewer.GetBounds();state.warnings=messages;status(`${rows.length} layers · model space only${messages.length?' · '+messages.length+' reader warnings':''}`);$('selection').textContent=messages.length?messages.slice(0,2).join(' · '):'Preview: fonts, dimensions and line styles may differ from CAD.';
 }catch(e){if(id===sequence){clear();reset();fail(e);}}finally{if(url)URL.revokeObjectURL(url);}}
 $('file').onchange=e=>{const f=e.target.files[0];e.target.value='';if(f)open(f)};window.openViewerFile=open;window.addEventListener('pagehide',clear);
}
