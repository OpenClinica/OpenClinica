<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC3-0.xsd">
	<xsl:output encoding="utf-8" indent="yes" method="xml" name="xml"/>
	<xsl:template match="/">
		<!-- Get the parent study oid, which is listed first. -->
		<xsl:variable name="vStudyName" select="substring(concat('S',substring(//odm:Study[position()=1]/@OID, 3)),1,8)"/>
		<xsl:element name="{$vStudyName}">
			<xsl:for-each select="odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData">
				<xsl:element name="{@ItemGroupOID}">
					<xsl:element name="SubjectID">
						<xsl:value-of select="../../../@OpenClinica:StudySubjectID"/>
					</xsl:element>
					<xsl:variable name="vStudyEventOID">
						<xsl:value-of select="../../@StudyEventOID"/>
					</xsl:variable>
					<xsl:element name="StudyEvent">
						<xsl:value-of select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID=$vStudyEventOID]/@Name"/>
					</xsl:element>
					<xsl:element name="StudyEventRepeatKey">
						<xsl:value-of select="../../@StudyEventRepeatKey"/>
					</xsl:element>
					<xsl:element name="ItemGroupRepeatKey">
						<xsl:value-of select="@ItemGroupRepeatKey"/>
					</xsl:element>
					<xsl:for-each select="odm:ItemData">
						<xsl:element name="{@ItemOID}">
							<xsl:value-of select="@Value"/>
						</xsl:element>
					</xsl:for-each>
				</xsl:element>
			</xsl:for-each>
		</xsl:element>
	</xsl:template>
	<xsl:template name="get_tablename">
		<xsl:param name="formname"/>
		<xsl:param name="groupname"/>
		<xsl:param name="groupid"/>
		<xsl:value-of select="$groupid"/>
	</xsl:template>
</xsl:stylesheet>
