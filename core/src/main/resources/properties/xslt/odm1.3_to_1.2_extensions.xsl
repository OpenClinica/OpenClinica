<xsl:stylesheet
   version="2.0"
   xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
   xmlns:olddef="http://www.cdisc.org/ns/odm/v1.3"
   xmlns="http://www.cdisc.org/ns/odm/v1.2"
   xmlns:oldOC="http://www.openclinica.org/ns/odm_ext_v130/v3.1"
   xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v1.2"
    xmlns:OpenClinicaRules="http://www.openclinica.org/ns/rules/v3.1" 
   exclude-result-prefixes="olddef oldOC">


<!-- ****************************************************************************************************** -->
	<!-- File: odm1.3_to_1.2_extensions.xsl -->
	<!-- Date: 2011-08-16 -->
	<!-- Version: 1.0.0 -->
	<!-- Author: Pradnya Gawade(Akaza), Jamuna Nyayapathi(Akaza) -->
	<!-- Organization: Akaza Research -->
	<!-- Description: XSL sheet to convert ODM 1.3 to ODM 1.2 with extensions. -->
	<!-- Notes:  none yet	-->
	<!-- Source Location:  SVN repository  -->
	<!-- Release Notes for version 1.0.0: -->
	<!--   1. TBD	-->
	<!-- ****************************************************************************************************** -->
	
	<!-- standard copy template -->
	<xsl:template name="copyTemplate" match="node()|@*" >
		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>

<xsl:template name="removeOCExtnElmnt" priority="6" match="//*[ namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' ]" ></xsl:template>

<xsl:template name="removeOCExtnAttrib" priority="5" match="//@*[ namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' ]" ></xsl:template>

<xsl:template name="updateODMVersion" priority="7" match="@ODMVersion" >
	<xsl:attribute name="ODMVersion">1.2</xsl:attribute>
</xsl:template>
		
<xsl:template name="copyOC1.2Elmnt" priority="8" match="oldOC:MultiSelectList | oldOC:MultiSelectListRef | oldOC:MultiSelectListItem" >
	<xsl:element name="OpenClinica:{local-name()}" namespace="http://www.openclinica.org/ns/odm_ext_v130/v1.2">
		<xsl:apply-templates select="@*,node()"/>
	 </xsl:element>
</xsl:template>
	
<xsl:template name="copyOC1.2Attrib" priority="9" match="@*:StudySubjectID | @*:UniqueIdentifier | @*:Status
| @*:DateOfBirth | @*:Sex | @*:StudyEventLocation | @*:StartDate 
| @*:SubjectAgeAtEvent | @*:Version | @*:InterviewerName 
| @*:InterviewDate | @*:Status" >
	<xsl:attribute name="OpenClinica:{local-name()}" namespace="http://www.openclinica.org/ns/odm_ext_v130/v1.2">
		<xsl:value-of select="."/>
	</xsl:attribute>	
</xsl:template>

<xsl:template name="removeOCRulesElmnts" priority="10" match="//*[ namespace-uri()='http://www.openclinica.org/ns/rules/v3.1' ]" ></xsl:template>

<xsl:template name="removeOCRulesAttribs" priority="11" match="//@*[ namespace-uri()='http://www.openclinica.org/ns/rules/v3.1' ]" ></xsl:template>

<xsl:template name="changeNSTo1.2" priority="12" match="olddef:*" >
	<xsl:element name="{local-name()}" namespace="http://www.cdisc.org/ns/odm/v1.2">
		<xsl:namespace name="OpenClinica" select="'http://www.openclinica.org/ns/odm_ext_v130/v1.2'"/>	
		<xsl:apply-templates select="@*,node()"/>
	 </xsl:element>
</xsl:template>

</xsl:stylesheet>