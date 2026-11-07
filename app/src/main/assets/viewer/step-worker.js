importScripts('vendor/occt-import-js.js');
self.onmessage=async({data})=>{try{
 const occt=await occtimportjs({locateFile:p=>new URL("vendor/"+p,self.location.href).href});
 const r=occt.ReadStepFile(new Uint8Array(data),{linearUnit:'millimeter',linearDeflectionType:'absolute_value',linearDeflection:.5,angularDeflection:.5});
 if(!r.success||!r.meshes?.length)throw Error('No supported STEP geometry found.');
 const triangles=r.meshes.reduce((n,m)=>n+m.index.array.length/3,0);
 if(triangles>2000000)throw Error('Model exceeds the prototype limit of 2 million triangles.');
 const transfers=[];for(const m of r.meshes){m.attributes.position.array=new Float32Array(m.attributes.position.array);m.index.array=new Uint32Array(m.index.array);transfers.push(m.attributes.position.array.buffer,m.index.array.buffer);if(m.attributes.normal){m.attributes.normal.array=new Float32Array(m.attributes.normal.array);transfers.push(m.attributes.normal.array.buffer);}}
 postMessage({result:r,triangles},transfers);
 }catch(e){postMessage({error:String(e.message||e)})}};

