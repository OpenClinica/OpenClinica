<div class='item_def_wrapper_3col'>
  <span class='item_def_number'>${itemNumber}</span>
  <span class='item_def_title'>${name}</span>
  <#if responseType == 'text' || responseType == 'calculation'>
  <span class='item_def_control_3col'> __________ </span>
  </#if>
  <#if responseType == 'single-select'>
  <span class='item_def_control_3col'> 
    <div class='checkbox_control'>
    <#list optionNames as option>
      <div><input type='checkbox'/> ${option.label}</div>
    </#list>
    </div>
  </span>
  </#if>
  <span class="item_def_format_label">${unitLabel}</span> 
</div> 