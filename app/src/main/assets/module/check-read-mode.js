// ==UserScript==
// @require      assets:module/extract_text.js
// 修改此文件后需要清空缓存重新编译修改才能生效
// ==/UserScript==
!(function() {
    try {
        if(!TextExtractor) {
            return;
        } else {

            var score = TextExtractor.getContentTextScore();
            if(mbrowser) {
//                mbrowser.showToast("score:" + score);
                console.log(">>>> page scores:" + score);
            }
            if(score > 1) {  //该文章可以进入阅读模式
                var chapterUrl = window.location.href;
                var title = window.TextExtractor.extractContentTitle();
//                var textContentHtml = window.TextExtractor.getFilterContentOuterHtml();
                var textContentHtml = window.TextExtractor.getContentSimpleHtml();
                var nextPageUrl = window.TextExtractor.extractNextPageUrl();
                mbrowser.onFoundNewChapter(chapterUrl, title, textContentHtml, nextPageUrl);
            } else {
                mbrowser.notifyNotSupportReadMode();
            }
        }

    } catch (e) {
        console.log(e);
    }
})();