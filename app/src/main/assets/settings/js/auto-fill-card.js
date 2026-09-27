!function(){function i(e){e=function(e){for(var t={auto_fill_type:"auto_fill_card",s_id:e},n=document.querySelectorAll("#edit-auto-fill-card input"),i=0;i<n.length;i++){var l=n[i];t[l.id]=l.value}return t}(e);_XAPP_.updateAutoFill(JSON.stringify(e)),_XAPP_.showToast("toast_data_updated"),closePopPanel(),r()}function l(e={}){return`<div class="section">
            <div class="input-group">
                <div class="input-item">
                    <label class="input-label">${_XAPP_.getStringResource("web_str_input_card_num")}</label>
                    <input id="card_number" type="text" value="${e.card_number||""}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_card_num")}">
                </div>
                <div class="input-item">
                    <label class="input-label">${_XAPP_.getStringResource("web_str_input_card_owner")}</label>
                    <input id="card_owner" type="text" value="${e.card_owner||""}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_card_owner")}">
                </div>
                <div class="input-item">
                    <label class="input-label">${_XAPP_.getStringResource("web_str_input_card_expiry")}</label>
                    <input id="card_expiry" type="text" value="${e.card_expiry||""}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_card_expiry")}">
                </div>
                <div class="button-group">
                    ${e.s_id?`
                        <button id="btn-del" class="btn-danger">${_XAPP_.getStringResource("btn_text_del")}</button>
                        <button id="btn-update" class="btn-primary">${_XAPP_.getStringResource("web_str_save")}</button>
                    `:`
                        <button id="btn-add" class="btn-primary">${_XAPP_.getStringResource("web_str_save")}</button>
                    `}
                </div>
            </div>
        </div>`}function e(){document.querySelector("#new-auto-fill-card").innerHTML=l(),openPopPanel("new-auto-fill-card-panel"),document.querySelector("#btn-add").addEventListener("click",t)}function t(){for(var e={s_id:_UTILS_.genSessionId(),auto_fill_type:"auto_fill_card"},t=document.querySelectorAll("#new-auto-fill-card input"),n=0;n<t.length;n++){var i=t[n];e[i.id]=i.value}e.card_number?(e.update_at=(new Date).valueOf(),_XAPP_.updateAutoFill(JSON.stringify(e)),_XAPP_.showToast("toast_added_one_record"),closePopPanel(),r()):(console.log("lost required fields"),_XAPP_.showToast("toast_lost_required_fields"))}function o(n){document.querySelector("#edit-auto-fill-card").innerHTML=l(n);var e=document.querySelector("#btn-del"),t=document.querySelector("#btn-update");e.addEventListener("click",function(){var e,t;e=n.s_id,(t=document.querySelector("#"+e))&&t.remove(),_XAPP_.removeAutoFill("auto_fill_card",e),closePopPanel(),r(),_XAPP_.showToast("toast_deleted_one_record")}),t.addEventListener("click",function(){i(n.s_id)}),openPopPanel("edit-auto-fill-card-panel")}function r(){var e,t,n=_XAPP_.loadAutoFill("auto_fill_card");if(n&&0<n.length){var i,l=document.querySelector("#auto-fill-card-list");l||((l=document.createElement("ul")).setAttribute("class","list-view"),l.setAttribute("id","auto-fill-card-list"),(i=document.querySelector(".content")).innerHTML="",i.appendChild(l)),l.innerHTML="";for(var r=0;r<n.length;r++){var a=n[r],d=document.createElement("li");d.setAttribute("class","list-view-item"),d.setAttribute("id",a.s_id),d.innerHTML=`
                    <span class="setting-label">${e=a,void 0,t="",t=e.card_number?"**** "+e.card_number.slice(-4):t}</span>
                    <span class="arrow-right">›</span>`,l.appendChild(d),function(e){d.addEventListener("click",function(){o(e)})}(a)}}else emptyContent()}document.addEventListener("DOMContentLoaded",function(){document.querySelector("#btn-add-card-auto-fill").addEventListener("click",e),r()})}();