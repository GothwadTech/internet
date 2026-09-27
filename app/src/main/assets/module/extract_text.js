
/**
 * 文本提取模块 - 用于从网页中提取主要内容和格式化文本
 */
(function (window) {
    'use strict';

    let currentScore = 0;
    let cachedMainContentElement = null;
    // --- 内部辅助函数 ---

    /**
     * 计算元素中的纯文本长度，忽略script和style标签
     * @param {Element} element - 要计算的DOM元素
     * @return {number} 纯文本长度
     */
    function getPlainTextLength(element) {
        if (!element) return 0;

        let textLength = 0;
        for (const node of element.childNodes) {
            if (node.nodeType === Node.TEXT_NODE) {
                textLength += node.textContent.trim().length;
            } else if (node.nodeType === Node.ELEMENT_NODE) {
                // 忽略script和style标签
                if (node.tagName !== 'SCRIPT' && node.tagName !== 'STYLE') {
                    // 递归计算子元素的文本长度
                    textLength += getPlainTextLength(node);
                }
            }
        }
        return textLength;
    }

    /**
     * 计算元素的总HTML文本长度（outerHTML长度）
     * @param {Element} element - 要计算的DOM元素
     * @return {number} HTML文本长度
     */
    function getAllHtmlTextLength(element) {
        if (!element || !element.outerHTML) return 0;
        return element.outerHTML.length;
    }

    /**
     * 计算元素的文本密度
     * @param {Element} element - 要计算的DOM元素
     * @return {number} 文本密度值
     */
    function calculateDensity(element) {
        const plainTextLen = getPlainTextLength(element);
        const allHtmlTextLen = getAllHtmlTextLength(element);

        if (allHtmlTextLen === 0) {
            return 0;
        }

        // 使用基本密度公式：密度 = 纯文本长度 / HTML总长度
        return plainTextLen / allHtmlTextLen;
    }

    /**
     * 计算元素的内容得分
     * @param {Element} element - 要计算的DOM元素
     * @return {number} 内容得分
     */
    function calculateScore(element) {
        // 首先通过文本密度计算基础得分
        let density = calculateDensity(element);
        let score = density;

        // 段落数量加分
        let numberOfPtag = element.querySelectorAll('p').length;
        if (numberOfPtag > 1) {
            score += numberOfPtag * 0.05;
        }
        //换行同样加分
        let numberOfBrtag = element.querySelectorAll('br').length;
        if (numberOfBrtag > 1) {
            score += numberOfBrtag * 0.02;
        }
        let tagName = element.tagName.toLowerCase();
        // 其他可选加分规则（已注释）
        if (tagName === 'article') {
            score += 0.5;  // 文章标签加分
        }

        if(tagName === 'article-main') {
            score += 0.5;
        }

        let elementId = element.id.toLowerCase();
        if(elementId.includes('content') || elementId.includes('main') || elementId.includes('article')) {
            score += 0.5;
        }

        //元素的innerText长度大于300，增加0.5分
         var textLen = element.innerText.length;
         if(textLen > 300) {
             score += 0.5;
         } else {
             score -= (300 - textLen) / 300 * 0.5;
         }

        for (let cls of element.classList) {
            cls = cls.toLowerCase();
            if(cls.includes('comment')) {
                score -= 0.5; // 评论类名减分
            } else if(cls.includes('recommend') ) {
                score -= 0.5; // 推荐类名减分
//                alert(element.tagName + " reduce score:" + score + "  classList:" + element.classList.toString() +  " content:" + element.textContent);
            }
            if(cls == 'content' || cls == 'maincontent') {
                score += 0.7; // 精确匹配content或maincontent加分
                break;
            }
            else if (cls.includes('content') ) {
                score += 0.5; // 包含content的类名加分
//                 alert(element.tagName + " score:" + score + "  classList:" + element.classList.toString() +  " content:" + element.textContent);
                break;
            } 
            if(cls == 'article') {
                score += 0.5; // 精确匹配article的类名加分
                break;
            }
            if(cls == 'article-main') {
                score += 0.5; // 精确匹配article-main的类名加分
                break;
            }
            if(cls == 'article-content') {
                score += 0.7; // 精确匹配article-content的类名加分
                break;
            }

           if(cls.includes('article') ) {
                score += 0.3; // 包含article或content的类名加分
                break;
            }
    
        }

        // 元素中每多一个链接减0.02分
        let linkCount = element.querySelectorAll('a').length;
        score -= linkCount * 0.02;

        // 元素的子元素的类名包含'comment'减0.1分
        let commentCount = element.querySelectorAll('[class*="comment"]').length;
        score -= commentCount * 0.1;

        console.log("score:" + score + " element:" + element.tagName + "  class:" + element.classList);
        return score;


    }

    /**
     * 获取页面中最可能包含正文内容的元素
     * @return {Element|null} 主要内容元素或null
     */
    function getMainContentElement() {
        if (cachedMainContentElement && document.contains(cachedMainContentElement)) {
            return cachedMainContentElement;
        }
        const containerTags = ['DIV', 'section', 'article', 'main', 'article-main','TH', 'UL', 'LI', 'DL', 'DT', 'DD'];
        let maxScore = 0;
        let mainContentElement = null;
        const allElements = document.querySelectorAll(containerTags.join(','));
        
        allElements.forEach(element => {
            // 跳过过小或隐藏元素
            if (element.textContent.trim().length < 50 || element.offsetWidth === 0 || element.offsetHeight === 0) {
                return;
            }
            let score = calculateScore(element);
            if (score > maxScore) {
                maxScore = score;
                mainContentElement = element;
            }
            //最后由body 元素兜底
            /*
            if(score <= 0)  {
                let finalElement = document.querySelector("body");
                score = calculateScore(finalElement);
                if(score > 0 ) mainContentElement= finalElement;
            }*/
        });
        //调试
//        alert(mainContentElement.tagName + " score:" + maxScore + "  classList:" + mainContentElement.classList.toString() +  " content:" + mainContentElement.textContent);
        currentScore = maxScore;
        cachedMainContentElement = mainContentElement;
        return mainContentElement;
    }

    /**
     * 提取主要内容的标题
     * @returns {string} 标题文本   
     */
    function extractContentTitle() {
        const headingTags = ['h1', 'h2', 'h3', 'h4', 'h5'];

        // 获取元素的纯文本（排除 script/style 标签内容）
        function getCleanText(el) {
            var clone = el.cloneNode(true);
            var toRemove = clone.querySelectorAll('script, style, noscript');
            for (var i = 0; i < toRemove.length; i++) {
                toRemove[i].parentNode.removeChild(toRemove[i]);
            }
            return clone.textContent.trim();
        }

        // 从指定容器中查找第一个可见的标题
        function findHeadingIn(container) {
            if (!container) return null;
            for (const tag of headingTags) {
                const h = container.querySelector(tag);
                if (h && h.offsetHeight > 0) {
                    var text = getCleanText(h);
                    if (text.length >= 2) return text;
                }
            }
            return null;
        }

        // 通过常见标题类名/属性查找标题
        function findTitleBySelector(container) {
            if (!container) return null;
            const selectors = [
                '.title', '.article-title', '.post-title', '.entry-title', '.chapter-title'
            ];
            for (const sel of selectors) {
                const el = container.querySelector(sel);
                if (el && el.offsetHeight > 0) {
                    var text = getCleanText(el);
                    if (text.length > 0) return text;
                }
            }
            // 宽泛匹配 class 包含 "title" 的元素，需要额外过滤
            var els = container.querySelectorAll('[class*="title"]');
            for (var i = 0; i < els.length; i++) {
                var el = els[i];
                var text = getCleanText(el);
                if (el.offsetHeight > 0 && text.length >= 5
                    && !el.closest('footer, nav, [class*="comment"], [class*="recommend"], [class*="statement"]')) {
                    return text;
                }
            }
            return null;
        }

        // 0. 通过常见标题id/属性直接查找
        var titleById = document.querySelector('#chapterTitle, #chapter-title, #articleTitle, #article-title, #bookTitle, #title');
        if (titleById && titleById.offsetHeight > 0) {
            var _t = getCleanText(titleById);
            if (_t.length >= 2) return _t;
        }

        // 1. 优先从article中取出标题
        var title = findHeadingIn(document.querySelector("article"));
        if (title) return title;

        // 2. 从主要内容元素中查找标题
        var mainContent = getMainContentElement();
        if (mainContent) {
            title = findHeadingIn(mainContent);
            if (title) return title;

            // 2.1 从主要内容元素及其父元素中按标题类名查找
            title = findTitleBySelector(mainContent);
            if (title) return title;
            title = findTitleBySelector(mainContent.parentElement);
            if (title) return title;

            // 3. 尝试从主要内容元素的前面兄弟节点查找标题
            var prev = mainContent.previousElementSibling;
            while (prev) {
                for (const tag of headingTags) {
                    if (prev.tagName && prev.tagName.toLowerCase() === tag && prev.offsetHeight > 0) {
                        return getCleanText(prev);
                    }
                }
                title = findHeadingIn(prev);
                if (title) return title;
                prev = prev.previousElementSibling;
            }

            // 3.5 向上遍历祖先元素，在每级祖先的前面兄弟中查找标题
            var ancestor = mainContent.parentElement;
            var maxDepth = 5;
            while (ancestor && ancestor !== document.body && maxDepth-- > 0) {
                var prevAnc = ancestor.previousElementSibling;
                while (prevAnc) {
                    for (const tag of headingTags) {
                        if (prevAnc.tagName && prevAnc.tagName.toLowerCase() === tag && prevAnc.offsetHeight > 0) {
                            return getCleanText(prevAnc);
                        }
                    }
                    title = findHeadingIn(prevAnc);
                    if (title) return title;
                    prevAnc = prevAnc.previousElementSibling;
                }
                ancestor = ancestor.parentElement;
            }
        }

        // 3.8 在页面中查找第一个可见的h1/h2标题（位于mainContent之前）
        for (const tag of ['h1', 'h2']) {
            var headings = document.querySelectorAll(tag);
            for (var hi = 0; hi < headings.length; hi++) {
                var hEl = headings[hi];
                if (hEl.offsetHeight > 0 && hEl.textContent.trim().length >= 2) {
                    if (!mainContent || (mainContent.compareDocumentPosition(hEl) & Node.DOCUMENT_POSITION_PRECEDING)) {
                        return hEl.textContent.trim();
                    }
                }
            }
        }

        // 4. 通过标题类名查找（如 <span class="title">）
        var header = document.querySelector('.header, header, [class*="header"]');
        title = findTitleBySelector(header);
        if (title) return title;

        // 5. 在整个页面中按类名查找标题
        title = findTitleBySelector(document.body);
        if (title) return title;

        // 6. fallback到document.title
        return document.title || "";
    }

    /**
     * 提取下一页的URL
     * @returns {string} 下一页的URL
     */
    function extractNextPageUrl() {
        // 下一页相关的关键词
        const nextKeywords = ['下一页', '下一章', '下一节', '下一篇', '下页', '下一本','下一节','next', 'nextpage', 'nextchapter'];
        // 需要排除的关键词（避免误匹配"上一页"等）
        const prevKeywords = ['上一页', '上一章', '上一节', '上一篇', '上页', 'prev', 'previous'];

        function normalizeText(text) {
            return text.replace(/\s+/g, '').replace(/\u30FC/g, '\u4E00').toLowerCase();
        }

        function isNextLink(text) {
            text = normalizeText(text);
            if (text.length === 0 || text.length > 30) return false;
            for (const kw of prevKeywords) {
                if (text.includes(kw)) return false;
            }
            for (const kw of nextKeywords) {
                if (text.includes(kw)) return true;
            }
            return false;
        }

        function isValidUrl(href) {
            if (!href) return false;
            if (href === '#') return false;
            if (isUselessJsHref(href)) return false;
            return true;
        }

        function isRealUrl(href) {
            return isValidUrl(href) && !href.startsWith('javascript:');
        }

        // 判断是否是无意义的 javascript: 伪链接，例如 javascript:void(0) / javascript:; / javascript:
        // 这种链接本身不带可解析的 URL，作为回退也无价值，应当忽略。
        function isUselessJsHref(href) {
            if (!href) return false;
            var s = href.replace(/\s+/g, '').toLowerCase();
            if (s === 'javascript:' || s === 'javascript:;') return true;
            if (s.indexOf('javascript:void(') === 0) return true;
            return false;
        }

        // 获取链接的href，javascript:链接返回原始属性值
        function getHref(a) {
            var raw = a.getAttribute('href');
            if (raw && raw.startsWith('javascript:')) return raw;
            return a.href;
        }

        function isPrevLink(text) {
            text = normalizeText(text);
            for (const kw of prevKeywords) {
                if (text.includes(kw)) return true;
            }
            return false;
        }

        // 1. 优先检查 <link rel="next">
        var linkNext = document.querySelector('link[rel="next"]');
        if (linkNext && isRealUrl(linkNext.href)) {
            return linkNext.href;
        }

        // 2. 通过链接文本匹配（优先真实URL，其次javascript:）
        var allLinks = document.querySelectorAll('a[href]');
        var jsFallback = null;
        for (var i = 0; i < allLinks.length; i++) {
            var a = allLinks[i];
            if (isNextLink(a.textContent) && isValidUrl(getHref(a))) {
                if (isRealUrl(a.href)) {
                    return a.href;
                }
                console.log(a.outerHTML);
                if (!jsFallback) jsFallback = a;
            }
        }

        // 3. 通过id/class属性匹配
        var selectorPatterns = [
            'a[id*="next"]', 'a[class*="next"]',
            'a[id*="Next"]', 'a[class*="Next"]',
            '[class*="next"] a', '[id*="next"] a',
            '[class*="pagination"] a:last-child'
        ];
        for (var j = 0; j < selectorPatterns.length; j++) {
            var el = document.querySelector(selectorPatterns[j]);
            if (el && isValidUrl(getHref(el))) {
                if (isPrevLink(el.textContent)) continue;
                if (isRealUrl(el.href)) return el.href;
                if (!jsFallback) jsFallback = el;
            }
        }

        // 4. 通过 > 或 >> 或 → 符号匹配
        for (var m = 0; m < allLinks.length; m++) {
            var link = allLinks[m];
            var linkText = link.textContent.trim();
            if ((linkText === '>' || linkText === '>>' || linkText === '→' || linkText === '»') && isValidUrl(getHref(link))) {
                if (isRealUrl(link.href)) return link.href;
                if (!jsFallback) jsFallback = link;
            }
        }

        // 5. 解析出包含nextKeywords，并且含有onclick 事件的元素
        /*
        var onclickEls = document.querySelectorAll('[onclick]');
        for (var n = 0; n < onclickEls.length; n++) {
            var el = onclickEls[n];
            if (isNextLink(el.textContent)) {
                var onclickJs = el.getAttribute('onclick');
                if (onclickJs) {
                    // 如果元素本身是<a>且有真实URL，优先用URL
                    if (el.tagName === 'A' && el.href && isRealUrl(el.href)) {
                        return el.href;
                    }
                    // 构造 javascript: 伪协议 URL 供上层执行
                    var jsUrl = onclickJs.indexOf('javascript:') === 0
                            ? onclickJs : 'javascript:' + onclickJs;
                    if (isValidUrl(jsUrl)) {
                        return jsUrl;
                    }
                }
            }
        } */

        // 6. fallback: 返回javascript:链接的原始href
        if (jsFallback) {
            return getHref(jsFallback);
        }

        return "";
    }

    /**
     * 提取上一页的URL（与 extractNextPageUrl 对称）
     * @returns {string} 上一页的URL
     */
    function extractPrevPageUrl() {
        const prevKeywords = ['上一页', '上一章', '上一节', '上一篇', '上页', 'prev', 'previous'];
        const nextKeywords = ['下一页', '下一章', '下一节', '下一篇', '下页', 'next', 'nextpage', 'nextchapter'];

        function normalizeText(text) {
            return (text || '').replace(/\s+/g, '').replace(/\u30FC/g, '\u4E00').toLowerCase();
        }

        function isPrevLink(text) {
            text = normalizeText(text);
            if (text.length === 0 || text.length > 30) return false;
            for (const kw of nextKeywords) {
                if (text.includes(kw)) return false;
            }
            for (const kw of prevKeywords) {
                if (text.includes(kw)) return true;
            }
            return false;
        }

        function hasNextKeyword(text) {
            text = normalizeText(text);
            for (const kw of nextKeywords) {
                if (text.includes(kw)) return true;
            }
            return false;
        }

        function isUselessJsHref(href) {
            if (!href) return false;
            var s = href.replace(/\s+/g, '').toLowerCase();
            if (s === 'javascript:' || s === 'javascript:;') return true;
            if (s.indexOf('javascript:void(') === 0) return true;
            return false;
        }
        function isValidUrl(href) {
            if (!href) return false;
            if (href === '#') return false;
            if (isUselessJsHref(href)) return false;
            return true;
        }
        function isRealUrl(href) { return isValidUrl(href) && !href.startsWith('javascript:'); }
        function getHref(a) {
            var raw = a.getAttribute('href');
            if (raw && raw.startsWith('javascript:')) return raw;
            return a.href;
        }

        // 1. <link rel="prev">
        var linkPrev = document.querySelector('link[rel="prev"]');
        if (linkPrev && isRealUrl(linkPrev.href)) return linkPrev.href;

        // 2. 通过链接文本匹配
        var allLinks = document.querySelectorAll('a[href]');
        var jsFallback = null;
        for (var i = 0; i < allLinks.length; i++) {
            var a = allLinks[i];
            if (isPrevLink(a.textContent) && isValidUrl(getHref(a))) {
                if (isRealUrl(a.href)) return a.href;
                if (!jsFallback) jsFallback = a;
            }
        }

        // 3. 通过 id/class 属性匹配
        var selectorPatterns = [
            'a[id*="prev"]', 'a[class*="prev"]',
            'a[id*="Prev"]', 'a[class*="Prev"]',
            '[class*="prev"] a', '[id*="prev"] a',
            '[class*="pagination"] a:first-child'
        ];
        for (var j = 0; j < selectorPatterns.length; j++) {
            var el = document.querySelector(selectorPatterns[j]);
            if (el && isValidUrl(getHref(el))) {
                // 过滤误命中"下一页"
                if (hasNextKeyword(el.textContent)) continue;
                if (isRealUrl(el.href)) return el.href;
                if (!jsFallback) jsFallback = el;
            }
        }

        // 4. 通过 < 或 << 或 ← 符号匹配
        for (var m = 0; m < allLinks.length; m++) {
            var link = allLinks[m];
            var linkText = link.textContent.trim();
            if ((linkText === '<' || linkText === '<<' || linkText === '←' || linkText === '«') && isValidUrl(getHref(link))) {
                if (isRealUrl(link.href)) return link.href;
                if (!jsFallback) jsFallback = link;
            }
        }

        if (jsFallback) return getHref(jsFallback);
        return "";
    }

    /**
     * 把元素过滤和简化为只有<p>,<b>,<img>,<strong> 等标签组成的极简形式
     * 1. 克隆初始元素，在克隆后的元素上进行操作
     * 2. 移除script、style、iframe等标签，把一些常见的块元素转化为<p></p>
     * 3. <img>元素，的src改成绝对网址，图片如果如果加载失败可以使用空白占位符替代。
     * @param {*} element 
     */     
    function simplifyHtml(element)  {
        // 1. 克隆初始元素，在克隆后的元素上进行操作
        const clonedElement = element.cloneNode(true);
        // 1.1 移除元素本身的一些属性设定
        clonedElement.removeAttribute('style');
        clonedElement.removeAttribute('class');
        
        // 2. 移除script、style、iframe等标签，把一些常见的块元素转化为<p></p>
        const scriptElements = clonedElement.querySelectorAll('script, style, iframe');
        scriptElements.forEach(el => el.remove());
        // 移除所有子元素的style和class属性
        clonedElement.querySelectorAll('[style]').forEach(el => el.removeAttribute('style'));
        clonedElement.querySelectorAll('[class]').forEach(el => el.removeAttribute('class'));
        // 移除HTML注释节点
        const walker = document.createTreeWalker(clonedElement, NodeFilter.SHOW_COMMENT, null, false);
        const comments = [];
        while (walker.nextNode()) comments.push(walker.currentNode);
        comments.forEach(c => c.parentNode.removeChild(c));

        // 将<br>转换为段落分隔
        clonedElement.querySelectorAll('br').forEach(br => {
            br.outerHTML = '</p><p>';
        });

        const blockElements = clonedElement.querySelectorAll('div, span, p, h1, h2, h3, h4, h5, h6');
        blockElements.forEach(el => {
            if (el.tagName.toLowerCase() === 'div' || el.tagName.toLowerCase() === 'span') {
                el.outerHTML = `<p>${el.innerHTML}</p>`;
            }
        });
        
        // 3. <img>元素：把 src 改成绝对网址，并在加载完成前用占位图替代。
        //    - 输出的 HTML 中：src = 1x1 透明像素占位，class 带 reader-img-loading（CSS 显示占位背景图）
        //    - 真实 URL 放到 data-src，由 reader 页面异步加载后替换
        // 1x1 透明 GIF，占位避免浏览器显示 broken image 图标
        const blankPixel = 'data:image/gif;base64,R0lGODlhAQABAAAAACH5BAEKAAEALAAAAAABAAEAAAICTAEAOw==';
        // 装饰类图片过滤规则：匹配到任何一条即剔除
        const DECOR_KEYWORDS = ['icon', 'logo', 'avatar', 'emoji', 'sprite', 'badge'];
        function isDecorativeImg(img) {
            const attrs = [
                img.getAttribute('class') || '',
                img.getAttribute('id') || '',
                img.getAttribute('alt') || '',
                img.getAttribute('aria-label') || ''
            ].join(' ').toLowerCase();
            for (let i = 0; i < DECOR_KEYWORDS.length; i++) {
                if (attrs.indexOf(DECOR_KEYWORDS[i]) >= 0) return true;
            }
            // URL 路径特征
            const src = (img.getAttribute('src') || img.getAttribute('data-src') || '').toLowerCase();
            if (src && /\/(icon|logo|avatar|emoji|sprite|badge)s?\//.test(src)) return true;
            return false;
        }

        const imgElements = clonedElement.querySelectorAll('img');
        imgElements.forEach(img => {
            const src = img.getAttribute('src') || '';
            // base64 / blob 直接保留原样（通常是内联图或媒体流，不动 src）
            if (src.startsWith('data:image') || src.startsWith('blob:')) {
                return;
            }
            // 装饰性图片（icon / logo / avatar 等）整体剔除
            if (isDecorativeImg(img)) {
                img.remove();
                return;
            }

            // 移除可能干扰的 srcset / loading 属性
            img.removeAttribute('srcset');
            img.removeAttribute('loading');
            const rawSrc = img.getAttribute('src') || img.getAttribute('data-src') || img.getAttribute('data-original') || '';
            if (rawSrc) {
                try {
                    const absoluteUrl = new URL(rawSrc, window.location.href).href;
                    img.setAttribute('data-src', absoluteUrl);
                } catch (e) {
                    // 无效 URL：保持占位，不设置 data-src
                }
            }
            img.setAttribute('src', blankPixel);
            // 添加标记类，CSS 中统一控制占位背景图样式
            img.classList.add('reader-img-loading');
        });

        
        
        return clonedElement;
    }   

    function getContentSimpleHtml() {
        let mainContentElement = cachedMainContentElement;
        if(!mainContentElement) {
            mainContentElement = getMainContentElement();
        }
        return simplifyHtml(mainContentElement).outerHTML;
    }

    /**
     * 过滤并克隆主要内容元素,移除不需要的元素
     *  1. 移除script、style、iframe等标签
     *  2. 移除元素中的class 属性和内联sytle属性
     *  3. 移除链接元素
     * @return {Element|null} 过滤后的克隆元素或null
     */
    function filterAndCloneMainContentElement(mainContentElement) {
        if (!mainContentElement) return null;
        
        // 创建克隆元素
        const clonedElement = mainContentElement.cloneNode(true);
        
        // 移除script、style、iframe等标签
        const unwantedTags = ['script', 'style', 'iframe', 'noscript', 'meta', 'link', 'base'];
        unwantedTags.forEach(tag => {
            const elements = clonedElement.querySelectorAll(tag);
            elements.forEach(element => element.remove());
        });

        //如果元素的sytle 属性为display:none，则移除该元素
        const styleElements = clonedElement.querySelectorAll('[style]');
        styleElements.forEach(element => {
            if (element.style.display === 'none') {
                element.remove();
            }
        });
        
        // 移除class属性和内联style属性（包括克隆根元素自身）
        clonedElement.removeAttribute('class');
        clonedElement.removeAttribute('style');
        const allElements = clonedElement.querySelectorAll('*');
        allElements.forEach(element => {
            element.removeAttribute('class');
            element.removeAttribute('style');
        });
        
        // 移除链接元素
        const linkElements = clonedElement.querySelectorAll('a');
        linkElements.forEach(element => element.remove());
        
        return clonedElement;
    }


    function getFilterContentOuterHtml() {
        return filterAndCloneMainContentElement(getMainContentElement())?.outerHTML || '';
    }

    /**
     * 获取当前内容的得分
     * @return {number} 当前内容得分
     */
    function getContentTextScore() {
        if(!cachedMainContentElement) {
            cachedMainContentElement = getMainContentElement();
        }
        return currentScore;
    }



    /**
     * 提取元素中的纯文本内容，保留段落结构
     * @param {Element} element - 要提取文本的DOM元素
     * @return {string} 格式化后的文本内容
     */
    function extractText(element) {
        if (!element) return '';
        
        // 常见块级元素，遇到这些自动加换行
        const blockTags = new Set([
            'DIV', 'P', 'SECTION', 'ARTICLE', 'BR', 'LI', 'UL', 'OL', 'TABLE', 'TR', 'TD', 'TH', 
            'HEADER', 'FOOTER', 'NAV', 'MAIN', 'ASIDE', 'DL', 'DT', 'DD', 'HR', 'FIGURE', 
            'ADDRESS', 'BLOCKQUOTE', 'PRE', 'FORM', 'H1', 'H2', 'H3', 'H4', 'H5', 'H6'
        ]);
        
        let text = '';
        for (const node of element.childNodes) {
            if (node.nodeType === Node.TEXT_NODE) {
                text += node.textContent;
            } else if (node.nodeType === Node.ELEMENT_NODE) {
                if (node.tagName === 'SCRIPT' || node.tagName === 'STYLE') continue;
                let childText = extractText(node);
                if (blockTags.has(node.tagName)) {
                    // 块级元素前后加换行，避免多余换行
                    if (text && !text.endsWith('\n')) text += '\n';
                    text += childText.trim();
                    if (!text.endsWith('\n')) text += '\n';
                } else {
                    text += childText;
                }
            }
        }
        // 合理去除多余空行
        return text.replace(/\n{2,}/g, '\n').trim();
    }
    
    // --- 导出公共API ---
    
    // 创建命名空间
    window.TextExtractor = window.TextExtractor || {};
    
    // 导出公共方法
    window.TextExtractor.getMainContentElement = getMainContentElement;
    window.TextExtractor.filterAndCloneMainContentElement = filterAndCloneMainContentElement;
    window.TextExtractor.extractText = extractText;
    window.TextExtractor.getContentTextScore = getContentTextScore;
    window.TextExtractor.extractContentTitle = extractContentTitle;
    window.TextExtractor.extractNextPageUrl = extractNextPageUrl;
    window.TextExtractor.extractPrevPageUrl = extractPrevPageUrl;
    window.TextExtractor.getFilterContentOuterHtml = getFilterContentOuterHtml;
    window.TextExtractor.getContentSimpleHtml = getContentSimpleHtml;
    window.TextExtractor.clearCache = function() {
        cachedMainContentElement = null;
        currentScore = 0;
    };
    
    // 便捷方法：直接提取页面主要内容的文本
    window.TextExtractor.extractMainText = function() {
        const mainElement = getMainContentElement();
        return mainElement ? extractText(mainElement) : '';
    };
    
})(window);