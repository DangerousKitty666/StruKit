const {chromium}=require('C:/Users/gidro/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const http=require('http'),fs=require('fs'),path=require('path'),assert=require('node:assert/strict');
const root=path.resolve('work/rev0789a/StruKit_Revision_0.789_A/app/src/main/assets/viewer'),out=path.resolve('work/rev0789a');
const server=http.createServer((req,res)=>{const f=path.join(root,new URL(req.url,'http://local').pathname);if(!fs.existsSync(f)){res.writeHead(404).end();return}res.setHeader('Content-Type',({'.html':'text/html','.js':'application/javascript','.css':'text/css','.wasm':'application/wasm'})[path.extname(f)]||'application/octet-stream');fs.createReadStream(f).pipe(res)});
(async()=>{await new Promise(r=>server.listen(0,'127.0.0.1',r));const b=await chromium.launch({executablePath:'C:/Program Files/Google/Chrome/Application/chrome.exe',headless:true,args:['--enable-unsafe-swiftshader']});try{const p=await b.newPage({viewport:{width:390,height:760},isMobile:true,hasTouch:true});const errors=[];p.on('pageerror',e=>errors.push(e.message));const url='http://127.0.0.1:'+server.address().port;
await p.goto(url+'/index.html?mode=3d');await p.waitForFunction(()=>window.openViewerFile);
const cube='v -50 -50 -50\nv 50 -50 -50\nv 50 50 -50\nv -50 50 -50\nv -50 -50 50\nv 50 -50 50\nv 50 50 50\nv -50 50 50\nf 1 4 3 2\nf 5 6 7 8\nf 1 2 6 5\nf 4 8 7 3\nf 1 5 8 4\nf 2 3 7 6\n';
await p.locator('#file').setInputFiles({name:'cube.obj',mimeType:'text/plain',buffer:Buffer.from(cube)});await p.waitForFunction(()=>viewerState.ready||viewerState.error);assert(await p.evaluate(()=>viewerState.ready),await p.evaluate(()=>viewerState.error));
await p.screenshot({path:out+'/cube-initial.png'});console.log('Initial camera',await p.evaluate(()=>viewerState.camera));assert.equal(await p.locator('#tools3 #light,#tools3 #camera').count(),0);assert.equal(await p.locator('#tools3 #view').count(),1);
const shown=()=>p.locator('.viewer-options:visible').count();
for(const viewport of [{width:390,height:760},{width:800,height:360}]){await p.setViewportSize(viewport);await p.keyboard.press('Escape');
for(const a of ['view','section'])for(const c of ['view','section']){await p.keyboard.press('Escape');await p.locator('#'+a).tap();assert.equal(await shown(),1);await p.locator('#'+c).tap();assert.equal(await shown(),a===c?0:1);}
await p.keyboard.press('Escape');await p.locator('#view').tap();for(const sub of ['light','camera','mode']){await p.locator('[data-submenu='+sub+']').tap();assert.equal(await p.locator('.view-submenu:visible').count(),1);}
await p.locator('[data-submenu=light]').tap();await p.locator('[data-light=Soft]').tap();assert.equal(await p.evaluate(()=>viewerState.lighting),'Soft');
await p.locator('[data-submenu=camera]').tap();await p.locator('[data-option=Projected]').tap();assert.equal(await p.evaluate(()=>viewerState.cameraStyle),'Projected');await p.locator('[data-option=Normal]').tap();console.log('Normal camera',await p.evaluate(()=>viewerState.camera));
await p.locator('[data-submenu=mode]').tap();await p.locator('[data-option=Wireframe]').tap();assert.equal(await p.evaluate(()=>viewerState.viewStyle),'Wireframe');await p.locator('[data-option="Solid with lines"]').tap();
await p.touchscreen.tap(viewport.width-3,viewport.height/2);assert.equal(await shown(),0);
await p.locator('#section').tap();await p.locator('#section-enabled').check();assert(await p.evaluate(()=>viewerState.section.enabled));
await p.locator('[data-axis=x]').tap();await p.locator('#section-slider').fill('75');assert.equal(await p.evaluate(()=>viewerState.section.position),25);
const oldSide=await p.evaluate(()=>viewerState.section.side);await p.locator('#section-flip').tap();assert.equal(await p.evaluate(()=>viewerState.section.side),-oldSide);
await p.locator('#section-position').fill('-25');await p.locator('#section-position').press('Tab');assert.equal(await p.evaluate(()=>viewerState.section.fraction),.25);
for(const axis of ['y','z','x']){await p.locator('[data-axis='+axis+']').tap();assert.equal(await p.evaluate(()=>viewerState.section.axis),axis);assert.equal(await p.evaluate(()=>viewerState.section.position),0);}
await p.touchscreen.tap(viewport.width-3,viewport.height/2);await p.screenshot({path:out+'/section-'+viewport.width+'.png'});
await p.locator('#list').tap();assert(await p.locator('#drawer').isVisible());await p.touchscreen.tap(viewport.width-3,viewport.height/2);assert(!await p.locator('#drawer').isVisible());
await p.locator('#measure').tap();assert.equal(await p.locator('#measure-result').evaluate(e=>getComputedStyle(e).fontSize),'20px');await p.locator('#view').tap();assert(!await p.locator('#measurement-panel').isVisible());await p.locator('#measure').tap();assert.equal(await shown(),0);await p.locator('#close-measure').tap();
await p.locator('#section').tap();await p.locator('#section-off').tap();assert.equal(await p.evaluate(()=>viewerState.section.enabled),false);await p.locator('#close-section').tap();}
await p.locator('#close-file').click();assert.equal(await p.evaluate(()=>viewerState.section.enabled),false);assert(await p.locator('#section').isDisabled());
await p.setViewportSize({width:390,height:760});await p.goto(url+'/index.html?mode=2d');await p.waitForFunction(()=>window.openViewerFile);
const dxf='0\nSECTION\n2\nENTITIES\n0\nLINE\n8\nTest\n10\n0\n20\n0\n11\n100\n21\n0\n0\nLINE\n8\nTest\n10\n100\n20\n0\n11\n100\n21\n50\n0\nENDSEC\n0\nEOF\n';
await p.locator('#file').setInputFiles({name:'test.dxf',mimeType:'text/plain',buffer:Buffer.from(dxf)});await p.waitForFunction(()=>viewerState.ready||viewerState.error);assert(await p.evaluate(()=>viewerState.ready),await p.evaluate(()=>viewerState.error));
assert.equal(await p.locator('#status').evaluate(e=>e.parentElement.id),'tools3');const st=await p.locator('#status').boundingBox(),bt=await p.locator('#list').boundingBox(),vp=await p.locator('#viewport').boundingBox();assert(st.x>bt.x+bt.width);assert(st.y>=vp.y+vp.height);assert(vp.height>500);
await p.locator('#list').tap();const dr=await p.locator('#drawer').boundingBox();assert(dr.x<15);await p.screenshot({path:out+'/layers.png'});await p.touchscreen.tap(387,380);assert(!await p.locator('#drawer').isVisible());
await p.locator('#measure').tap();assert.equal(await p.locator('#measure-result').evaluate(e=>getComputedStyle(e).fontSize),'20px');await p.screenshot({path:out+'/2d-measure.png'});
assert.deepEqual(errors,[]);console.log('PASS: unified View submenus, exclusive panels/outside tap, section axes/offset/flip/reset, camera/mode retention, 2D status alignment/Layers styling, 20px results in both viewers.');}finally{await b.close();server.close()}})().catch(e=>{console.error(e);process.exitCode=1});


