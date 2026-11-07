const fs=require('fs');
(async()=>{
 if(process.argv[2]==='step'){
  const occt=await require('occt-import-js')();
  for(const file of process.argv.slice(3)){
   const start=Date.now(),r=occt.ReadStepFile(fs.readFileSync(file),{linearUnit:'millimeter',linearDeflectionType:'absolute_value',linearDeflection:.5,angularDeflection:.5});
   const info={file,success:r.success,meshes:r.meshes?.length,triangles:r.meshes?.reduce((a,m)=>a+m.index.array.length/3,0),seconds:(Date.now()-start)/1000,root:r.root?.name,children:r.root?.children?.length};
   console.log(JSON.stringify(info));fs.writeFileSync('../step-probe-'+(file.includes('A007')?'A007':'A014')+'.json',JSON.stringify(info));
  }
 }else{
  const {default:create}=await import('./node_modules/@mlightcad/libredwg-web/wasm/libredwg-web.js');
  let warnings=[];const module=await create({printErr:s=>{if(warnings.length<100)warnings.push(s)}});
  module.FS.writeFile('input.dwg',fs.readFileSync(process.argv[3]));
  const err=module.dwg_write_dxf('input.dwg','output.dxf');
  const output=module.FS.analyzePath('output.dxf').exists?module.FS.readFile('output.dxf'):null;
  if(output)fs.writeFileSync('../HVAC-test.dxf',output);
  const info={error:err,bytes:output?.length,warnings};fs.writeFileSync('../dwg-probe.json',JSON.stringify(info,null,2));console.log(JSON.stringify(info));
 }
})();
