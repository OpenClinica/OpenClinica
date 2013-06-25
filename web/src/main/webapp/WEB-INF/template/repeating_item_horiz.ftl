<td class='repeating_item_group' valign='center'>
  <table class='repeating-horiz-items' width='100%' height='100%'>
  <tr class="repeating_item_option_names">
  
  <#if responseType == 'text' || responseType == 'calculation' || responseType == 'file' || 
    responseType == 'instant-calculation' || responseType == 'group-calculation'>
  <td align='center' valign='center'> <input type='text'/></td>
  <#elseif responseType == 'radio'>
    {{each optionNames}}
      <td align='center' valign='center' class='repeating_item_group'><div class='select-option'><input type='radio'/></div></td>
    {{/each}}
  <#elseif responseType == 'single-select'>
    {{each optionNames}}
      <td align='center' valign='center' class='repeating_item_group'><div class='select-option'><input type='checkbox'/></div></td>
    {{/each}}
  <#elseif responseType == 'multi-select' || responseType == 'checkbox'>
    {{each multiSelectOptionNames}}
      <td align='center' valign='center' class='repeating_item_group'><div class='select-option'><input type='checkbox'/></div></td>
    {{/each}}
   <#elseif responseType == 'textarea'>
     <td align='center' valign='center' class='repeating_item_group'><span><textarea cols="40" rows="5"></textarea></span></td>
   <#else>
    [item]
  <#/if>
  <td>
  <span class="item_def_format_label">${unitLabel}</span> 
  </td>
  </tr>
  </table>
</td>

