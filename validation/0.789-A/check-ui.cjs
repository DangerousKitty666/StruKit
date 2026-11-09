const fs=require('fs'),path=require('path'),assert=require('node:assert/strict');
const {chromium}=require('C:/Users/gidro/.cache/codex-runtimes/codex-primary-runtime/dependencies/node/node_modules/playwright');
const root=path.resolve('work/rev0789a/StruKit_Revision_0.789_A'),out=path.resolve('work/rev0789a');
(async()=>{const browser=await chromium.launch({executablePath:'C:/Program Files/Google/Chrome/Application/chrome.exe',headless:true});const page=await browser.newPage({viewport:{width:300,height:620},isMobile:true,hasTouch:true});const errors=[];page.on('pageerror',e=>errors.push(e.message));
for(const name of ['hvac','electricity']){
 await page.goto('file:///'+(root+'/app/src/main/assets/standards/tool-'+name+'.html').replaceAll('\\','/'));
 await page.addStyleTag({content:'body{padding:4px 8px 16px!important}#tabs,#title{display:none!important}'});
 const tabs=await page.locator('#tabs button').allTextContents();
 for(const tab of tabs){await page.evaluate(t=>window.strukitSelectTab(t),tab);assert.equal(await page.locator('body').getAttribute('data-tab'),tab);assert(!await page.evaluate(()=>document.documentElement.scrollWidth>innerWidth+1));if(await page.locator('.tool-diagram').count())await page.screenshot({path:out+'/'+name+'-'+tab.replaceAll(/[^a-zA-Z]/g,'')+'.png'});}
 if(name==='electricity'){
  await page.evaluate(()=>strukitSelectTab('Circuit builder'));await page.getByRole('button',{name:'Add Resistor',exact:true}).tap();await page.locator('.tile-body').tap();assert(await page.locator('#component-dialog').isVisible());
  await page.locator('#component-value').fill('2200');await page.getByRole('button',{name:'Apply',exact:true}).tap();assert(!await page.locator('#component-dialog').isVisible());assert((await page.locator('.tile-body').innerText()).includes('2,200'));
  await page.locator('.tile-body').tap();await page.touchscreen.tap(2,2);assert(!await page.locator('#component-dialog').isVisible(),'outside tap');
  await page.locator('.tile-body').tap();assert(await page.evaluate(()=>strukitDismissDialog()));assert(!await page.locator('#component-dialog').isVisible());assert.equal(await page.evaluate(()=>strukitDismissDialog()),false);
  await page.setViewportSize({width:300,height:250});await page.locator('.tile-body').tap();await page.getByRole('button',{name:'Apply',exact:true}).scrollIntoViewIfNeeded();await page.screenshot({path:out+'/properties-short.png'});await page.getByRole('button',{name:'Apply',exact:true}).tap();assert(!await page.locator('#component-dialog').isVisible());
 }
}
assert.deepEqual(errors,[]);await browser.close();console.log('PASS: native-tab bridge, bounded WebView layouts, all diagrams, touch Apply, outside dismissal, back-handler dialog contract and short viewport.');})().catch(e=>{console.error(e);process.exit(1)});
