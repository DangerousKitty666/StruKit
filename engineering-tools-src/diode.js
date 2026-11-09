import {solveCircuit} from './core.js';
export function solveDC(elements){
 const diodes=elements.filter(e=>e.type==='D'||e.type==='LED');if(!diodes.length)return solveCircuit(elements);
 if(diodes.length>10)throw Error('Maximum 10 diodes in the DC solver.');
 for(const d of diodes)if(!(d.value>=0&&d.value<=100&&Number.isFinite(d.value)))throw Error('Enter a forward drop from 0 to 100 V.');
 let lastError=null;
 for(let mask=0;mask<2**diodes.length;mask++){
  const transformed=[];for(const e of elements){const n=diodes.indexOf(e);if(n<0){transformed.push(e);continue}const on=!!(mask&(1<<n));transformed.push({...e,id:'dr'+e.id,type:'R',value:on?1:1e9});if(on)transformed.push({...e,id:'di'+e.id,type:'I',value:-e.value})}
  let r;try{r=solveCircuit(transformed)}catch(e){lastError=e;continue}
  const results=elements.map(e=>{const d=diodes.indexOf(e);if(d<0)return r.results.find(x=>x.id===e.id);const v=r.nodes[e.a]-r.nodes[e.b],on=!!(mask&(1<<d)),i=on?v-e.value:v/1e9;return {...e,v,i,p:v*i,on}});
  if(results.some(e=>diodes.includes(elements.find(x=>x.id===e.id))&&(e.on?e.i < -1e-8:e.v>e.value+1e-8)))continue;
  return {...r,results,powerBalance:results.reduce((n,e)=>n+e.p,0)};
 }
 throw Error(lastError?.message||'No consistent DC diode operating point. Check connections and sources.');
}
