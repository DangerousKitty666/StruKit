const fs=require('fs'),assert=require('node:assert/strict');
(async()=>{const root='work/rev0788a/StruKit_Revision_0.788_A/engineering-tools-src/';const read=async name=>import('data:text/javascript;base64,'+Buffer.from(fs.readFileSync(root+name,'utf8')).toString('base64'));const {duct,moistAir,solveCircuit}=await read('core.js'),{solveAC,acPower,polar}=await read('ac.js');const near=(a,b,t=1e-7)=>assert(Math.abs(a-b)<t,`${a} != ${b}`);
let d=duct({q:1.2,a:2200,b:1000,shape:'Rectangular'});near(d.area,2.2);near(d.v,1.2/2.2);d=duct({q:1,d:400,shape:'Round'});near(d.area,Math.PI*.04);near(d.dh,400);assert.throws(()=>duct({q:0,d:400,shape:'Round'}));
const m=moistAir(20,50,101.325);near(m.w,7.262, .003);near(m.dew,9.273,.02);near(m.h,38.55,.05);assert.throws(()=>moistAir(20,101,101.325));
let r=solveCircuit([{id:1,type:'V',a:'1',b:'0',value:12},{id:2,type:'R',a:'1',b:'2',value:1000},{id:3,type:'R',a:'2',b:'0',value:2000}]);near(r.nodes['2'],8);near(r.results[1].i,.004);near(r.powerBalance,0);
r=solveAC([{type:'V',a:'1',b:'0',value:230},{type:'R',a:'1',b:'0',value:46}],50);near(polar(r.results[1].i).magnitude,5);near(r.results[1].p,1150);near(r.powerBalance,0);
r=solveAC([{type:'V',a:'1',b:'0',value:100},{type:'R',a:'1',b:'2',value:10},{type:'L',a:'2',b:'0',value:1000/(2*Math.PI*50)}],50);near(polar(r.results[1].i).magnitude,100/Math.sqrt(101));near(r.reactiveBalance,0);
r=solveAC([{type:'V',a:'1',b:'0',value:100},{type:'C',a:'1',b:'0',value:1e6/(2*Math.PI*50*10)}],50);near(polar(r.results[1].i).phase,90);near(r.results[1].q,-1000);assert.throws(()=>solveAC([{type:'V',a:'1',b:'0',value:1},{type:'R',a:'2',b:'3',value:1}],50));near(acPower(400,10,.8,true).active,Math.sqrt(3)*3200);
console.log('Engineering checks passed: workbook geometry, psychrometric reference, DC network, AC RMS R/L/C, power balance, 3-phase and invalid-input handling.');})();

