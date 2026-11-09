import {Vector3,Triangle} from 'three';
const v=p=>new Vector3(p.x,p.y,p.z||0);
function segments(a,b,c,d){
 const u=b.clone().sub(a),w=d.clone().sub(c),r=a.clone().sub(c),A=u.dot(u),B=u.dot(w),C=w.dot(w),D=u.dot(r),E=w.dot(r),den=A*C-B*B;
 let s=den>1e-16?Math.max(0,Math.min(1,(B*E-C*D)/den)):0,t=C>1e-16?(B*s+E)/C:0;
 if(C<1e-16){t=0;s=A>1e-16?Math.max(0,Math.min(1,-D/A)):0}
 else if(t<0){t=0;s=A>1e-16?Math.max(0,Math.min(1,-D/A)):0}else if(t>1){t=1;s=A>1e-16?Math.max(0,Math.min(1,(B-D)/A)):0}
 return [a.clone().addScaledVector(u,s),c.clone().addScaledVector(w,t)];
}
function shape(f){
 if(f.kind==='circle')return {points:[v(f.center)],edges:[],triangles:[]};
 const points=f.points.map(v),triangles=(f.triangles||[]).map(t=>new Triangle(...t.map(v))),edges=[];
 if(triangles.length){for(const t of triangles)edges.push([t.a,t.b],[t.b,t.c],[t.c,t.a])}
 else for(let i=1;i<points.length;i++)edges.push([points[i-1],points[i]]);
 return {points,edges,triangles};
}
export function featureDistance(a,b){
 const A=shape(a),B=shape(b);let best=null,d=Infinity;
 const keep=(p,q)=>{const n=p.distanceToSquared(q);if(n<d){d=n;best=[p.clone(),q.clone()]}};
 // Bound interactive work; report unavailable rather than silently sampling a large face.
 if(A.points.length*B.points.length+A.edges.length*B.edges.length+A.points.length*B.triangles.length+B.points.length*A.triangles.length>300000)throw Error('These faces are too detailed. Choose smaller features or Two points.');
 for(const p of A.points)for(const q of B.points)keep(p,q);
 for(const p of A.points)for(const [c,e]of B.edges){const [x,y]=segments(p,p,c,e);keep(x,y)}
 for(const q of B.points)for(const [c,e]of A.edges){const [x,y]=segments(c,e,q,q);keep(x,y)}
 for(const [a,b]of A.edges)for(const [c,e]of B.edges){const [x,y]=segments(a,b,c,e);keep(x,y)}
 for(const p of A.points)for(const t of B.triangles)keep(p,t.closestPointToPoint(p,new Vector3()));
 for(const q of B.points)for(const t of A.triangles)keep(t.closestPointToPoint(q,new Vector3()),q);
 // Segment/triangle crossings make the finite surface distance zero.
 const intersect=(edges,triangles)=>{for(const [p,q]of edges)for(const t of triangles){const n=t.getNormal(new Vector3()),den=n.dot(q.clone().sub(p));if(Math.abs(den)<1e-12)continue;const k=n.dot(t.a.clone().sub(p))/den;if(k<0||k>1)continue;const hit=p.clone().lerp(q,k);if(t.containsPoint(hit))keep(hit,hit)}};
 intersect(A.edges,B.triangles);intersect(B.edges,A.triangles);
 if(!best)throw Error('No measurable geometry');
 return {points:best,distance:Math.sqrt(d),basis:a.kind==='circle'||b.kind==='circle'?'Circle centres to selected feature':'Shortest distance between finite features',approximate:!!(a.approximate||b.approximate||a.kind==='arc'||b.kind==='arc')};
}
