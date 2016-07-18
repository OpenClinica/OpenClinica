<xsl:stylesheet version="2.0"
                xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xlink="http://www.w3c.org/1999/xlink"
                exclude-result-prefixes="xlink">


	<!-- ****************************************************************************************************** -->
	<!-- File: odm1.3_to_1.2_extensions.xsl -->
	<!-- Date: 2011-04-15 -->
	<!-- Version: 1.0.0 -->
	<!-- Author: Pradnya Gawade(Akaza) -->
	<!-- Organization: Akaza Research -->
	<!-- Description: XSL sheetsheet to convert ODM 1.3 to ODM 1.3 without extensions. -->
	<!-- Notes: none yet -->
	<!-- Source Location: SVN repository -->
	<!-- Release Notes for version 1.0.0: -->
	<!-- 1. TBD -->
	<!-- ****************************************************************************************************** -->


	<!-- standard copy template -->
	<xsl:strip-space elements="*" />

	<xsl:template name="copyTemplate" match="node()|@*">

		<xsl:copy>
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:copy>
	</xsl:template>


	
<xsl:template name="removeOCExtnElmnt" priority="2" match="//*[namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' or namespace-uri()='http://www.openclinica.org/ns/rules/v3.1']" ></xsl:template>

<xsl:template name="removeOCExtnAttrib" priority="1" match="//@*[namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' or namespace-uri()='http://www.openclinica.org/ns/rules/v3.1']" ></xsl:template>
<!--
	<xsl:template name="namespaceTo1.2_no" priority="1"
		match="//@*[namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' ] ">
		<xsl:element name="{local-name()}" namespace="''">
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:element>
	</xsl:template>

	<xsl:template name="namespaceTo1.2_rules" priority="2"
		match="//@*[namespace-uri()='http://www.openclinica.org/ns/rules/v3.1' ] ">
		<xsl:element name="{local-name()}" namespace="''">
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:element>
	</xsl:template>
-->
<!--

	<xsl:template priority="4" match="@ODMVersion">
		<xsl:attribute name="ODMVersion">1.2</xsl:attribute>
	</xsl:template>
	-->
<xsl:template match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:RuleImport" priority="3"></xsl:template>


	
	<!--<xsl:template priority="4" match="ODM[@xmlns:OpenClinica='http://www.openclinica.org/ns/odm_ext_v130/v3.1']"/>-->

</xsl:stylesheet>