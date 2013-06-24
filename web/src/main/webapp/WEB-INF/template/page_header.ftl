<table border="0" class="page-header">
<tr>
<td>
  <div class='left-header'>
    <div class='header-text text-left-aligned'>${app_protocolIDLabel}: ${protocolName ? protocolName : '___________________'}</div>  
    <div class='header-text text-left-aligned'>${app_studyNameLabel}: ${studyName ? studyName : '___________________'}</div>  
    <div class='header-text text-left-aligned'>${app_siteNameLabel}: ${siteName ? siteName : '___________________'}</div> 
    <#if pageType != app_studyCoverPageType>
      <div class='header-text text-left-aligned'>${app_eventNameLabel}: ${eventName ? eventName : '___________________'}</div>  
    </#if>
    <#if app_eventLocationRequired == 'required'>
      <div class='header-text text-left-aligned'>${app_eventLocationLabel}: ${app_eventLocation ? app_eventLocation : '___________________'}</div>  
    <#elseif app_eventLocationRequired == 'optional'>
      <div class='header-text text-left-aligned'>${app_eventLocationLabel}: ${app_eventLocation ? app_eventLocation : '___________________'}</div>  
    </#if>
    <#if pageType != app_studyCoverPageType>
      <div class='header-text text-left-aligned'>${app_eventDateLabel}: ___________________</div>  
    </#if>
  </div> 
</td>
<td>
  <div class='right-header'>
    <div class='header-text'>${app_studySubjectIDLabel}:___________</div>  
    
    <#if showPersonID == 'true' && app_personIDRequired != 'not_used' && app_personIDRequired != 'not used'>
      <div class='header-text'>${app_personIDLabel}:___________</div>  
    </#if>
    
    <#if app_secondaryLabelViewable == 'true'>
      <div class='header-text text-left-aligned'>${app_secondaryLabel}: ___________________</div>  
    </#if>
    <#if collectSubjectDOB == 1>
      <div class='header-text'>${app_studySubjectDOBLabel}:___________</div>  
    <#elseif collectSubjectDOB == 2>
      <div class='header-text'>${app_studySubjectBirthYearLabel}:___________</div>  
    </#if>
    <#if app_interviewerNameRequired != "not_used">
      <div class='header-text'>${app_interviewerLabel}:___________</div>  
    <#if app_interviewDateRequired != "not_used">
      <div class='header-text'>${app_interviewDateLabel}:___________</div>  
    </#if>
  </div>
</td>
</tr>
</table>