!function(){function l(e){e=function(e){for(var t={auto_fill_type:"auto_fill_addr",s_id:e},i=document.querySelectorAll("#edit-auto-fill-addr input"),l=0;l<i.length;l++){var n=i[l];t[n.id]=n.value}return console.log(JSON.stringify(t)),t}(e);_XAPP_.updateAutoFill(JSON.stringify(e)),_XAPP_.showToast("toast_data_updated"),closePopPanel(),n()}function e(){document.querySelector("#new-auto-fill-addr").innerHTML=`<div class="section"><div class="input-group">
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_first_name")}</label>
                        <input id="first_name" type="text" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_first_name")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_last_name")}</label>
                        <input id="last_name" type="text" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_last_name")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_phone")}</label>
                        <input id="phone" type="text" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_phone")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_mail")}</label>
                        <input id="email" type="text" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_mail")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_city")}</label>
                        <input id="city" type="text" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_city")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_company")}</label>
                        <input id="company" type="text" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_company")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_zip")}</label>
                        <input id="zip" type="text" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_zip")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_addr")}</label>
                        <input id="address" type="text" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_addr")}">
                    </div>
                    <div class="button-group">
                        <button id="btn-add" class="btn-primary">${_XAPP_.getStringResource("web_str_save")}</button>
                    </div>


                </div></div>`,openPopPanel("new-auto-fill-addr-panel"),document.querySelector("#btn-add").addEventListener("click",t)}function t(){for(var e={s_id:_UTILS_.genSessionId(),auto_fill_type:"auto_fill_addr"},t=document.querySelectorAll("#new-auto-fill-addr input"),i=0;i<t.length;i++){var l=t[i];e[l.id]=l.value}e.phone||e.email||e.city||e.address||e.zip||e.company?(e.update_at=(new Date).valueOf(),_XAPP_.updateAutoFill(JSON.stringify(e)),_XAPP_.showToast("toast_added_one_record"),closePopPanel()):(console.log("lost required fields"),_XAPP_.showToast("toast_lost_required_fields")),n()}function s(i){document.querySelector("#edit-auto-fill-addr").innerHTML=`<div class="section"><div class="input-group">
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_first_name")}</label>
                        <input id="first_name" type="text" value="${null==i.first_name?"":i.first_name}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_first_name")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_last_name")}</label>
                        <input id="last_name" type="text" value="${null==i.last_name?"":i.last_name}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_last_name")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_phone")}</label>
                        <input id="phone" type="text" value="${null==i.phone?"":i.phone}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_phone")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_mail")}</label>
                        <input id="email" type="text" value="${null==i.email?"":i.email}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_mail")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_city")}</label>
                        <input id="city" type="text" value="${null==i.city?"":i.city}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_city")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_company")}</label>
                        <input id="company" type="text" value="${null==i.company?"":i.company}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_company")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_zip")}</label>
                        <input id="zip" type="text" value="${null==i.zip?"":i.zip}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_zip")}">
                    </div>
                    <div class="input-item">
                        <label class="input-label">${_XAPP_.getStringResource("web_str_input_addr")}</label>
                        <input id="address" type="text" value="${null==i.address?"":i.address}" class="input-field" placeholder="${_XAPP_.getStringResource("web_str_input_addr")}">
                    </div>
                    <div class="button-group">
                        <button id="btn-del"  class="btn-danger">${_XAPP_.getStringResource("btn_text_del")}</button>
                        <button id="btn-update" class="btn-primary">${_XAPP_.getStringResource("web_str_save")}</button>
                    </div>
                </div></div>`;var e=document.querySelector("#btn-del"),t=document.querySelector("#btn-update");e.addEventListener("click",function(){var e,t;e=i.s_id,(t=document.querySelector("#"+e))&&t.remove(),_XAPP_.removeAutoFill("auto_fill_addr",e),closePopPanel(),n(),_XAPP_.showToast("toast_deleted_one_record")}),t.addEventListener("click",function(){l(i.s_id)}),openPopPanel("edit-auto-fill-addr-panel")}function n(){var e=_XAPP_.loadAutoFill("auto_fill_addr");if(0==e.length)emptyContent();else{var t,i=document.querySelector("#auto-fill-addr-list");i||((i=document.createElement("ul")).setAttribute("class","list-view"),i.setAttribute("id","auto-fill-addr-list"),(t=document.querySelector(".content")).innerHTML="",t.appendChild(i)),i.innerHTML="";for(var l=0;l<e.length;l++){var n=e[l],a=document.createElement("li");a.setAttribute("class","list-view-item"),a.innerHTML=`
                <span  class="setting-label">${function(e){var t="";for(key in e)if("s_id"!=key&&"auto_fill_type"!=key&&"first_name"!=key&&"last_name"!=key&&(t=e[key]))break;return 4<t.length?t.substring(0,t.length-4)+"****":t}(n)}</span>
                <span class="arrow-right">›</span>`,i.appendChild(a),a.addEventListener("click",function(){s(n)})}}}document.addEventListener("DOMContentLoaded",function(){document.querySelector("#btn-add-addr-auto-fill").addEventListener("click",e),n()})}();