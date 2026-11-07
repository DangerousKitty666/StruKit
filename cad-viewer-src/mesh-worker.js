import {STLLoader} from 'three/addons/loaders/STLLoader.js';
import {OBJLoader} from 'three/addons/loaders/OBJLoader.js';
self.onmessage=({data:{buffer,ext}})=>{try{
 const meshes=[],transfers=[];let triangles=0;
 function add(g,name){triangles+=(g.index?.count||g.attributes.position.count)/3;if(triangles>2000000)throw Error('Mesh exceeds 2 million triangles.');const position=g.attributes.position.array,normal=g.attributes.normal?.array,index=g.index?.array;meshes.push({name,position,normal,index});transfers.push(position.buffer);if(normal)transfers.push(normal.buffer);if(index)transfers.push(index.buffer);}
 if(ext==='stl')add(new STLLoader().parse(buffer),'STL mesh');
 else {const object=new OBJLoader().parse(new TextDecoder().decode(buffer));object.updateMatrixWorld(true);object.traverse(o=>{if(o.isMesh)add(o.geometry.applyMatrix4(o.matrixWorld),o.name);});}
 if(!meshes.length)throw Error('No supported mesh geometry found.');postMessage({meshes,triangles},transfers);
 }catch(e){postMessage({error:String(e.message||e)})}};
