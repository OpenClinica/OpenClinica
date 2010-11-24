<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Copyright (C) 2010, Akaza Research, LLC.  -->
<xsl:stylesheet version="1.0" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance" xmlns:def="http://www.cdisc.org/ns/def/v1.0" xmlns:xlink="http://www.w3c.org/1999/xlink" xmlns:OpenClinica="http://www.openclinica.org/ns/openclinica_odm/v1.3" xmlns:fn="http://www.w3.org/2005/02/xpath-functions" xmlns:fo="http://www.w3.org/1999/XSL/Format" exclude-result-prefixes="fo">
	<xsl:output method="xml" version="1.0" encoding="utf-8" omit-xml-declaration="no" indent="yes"/>
	<xsl:variable name="language">
		<xsl:text>en</xsl:text>
	</xsl:variable>
    <xsl:variable name="Study" select="/odm:ODM/odm:Study[1]"/>
    <xsl:variable name="protocolNameStudy" select="$Study/odm:GlobalVariables/odm:ProtocolName"/>
    <xsl:template match="/">
	<fo:root xmlns:fo="http://www.w3.org/1999/XSL/Format">
      <fo:layout-master-set>
        <fo:simple-page-master master-name="simpleA4" page-height="29.7cm" page-width="21cm" margin-top="2cm" margin-bottom="2cm" margin-left="2cm" margin-right="2cm">
          <fo:region-body/>
        </fo:simple-page-master>
      </fo:layout-master-set>
      <fo:page-sequence master-reference="simpleA4">
        <fo:flow flow-name="xsl-region-body">
          <fo:block font-size="16pt" font-weight="bold" space-after="5mm">View Dataset
          </fo:block>
          <fo:block font-size="12pt" space-after="5mm"><xsl:value-of select="$protocolNameStudy"/>
          </fo:block>
          <fo:block font-size="10pt">
            <fo:table table-layout="fixed" width="100%" border-collapse="separate">
     <!--<html>
      <body>
        <h1>View Dataset</h1>
        <table border="1" >
            <tr>-->
            <fo:table-column column-width="2cm"><xsl:text>Subject Unique ID</xsl:text></fo:table-column>
            <fo:table-column column-width="2cm"><xsl:text>Protocol ID_Site ID</xsl:text></fo:table-column>
            <fo:table-column column-width="2cm"><xsl:text>Date of Birth</xsl:text></fo:table-column>
            <fo:table-column column-width="2cm"><xsl:text>Sex</xsl:text></fo:table-column>
            <fo:table-column column-width="2cm"><xsl:text>Subject Status</xsl:text></fo:table-column>
            <fo:table-column column-width="2cm">Unique ID</fo:table-column>
            <fo:table-column column-width="2cm">Secondary ID</fo:table-column>
            <fo:table-column column-width="2cm">Location</fo:table-column>
            <fo:table-column column-width="2cm">Start Date</fo:table-column>
            <fo:table-column column-width="2cm">End Date</fo:table-column>
            <fo:table-column column-width="2cm">Subject Event Status</fo:table-column>
            <fo:table-column column-width="2cm">Age at Event</fo:table-column>
            <fo:table-column column-width="2cm">Interview Date</fo:table-column>
            <fo:table-column column-width="2cm">Interviewer Name</fo:table-column>
            <fo:table-column column-width="2cm">Crf Version Status</fo:table-column>
            <fo:table-column column-width="2cm">Crf Name</fo:table-column>
            <!--</tr>
        <tr>-->
		<fo:table-body>
			<fo:table-row>
            <xsl:for-each select="/odm:ODM/odm:ClinicalData">
                <xsl:for-each select="./odm:SubjectData">
				
                    <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:UniqueIdentifier"/></fo:block></fo:table-cell>
                    <fo:table-cell><fo:block><xsl:value-of select="$protocolNameStudy"/></fo:block></fo:table-cell>
                    <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:DateOfBirth"/></fo:block></fo:table-cell>
                    <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:Sex"/></fo:block></fo:table-cell>
                    <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:Status"/></fo:block></fo:table-cell>
                    <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:UniqueIdentifier"/></fo:block></fo:table-cell>
                    <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:SecondaryId"/></fo:block></fo:table-cell>
                    <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:StudyEventLocation"/></fo:block></fo:table-cell>
                    <xsl:for-each select="./odm:StudyEventData">
                        <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:StartDate"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:EndDate"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:Status"/></fo:block></fo:table-cell>
                        <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"/></fo:block></fo:table-cell>
                        <xsl:for-each select="./odm:FormData">
                            <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:InterviewDate"/></fo:block></fo:table-cell>
                            <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:InterviewerName"/></fo:block></fo:table-cell>
                            <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:Status"/></fo:block></fo:table-cell>
                            <fo:table-cell><fo:block><xsl:value-of select="@OpenClinica:Version"/></fo:block></fo:table-cell>
                        </xsl:for-each>
                    </xsl:for-each>
                    
                 </xsl:for-each>
            </xsl:for-each>
        <!--</tr>
        </table>
      </body>
     </html>-->
		</fo:table-row>
	  </fo:table-body>
            </fo:table>
          </fo:block>
        </fo:flow>
      </fo:page-sequence>
    </fo:root>
    </xsl:template>
</xsl:stylesheet>
