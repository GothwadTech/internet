// ==UserScript==
// @require      assets:module/extract_text.js
// ==/UserScript==
!(function() {
    try {

        if(!TextExtractor) {
            mbrowser.notifyLoadModuleFailed("page_tts_v2_1");
            return;
        } else {
            var textContent = TextExtractor.extractMainText();
            var score = TextExtractor.getContentTextScore();
            if(score < 1) {
                var message = mbrowser.getStringResource("toast_may_not_an_article");
                mbrowser.showToast(message);
                mbrowser.TTS(message);
                return;
            }
            mbrowser.TTS(textContent);
        }

    } catch (e) {
        console.log(e);
    }
})();