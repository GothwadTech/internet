!function(){function t(){var t=_XAPP_.getItem("user_scripts");if(0==t.length)emptyContent();else{var e=document.querySelector("#user-script-list");e.innerHTML="";for(var i=0;i<t.length;i++){var n=function(t){var e=document.createElement("li");e.setAttribute("class","media-item"),e.setAttribute("data-script-id",t.script_id),e.id=t.id,e.innerHTML=`<div class="item-icon"></div>
        <div class="item-info">
            <div class="item-title">${t.title}</div>
            <div class="item-description">${t.script_desc}</div>
            <div class="meta-info">
                <span>v${t.version}</span><span>${t.updated_at}</span>
            </div>
        </div>
        <button class="menu-button"></button>
        `,0==t.status&&(e.querySelector(".item-title").style.textDecoration="line-through");t.icon_url&&(e.querySelector(".item-icon").style.backgroundImage=`url(${t.icon_url})`);return e.addEventListener("click",function(){var t=this.getAttribute("data-script-id");t&&(window.location.href="user-script-form.html?script_id="+t)}),e.querySelector(".menu-button").addEventListener("click",function(t){var e=this.closest("[data-script-id]").getAttribute("data-script-id");_XAPP_.dispatchEvent("event_page_to_app",{transId:"show_user_script_menu","data-script-id":e}),t.stopPropagation()}),e}(t[i]);e.appendChild(n)}}}document.addEventListener("event_app_to_page",function(t){var e,i,n,t=t.detail,r=t.transId;"update_user_script_status"==r?(e=t.status,i=t.id,n=document.getElementById(i),"1"==e?n.querySelector(".item-title").style.textDecoration="none":"0"==e&&(n.querySelector(".item-title").style.textDecoration="line-through")):"delete_user_script"==r&&(i=t.id,(n=document.getElementById(i)).remove())}),document.addEventListener("DOMContentLoaded",function(){t()})}();