import * as THREE from 'three';
import {meshFeatures} from './mesh-features.js';
import {dxfFeatures,nearestFeature} from './features.js';
import assert from 'assert/strict';
const d=dxfFeatures({entities:[{type:'LINE',vertices:[{x:0,y:0},{x:30,y:40}]},{type:'CIRCLE',center:{x:100,y:0},radius:10},{type:'LWPOLYLINE',vertices:[{x:0,y:0,bulge:1},{x:20,y:0}]},{type:'INSERT',name:'b',position:{x:0,y:10},rotation:90,xScale:2,yScale:2}],blocks:{b:{position:{x:0,y:0},entities:[{type:'LINE',vertices:[{x:0,y:0},{x:10,y:0}]}]}}});
assert.equal(d.features.find(f=>f.kind==='line').length,50);assert.equal(d.features.find(f=>f.kind==='circle').radius,10);assert(Math.abs(d.features.find(f=>f.kind==='arc').length-Math.PI*10)<1e-8);assert(d.features.some(f=>f.length===20));
assert.equal(nearestFeature(d.features,{x:100,y:0},p=>p,'hole').radius,10);
const shape=new THREE.Shape();shape.moveTo(-50,-40);shape.lineTo(50,-40);shape.lineTo(50,40);shape.lineTo(-50,40);shape.closePath();const hole=new THREE.Path();hole.absarc(0,0,10,0,Math.PI*2,true);shape.holes.push(hole);
const geo=new THREE.ExtrudeGeometry(shape,{depth:4,bevelEnabled:false,curveSegments:32});const mf=meshFeatures(geo);const circles=mf.features.filter(f=>f.kind==='circle');assert.equal(circles.length,2);for(const c of circles)assert(Math.abs(c.radius-10)<.0001);assert(mf.features.some(f=>f.kind==='line'&&Math.abs(f.length-100)<.001));
console.log('Passed: DXF exact line, circle, bulge arc, transformed block; mesh plate edges and both 20 mm hole rims.');

