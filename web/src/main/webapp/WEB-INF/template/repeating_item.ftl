<td class='repeating_item_group'>
  <table border="0">
  <tr>
  <td valign="top">
  <#if responseType == 'text' || responseType == 'calculation' || responseType == 'file' || 
       responseType == 'instant-calculation' || responseType == 'group-calculation'>
   <input type='text' style='margin-left:3px'/>
  <#elseif responseType == 'radio'>
  <div class='item_def_control'> 
    <div class='checkbox_control'>
      <#list optionNames as option>
        <div class='select-option'><input type='radio'/> ${option.label}</div>
      </#list>
    </div>
  </div>
  <#elseif responseType == 'single-select'>
  <div class='item_def_control'> 
    <div class='checkbox_control'>
      <#list optionNames as option>
        <div class='select-option'><input type='checkbox'/> ${option.label}</div>
      </#list>
    </div>
  </div>
 <#elseif responseType == 'multi-select' || responseType == 'checkbox'>
  <div class='item_def_control'> 
    <div class='checkbox_control'>
      <#list multSelectOptionNames as option>
        <div class='select-option'><input type='checkbox'/> ${option.label}</div>
      </#list>
    </div>
  </div>
 <#elseif responseType == 'textarea'>
 <span><textarea cols="40" rows="5"></textarea></span>
   <#else>
    [item]
  <#/if>
  </td>
  <td valign="top" width="5"><span class="item_def_format_label">${unitLabel}</span></td>
  </td>
  </tr>
  </table> 
</td>

