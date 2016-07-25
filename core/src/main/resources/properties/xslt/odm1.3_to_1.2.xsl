<xsl:stylesheet version="2.0"
	xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance" xmlns:def="http://www.cdisc.org/ns/def/v1.0"
	xmlns:xlink="http://www.w3c.org/1999/xlink" xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
	exclude-result-prefixes="xlink" xmlns:exsl="http://exslt.org/common">


	<!-- ****************************************************************************************************** -->
	<!-- File: odm1.3_to_1.2.xsl -->
	<!-- Date: 2010-01-25 -->
	<!-- Version: 1.0.0 -->
	<!-- Author: Jamuna Nyayapathi(Akaza), Pradnya Gawade(Akaza) -->
	<!-- Organization: Akaza Research -->
	<!-- Description: XSL sheet to convert ODM 1.3 to ODM 1.2 without extensions. -->
	<!-- Notes: none yet -->
	<!-- Source Location: SVN repository -->
	<!-- Release Notes for version 1.0.0: -->
	<!-- 1. TBD -->
	<!-- ****************************************************************************************************** -->


	<!-- standard copy template -->
	<xsl:strip-space elements="*" />



	<xsl:template name="copyTemplate" match="node()|@*">

		<xsl:copy>
			<xsl:apply-templates select="@*|node()" />
		</xsl:copy>
	</xsl:template>






	<!-- Namespace uri needs to be changed to cdisc 1.2 -->
	<!-- Namespace uri needs to be changed to cdisc 1.2 -->
	
	<xsl:template name="namespaceTo1.2" priority="1"
		match="//*[namespace-uri()='http://www.cdisc.org/ns/odm/v1.3'] ">
		<xsl:element name="{local-name()}" namespace="http://www.cdisc.org/ns/odm/v1.2">
			<xsl:apply-templates select="@*|*|text()" />
		</xsl:element>
	</xsl:template>

<xsl:template name="removeOCExtnElmnt" priority="3" match="//*[ namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' or namespace-uri()='http://www.openclinica.org/ns/rules/v3.1']" ></xsl:template>

<xsl:template name="removeOCExtnAttrib" priority="2" match="//@*[ namespace-uri()='http://www.openclinica.org/ns/odm_ext_v130/v3.1' or namespace-uri()='http://www.openclinica.org/ns/rules/v3.1']" ></xsl:template>
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

	<xsl:template priority="4" match="@ODMVersion">
		<xsl:attribute name="ODMVersion">1.2</xsl:attribute>
	</xsl:template>
<xsl:template match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:RuleImport" priority="6"></xsl:template>

<!-- template for changing the datatype to 'partialDate' to 'text' as ODM 1.2 did not support data type 'partialDate' -->
<xsl:template match="@DataType[parent::*:ItemDef and .='partialDate']" priority="13">
	<xsl:attribute name="DataType">
		<xsl:value-of select="'text'"></xsl:value-of>
	</xsl:attribute>
</xsl:template>


</xsl:stylesheet>