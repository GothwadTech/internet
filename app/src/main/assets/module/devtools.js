// ==UserScript==
// @require      https://gcore.jsdelivr.net/gh/examplecode/third-party-res-for-xbrowser@v1.0/eruda.js
// @require      https://gcore.jsdelivr.net/npm/eruda-dom
// ==/UserScript==
!(function() {

    try {
        if(!eruda) {
            mbrowser.notifyLoadModuleFailed("devtools");
            return;
        }
        eruda.init();
        eruda.add(erudaDom);
        eruda.show("elements");
        eruda.show();
        if (_XJSAPI_.current_hit_element()) {
            var element = eruda.get("elements");
            element.set(_XJSAPI_.current_hit_element());
        }
    } catch (e) {
        console.log(e);
    }
})();