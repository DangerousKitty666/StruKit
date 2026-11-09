// Feature records use drawing/scene coordinates, independent of camera and unit display.
export const distance=(a,b)=>Math.hypot(a.x-b.x,a.y-b.y,(a.z||0)-(b.z||0));
const point=(x,y,z=0)=>({x,y,z});
const identity=[1,0,0,1,0,0];
const mul=(p,q)=>[p[0]*q[0]+p[2]*q[1],p[1]*q[0]+p[3]*q[1],p[0]*q[2]+p[2]*q[3],p[1]*q[2]+p[3]*q[3],p[0]*q[4]+p[2]*q[5]+p[4],p[1]*q[4]+p[3]*q[5]+p[5]];
export function dxfFeatures(dxf,origin={x:0,y:0}){
 const result=[],limit=500000,pointKeys=new Set();let skipped=0,truncated=false;
 const add=f=>{if(f.kind==='point'){const p=f.points[0],key=p.x+','+p.y+'|'+f.layers.join('|');if(pointKeys.has(key))return;pointKeys.add(key)}if(result.length>=limit){truncated=true;return}if(f.points.every(p=>Number.isFinite(p.x)&&Number.isFinite(p.y)))result.push({approximate:false,...f})};
 function walk(entities,m=identity,layers=[],depth=0){if(depth>12){skipped++;return}for(const e of entities||[]){if(result.length>=limit){truncated=true;break}if(e.inPaperSpace||e.paperSpace||e.visible===false)continue;const ex=e.extrusionDirection;if(ex&&(Math.abs(ex.x||0)>1e-8||Math.abs(ex.y||0)>1e-8)){skipped++;continue}const em=ex?.z<0&&!['LINE','POINT','3DFACE','ELLIPSE','SPLINE'].includes(e.type)?mul(m,[-1,0,0,1,0,0]):m;
  const layer=e.layer&&e.layer!=='0'?e.layer:(layers.at(-1)||'0'),ls=[...new Set([...layers,layer])];
  const tr=p=>point(em[0]*p.x+em[2]*p.y+em[4]-origin.x,em[1]*p.x+em[3]*p.y+em[5]-origin.y);
  const sx=Math.hypot(m[0],m[1]),sy=Math.hypot(m[2],m[3]),uniform=Math.abs(sx-sy)<Math.max(sx,sy)*1e-7&&Math.abs(m[0]*m[2]+m[1]*m[3])<1e-7*Math.max(sx*sy,1);
  const pt=p=>add({kind:'point',points:[tr(p)],layers:ls,source:'DXF vertex'});
  const line=(a,b)=>{const p=tr(a),q=tr(b);if(distance(p,q)>1e-10)add({kind:'line',points:[p,q],length:distance(p,q),layers:ls,source:'DXF line'});pt(a);pt(b)};
  const arc=(center,r,start,span,closed=false)=>{if(!(r>0)){skipped++;return}if(!uniform){curve(Array.from({length:129},(_,i)=>point(center.x+r*Math.cos(start+span*i/128),center.y+r*Math.sin(start+span*i/128))),'Transformed arc');return}const n=Math.max(12,Math.ceil(Math.abs(span)/(Math.PI/32))),ps=Array.from({length:n+1},(_,i)=>tr(point(center.x+r*Math.cos(start+span*i/n),center.y+r*Math.sin(start+span*i/n))));add({kind:closed?'circle':'arc',points:ps,center:tr(center),radius:r*sx,length:Math.abs(span)*r*sx,layers:ls,source:closed?'DXF circle':'DXF arc'});if(!closed){add({kind:'point',points:[ps[0]],layers:ls,source:'Arc endpoint'});add({kind:'point',points:[ps.at(-1)],layers:ls,source:'Arc endpoint'})}};
  function curve(ps,source){const points=ps.map(tr);if(points.length<2)return;add({kind:'arc',points,length:points.slice(1).reduce((sum,p,i)=>sum+distance(p,points[i]),0),layers:ls,source,approximate:true});pt(ps[0]);pt(ps.at(-1));}
  if(e.type==='INSERT'){const block=dxf.blocks?.[e.name];if(!block){skipped++;continue}const rot=(e.rotation||0)*Math.PI/180,c=Math.cos(rot),s=Math.sin(rot),base=block.position||{x:0,y:0},scale=[e.xScale??1,0,0,e.yScale??1,0,0];for(let row=0;row<Math.min(e.rowCount||1,1000);row++)for(let col=0;col<Math.min(e.columnCount||1,1000);col++){if(truncated)break;const local=mul([c,s,-s,c,e.position?.x||0,e.position?.y||0],mul([1,0,0,1,col*(e.columnSpacing||0),row*(e.rowSpacing||0)],mul(scale,[1,0,0,1,-base.x,-base.y])));walk(block.entities,mul(em,local),ls,depth+1)}}
  else if(e.type==='LINE'&&e.vertices?.length>=2)line(e.vertices[0],e.vertices[1]);
  else if(e.type==='POINT'&&e.position)pt(e.position);
  else if(e.type==='CIRCLE')arc(e.center,e.radius,0,Math.PI*2,true);
  else if(e.type==='ARC'){let span=(e.endAngle-e.startAngle)%(Math.PI*2);if(span<=0)span+=Math.PI*2;arc(e.center,e.radius,e.startAngle,span)}
  else if((e.type==='LWPOLYLINE'||e.type==='POLYLINE')&&e.vertices?.length){const vs=e.vertices,n=e.shape||e.closed?vs.length:vs.length-1;for(let i=0;i<n;i++){const a=vs[i],b=vs[(i+1)%vs.length],bulge=a.bulge||0;if(Math.abs(bulge)<1e-10){line(a,b);continue}const chord=Math.hypot(b.x-a.x,b.y-a.y);if(!chord)continue;const offset=chord*(1-bulge*bulge)/(4*bulge),center=point((a.x+b.x)/2-(b.y-a.y)*offset/chord,(a.y+b.y)/2+(b.x-a.x)*offset/chord),r=Math.hypot(a.x-center.x,a.y-center.y);arc(center,r,Math.atan2(a.y-center.y,a.x-center.x),4*Math.atan(bulge))}}
  else if(e.type==='ELLIPSE'&&e.center&&e.majorAxisEndPoint){const u=e.majorAxisEndPoint,ratio=(e.axisRatio??1)*(ex?.z<0?-1:1),start=e.startAngle??0;let span=(e.endAngle??Math.PI*2)-start;if(span<=0)span+=Math.PI*2;curve(Array.from({length:129},(_,i)=>{const t=start+span*i/128;return point(e.center.x+u.x*Math.cos(t)-u.y*ratio*Math.sin(t),e.center.y+u.y*Math.cos(t)+u.x*ratio*Math.sin(t))}),'DXF ellipse (sampled)')}
  else if(e.type==='SPLINE'&&e.controlPoints?.length){const cp=e.controlPoints,p=e.degreeOfSplineCurve??3,k=e.knotValues; if(!k||k.length<cp.length+p+1){skipped++;continue}const lo=k[p],hi=k[cp.length],weights=e.weights||[];function evalSpline(t){let span=cp.length-1;for(let i=p;i<cp.length;i++)if(t<k[i+1]){span=i;break}const d=Array.from({length:p+1},(_,j)=>{const i=span-p+j,w=weights[i]??1;return [cp[i].x*w,cp[i].y*w,w]});for(let r=1;r<=p;r++)for(let j=p;j>=r;j--){const i=span-p+j,den=k[i+p-r+1]-k[i],a=den?(t-k[i])/den:0;d[j]=d[j].map((x,n)=>(1-a)*d[j-1][n]+a*x)}return point(d[p][0]/d[p][2],d[p][1]/d[p][2])}if(hi>lo&&p>=1&&p<12)curve(Array.from({length:257},(_,i)=>evalSpline(lo+(hi-lo)*i/256)),'DXF spline (sampled)');else skipped++;}
  else if(['SOLID','TRACE','3DFACE'].includes(e.type)&&(e.points||e.vertices)?.length){const raw=e.points||e.vertices,vs=e.type==='SOLID'&&raw.length===4?[raw[0],raw[1],raw[3],raw[2]]:raw;for(let i=0;i<vs.length;i++)line(vs[i],vs[(i+1)%vs.length])}
  else if(e.type==='DIMENSION'&&dxf.blocks?.[e.block])walk(dxf.blocks[e.block].entities,em,ls,depth+1);
  else skipped++;
 }}walk(dxf.entities);return {features:result,skipped,truncated};
}
function screenSegment(p,a,b){const dx=b.x-a.x,dy=b.y-a.y,t=Math.max(0,Math.min(1,((p.x-a.x)*dx+(p.y-a.y)*dy)/(dx*dx+dy*dy||1)));return Math.hypot(p.x-a.x-t*dx,p.y-a.y-t*dy)}
export function nearestFeature(features,p,project,filter='auto',tolerance=16,visible=()=>true){let best=null,score=Infinity;for(const f of features){if(!visible(f)||(filter!=='auto'&&!(filter==='line'?['line','arc','circle'].includes(f.kind):f.kind===(filter==='hole'?'circle':filter))))continue;const ps=f.points.map(project);if(ps.some(q=>!q||!Number.isFinite(q.x)||!Number.isFinite(q.y)))continue;let d=f.kind==='point'?Math.hypot(p.x-ps[0].x,p.y-ps[0].y):Infinity;for(let i=1;i<ps.length;i++)d=Math.min(d,screenSegment(p,ps[i-1],ps[i]));if(f.center){const c=project(f.center);if(c)d=Math.min(d,Math.hypot(p.x-c.x,p.y-c.y))}if(d>tolerance)continue;const rank=d+(f.kind==='point'?0:f.kind==='circle'?2:4);if(rank<score||(Math.abs(rank-score)<1e-8&&f.kind==='circle'&&best?.kind==='circle'&&f.radius<best.radius)){score=rank;best=f}}return best;}

