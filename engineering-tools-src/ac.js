// Sinusoidal steady state; all source amplitudes are RMS.
const add=(a,b)=>[a[0]+b[0],a[1]+b[1]], neg=a=>[-a[0],-a[1]], mul=(a,b)=>[a[0]*b[0]-a[1]*b[1],a[0]*b[1]+a[1]*b[0]];
const abs=a=>Math.hypot(...a), div=(a,b)=>{const d=b[0]**2+b[1]**2;if(d===0)throw Error('Singular circuit');return [(a[0]*b[0]+a[1]*b[1])/d,(a[1]*b[0]-a[0]*b[1])/d]};
export const polar=z=>({magnitude:abs(z),phase:Math.atan2(z[1],z[0])*180/Math.PI});
export function solveAC(elements,frequency){
 if(!Number.isFinite(frequency)||frequency<=0)throw Error('Frequency must be positive.');
 if(!elements.length||elements.length>40)throw Error('Use 1–40 components.');
 const nodes=[...new Set(elements.flatMap(e=>[e.a,e.b]))].filter(n=>n!=='0');
 if(!elements.some(e=>e.a==='0'||e.b==='0'))throw Error('Connect reference node 0.');
 if(nodes.length>24)throw Error('Maximum 24 non-ground nodes.');
 const ids=new Map(nodes.map((n,i)=>[n,i])),sources=elements.filter(e=>['V','W'].includes(e.type)||(e.type==='S'&&e.closed)),size=nodes.length+sources.length,w=2*Math.PI*frequency;
 const A=Array.from({length:size},()=>Array.from({length:size},()=>[0,0])),b=Array.from({length:size},()=>[0,0]);
 const stamp=(i,j,z)=>{if(i!==undefined&&j!==undefined)A[i][j]=add(A[i][j],z)},rhs=(i,z)=>{if(i!==undefined)b[i]=add(b[i],z)};
 const phasor=e=>{const p=(e.phase||0)*Math.PI/180;return [e.value*Math.cos(p),e.value*Math.sin(p)]};
 const admittance=e=>e.type==='R'?[1/e.value,0]:e.type==='C'?[0,w*e.value/1e6]:[0,-1/(w*e.value/1000)];
 for(const e of elements){
  if(e.a===e.b)throw Error('A component must join different nodes.');
  if(!Number.isFinite(e.value)||!Number.isFinite(e.phase||0))throw Error('Use finite component values and phases.');
  if(!['R','C','L','V','I','W','S'].includes(e.type))throw Error('Unsupported component.');
  const a=ids.get(e.a),c=ids.get(e.b);
  if(['R','C','L'].includes(e.type)){if(e.value<=0)throw Error('R, C and L must be positive.');const y=admittance(e);stamp(a,a,y);stamp(c,c,y);stamp(a,c,neg(y));stamp(c,a,neg(y));}
  else if(e.type==='I'){rhs(a,neg(phasor(e)));rhs(c,phasor(e));}
  else if(sources.includes(e)){const k=nodes.length+sources.indexOf(e);stamp(a,k,[1,0]);stamp(c,k,[-1,0]);stamp(k,a,[1,0]);stamp(k,c,[-1,0]);b[k]=e.type==='V'?phasor(e):[0,0];}
 }
 const original=A.map(r=>r.map(z=>[...z])),originalB=b.map(z=>[...z]);
 // Row scaling prevents conductance magnitudes dominating the singularity test.
 for(let i=0;i<size;i++){const scale=Math.max(...A[i].map(abs));if(!scale)throw Error('Floating or disconnected circuit.');A[i]=A[i].map(z=>[z[0]/scale,z[1]/scale]);b[i]=[b[i][0]/scale,b[i][1]/scale];}
 for(let k=0;k<size;k++){let pivot=k;for(let i=k+1;i<size;i++)if(abs(A[i][k])>abs(A[pivot][k]))pivot=i;if(abs(A[pivot][k])<1e-12)throw Error('Floating circuit or conflicting / redundant ideal sources.');[A[k],A[pivot]]=[A[pivot],A[k]];[b[k],b[pivot]]=[b[pivot],b[k]];const d=A[k][k];A[k]=A[k].map(z=>div(z,d));b[k]=div(b[k],d);for(let i=0;i<size;i++){if(i===k)continue;const factor=A[i][k];for(let j=k;j<size;j++)A[i][j]=add(A[i][j],neg(mul(factor,A[k][j])));b[i]=add(b[i],neg(mul(factor,b[k])));}}
 for(let i=0;i<size;i++){const residual=add(original[i].reduce((sum,z,j)=>add(sum,mul(z,b[j])),[0,0]),neg(originalB[i]));const scale=1+abs(originalB[i])+original[i].reduce((sum,z,j)=>sum+abs(mul(z,b[j])),0);if(abs(residual)>1e-8*scale)throw Error('Circuit is numerically unstable.');}
 const voltage=n=>n==='0'?[0,0]:b[ids.get(n)];
 const results=elements.map(e=>{const v=add(voltage(e.a),neg(voltage(e.b)));const i=['R','C','L'].includes(e.type)?mul(v,admittance(e)):e.type==='I'?phasor(e):sources.includes(e)?b[nodes.length+sources.indexOf(e)]:[0,0];const power=mul(v,[i[0],-i[1]]);return {...e,v,i,p:power[0],q:power[1]};});
 return {nodes:Object.fromEntries(['0',...nodes].map(n=>[n,voltage(n)])),results,powerBalance:results.reduce((s,e)=>s+e.p,0),reactiveBalance:results.reduce((s,e)=>s+e.q,0)};
}
export function acPower(voltage,current,pf,threePhase=false){
 if(![voltage,current,pf].every(Number.isFinite)||voltage<=0||current<0||pf<0||pf>1)throw Error('Use positive RMS voltage, non-negative current and a power factor from 0 to 1.');
 const apparent=voltage*current*(threePhase?Math.sqrt(3):1);
 return {apparent,active:apparent*pf,reactive:apparent*Math.sqrt(Math.max(0,1-pf*pf))};
}
