import createModule from './vendor/libredwg-web.js';
self.onmessage=async({data})=>{try{
 const warnings=[];const m=await createModule({printErr:s=>{if(warnings.length<30)warnings.push(String(s))}});
 m.FS.writeFile('input.dwg',new Uint8Array(data));
 const error=m.dwg_write_dxf('input.dwg','drawing.dxf');
 if(error!==0)throw Error('DWG decoder returned '+error+'. This drawing is not supported reliably.');
 if(!m.FS.analyzePath('drawing.dxf').exists)throw Error('No drawing geometry was decoded.');
 const bytes=m.FS.readFile('drawing.dxf');
 postMessage({buffer:bytes.buffer,warnings},[bytes.buffer]);
 }catch(e){postMessage({error:String(e.message||e)})}};
