<table border="0" class="page-header">
<tr>
<td>
  <div class='left-header'>
    <div class='header-text text-left-aligned'>${protocolIdLabel}: ${protocolName!"___________________"}</div>  
    <div class='header-text text-left-aligned'>${studyNameLabel}: ${studyName!"___________________"}</div>  
    <div class='header-text text-left-aligned'>${siteNameLabel}: ${siteName!"___________________"}</div> 
    <#if pageType != studyCoverPageType>
      <div class='header-text text-left-aligned'>$eventNameLabel}: ${eventName!"___________________"}</div>  
    </#if>
    <#if eventLocationRequired == "required">
      <div class='header-text text-left-aligned'>${eventLocationLabel}: ${eventLocation!"___________________"}</div>  
    <#elseif app_eventLocationRequired == "optional">
      <div class='header-text text-left-aligned'>${eventLocationLabel}: ${eventLocation!"___________________"}</div>  
    </#if>
    <#if pageType != studyCoverPageType>
      <div class='header-text text-left-aligned'>${eventDateLabel}: ___________________</div>  
    </#if>
  </div> 
</td>
<td>
  <div class='right-header'>
    <div class='header-text'>${studySubjectIdLabel}:___________</div>  
    
    <#if showPersonId == "true" && personIdRequired != "not_used" && personIdRequired != "not used">
      <div class='header-text'>${personIdLabel}:___________</div>  
    </#if>
    <#if secondaryLabelViewable == "true">
      <div class='header-text text-left-aligned'>${secondaryLabel}: ___________________</div>  
    </#if>
    <#if collectSubjectDOB == 1>
      <div class='header-text'>${studySubjectDOBLabel}:___________</div>  
    <#elseif collectSubjectDOB == 2>
      <div class='header-text'>${studySubjectBirthYearLabel}:___________</div>  
    </#if>
    <#if interviewerNameRequired != "not_used">
      <div class='header-text'>${interviewerLabel}:___________</div>  
    </#if>
    <#if interviewDateRequired != "not_used">
      <div class='header-text'>${interviewDateLabel}:___________</div>  
    </#if>
  </div>
</td>
</tr>
</table>