process.chdir(__dirname);
const fs=require('fs'),path=require('path'),esbuild=require('esbuild'),{createRequire}=require('module');
const out=path.resolve(__dirname,'../app/src/main/assets/viewer');fs.mkdirSync(out,{recursive:true});fs.mkdirSync(out+'/vendor',{recursive:true});
const reader={name:'node-filesystem',setup(build){
 build.onResolve({filter:/.*/},args=>{if(require('module').builtinModules.includes(args.path))return{path:args.path,external:true};const base=args.importer||path.join(__dirname,'build.cjs');let file=path.isAbsolute(args.path)?args.path:args.path.startsWith('.')?path.resolve(path.dirname(base),args.path):createRequire(base).resolve(args.path);if(file.endsWith('three.cjs'))file=file.replace('three.cjs','three.module.js');return{path:file,namespace:'node-file'};});
 build.onLoad({filter:/.*/,namespace:'node-file'},args=>({contents:fs.readFileSync(args.path,'utf8'),loader:'js'}));
}};
esbuild.build({absWorkingDir:__dirname,tsconfigRaw:{},entryPoints:[path.resolve('feature-check.js')],plugins:[reader],bundle:true,format:'cjs',platform:'node',outfile:path.resolve('../../feature-check.cjs')});