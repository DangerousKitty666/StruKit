process.chdir(__dirname);
const fs=require('fs'),path=require('path'),esbuild=require('esbuild'),{createRequire}=require('module');
const out=path.resolve(__dirname,'../app/src/main/assets/viewer');fs.mkdirSync(out,{recursive:true});fs.mkdirSync(out+'/vendor',{recursive:true});
const reader={name:'node-filesystem',setup(build){
 build.onResolve({filter:/.*/},args=>{if(require('module').builtinModules.includes(args.path))return{path:args.path,external:true};const base=args.importer||path.join(__dirname,'build.cjs');let file=path.isAbsolute(args.path)?args.path:args.path.startsWith('.')?path.resolve(path.dirname(base),args.path):createRequire(base).resolve(args.path);if(file.endsWith('three.cjs'))file=file.replace('three.cjs','three.module.js');return{path:file,namespace:'node-file'};});
 build.onLoad({filter:/.*/,namespace:'node-file'},args=>({contents:fs.readFileSync(args.path,'utf8'),loader:'js'}));
}};
(async()=>{
 for(const name of ['two','three','dxf-worker','mesh-worker'])await esbuild.build({absWorkingDir:__dirname,tsconfigRaw:{},entryPoints:[path.resolve(name+'.js')],plugins:[reader],bundle:true,minify:true,format:name.endsWith('worker')?'iife':'esm',platform:'browser',target:'chrome100',outfile:out+'/'+name+'.js',legalComments:'linked'});
 for(const name of ['index.html','viewer.css','dwg-worker.js','step-worker.js'])fs.copyFileSync(name,out+'/'+name);
 for(const file of ['occt-import-js.js','occt-import-js.wasm','license.occt-import-js.txt','license.occt.txt'])fs.copyFileSync('node_modules/occt-import-js/dist/'+file,out+'/vendor/'+file);
 for(const file of ['libredwg-web.js','libredwg-web.wasm'])fs.copyFileSync('node_modules/@mlightcad/libredwg-web/wasm/'+file,out+'/vendor/'+file);
 for(const [pkg,file,dest]of [['three','LICENSE','license.three.txt'],['dxf-viewer','LICENSE','license.dxf-viewer.txt']])fs.copyFileSync('node_modules/'+pkg+'/'+file,out+'/vendor/'+dest);
 console.log(out);
})();


