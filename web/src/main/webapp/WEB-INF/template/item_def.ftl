<td class='item-def-cell'>
<div class='item_def_wrapper'>
  <table border="0">
  <tr>
  <td valign='top'> 
    <span class='item_def_number'>${itemNumber}</span>
    <span class='item_def_title'>${name}  <#if mandatory == true> * </#if></span>
  </td>
  <#if responseType == 'text' || responseType == 'calculation' || responseType == 'file' || 
       responseType == 'instant-calculation' || responseType == 'group-calculation'>
  <td td valign="top">  
    <input type='text'/> 
  </td>
  <#elseif responseType == 'radio'>
  <td valign="top">
  <div class='item_def_control ${isInline}'> 
    <div class='checkbox_control ${isInline}'>
      <#list optionNames as option>
        <div class='select-option ${isInline}'><input type='radio'/> ${option.label}</div>
      </#list>
    </div>
  </div>
  </td>
  <#elseif responseType == 'single-select'>
  <td valign="top">
  <div class='item_def_control ${isInline}'> 
    <div class='checkbox_control ${isInline}'>
      <#list optionNames as option>
        <div class='select-option ${isInline}'><input type='checkbox'/> ${option.label}</div>
      </#list>
    </div>
  </div>
  </td>
 <#elseif responseType == 'multi-select' || responseType == 'checkbox'>
  <td valign='top'>
  <div class='item_def_control ${isInline}'> 
    <div class='checkbox_control ${isInline}'>
      <#list multiSelectOptionNames as option>
        <div class='select-option ${isInline}'><input type='checkbox'/> ${option.label}</div>
      </#list>
    </div>
  </div>
  </td>
  <#elseif responseType == 'textarea'>
  <td>
 <span><textarea cols="40" rows="5"></textarea></span>
  </td>
  <#else>
    [item]
  </#if>
  <td align="left">
  <span class="item_def_format_label">${unitLabel}</span> 
  </td>
  <td>
  <span class='item_def_format_label'>${rightItemText}</span>
  </td>
  </tr>
  </table>
</div> 
</td>