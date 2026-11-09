const fs=require('fs'),path=require('path'),assert=require('node:assert/strict');
const {chromium}=require('C:/Users/gidro/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const root=path.resolve('work/rev0788a/StruKit_Revision_0.788_A'),out=path.resolve('work/rev0788a');
(async()=>{
const esbuild=require(root+'/cad-viewer-src/node_modules/esbuild');
const built=await esbuild.build({stdin:{contents:"export {solveDC} from './diode.js';export {network} from './builder.js';",resolveDir:root+'/engineering-tools-src'},bundle:true,platform:'node',format:'cjs',write:false,plugins:[{name:'read-source',setup(b){b.onResolve({filter:/.*/},a=>({path:path.resolve(a.importer && a.importer!=='<stdin>'?path.dirname(a.importer):root+'/engineering-tools-src',a.path),namespace:'source'}));b.onLoad({filter:/.*/,namespace:'source'},a=>({contents:fs.readFileSync(a.path,'utf8'),loader:'js'}));}}]});fs.writeFileSync(out+'/solver.cjs',built.outputFiles[0].text);
const {solveDC,network}=require(out+'/solver.cjs');const near=(a,b,t=1e-7)=>assert(Math.abs(a-b)<t,`${a} != ${b}`);
let es=[{id:1,type:'V',a:'1',b:'0',value:12},{id:2,type:'R',a:'1',b:'2',value:1000},{id:3,type:'D',a:'2',b:'0',value:.7}];
let r=solveDC(es);near(r.results[2].i,11.3/1001);near(r.powerBalance,0);assert(r.results[2].on);
r=solveDC(es.map(e=>e.type==='D'?{...e,a:'0',b:'2'}:e));assert(!r.results[2].on);near(r.results[2].i,-12/1000001000,1e-12);
r=solveDC(es.map(e=>e.type==='D'?{...e,type:'LED',value:1.8}:e));near(r.results[2].i,10.2/1001);near(r.powerBalance,0);
const wired=network(es,[['1:a','2:a'],['2:b','3:a'],['3:b','1:b']],'1:b');near(solveDC(wired).results[2].i,11.3/1001);
assert.throws(()=>network([],[],null));assert.throws(()=>network(es,[],null));
const browser=await chromium.launch({executablePath:'C:/Program Files/Google/Chrome/Application/chrome.exe',headless:true});
const page=await browser.newPage({viewport:{width:360,height:800}}),errors=[];page.on('pageerror',e=>errors.push(e.message));
await page.setContent('<style>.air{color:red}</style><h1>Air</h1><p>chair repair air AIR air-flow</p><details><summary>Hidden content</summary><p>air flow</p></details><p>R + C (test)</p>');
await page.addScriptTag({path:root+'/app/src/main/assets/standards/search.js'});
await page.evaluate(()=>strukitSearch('air'));assert.equal(await page.locator('mark').count(),5);
await page.evaluate(()=>strukitSearchNext(1));assert.equal(await page.locator('mark.strukit-current').count(),1);
await page.evaluate(()=>strukitSearch('air flow'));assert.equal(await page.locator('mark').count(),1);
await page.evaluate(()=>strukitSearch('R + C'));assert.equal(await page.locator('mark').count(),1);
await page.evaluate(()=>strukitSearch(''));assert.equal(await page.locator('mark').count(),0);assert.equal(await page.locator('details').getAttribute('open'),null);
for(const name of ['electricity','hvac']){
 await page.goto('file:///'+(root+'/app/src/main/assets/standards/tool-'+name+'.html').replaceAll('\\','/'));
 const tabs=await page.locator('#tabs button').allTextContents();assert(!tabs.includes('DW/144'));
 for(const tab of tabs){await page.getByRole('button',{name:tab,exact:true}).click();assert(!await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1),name+' '+tab+' overflow');
  if(await page.locator('.calculate-row').count()){const b=await page.locator('.calculate-row button').boundingBox(),p=await page.locator('.calculate-row').boundingBox();assert(Math.abs((p.x+p.width)-(b.x+b.width))<5);}
  if(name==='hvac'&&tab!=='Air moisture'){const cols=await page.locator('.input-grid').evaluate(e=>getComputedStyle(e).gridTemplateColumns);assert.equal(cols.split(' ').length,2);}
 }
 if(name==='hvac'){await page.getByRole('button',{name:'Ducts',exact:true}).click();await page.screenshot({path:out+'/hvac.png',fullPage:true});continue;}
 await page.getByRole('button',{name:'Symbols',exact:true}).click();assert((await page.locator('.symbol').count())>=18);await page.screenshot({path:out+'/symbols.png',fullPage:true});
 await page.getByRole('button',{name:'Circuit builder',exact:true}).click();assert.equal(await page.locator('.circuit-tile').count(),0);
 for(const label of ['Voltage source','Resistor','Diode'])await page.getByRole('button',{name:'Add '+label,exact:true}).click();
 assert.equal(await page.locator('.circuit-tile').count(),3);
 for(const [a,b]of [['V1 terminal a','R2 terminal a'],['R2 terminal b','D3 terminal a'],['D3 terminal b','V1 terminal b']]){await page.getByRole('button',{name:a,exact:true}).click();await page.getByRole('button',{name:b,exact:true}).click();}
 await page.getByRole('button',{name:'Reference',exact:true}).click();await page.getByRole('button',{name:'V1 terminal b',exact:true}).click();
 await page.getByRole('button',{name:'Test circuit',exact:true}).click();console.log(await page.locator('#result').innerText());assert((await page.locator('#result').innerText()).includes('11.28'));
 const tile=page.locator('.circuit-tile[data-id="2"] .tile-body');await tile.click();assert(await page.locator('#component-dialog').isVisible());await page.locator('#component-value').fill('2000');await page.getByRole('button',{name:'Save',exact:true}).click();
 await page.getByRole('button',{name:'Test circuit',exact:true}).click();assert((await page.locator('#result').innerText()).includes('5.647'));
 await tile.scrollIntoViewIfNeeded();const before=await tile.boundingBox();await page.mouse.move(before.x+25,before.y+20);await page.mouse.down();await page.waitForTimeout(300);await page.mouse.move(before.x+25,before.y+140,{steps:8});await page.mouse.up();const after=await tile.boundingBox();assert(after.y>before.y+60);
 await page.getByRole('button',{name:'Calculator',exact:true}).click();await page.getByRole('button',{name:'Circuit builder',exact:true}).click();assert.equal(await page.locator('.circuit-tile').count(),3);
 await page.screenshot({path:out+'/circuit.png',fullPage:true});
 await page.getByRole('button',{name:'Clear circuit',exact:true}).click();await page.getByRole('button',{name:'Clear',exact:true}).click();assert.equal(await page.locator('.circuit-tile').count(),0);
}
assert.deepEqual(errors,[]);await browser.close();console.log('PASS: DC diode/LED/reverse bias, wiring graph, search exact matches, all tabs, narrow layouts, right-aligned calculate, empty circuit, add/wire/edit/drag/preserve/clear.');
})().catch(e=>{console.error(e);process.exit(1)});


