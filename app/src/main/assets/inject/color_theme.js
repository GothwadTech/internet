function baseCss(o){return`
        .toggle.active {
            background-color: transparent;
            border: 2px solid ${o.link_color};
            box-shadow: inset 0 0 0 13px ${o.link_color} !important;
        }
        /* Switch styles */
        span[class="slider"] {
            background-color: #ccc !important;
        }
        span[class="slider"]:before {
            background-color: white !important;
        }
        .switch input:checked + .slider {
            background-color: ${o.link_color} !important;
        }
        html,
        body,
        iframe,
        table,
        tr,
        td,
        th,
        tbody,
        form,
        article,
        dt,
        ul,
        ol,
        li,
        dl,
        dd,
        section,
        footer,
        nav,
        strong,
        aside,
        header,
        label,
        address,
        bdo,
        big,
        blockquote,
        caption,
        center,
        cite,
        dialog,
        dir,
        fieldset,
        figcaption,
        figure,
        main,
        pre,
        small,
        h1,
        h2,
        h3,
        h4,
        h5,
        h6 {
            background: ${o.primary_bg} !important;
            background-image: none !important;
            background-color: ${o.primary_bg} !important;
            color: ${o.text_color} !important;
            border-color: ${o.border_color} !important;
            box-shadow: 0 0 0 !important;
            text-shadow: 0 0 0 !important;
        }

        div {
            background-color: transparent !important;
            color: ${o.text_color} !important;
            border-color: ${o.border_color} !important;
            box-shadow: 0 0 0 !important;
            text-shadow: 0 0 0 !important;
        }
        div[class*="guide"],
        div[class*="banner"],
        div[class*="dialog"],
        div[class*="header"],
        div[class*="qa-dir-icon"],
        div[class*="qa-icon"],
        div[class*="box"],
        div[class*="nav"],
        div[class*="footer"],
        div[class*="menu"],
        div[class*="tab"],
        div[class*="area"],
        div[class*="bar"],
        div[class*="tab-header"],
        div[class*="tab-content"],
        div[class*="module-content"],
        div[class*="modal-content"],
        div[class*="fixed"] {
            background-color: ${o.secondary_bg} !important;
            color: ${o.text_color} !important;
            box-shadow: 0 0 0 !important;
            text-shadow: 0 0 0 !important;
        }
        div[class="nav-bar"],
        div[class="date-header"] {
            background-color: ${o.primary_bg} !important;
            color: ${o.text_color} !important;
            box-shadow: 0 0 0 !important;
            text-shadow: 0 0 0 !important;
        }
        div[class*='dialog'] {
            border: 1px solid ${o.border_color} !important;
        }

        div[class="game-icon-layer"],
        div[id="slides"],
        div[class="icon"] {
            background: none !important;
        }
        p {
            color: ${o.text_color} !important;
            border-color: ${o.border_color} !important;
            box-shadow: 0 0 0 !important;
            text-shadow: 0 0 0 !important;
        }

        html,
        body {
            scrollbar-base-color: ${o.secondary_bg} !important;
            scrollbar-face-color: ${o.secondary_bg} !important;
            scrollbar-shadow-color: #222 !important;
            scrollbar-highlight-color: ${o.secondary_bg} !important;
            scrollbar-dlight-color: #2e3952 !important;
            scrollbar-darkshadow-color: #222 !important;
            scrollbar-track-color: ${o.secondary_bg} !important;
            scrollbar-arrow-color: #000 !important;
            scrollbar-3dlight-color: #7a7967 !important;
        }

        input,
        select,
        button,
        textarea {
            box-shadow: 0 0 0 !important;
            color: ${o.text_color} !important;
            background-color: ${o.secondary_bg} !important;
            border-color: ${o.border_color} !important;
            opacity: 1;
        }

        span,
        em {
            background-color: transparent !important;
            color: ${o.text_color} !important;
            border-color: ${o.border_color} !important;
            box-shadow: 0 0 0 !important;
            text-shadow: 0 0 0 !important;
        }

        input:focus,
        select:focus,
        option:focus,
        button:focus,
        textarea:focus {
            background-color: ${o.secondary_bg} !important;
            color: ${o.text_color} !important;
            border-color: ${o.border_color} !important;
            outline: 2px solid ${o.border_color} !important;
        }

        input[type=text],
        input[type=password] {
            background-image: none !important;
        }

        input[type=submit],
        button {
            border: 1px solid ${o.border_color} !important;
        }
        
        img[src],
        input[type=image],
        input[type=checkbox],
        input[type=file] {
            opacity: .7;
        }

        a,
        a * {
            background-color: transparent !important;
            color: ${o.link_color} !important;
            text-decoration: none !important;
            border-color: ${o.border_color} !important;
            text-shadow: 0 0 0 !important;
        }

        a:visited,
        a:visited * {
            color: ${o.link_color} !important;
        }
        a:active {
            color: none !important;
            border-color: none !important;
        }

        a img {
            background: none !important;
        }

        div:empty,
        div[id="x-video-button"],
        div[class="x-advert"],
        div[class="player_controls svp_ctrl"] {
            background-color: transparent !important;
        }
        span,
        em {
            background-color: transparent !important;
            color: ${o.text_color} !important;
            border-color: ${o.border_color} !important;
            box-shadow: 0 0 0 !important;
            text-shadow: 0 0 0 !important;
        }

        html input[type=image]:hover {
            opacity: 1;
        }
        a[class^="arrow"] {
            height: 0;
        }
        div::after {
            background-color: transparent !important;
        }
        *:before,
        *:after {
            background-color: transparent !important;
            border-color: ${o.border_color} !important;
            color: ${o.text_color} !important;
        }
        
                
        `}function createThemeStyle(o){console.log("call createThemeStyle>>>>>");var r="data:text/css;charset=UTF-8,"+encodeURIComponent(o);const t=document.createElement("link");t.rel="stylesheet",t.type="text/css",t.href=r,t.id="dynamic-theme-with-link",removeThemeStyle();var r=document.getElementsByTagName("head");0<r.length&&r[0].appendChild(t),document.getElementById("dynamic-theme-with-link")||(console.log("Error: Failed to create dynamic theme with link."),removeThemeStyle(),(r=document.createElement("style")).type="text/css",r.rel="stylesheet",r.id="dynamic-theme-with-style",r.appendChild(document.createTextNode(o)),null!=document.documentElement&&document.documentElement.appendChild(r),document.getElementById("dynamic-theme-with-style")||console.log("Error: Failed to create dynamic theme with style."))}function removeThemeStyle(){var o=document.getElementById("dynamic-theme-with-link");o&&document.head.removeChild(o);const r=document.getElementById("dynamic-theme-with-style");r&&r.remove()}function applyColorTheme(o){o=themes[o];o&&createThemeStyle(baseCss(o))}var themes;window._COLOR_THEME_||(themes={greenBean:{primary_bg:"#C7EDCC",secondary_bg:"#E3F5E1",text_color:"#2B580C",link_color:"#3A6B4C",link_hover:"#43A047",border_color:"#A8D08D",code_bg:"#DAF0D4"},almondYellow:{primary_bg:"#FAF9DE",secondary_bg:"#FFF8E1",text_color:"#5D4037",link_color:"#8D6E63",link_hover:"#A1887F",border_color:"#FFD699",code_bg:"#FFF3C2"},autumnBrown:{primary_bg:"#FFF2E2",secondary_bg:"#FFE0B2",text_color:"#4A362C",link_color:"#6D4C41",link_hover:"#8D6E63",border_color:"#D7A679",code_bg:"#FFEBD6"},rougeRed:{primary_bg:"#FDE6E0",secondary_bg:"#FFCDD2",text_color:"#6D1F1A",link_color:"#C62828",link_hover:"#EF5350",border_color:"#EF9A9A",code_bg:"#FFE5E0"},oceanBlue:{primary_bg:"#DCE2F1",secondary_bg:"#BBDEFB",text_color:"#0D47A1",link_color:"#1976D2",link_hover:"#2196F3",border_color:"#90CAF9",code_bg:"#E3F2FD"},kudzuPurple:{primary_bg:"#E9EBFE",secondary_bg:"#D1C4E9",text_color:"#4A148C",link_color:"#6A1B9A",link_hover:"#9C27B0",border_color:"#CE93D8",code_bg:"#EDE7F6"},grassGreen:{primary_bg:"#E3EDCD",secondary_bg:"#DAEBC6",text_color:"#1B5E20",link_color:"#2E7D32",link_hover:"#43A047",border_color:"#A5D6A7",code_bg:"#E8F5E9"},lavender:{primary_bg:"#f5f0fa",secondary_bg:"#f8f5fc",text_color:"#3e2f5c",link_color:"#6a1b9a",link_hover:"#9c27b0",border_color:"#d1c4e9",code_bg:"#f5f0fa"}},window._COLOR_THEME_={applyColorTheme:applyColorTheme,removeThemeStyle:removeThemeStyle});