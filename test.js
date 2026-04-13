const jsdom = require('jsdom');
const dom = new jsdom.JSDOM();
const tempDiv = dom.window.document.createElement('div');

tempDiv.innerHTML = `<div class='log-box'>log</div><hr><h4>8</h4><p>8</p></div>`;

console.log('count:', tempDiv.childNodes.length);
tempDiv.childNodes.forEach(n => console.log(n.nodeName));
