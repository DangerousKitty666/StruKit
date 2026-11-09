
(()=>{let hits=[],current=-1,last=null;
const style=document.createElement('style');style.textContent='mark.strukit-hit{background:#ffcf61!important;color:#10151b!important;border-radius:2px}mark.strukit-current{outline:2px solid #9ec5e8}';document.head.append(style);
window.strukitSearch=(query)=>{if(query===last)return;last=query;document.querySelectorAll('mark.strukit-hit').forEach(m=>m.replaceWith(document.createTextNode(m.textContent)));document.body.normalize();hits=[];current=-1;const q=query.trim();
 document.querySelectorAll('details').forEach(d=>{if(!d.hasAttribute('data-search-open'))d.dataset.searchOpen=d.open?'1':'0';d.open=q?true:d.dataset.searchOpen==='1'});
 if(!q)return;const escape=s=>s.replace(/[.*+?^${}()|[\]\\]/g,'\\$&');
 const body=q.split(/\s+/).map(escape).join('\\s+');const pattern=(/^[\p{L}\p{N}]/u.test(q)?'(?<![\\p{L}\\p{N}_])':'')+body+(/[\p{L}\p{N}]$/u.test(q)?'(?![\\p{L}\\p{N}_])':'');
 let re;try{re=new RegExp(pattern,'giu')}catch{return}
 const walker=document.createTreeWalker(document.body,NodeFilter.SHOW_TEXT,{acceptNode:n=>n.parentElement.closest('script,style,head,button,input,select,textarea,mark,[hidden]')?NodeFilter.FILTER_REJECT:NodeFilter.FILTER_ACCEPT}),nodes=[];while(walker.nextNode())nodes.push(walker.currentNode);
 for(const n of nodes){const matches=[...n.data.matchAll(re)];if(!matches.length)continue;const frag=document.createDocumentFragment();let start=0;for(const m of matches){frag.append(document.createTextNode(n.data.slice(start,m.index)));const mark=document.createElement('mark');mark.className='strukit-hit';mark.textContent=m[0];frag.append(mark);hits.push(mark);start=m.index+m[0].length}frag.append(document.createTextNode(n.data.slice(start)));n.replaceWith(frag)}
 if(hits.length)window.strukitSearchNext(1);
};window.strukitSearchNext=dir=>{if(!hits.length)return;if(current>=0)hits[current].classList.remove('strukit-current');current=(current+dir+hits.length)%hits.length;hits[current].classList.add('strukit-current');hits[current].scrollIntoView({block:'center'})};})();
