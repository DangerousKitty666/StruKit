// Feature records use drawing/scene coordinates, independent of camera and unit display.
export const distance=(a,b)=>Math.hypot(a.x-b.x,a.y-b.y,(a.z||0)-(b.z||0));
const point=(x,y,z=0)=>({x,y,z});
const identity=[1,0,0,1,0,0];
const mul=(p,q)=>[p[0]*q[0]+p[2]*q[1],p[1]*q[0]+p[3]*q[1],p[0]*q[2]+p[2]*q[3],p[1]*q[2]+p[3]*q[3],p[0]*q[4]+p[2]*q[5]+p[4],p[1]*q[4]+p[3]*q[5]+p[5]];
export function dxfFeatures(dxf,origin={x:0,y:0}){
 const result=[],limit=100000;let skipped=0,truncated=false;
 const add=f=>{if(result.length>=limit){truncated=true;return}if(f.points.every(p=>Number.isFinite(p.x)&&Number.isFinite(p.y)))result.push({...f,approximate:false})};
 function walk(entities,m=identity,layers=[],depth=0){if(depth>12){skipped++;return}for(const e of entities||[]){if(result.length>=limit){truncated=true;break}if(e.inPaperSpace||e.paperSpace||e.visible===false)continue;const ex=e.extrusionDirection;if(ex&&(Math.abs(ex.x||0)>1e-8||Math.abs(ex.y||0)>1e-8||(ex.z??1)<0)){skipped++;continue}
  const layer=e.layer&&e.layer!=='0'?e.layer:(layers.at(-1)||'0'),ls=[...new Set([...layers,layer])];
  const tr=p=>point(m[0]*p.x+m[2]*p.y+m[4]-origin.x,m[1]*p.x+m[3]*p.y+m[5]-origin.y);
  const sx=Math.hypot(m[0],m[1]),sy=Math.hypot(m[2],m[3]),uniform=Math.abs(sx-sy)<Math.max(sx,sy)*1e-7&&Math.abs(m[0]*m[2]+m[1]*m[3])<1e-7*Math.max(sx*sy,1);
  const pt=p=>add({kind:'point',points:[tr(p)],layers:ls,source:'DXF vertex'});
  const line=(a,b)=>{const p=tr(a),q=tr(b);if(distance(p,q)>1e-10)add({kind:'line',points:[p,q],length:distance(p,q),layers:ls,source:'DXF line'});pt(a);pt(b)};
  const arc=(center,r,start,span,closed=false)=>{if(!uniform||!(r>0)){skipped++;return}const n=Math.max(12,Math.ceil(Math.abs(span)/(Math.PI/32))),ps=Array.from({length:n+1},(_,i)=>tr(point(center.x+r*Math.cos(start+span*i/n),center.y+r*Math.sin(start+span*i/n))));add({kind:closed?'circle':'arc',points:ps,center:tr(center),radius:r*sx,length:Math.abs(span)*r*sx,layers:ls,source:closed?'DXF circle':'DXF arc'});if(!closed){add({kind:'point',points:[ps[0]],layers:ls,source:'Arc endpoint'});add({kind:'point',points:[ps.at(-1)],layers:ls,source:'Arc endpoint'})}};
  if(e.type==='INSERT'){const block=dxf.blocks?.[e.name];if(!block){skipped++;continue}const rot=(e.rotation||0)*Math.PI/180,c=Math.cos(rot),s=Math.sin(rot),base=block.position||{x:0,y:0},scale=[e.xScale??1,0,0,e.yScale??1,0,0];for(let row=0;row<Math.min(e.rowCount||1,1000);row++)for(let col=0;col<Math.min(e.columnCount||1,1000);col++){if(truncated)break;const local=mul([c,s,-s,c,e.position?.x||0,e.position?.y||0],mul([1,0,0,1,col*(e.columnSpacing||0),row*(e.rowSpacing||0)],mul(scale,[1,0,0,1,-base.x,-base.y])));walk(block.entities,mul(m,local),ls,depth+1)}}
  else if(e.type==='LINE'&&e.vertices?.length>=2)line(e.vertices[0],e.vertices[1]);
  else if(e.type==='POINT'&&e.position)pt(e.position);
  else if(e.type==='CIRCLE')arc(e.center,e.radius,0,Math.PI*2,true);
  else if(e.type==='ARC'){let span=(e.endAngle-e.startAngle)%(Math.PI*2);if(span<=0)span+=Math.PI*2;arc(e.center,e.radius,e.startAngle,span)}
  else if((e.type==='LWPOLYLINE'||e.type==='POLYLINE')&&e.vertices?.length){const vs=e.vertices,n=e.shape||e.closed?vs.length:vs.length-1;for(let i=0;i<n;i++){const a=vs[i],b=vs[(i+1)%vs.length],bulge=a.bulge||0;if(Math.abs(bulge)<1e-10){line(a,b);continue}const chord=Math.hypot(b.x-a.x,b.y-a.y);if(!chord)continue;const offset=chord*(1-bulge*bulge)/(4*bulge),center=point((a.x+b.x)/2-(b.y-a.y)*offset/chord,(a.y+b.y)/2+(b.x-a.x)*offset/chord),r=Math.hypot(a.x-center.x,a.y-center.y);arc(center,r,Math.atan2(a.y-center.y,a.x-center.x),4*Math.atan(bulge))}}
  else skipped++;
 }}walk(dxf.entities);return {features:result,skipped,truncated};
}
function screenSegment(p,a,b){const dx=b.x-a.x,dy=b.y-a.y,t=Math.max(0,Math.min(1,((p.x-a.x)*dx+(p.y-a.y)*dy)/(dx*dx+dy*dy||1)));return Math.hypot(p.x-a.x-t*dx,p.y-a.y-t*dy)}
export function nearestFeature(features,p,project,filter='auto',tolerance=16,visible=()=>true){let best=null,score=Infinity;for(const f of features){if(!visible(f)||(filter!=='auto'&&f.kind!==(filter==='hole'?'circle':filter)))continue;const ps=f.points.map(project);if(ps.some(q=>!q||!Number.isFinite(q.x)||!Number.isFinite(q.y)))continue;let d=f.kind==='point'?Math.hypot(p.x-ps[0].x,p.y-ps[0].y):Infinity;for(let i=1;i<ps.length;i++)d=Math.min(d,screenSegment(p,ps[i-1],ps[i]));if(f.center){const c=project(f.center);if(c)d=Math.min(d,Math.hypot(p.x-c.x,p.y-c.y))}if(d>tolerance)continue;const rank=d+(f.kind==='point'?0:f.kind==='circle'?2:4);if(rank<score||(Math.abs(rank-score)<1e-8&&f.kind==='circle'&&best?.kind==='circle'&&f.radius<best.radius)){score=rank;best=f}}return best;}
