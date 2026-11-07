import * as THREE from 'three';
// Extract welded boundary/sharp edges, excluding internal triangulation diagonals.
export function meshFeatures(geometry){
 const pos=geometry.attributes.position,idx=geometry.index,count=(idx?.count||pos.count)/3;
 if(count>400000)return {features:[],limited:true};
 geometry.computeBoundingBox();const size=geometry.boundingBox.getSize(new THREE.Vector3()),eps=Math.max(size.length()*1e-7,1e-7),map=new Map(),vertices=[],ids=new Int32Array(pos.count);
 for(let i=0;i<pos.count;i++){const p=new THREE.Vector3().fromBufferAttribute(pos,i),key=[p.x,p.y,p.z].map(n=>Math.round(n/eps)).join(',');let id=map.get(key);if(id===undefined){id=vertices.length;map.set(key,id);vertices.push(p)}ids[i]=id}
 const labels=new Int32Array(count).fill(-1);for(const [face,range]of(geometry.userData.brepFaces||[]).entries())for(let i=range.first;i<=range.last&&i<count;i++)labels[i]=face;
 const edges=new Map(),normal=new THREE.Vector3(),u=new THREE.Vector3(),v=new THREE.Vector3();
 for(let t=0;t<count;t++){const a=ids[idx?idx.getX(t*3):t*3],b=ids[idx?idx.getX(t*3+1):t*3+1],c=ids[idx?idx.getX(t*3+2):t*3+2];if(a===b||b===c||c===a)continue;normal.copy(u.subVectors(vertices[b],vertices[a]).cross(v.subVectors(vertices[c],vertices[a]))).normalize();for(const [x,y]of[[a,b],[b,c],[c,a]]){const key=x<y?x+','+y:y+','+x;let e=edges.get(key);if(!e)edges.set(key,{a:x,b:y,n:normal.clone(),count:1,face:labels[t],sharp:false});else{e.count++;if((e.face>=0&&labels[t]>=0&&e.face!==labels[t])||e.n.dot(normal)<Math.cos(Math.PI/6))e.sharp=true}}}
 const boundary=[...edges.values()].filter(e=>e.count===1||e.sharp),adj=new Map();for(const [i,e]of boundary.entries()){for(const a of[e.a,e.b]){if(!adj.has(a))adj.set(a,[]);adj.get(a).push(i)}}
 const used=new Set(),checked=new Set(),features=[];
 function fitCircle(loop){if(loop.length<10)return null;const ps=loop.map(i=>vertices[i]),a=ps[0],b=ps[Math.floor(ps.length/3)],c=ps[Math.floor(ps.length*2/3)],ab=b.clone().sub(a),ac=c.clone().sub(a),n=ab.clone().cross(ac);if(n.length()<eps)return null;n.normalize();const e1=ab.clone().normalize(),e2=n.clone().cross(e1),bx=ab.length(),cx=ac.dot(e1),cy=ac.dot(e2);if(Math.abs(cy)<eps)return null;const center=a.clone().addScaledVector(e1,bx/2).addScaledVector(e2,(cx*cx+cy*cy-bx*cx)/(2*cy)),r=center.distanceTo(a);if(r<=eps||ps.some(p=>Math.abs(p.distanceTo(center)-r)>r*.005||Math.abs(p.clone().sub(center).dot(n))>r*.002))return null;return {kind:'circle',center,radius:r,points:[...ps,ps[0]],approximate:true,source:'Fitted circular rim · mesh estimate'};}
 for(let start=0;start<boundary.length;start++){if(used.has(start)||checked.has(start))continue;const first=boundary[start];if(adj.get(first.a).length!==2)continue;let node=first.a,edge=start;const route=[],loop=[],routeSet=new Set();while(route.length<=boundary.length&&!routeSet.has(edge)){routeSet.add(edge);route.push(edge);checked.add(edge);loop.push(node);const e=boundary[edge],next=e.a===node?e.b:e.a;if(next===first.a){const circle=fitCircle(loop);if(circle){features.push(circle);route.forEach(i=>used.add(i))}break}if(adj.get(next)?.length!==2)break;const following=adj.get(next).find(i=>i!==edge);node=next;edge=following;}}
 const endpoints=new Set();
 for(let i=0;i<boundary.length;i++){if(used.has(i))continue;const e=boundary[i];used.add(i);let a=e.a,b=e.b;
  const extend=(at,previous)=>{while(adj.get(at)?.length===2){const nextIndex=adj.get(at).find(j=>!used.has(j));if(nextIndex===undefined)break;const next=boundary[nextIndex],other=next.a===at?next.b:next.a;const before=vertices[at].clone().sub(vertices[previous]).normalize(),after=vertices[other].clone().sub(vertices[at]).normalize();if(before.dot(after)<.999999)break;used.add(nextIndex);previous=at;at=other;}return at};
  a=extend(a,b);b=extend(b,e.a);const p=vertices[a],q=vertices[b];features.push({kind:'line',points:[p,q],length:p.distanceTo(q),approximate:true,source:'Mesh boundary / sharp edge'});endpoints.add(a);endpoints.add(b);
 }
 for(const id of endpoints)features.push({kind:'point',points:[vertices[id]],approximate:true,source:'Mesh edge vertex'});
 return {features,limited:false};
}
