<?xml version="1.0" encoding="UTF-8"?>
<!-- Copyright (C) 2010, Akaza Research, LLC.  -->
<xsl:stylesheet version="2.0" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance" xmlns:def="http://www.cdisc.org/ns/def/v1.0" xmlns:xlink="http://www.w3c.org/1999/xlink" xmlns:OpenClinica="http://www.openclinica.org/ns/openclinica_odm/v1.3" xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
xmlns:saxon="http://icl.com/saxon" 
extension-element-prefixes="saxon">
	
	<xsl:output method="xml" encoding="US-ASCII"  use-character-maps="quot"/>
	<!-- ****************************************************************************************************** -->
	<!-- File: ODMReportStylesheet.xsl -->
	<!-- Date: 2010-03-15 -->
	<!-- Version: 1.0.0 -->
	<!-- Author: Pradnya Gawade (Akaza) -->
	<!-- Organization: Akaza Research -->
	<!-- Description: A style sheet for transforming CDISC ODM XML data into table-based data format. -->
	<!-- Notes:  none yet	-->
	<!-- Source Location:  SVN repository  -->
	<!-- Release Notes for version 1.0.0: -->
	<!--   1. TBD	-->
	<!-- ****************************************************************************************************** -->
	<xsl:template match="/">	
  <xsl:copy-of select="." />
</xsl:template>
<xsl:character-map name="quot">
   <xsl:output-character character="&#x27;" string="&amp;apos;" />
   <xsl:output-character character="&#x22;" string="&amp;quot;" />
 </xsl:character-map>
</xsl:stylesheet>
