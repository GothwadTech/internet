// ==UserScript==
// @require      assets:js/Readability-readerable.js
// @require      assets:js/Readability.js
// ==/UserScript==
!(function() {

    try {
        if(!Readability) {
            mbrowser.notifyLoadModuleFailed("page_tts");
            return;
        } else if(!isProbablyReaderable(document)){
            var message = mbrowser.getStringResource("toast_may_not_an_article");
            mbrowser.showToast(message);
            mbrowser.TTS(message);
        } else {
            var documentClone = document.cloneNode(true);
            var article = new Readability(documentClone).parse();
            mbrowser.TTS(article.textContent);
        }

    } catch (e) {
        console.log(e);
    }
})();