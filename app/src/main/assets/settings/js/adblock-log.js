!function(){var n=1,l=10,o=_UTILS_.getQueryString("filter"),i=null,a=null;async function t(){var t;i.style.display="block",t=n,await new Promise(function(e){e(_XAPP_.getAdbLogs(o,l,t*l))}).then(function(e){s(e),i.style.display="none",n++})}function s(e){for(var t=0;t<e.length;t++){var n=e[t],l=document.createElement("li"),o=(l.setAttribute("class","list-item"),l.setAttribute("data-rule-hash",n.rule_hash),l.innerHTML=`      <div class="log-header">
                    <span class="domain">${n.origin_host}</span>
                    <span class="badge">${n.hit_times}</span>
                </div>
                <div class="rule">        
                    <div class="media-body">
                        <span class="rule-flag icon-rule"> </span>
                        <div class="text-content">${n.rule}</div>
                    </div>
                    <label class="switch right-switch">
                        <input type="checkbox">
                        <span class="slider"></span>
                    </label>
          
                 </div>
                 <div class="media-body">
                    <div class="ad-flag">AD</div>
                    <div class="text-content">${n.block_url}</div>
                </div>`,l.querySelector(".text-content")),o=(n.disabled?o.style.textDecoration="line-through":o.style.textDecoration="none",l.querySelector(".switch"));o.querySelector("input").checked=!n.disabled,o.onclick=function(){var e=this.querySelector("input").checked,t=this.parentElement.parentElement.getAttribute("data-rule-hash"),t=(_XAPP_.disableRule(t,!e),this.parentElement.querySelector(".text-content"));t.style.textDecoration=e?"none":"line-through"},a.appendChild(l)}}function c(){var e=document.documentElement.scrollTop;document.documentElement.scrollHeight-5<=e+document.documentElement.clientHeight&&(console.log("load loadMoreData >>>>>>>>>>>"),window.removeEventListener("scroll",c),t().then(function(){window.addEventListener("scroll",c)}))}o=o||"all",document.addEventListener("DOMContentLoaded",function(){var e;renderInfoBanner("tips_adb_logs"),i=document.getElementById("loading"),a=document.querySelector("#list"),0==(e=_XAPP_.getAdbLogs(o,l,0)).length&&1==n?emptyContent():s(e),window.addEventListener("scroll",c)}),document.addEventListener("event_app_to_page",function(e){e=e.detail;console.log("recevice app event "+JSON.stringify(e)),"clean_adblock_logs"==e.transId&&emptyContent()})}();