// World-space broad phase: unchanged by pan, zoom or rotation. Long edges are kept in a small overflow list.
export function featureGrid(features){
 if(!features.length)return ()=>[];
 let minX=Infinity,minY=Infinity,maxX=-Infinity,maxY=-Infinity;
 const boxes=features.map(f=>{let a=Infinity,b=Infinity,c=-Infinity,d=-Infinity;for(const p of f.points){a=Math.min(a,p.x);b=Math.min(b,p.y);c=Math.max(c,p.x);d=Math.max(d,p.y)}minX=Math.min(minX,a);minY=Math.min(minY,b);maxX=Math.max(maxX,c);maxY=Math.max(maxY,d);return [a,b,c,d]});
 const n=Math.min(128,Math.max(8,Math.ceil(Math.sqrt(features.length/16)))),dx=(maxX-minX||1)/n,dy=(maxY-minY||1)/n,cells=new Map(),large=[];
 const ix=x=>Math.max(0,Math.min(n-1,Math.floor((x-minX)/dx))),iy=y=>Math.max(0,Math.min(n-1,Math.floor((y-minY)/dy)));
 boxes.forEach((b,i)=>{const x0=ix(b[0]),x1=ix(b[2]),y0=iy(b[1]),y1=iy(b[3]);if((x1-x0+1)*(y1-y0+1)>256){large.push(i);return}for(let y=y0;y<=y1;y++)for(let x=x0;x<=x1;x++){const k=y*n+x;if(!cells.has(k))cells.set(k,[]);cells.get(k).push(i)}});
 return (a,b,c,d)=>{const ids=new Set(large);for(let y=iy(b);y<=iy(d);y++)for(let x=ix(a);x<=ix(c);x++)for(const i of cells.get(y*n+x)||[])ids.add(i);return [...ids].filter(i=>{const q=boxes[i];return q[0]<=c&&q[2]>=a&&q[1]<=d&&q[3]>=b}).map(i=>features[i])};
}
