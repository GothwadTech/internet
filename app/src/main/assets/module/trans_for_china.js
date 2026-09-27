// ==UserScript==
// @name         网页翻译器(无排除功能版)
// @description  谷歌微软腾讯三引擎翻译，支持双语对照翻译，修复翻译失败时切换引擎也无法翻译的问题
// @version      8.2
// @match        *://*/*
// @grant        GM_xmlhttpRequest
// @grant        GM_addStyle
// @grant        GM_setValue
// @grant        GM_getValue
// @connect      translate.googleapis.com
// @connect      translate-pa.googleapis.com
// @connect      edge.microsoft.com
// @connect      api-edge.cognitive.microsofttranslator.com
// @connect      transmart.qq.com
// @run-at       document-end
// @namespace https://greasyfork.org/users/452911
// @downloadURL https://update.greasyfork.org/scripts/573432/%E7%BD%91%E9%A1%B5%E7%BF%BB%E8%AF%91%E5%99%A8.user.js
// @updateURL https://update.greasyfork.org/scripts/573432/%E7%BD%91%E9%A1%B5%E7%BF%BB%E8%AF%91%E5%99%A8.meta.js
// ==/UserScript==

(async () => {
  'use strict';

  try { if (document.contentType === 'application/xml') return } catch (_) {}

  // ══════════════════════════════════════════════════════════
  // 配置读取
  // ══════════════════════════════════════════════════════════
  const deviceLang = (navigator.language || navigator.userLanguage || 'zh-CN').split('-')[0];
  const [
    _engine,
    _targetLang,
    _autoMode,
    _displayMode,
    _pos
  ] = await Promise.all([
    GM_getValue('engine', 'microsoft'),
    GM_getValue('targetLang', deviceLang === 'zh' ? 'zh-CN' : deviceLang),
    GM_getValue('autoMode', true),
    GM_getValue('displayMode', 'translated'),
    GM_getValue('uiPos', JSON.stringify({ right: 20, bottom: 20 }))
  ]);

  let currentEngine = _engine;
  let targetLang = _targetLang;
  let autoMode = _autoMode;
  let displayMode = _displayMode;
  let uiPos = JSON.parse(_pos);

  // ══════════════════════════════════════════════════════════
  // 引擎定义 (保持不变)
  // ══════════════════════════════════════════════════════════
  const GoogleHelper_v2 = {
    _lastRequestAuthTime: null, _translateAuth: null, _authNotFound: false, _authPromise: null,
    get translateAuth() { return this._translateAuth; },
    _getAlternativeKey() { return new TextDecoder().decode(new Uint8Array([65,73,122,97,83,121,65,84,66,88,97,106,118,122,81,76,84,68,72,69,81,98,99,112,113,48,73,104,101,48,118,87,68,72,109,79,53,50,48])); },
    async findAuth() {
      if (this._authPromise) return await this._authPromise;
      this._authPromise = new Promise((resolve) => {
        let needUpdate = false;
        if (this._lastRequestAuthTime) {
          const d = new Date();
          if (this._translateAuth) d.setMinutes(d.getMinutes() - 20);
          else if (this._authNotFound) d.setMinutes(d.getMinutes() - 5);
          else d.setMinutes(d.getMinutes() - 1);
          if (d.getTime() > this._lastRequestAuthTime) needUpdate = true;
        } else { needUpdate = true; }
        if (needUpdate) {
          this._lastRequestAuthTime = Date.now();
          const altKey = this._getAlternativeKey();
          GM_xmlhttpRequest({
            method: 'GET',
            url: 'https://translate.googleapis.com/_/translate_http/_/js/k=translate_http.tr.en_US.YusFYy3P_ro.O/am=AAg/d=1/exm=el_conf/ed=1/rs=AN8SPfq1Hb8iJRleQqQc8zhdzXmF9E56eQ/m=el_main',
            timeout: 8000,
            onload: (r) => {
              if (r.responseText && r.responseText.length > 1) {
                const m = r.responseText.match(/['"]x-goog-api-key['"]\s*:\s*['"](\w{39})['"]/i);
                if (m && m.length === 2) { this._translateAuth = m[1]; this._authNotFound = false; }
                else { this._authNotFound = true; this._translateAuth = altKey; }
              } else { this._authNotFound = true; this._translateAuth = altKey; }
              resolve();
            },
            onerror: () => { this._translateAuth = altKey; resolve(); },
            ontimeout: () => { this._translateAuth = altKey; resolve(); }
          });
        } else { resolve(); }
      });
      const p = this._authPromise;
      p.finally(() => { this._authPromise = null; });
      return await p;
    }
  };

  const ALL_LANGUAGES = { "zh-CN": "中文（简体）", "zh-TW": "中文（繁體）", "en": "English", "ja": "日本語", "ko": "한국어", "fr": "Français", "de": "Deutsch", "es": "Español", "ru": "Русский", "pt": "Português", "pt-PT": "Português (Portugal)", "ar": "العربية", "th": "ไทย", "vi": "Tiếng Việt", "it": "Italiano", "tr": "Türkçe", "id": "Indonesia", "ms": "Bahasa Melayu", "nl": "Nederlands", "pl": "Polski", "uk": "Українська", "cs": "Čeština", "sk": "Slovenčina", "hu": "Magyar", "ro": "Română", "bg": "Български", "hr": "Hrvatski", "sr": "Српски", "sl": "Slovenščina", "lt": "Lietuvių", "lv": "Latviešu", "et": "Eesti", "fi": "Suomi", "sv": "Svenska", "da": "Dansk", "no": "Norsk", "is": "Íslenska", "el": "Ελληνικά", "he": "עبریת", "hi": "हिन्दी", "bn": "বাংলা", "ta": "தமிழ்", "te": "తెలుగు", "kn": "ಕನ್ನಡ", "ml": "മലയാളം", "pa": "ਪੰਜਾਬੀ", "gu": "ગુજરાતી", "mr": "मराठी", "ne": "नेपाली", "si": "සිංහල", "ur": "اردو", "fa": "فارسی", "ps": "پښتو", "my": "မြန်မာ", "km": "ខ្មဲر", "lo": "ລາວ", "ka": "ქართული", "hy": "Հայերեն", "az": "Azərbaycan", "kk": "Қазақ", "uz": "Oʻzbek", "mn": "Монгол", "sq": "Shqip", "mk": "Македонски", "be": "Беларуская", "bs": "Bosanski", "ca": "Català", "gl": "Galego", "eu": "Euskara", "mt": "Malti", "cy": "Cymraeg", "ga": "Gaeilge", "gd": "Gàidhlig", "lb": "Lëtzebuergesch", "af": "Afrikaans", "sw": "Kiswahili", "ha": "Hausa", "ig": "Igbo", "yo": "Yorùbá", "zu": "isiZulu", "xh": "isiXhosa", "sn": "chiShona", "st": "Sesotho", "so": "Soomaali", "am": "አማርኛ", "ti": "ትግርኛ", "om": "Oromoo", "mg": "Malagasy", "ny": "Chichewa", "lg": "Luganda", "rw": "Kinyarwanda", "tg": "Тоҷикӣ", "tk": "Türkmen", "ky": "Кыргызча", "tt": "Татар", "eo": "Esperanto", "la": "Latina", "co": "Corsu", "fy": "Frysk", "haw": "ʻŌlelo Hawaiʻi", "sm": "Gagana Samoa", "mi": "Te Reo Māori", "ceb": "Cebuano", "fil": "Filipino", "jv": "Basa Jawa", "su": "Basa Sunda", "hmn": "Hmong", "ht": "Kreyòl Ayisyen", "ku": "Kurdî", "ckb": "کوردی", "sd": "سنڌي", "or": "ଓଡ଼ିଆ", "as": "অসমীয়া", "sa": "संस्कृतम्", "mai": "मैथिली", "bho": "भोजपुरी", "doi": "डोगरी", "ug": "ئۇيغۇرچە", "dv": "ދިވެހި", "ak": "Akan", "ee": "Eʋegbe", "gn": "Guarani", "ay": "Aymar", "bm": "Bamanankan", "ln": "Lingála", "nso": "Sepedi", "ts": "Xitsonga", "qu": "Runasimi", "ilo": "Ilokano", "kri": "Krio", "lus": "Mizo tawng", "mni-Mtei": "ꯃꯤꯇꯩꯂꯣꯟ", "gom": "कोंκणी", "ab": "Аԥсуа", "ace": "Bahsa Acèh", "ach": "Lwo", "aa": "Qafaraf", "alz": "Alur", "av": "Авар", "awa": "अवधी", "ban": "ᬩᬮᬶ", "bal": "بلوچی", "bci": "Baoulé", "ba": "Башҡورت", "btx": "Batak Karo", "bts": "Batak Simalungun", "bbc": "Batak Toba", "bem": "Bemba", "bew": "Betawi", "bik": "Bikol", "br": "Brezhoneg", "bua": "Буряад", "yue": "粵語", "ch": "Chamoru", "ce": "Нохчийн", "chk": "Chuukese", "cv": "Чӑваш", "crh": "Qırımtatar", "prs": "دری", "din": "Thuɔŋjäŋ", "dov": "Dombe", "dyu": "Julakan", "dz": "རྫོང་ཁ", "fo": "Føroyskt", "fj": "Na Vosa Vakaviti", "fon": "Fɔ̀ngbè", "fr-CA": "Français (Canada)", "fur": "Furlan", "ff": "Pulaar", "gaa": "Gã", "cnh": "Lai", "hil": "Hiligaynon", "hrx": "Hunsrik", "iba": "Iban", "iu-Latn": "ᐃᓄᒃᑎᑐᑦ (Latin)", "jam": "Jamaican Patois", "kac": "Jingpo", "kl": "Kalaallisut", "kr": "Kanuri", "pam": "Kapampangan", "kha": "Khasi", "cgg": "Rukiga", "kg": "Kikongo", "mkw": "Kituba", "trp": "Kokborok", "kv": "Коми", "ltg": "Latgaļu", "lij": "Lìgure", "li": "Limburgs", "lmo": "Lombard", "luo": "Dholuo", "mad": "Madhurâ", "mak": "Makassar", "ms-Arab": "بهاس ملايو", "mam": "Mam", "gv": "Gaelg", "mh": "Kajin Majōl", "mwr": "मारवाड़ी", "mfe": "Kreol Morisien", "chm": "Марий", "min": "Minangkabau", "nhe": "Nahuatl", "ndc-ZW": "Ndau", "nr": "isiNdebele", "new": "नेपाल भाषा", "nqo": "ߒߞߏ", "nus": "Thok Nath", "oc": "Occitan", "os": "Ирон", "pag": "Pangasinan", "pap": "Papiamento", "pa-Arab": "پنجابی", "kek": "Qʼeqchiʼ", "rom": "Romani", "rn": "Ikirundi", "se": "Davvisámegiella", "sg": "Sängö", "bo": "བོད་ཡིག", "dsb": "Dolnoserbšćina", "hsb": "Hornjoserbšćina", "ikt": "Inuinnaqtun", "iu": "ᐃᓄᒃᑎᑐᑦ", "lzh": "文言文", "mvf": "ᠮᠣᠩᠭᠣᠯ", "brx": "बर'", "hne": "छत्तीसगढ़ी", "ks": "कॉशुर", "mrj": "Мары", "sa-Latn": "Sanskrit (Latin)", "sc": "Sardu", "scn": "Sicilianu", "szl": "Ślůnski", "su-Latn": "Sunda (Latin)", "tcy": "ತುಳು", "vec": "Vèneto", "war": "Winaray", "wo": "Wolof", "zap": "Zapotec", "ms-Latn": "Malay (Latin)" };
  const LANG_GROUPS = { "常用": ["zh-CN","zh-TW","en","ja","ko","fr","de","es","ru","pt","ar","th","vi","it","tr","id"], "欧洲": ["nl","pl","uk","cs","sk","hu","ro","bg","hr","sr","sl","lt","lv","et","fi","sv","da","no","is","el","be","bs","ca","gl","eu","mt","cy","ga","gd","lb","af","eo","la","co","fy","fo","br","oc","sc","scn","szl","fur","lij","lmo","li","vec","ltg","dsb","hsb","gv","se"], "亚洲": ["hi","bn","ta","te","kn","ml","pa","gu","mr","ne","si","ur","fa","ps","my","km","lo","ka","hy","az","kk","uz","mn","tg","tk","ky","tt","ug","dv","or","as","sa","mai","bho","doi","mni-Mtei","gom","awa","ks","brx","hne","mwr","trp","kac","bo","dz","yue","lzh","ms","fil","ceb","jv","su","hmn","ilo","hil","bik","pam","pag","war","ban","mad","mak","min","ace","btx","bts","bbc","bew","iba","ms-Arab","kha"], "非洲": ["sw","ha","ig","yo","zu","xh","sn","st","so","am","ti","om","mg","ny","lg","rw","ak","ee","bm","ln","nso","ts","kri","wo","ff","gaa","fon","bci","dyu","bem","luo","sg","kg","mkw","dov","nus","din","ach","alz","ndc-ZW","nr","rn","mfe"], "美洲/大洋洲": ["pt-PT","fr-CA","ht","qu","gn","ay","haw","sm","mi","fj","mh","ch","chk","jam","nhe","mam","kek","pap","hrx","ikt","iu","iu-Latn","kl"], "其他": ["ab","av","ba","bua","ce","cv","crh","kv","chm","mrj","os","rom","nqo","aa","bal","cnh","kr","prs","pa-Arab","sd","ckb","ku","he"] };

  const Engine = {
    google_v2: {
      name: 'Google (TWP v2)',
      _fixLang(lang) { return lang === "prs" ? "fa-AF" : lang; },
      _transformResponse(result, dontSort) {
        if (result.indexOf("<pre>") !== -1) { result = result.replace("<pre>", ""); const i = result.indexOf(">"); result = result.slice(i + 1); }
        const sentences = []; let idx = 0;
        while (true) {
          const s = result.indexOf("<b>", idx); if (s === -1) break;
          const e = result.indexOf("</b>", s);
          if (e === -1) { sentences.push(result.slice(s + 3)); break; } else { sentences.push(result.slice(s + 3, e)); }
          idx = e;
        }
        result = sentences.length > 0 ? sentences.join(" ") : result; result = result.replace(/<\/b>/g, "");
        let resultArray = []; let lastEnd = 0;
        for (const r of result.matchAll(/(<a i="[0-9]+">)([^<>]*(?=<\/a>))*/g)) {
          const fl = r[0].length, pos = r.index;
          if (pos > lastEnd) { resultArray.push(r[1] + result.slice(lastEnd, pos).replace(/<\/a>/g, "") + (r[2] || "")); } else { resultArray.push(r[0]); }
          lastEnd = pos + fl;
        }
        let indexes;
        if (resultArray.length > 0) {
          indexes = resultArray.map(v => parseInt(v.match(/[0-9]+(?=>)/g)?.[0])).filter(v => !isNaN(v));
          resultArray = resultArray.map(v => v.slice(v.indexOf(">") + 1));
        } else { resultArray = [result]; indexes = [0]; }
        resultArray = resultArray.map(v => Utils.unescapeHTML(v));
        if (dontSort) return resultArray;
        const final = [];
        for (const j in indexes) { if (final[indexes[j]]) final[indexes[j]] += " " + resultArray[j]; else final[indexes[j]] = resultArray[j]; }
        return final;
      },
      async translate(text, toLang) {
        const to = this._fixLang(toLang); await GoogleHelper_v2.findAuth();
        if (!GoogleHelper_v2.translateAuth) throw new Error('No auth');
        const r = await gmFetch({ method: 'POST', url: 'https://translate-pa.googleapis.com/v1/translateHtml', headers: { 'Content-Type': 'application/json+protobuf', 'X-Goog-Api-Key': GoogleHelper_v2.translateAuth }, data: JSON.stringify([[[text], "auto", to], "te"]), });
        if (r.status !== 200) throw new Error('v2 error');
        const data = JSON.parse(r.responseText);
        if (data && data[0]) { const raw = Array.isArray(data[0]) ? data[0][0] : data[0]; const parsed = this._transformResponse(raw, false); return parsed[0] || raw; }
        throw new Error('v2 empty');
      },
      async translateBatch(texts, toLang) {
        const to = this._fixLang(toLang); await GoogleHelper_v2.findAuth();
        if (!GoogleHelper_v2.translateAuth) throw new Error('No auth');
        const r = await gmFetch({ method: 'POST', url: 'https://translate-pa.googleapis.com/v1/translateHtml', headers: { 'Content-Type': 'application/json+protobuf', 'X-Goog-Api-Key': GoogleHelper_v2.translateAuth }, data: JSON.stringify([[texts, "auto", to], "te"]), });
        if (r.status !== 200) throw new Error('v2 batch error');
        const data = JSON.parse(r.responseText);
        if (data && data[0] && Array.isArray(data[0])) { return data[0].map(item => { const p = this._transformResponse(item, false); return p[0] || item; }); }
        if (data && data[0]) { const p = this._transformResponse(Array.isArray(data[0]) ? data[0][0] : data[0], false); return [p[0]]; }
        throw new Error('v2 batch empty');
      }
    },
    google_legacy: {
      name: 'Google (Legacy)',
      async translate(text, toLang) {
        const tk = GoogleHelper.calcHash(text);
        const r = await gmFetch({ method: 'GET', url: 'https://translate.googleapis.com/translate_a/single?client=webapp&sl=auto&tl=' + toLang + '&hl=' + toLang + '&dt=t&dt=bd&dt=ex&dt=ld&dt=md&dt=qca&dt=rw&dt=rm&dt=ss&dt=at&ie=UTF-8&oe=UTF-8&otf=1&ssel=0&tsel=0&kc=7&tk=' + tk + '&q=' + encodeURIComponent(text), });
        if (r.status !== 200) return await this._gtx(text, toLang);
        const data = JSON.parse(r.responseText); return data[0].filter(s => s && s[0]).map(s => s[0]).join('');
      },
      async _gtx(text, to) {
        const r = await gmFetch({ method: 'GET', url: 'https://translate.googleapis.com/translate_a/single?client=gtx&dt=t&sl=auto&tl=' + to + '&q=' + encodeURIComponent(text) });
        if (r.status !== 200) throw new Error('gtx error');
        const data = JSON.parse(r.responseText); return data[0].filter(s => s && s[0]).map(s => s[0]).join('');
      }
    },
    google: {
      name: 'Google (Auto)',
      async translate(text, toLang) { try { return await Engine.google_v2.translate(text, toLang); } catch (e) { return await Engine.google_legacy.translate(text, toLang); } },
      async translateBatch(texts, toLang) { try { return await Engine.google_v2.translateBatch(texts, toLang); } catch (e) { const res = []; for (const t of texts) { try { res.push(await Engine.google_legacy.translate(t, toLang)); } catch (_) { res.push(null); } } return res; } }
    },
    microsoft: {
      name: 'Microsoft', _token: null, _tokenTime: 0,
      async getToken() {
        if (this._token && Date.now() - this._tokenTime < 480000) return this._token;
        const r = await gmFetch({ method: 'GET', url: 'https://edge.microsoft.com/translate/auth' });
        if (r.status !== 200) throw new Error('MS auth');
        this._token = r.responseText; this._tokenTime = Date.now(); return this._token;
      },
      langCode(l) { const m = { 'zh': 'zh-Hans', 'zh-CN': 'zh-Hans', 'zh-TW': 'zh-Hant', 'no': 'nb', 'sr': 'sr-Cyrl', 'pt-PT': 'pt-pt', 'fr-CA': 'fr-ca' }; return m[l] || l; },
      async translate(text, toLang) {
        const token = await this.getToken(); const to = this.langCode(toLang);
        const r = await gmFetch({ method: 'POST', url: 'https://api-edge.cognitive.microsofttranslator.com/translate?from=&to=' + to + '&api-version=3.0', headers: { 'authorization': 'Bearer ' + token, 'Content-Type': 'application/json' }, data: JSON.stringify([{ Text: text }]), });
        if (r.status !== 200) throw new Error('MS error'); return JSON.parse(r.responseText)[0].translations[0].text;
      },
      async translateBatch(texts, toLang) {
        const token = await this.getToken(); const to = this.langCode(toLang); const results = [];
        for (let b = 0; b < texts.length; b += 25) {
          const chunk = texts.slice(b, b + 25);
          const r = await gmFetch({ method: 'POST', url: 'https://api-edge.cognitive.microsofttranslator.com/translate?from=&to=' + to + '&api-version=3.0', headers: { 'authorization': 'Bearer ' + token, 'Content-Type': 'application/json' }, data: JSON.stringify(chunk.map(t => ({ Text: t }))), });
          if (r.status === 200) { for (const item of JSON.parse(r.responseText)) results.push(item.translations[0].text); } else { for (let i = 0; i < chunk.length; i++) results.push(null); }
        }
        return results;
      }
    },
    tencent: {
      name: 'Tencent', _clientKey: null,
      getClientKey() { if (this._clientKey) return this._clientKey; this._clientKey = 'browser-chrome-120.0-Windows_10-' + crypto.randomUUID() + '-' + Date.now(); return this._clientKey; },
      langCode(l) { const m = { 'zh': 'zh', 'zh-CN': 'zh', 'zh-TW': 'zh-TW' }; return m[l] || l; },
      async translate(text, toLang) {
        const to = this.langCode(toLang);
        const r = await gmFetch({ method: 'POST', url: 'https://transmart.qq.com/api/imt', headers: { 'Content-Type': 'application/json' }, data: JSON.stringify({ header: { fn: 'auto_translation', session: '', client_key: this.getClientKey(), user: '' }, type: 'plain', model_category: 'normal', text_domain: 'general', source: { lang: 'auto', text_list: [text] }, target: { lang: to } }), });
        if (r.status !== 200) throw new Error('Tencent error'); return JSON.parse(r.responseText).auto_translation[0];
      },
      async translateBatch(texts, toLang) {
        const to = this.langCode(toLang);
        const r = await gmFetch({ method: 'POST', url: 'https://transmart.qq.com/api/imt', headers: { 'Content-Type': 'application/json' }, data: JSON.stringify({ header: { fn: 'auto_translation', session: '', client_key: this.getClientKey(), user: '' }, type: 'plain', model_category: 'normal', text_domain: 'general', source: { lang: 'auto', text_list: texts }, target: { lang: to } }), });
        if (r.status !== 200) throw new Error('Tencent batch error'); return JSON.parse(r.responseText).auto_translation;
      }
    }
  };

  const GoogleHelper = {
    googleTranslateTKK: "448487.932609646",
    shiftLeftOrRightThenSumOrXor(num, optString) {
      for (let i = 0; i < optString.length - 2; i += 3) {
        let acc = optString.charAt(i + 2); acc = ("a" <= acc) ? acc.charCodeAt(0) - 87 : Number(acc);
        acc = (optString.charAt(i + 1) === "+") ? num >>> acc : num << acc;
        num = (optString.charAt(i) === "+") ? (num + acc) & 4294967295 : num ^ acc;
      }
      return num;
    },
    transformQuery(query) {
      const b = []; let idx = 0;
      for (let i = 0; i < query.length; i++) {
        let c = query.charCodeAt(i);
        if (128 > c) { b[idx++] = c; }
        else {
          if (2048 > c) { b[idx++] = (c >> 6) | 192; }
          else {
            if (55296 === (c & 64512) && i + 1 < query.length && 56320 === (query.charCodeAt(i + 1) & 64512)) {
              c = 65536 + ((c & 1023) << 10) + (query.charCodeAt(++i) & 1023);
              b[idx++] = (c >> 18) | 240; b[idx++] = ((c >> 12) & 63) | 128;
            } else { b[idx++] = (c >> 12) | 224; }
            b[idx++] = ((c >> 6) & 63) | 128;
          }
          b[idx++] = (c & 63) | 128;
        }
      }
      return b;
    },
    calcHash(query) {
      const s = this.googleTranslateTKK.split("."); const tkkIdx = Number(s[0]) || 0; const tkkKey = Number(s[1]) || 0;
      const bytes = this.transformQuery(query); let enc = tkkIdx;
      for (const item of bytes) { enc += item; enc = this.shiftLeftOrRightThenSumOrXor(enc, "+-a^+6"); }
      enc = this.shiftLeftOrRightThenSumOrXor(enc, "+-3^+b+-f"); enc ^= tkkKey;
      if (enc <= 0) enc = (enc & 2147483647) + 2147483648;
      const n = enc % 1000000; return n.toString() + "." + (n ^ tkkIdx);
    }
  };

  const Utils = {
    escapeHTML(t) { const d = document.createElement('div'); d.appendChild(document.createTextNode(t)); return d.innerHTML; },
    unescapeHTML(t) { const d = new DOMParser().parseFromString(t, 'text/html'); return d.documentElement.textContent; }
  };

  function gmFetch(opts) { return new Promise((resolve, reject) => { GM_xmlhttpRequest({ timeout: 20000, ...opts, onload: resolve, onerror: reject, ontimeout: reject }); }); }

  const cache = new Map();
  const MAX_CACHE = 3000;
  function cacheGet(t) { return cache.get(t); }
  function cacheSet(t, v) { if (cache.size >= MAX_CACHE) cache.delete(cache.keys().next().value); cache.set(t, v); }

  async function translate(text) {
    if (!text || !text.trim()) return null;
    const trimmed = text.trim(); if (/^\d+$/.test(trimmed)) return null;
    const cached = cacheGet(trimmed); if (cached) return cached;
    try {
      const result = await Engine[currentEngine].translate(trimmed, targetLang);
      if (result && result !== trimmed) { cacheSet(trimmed, result); return result; }
    } catch (e) {
      const fallbackEngine = currentEngine === 'google' ? 'microsoft' : 'google_legacy';
      try {
        const result = await Engine[fallbackEngine].translate(trimmed, targetLang);
        if (result && result !== trimmed) { cacheSet(trimmed, result); return result; }
      } catch (_) {}
    }
    return null;
  }

  async function batchTranslate(texts) {
    const results = new Array(texts.length).fill(null);
    const uncached = [], uncachedIdx = [];
    for (let i = 0; i < texts.length; i++) {
      const t = texts[i].trim();
      if (!t || /^\d+$/.test(t)) continue;
      const c = cacheGet(t); if (c) { results[i] = c; continue; }
      uncached.push(t); uncachedIdx.push(i);
    }
    if (uncached.length === 0) return results;
    const engine = Engine[currentEngine];
    if (engine.translateBatch && uncached.length > 1) {
      try {
        const BATCH_SIZE = currentEngine === 'microsoft' ? 25 : 50;
        const chunks = [];
        for (let b = 0; b < uncached.length; b += BATCH_SIZE) { chunks.push({ texts: uncached.slice(b, b + BATCH_SIZE), idxs: uncachedIdx.slice(b, b + BATCH_SIZE) }); }
        await Promise.all(chunks.map(async ({ texts: chunk, idxs }) => {
          try {
            const batchResults = await engine.translateBatch(chunk, targetLang);
            if (batchResults) {
              for (let j = 0; j < batchResults.length; j++) {
                if (batchResults[j] && batchResults[j] !== chunk[j]) { cacheSet(chunk[j], batchResults[j]); results[idxs[j]] = batchResults[j]; }
              }
            }
          } catch (_) {}
        }));
        return results;
      } catch (e) {}
    }
    const CONCURRENCY = 8;
    for (let i = 0; i < uncached.length; i += CONCURRENCY) {
      const batch = uncached.slice(i, i + CONCURRENCY);
      const batchIdx = uncachedIdx.slice(i, i + CONCURRENCY);
      await Promise.allSettled(batch.map(async (text, j) => { const r = await translate(text); if (r) results[batchIdx[j]] = r; }));
    }
    return results;
  }

  const SKIP_TAGS = /^(script|style|code|pre|svg|math|noscript|iframe|canvas|video|audio|img|br|hr|input|select|option|textarea)$/i;
  const SKIP_CLASS = /translate-ui|notranslate|katex|mathjax/i;

  function shouldSkip(node) {
    if (!node) return true;
    if (node.nodeType === Node.ELEMENT_NODE) {
      if (SKIP_TAGS.test(node.tagName)) return true;
      if (SKIP_CLASS.test(node.className)) return true;
      if (node.isContentEditable) return true;
      if (node.dataset && node.dataset.translated) return true;
      if (node.classList && node.classList.contains('tu-bi')) return true;
    }
    return false;
  }

  const _langRegex = {};
  function getLangRegex(lang) {
    if (_langRegex[lang]) return _langRegex[lang];
    const patterns = { 'zh': /^[\u4e00-\u9fff\u3400-\u4dbf\uf900-\ufaff\s\d\p{P}]+$/u, 'en': /^[a-zA-Z\s\d\p{P}]+$/u, 'ja': /^[\u3040-\u309f\u30a0-\u30ff\u4e00-\u9fff\s\d\p{P}]+$/u, 'ko': /^[\uac00-\ud7af\u1100-\u11ff\s\d\p{P}]+$/u, 'ar': /^[\u0600-\u06ff\u0750-\u077f\s\d\p{P}]+$/u, 'th': /^[\u0e00-\u0e7f\s\d\p{P}]+$/u, 'ru': /^[\u0400-\u04ff\s\d\p{P}]+$/u, };
    _langRegex[lang] = patterns[lang] || null; return _langRegex[lang];
  }

  function isTargetLang(text) { if (!text || !text.trim()) return true; const lang = targetLang.split('-')[0]; const re = getLangRegex(lang); return re ? re.test(text.trim()) : false; }

  function collectTextNodes(root) {
    const nodes = [];
    const walker = document.createTreeWalker(root, NodeFilter.SHOW_TEXT, {
      acceptNode(node) {
        if (shouldSkip(node.parentElement)) return NodeFilter.FILTER_REJECT;
        const text = node.textContent.trim();
        if (!text || text.length < 2 || /^\d+$/.test(text)) return NodeFilter.FILTER_REJECT;
        if (isTargetLang(text)) return NodeFilter.FILTER_REJECT;
        if (node.parentElement?.dataset?.translated) return NodeFilter.FILTER_REJECT;
        return NodeFilter.FILTER_ACCEPT;
      }
    });
    while (walker.nextNode()) nodes.push(walker.currentNode);
    return nodes;
  }

  function collectPlaceholders(root) { return [...root.querySelectorAll('input[placeholder], textarea[placeholder]')].filter(el => !el.dataset.translated && el.placeholder.trim() && !isTargetLang(el.placeholder)); }

  let isTranslating = false;
  let pendingRoot = null;

  async function translatePage(root) {
    if (isTranslating) { pendingRoot = root || document.body; return; }
    isTranslating = true;
    try {
      root = root || document.body;
      do {
        pendingRoot = null;
        const textNodes = collectTextNodes(root);
        const placeholders = collectPlaceholders(root);
        if (textNodes.length === 0 && placeholders.length === 0) break;
        const allTexts = []; const allMeta = [];
        for (let i = 0; i < textNodes.length; i++) { allTexts.push(textNodes[i].textContent.trim()); allMeta.push({ type: 'text', node: textNodes[i] }); }
        for (let i = 0; i < placeholders.length; i++) { allTexts.push(placeholders[i].placeholder.trim()); allMeta.push({ type: 'ph', el: placeholders[i] }); }
        const results = await batchTranslate(allTexts);
        for (let i = 0; i < allMeta.length; i++) {
          if (!results[i]) continue;
          const meta = allMeta[i];
          if (meta.type === 'text') {
            const parent = meta.node.parentElement; if (!parent) continue;
            if (!parent.dataset.originalText) parent.dataset.originalText = meta.node.textContent;
            parent.dataset.translated = '1';
            if (displayMode === 'bilingual') {
              const s = document.createElement('span'); s.className = 'tu-bi'; s.textContent = results[i];
              if (meta.node.nextSibling) { parent.insertBefore(s, meta.node.nextSibling); } else { parent.appendChild(s); }
            } else { meta.node.textContent = results[i]; }
          } else { meta.el.dataset.originalPlaceholder = meta.el.placeholder; meta.el.placeholder = results[i]; meta.el.dataset.translated = '1'; }
        }
        if (pendingRoot) { root = pendingRoot; }
      } while (pendingRoot);
    } finally { isTranslating = false; }
  }

  function restorePage() {
    document.querySelectorAll('.tu-bi').forEach(el => el.remove());
    document.querySelectorAll('[data-translated]').forEach(el => {
      if (el.dataset.originalText) {
        for (const child of el.childNodes) { if (child.nodeType === Node.TEXT_NODE) { child.textContent = el.dataset.originalText; break; } }
        delete el.dataset.originalText;
      }
      if (el.dataset.originalPlaceholder) { el.placeholder = el.dataset.originalPlaceholder; delete el.dataset.originalPlaceholder; }
      delete el.dataset.translated;
    });
  }

  let lastHeight = 0;
  function onScroll() { const h = document.documentElement.scrollHeight; if (h > lastHeight) { lastHeight = h; if (autoMode) translatePage(); } }

  let mutationRafId = null;
  const pendingMutationRoots = new Set();
  const observer = new MutationObserver((mutations) => {
    if (!autoMode) return;
    for (const m of mutations) { for (const node of m.addedNodes) { if (node.nodeType === Node.ELEMENT_NODE && !shouldSkip(node)) { pendingMutationRoots.add(node); } } }
    if (pendingMutationRoots.size > 0 && !mutationRafId) {
      mutationRafId = setTimeout(() => {
        mutationRafId = null;
        const roots = [...pendingMutationRoots]; pendingMutationRoots.clear();
        if (roots.length > 5) { translatePage(document.body); } else { roots.forEach(r => translatePage(r)); }
      }, 200);
    }
  });

  function buildLangOptions() {
    let html = '';
    for (const [group, codes] of Object.entries(LANG_GROUPS)) {
      html += '<optgroup label="' + group + '">';
      for (const code of codes) { const name = ALL_LANGUAGES[code] || code; html += '<option value="' + code + '"' + (code === targetLang ? ' selected' : '') + '>' + name + '</option>'; }
      html += '</optgroup>';
    }
    return html;
  }

  function isPageInTargetLang() { const lang = (document.documentElement.lang || '').split('-')[0].toLowerCase(); const target = targetLang.split('-')[0].toLowerCase(); return lang === target; }

  function initWhenBodyReady() { if (document.body) { init(); } else { requestAnimationFrame(initWhenBodyReady); } }

  let _initialized = false;
  async function init() {
    if (_initialized) return;
    _initialized = true;

    lastHeight = document.documentElement.scrollHeight;

    GM_addStyle(
      '.translate-ui{position:fixed;z-index:999999;font-family:system-ui,-apple-system,sans-serif;touch-action:none}' +
      '.translate-ui *{box-sizing:border-box;margin:0;padding:0}' +
      '.tu-btn{width:42px;height:42px;border-radius:50%;border:none;background:rgba(0,0,0,0.5);color:#fff;cursor:grab;' +
        'display:flex;align-items:center;justify-content:center;backdrop-filter:blur(8px);-webkit-backdrop-filter:blur(8px);' +
        'box-shadow:0 2px 8px rgba(0,0,0,0.15);transition:transform .2s,background .2s}' +
      '.tu-btn:active{cursor:grabbing;transform:scale(0.9)}' +
      '.tu-btn.active{background:rgba(34,128,255,0.8)}' +
      '.tu-panel{position:absolute;bottom:52px;right:0;width:240px;max-height:80vh;overflow-y:auto;' +
        'background:rgba(255,255,255,0.97);backdrop-filter:blur(12px);-webkit-backdrop-filter:blur(12px);' +
        'border-radius:12px;box-shadow:0 4px 24px rgba(0,0,0,0.12);padding:12px;display:none;color:#333;font-size:13px}' +
      '.tu-panel.show{display:block}' +
      '.tu-panel label{display:block;margin:8px 0 4px;font-size:11px;color:#888;text-transform:uppercase;letter-spacing:.5px}' +
      '.tu-panel select{width:100%;padding:6px 8px;border:1px solid #ddd;border-radius:6px;font-size:12px;' +
        'background:#fff;color:#333;outline:none;appearance:auto}' +
      '.tu-panel select:focus{border-color:#4a9eff}' +
      '.tu-status{margin-top:8px;padding:6px;background:#f8f8f8;border-radius:6px;font-size:11px;color:#666;text-align:center}' +
      '.tu-modes{display:flex;margin-top:6px;background:#f0f0f0;border-radius:8px;padding:2px;gap:2px}' +
      '.tu-modes button{flex:1;padding:6px 0;border:none;border-radius:6px;font-size:11px;cursor:pointer;' +
        'background:transparent;color:#999;transition:all .15s;font-weight:500}' +
      '.tu-modes button.on{background:#fff;color:#333;box-shadow:0 1px 3px rgba(0,0,0,0.1)}' +
      '.tu-row{display:flex;gap:6px;margin-top:10px}' +
      '.tu-row button{flex:1;padding:7px 0;border:none;border-radius:6px;font-size:12px;cursor:pointer;' +
        'transition:background .2s}' +
      '.tu-row .tu-restore{background:#f0f0f0;color:#555}' +
      '.tu-row .tu-go{background:#4a9eff;color:#fff}' +
      '.tu-bi{display:block;margin-top:2px;font-size:.9em;line-height:1.5;color:#5a8fb4;' +
        'border-left:2px solid rgba(74,158,255,0.3);padding-left:8px}' +
      'a .tu-bi,span .tu-bi,em .tu-bi,strong .tu-bi,b .tu-bi,i .tu-bi,label .tu-bi,' +
        'small .tu-bi,sub .tu-bi,sup .tu-bi,u .tu-bi{display:inline;border-left:none;' +
        'padding-left:0;margin-top:0;margin-left:4px;font-size:.88em}' +
      'a .tu-bi::before,span .tu-bi::before,em .tu-bi::before,strong .tu-bi::before,' +
        'b .tu-bi::before,i .tu-bi::before,label .tu-bi::before,small .tu-bi::before,' +
        'sub .tu-bi::before,sup .tu-bi::before,u .tu-bi::before{content:"("}' +
      'a .tu-bi::after,span .tu-bi::after,em .tu-bi::after,strong .tu-bi::after,' +
        'b .tu-bi::after,i .tu-bi::after,label .tu-bi::after,small .tu-bi::after,' +
        'sub .tu-bi::after,sup .tu-bi::after,u .tu-bi::after{content:")"}' +
      '@media(prefers-color-scheme:dark){' +
        '.tu-panel{background:rgba(30,30,30,0.97);color:#eee}' +
        '.tu-panel select{background:#2a2a2a;color:#eee;border-color:#444}' +
        '.tu-row .tu-restore{background:#333;color:#ccc}' +
        '.tu-status{background:#222;color:#999}' +
        '.tu-modes{background:#333}' +
        '.tu-modes button.on{background:#444;color:#eee}' +
        '.tu-bi{color:#7babc8;border-left-color:rgba(100,160,220,0.25)}' +
      '}'
    );

    const ui = document.createElement('div');
    ui.className = 'translate-ui';
    ui.style.right = uiPos.right + 'px';
    ui.style.bottom = uiPos.bottom + 'px';

    ui.innerHTML =
      '<div class="tu-panel" id="tuPanel">' +
        '<label>翻译引擎</label>' +
        '<select id="tuEngine">' +
          '<option value="microsoft">Microsoft (Default)</option>' +
          '<option value="google">Google (Auto)</option>' +
          '<option value="google_v2">Google (v2 API)</option>' +
          '<option value="google_legacy">Google (Legacy)</option>' +
          '<option value="tencent">Tencent</option>' +
        '</select>' +
        '<label>目标语言</label>' +
        '<select id="tuLang">' + buildLangOptions() + '</select>' +
        '<label>显示模式</label>' +
        '<div class="tu-modes" id="tuModes">' +
          '<button data-m="translated"' + (displayMode === 'translated' ? ' class="on"' : '') + '>仅译文</button>' +
          '<button data-m="bilingual"' + (displayMode === 'bilingual' ? ' class="on"' : '') + '>双语</button>' +
          '<button data-m="original"' + (displayMode === 'original' ? ' class="on"' : '') + '>原文</button>' +
        '</div>' +
        '<div class="tu-status" id="tuStatus">Ready</div>' +
        '<div class="tu-row">' +
          '<button class="tu-restore" id="tuRestore">还原</button>' +
          '<button class="tu-go" id="tuGo">翻译</button>' +
        '</div>' +
      '</div>' +
      '<button class="tu-btn' + (autoMode ? ' active' : '') + '" id="tuBtn" draggable="false">' +
        '<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">' +
          '<path d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"/>' +
        '</svg>' +
      '</button>';
    document.body.appendChild(ui);

    const btn = document.getElementById('tuBtn');
    const panel = document.getElementById('tuPanel');
    const engineSelect = document.getElementById('tuEngine');
    const langSelect = document.getElementById('tuLang');
    const statusEl = document.getElementById('tuStatus');
    const modesEl = document.getElementById('tuModes');

    engineSelect.value = currentEngine;
    langSelect.value = targetLang;

    let isDragging = false;
    let startX, startY, startRight, startBottom;
    let hasMoved = false;

    btn.addEventListener('pointerdown', (e) => {
      isDragging = true;
      startX = e.clientX;
      startY = e.clientY;
      startRight = parseInt(ui.style.right);
      startBottom = parseInt(ui.style.bottom);
      hasMoved = false;
      btn.setPointerCapture(e.pointerId);
    });

    btn.addEventListener('pointermove', (e) => {
      if (!isDragging) return;
      const dx = startX - e.clientX;
      const dy = startY - e.clientY;
      if (Math.abs(dx) > 5 || Math.abs(dy) > 5) hasMoved = true;
      let newRight = startRight + dx;
      let newBottom = startBottom + dy;
      const maxX = window.innerWidth - ui.offsetWidth;
      const maxY = window.innerHeight - ui.offsetHeight;
      newRight = Math.max(0, Math.min(newRight, maxX));
      newBottom = Math.max(0, Math.min(newBottom, maxY));
      ui.style.right = newRight + 'px';
      ui.style.bottom = newBottom + 'px';
    });

    btn.addEventListener('pointerup', (e) => {
      if (!isDragging) return;
      isDragging = false;
      if (hasMoved) {
        uiPos = { right: parseInt(ui.style.right), bottom: parseInt(ui.style.bottom) };
        GM_setValue('uiPos', JSON.stringify(uiPos));
      }
    });

    function setStatus(msg) { if (statusEl) statusEl.textContent = msg; }

    btn.addEventListener('click', (e) => {
      if (hasMoved) return;
      e.stopPropagation();
      panel.classList.toggle('show');
    });

    document.addEventListener('click', (e) => { if (!ui.contains(e.target)) panel.classList.remove('show'); });

    engineSelect.addEventListener('change', async () => {
      currentEngine = engineSelect.value;
      GM_setValue('engine', currentEngine);
      cache.clear();
      setStatus('切换至: ' + (Engine[currentEngine] ? Engine[currentEngine].name : currentEngine));

      if (displayMode !== 'original' && autoMode) {
        isTranslating = false;
        restorePage();
        setStatus('正在重新翻译...');
        await translatePage();
      }
    });

    langSelect.addEventListener('change', async () => {
      targetLang = langSelect.value;
      GM_setValue('targetLang', targetLang);
      cache.clear();
      setStatus('语种切为: ' + (ALL_LANGUAGES[targetLang] || targetLang));

      if (displayMode !== 'original' && autoMode) {
        isTranslating = false;
        restorePage();
        setStatus('正在更新翻译...');
        await translatePage();
      }
    });

    modesEl.addEventListener('click', async (e) => {
      var b = e.target.closest('button[data-m]');
      if (!b) return;
      var m = b.dataset.m;
      if (m === displayMode) return;
      modesEl.querySelectorAll('button').forEach(x => x.classList.remove('on'));
      b.classList.add('on');
      displayMode = m;
      GM_setValue('displayMode', m);
      if (m === 'original') {
        restorePage();
        btn.classList.remove('active');
        setStatus('显示原文');
      } else {
        restorePage();
        btn.classList.add('active');
        setStatus(m === 'bilingual' ? '双语翻译中...' : '翻译中...');
        var start = performance.now();
        await translatePage();
        setStatus('完成 (' + ((performance.now() - start) / 1000).toFixed(1) + 's)');
      }
    });

    document.getElementById('tuGo').addEventListener('click', async () => {
      panel.classList.remove('show'); btn.classList.add('active');
      autoMode = true; GM_setValue('autoMode', true);
      restorePage(); cache.clear();
      lastHeight = document.documentElement.scrollHeight;
      setStatus('翻译中...');
      var start = performance.now();
      await translatePage();
      setStatus('完成 (' + ((performance.now() - start) / 1000).toFixed(1) + 's)');
    });

    document.getElementById('tuRestore').addEventListener('click', () => {
      panel.classList.remove('show'); btn.classList.remove('active');
      autoMode = false; GM_setValue('autoMode', false);
      restorePage(); setStatus('已还原');
    });

    window.addEventListener('scroll', onScroll, { passive: true });
    observer.observe(document.body, { childList: true, subtree: true });

    if (autoMode && !isPageInTargetLang() && displayMode !== 'original') {
      queueMicrotask(async () => {
        setStatus(displayMode === 'bilingual' ? '双语翻译中...' : '自动翻译中...');
        var start = performance.now();
        await translatePage();
        setStatus('完成 (' + ((performance.now() - start) / 1000).toFixed(1) + 's)');
      });
    }
  }

  initWhenBodyReady();

})();