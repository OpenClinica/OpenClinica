<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC1.xsd">
	<xsl:output encoding="utf-8" indent="yes" method="xml" name="xml"/>
	<xsl:variable name="dobExist"
				  select="//odm:SubjectData/@OpenClinica:DateOfBirth" />
	<xsl:variable name="dobYearExist"
				  select="//odm:SubjectData/@OpenClinica:YearOfBirth" />
	<xsl:template match="/">
		<!-- Get the parent study oid, which is listed first. -->
		<xsl:variable name="vStudyName" select="substring(concat('S',substring(//odm:Study[position()=1]/@OID, 3)),1,8)"/>
		<xsl:element name="{$vStudyName}">
			<xsl:for-each select="odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData">
				<xsl:element name="{@ItemGroupOID}">
					<xsl:element name="SubjectID">
						<xsl:value-of select="../../../@OpenClinica:StudySubjectID"/>
					</xsl:element>
					<xsl:element name="SiteID">
                        <xsl:variable name="studyOID" select="../../../../@StudyOID"/>
                        <xsl:variable name="studyElement" select="//odm:Study[@OID = $studyOID]"/>
                        <xsl:value-of select="$studyElement/odm:GlobalVariables/odm:ProtocolName"/>
                    </xsl:element>
					<xsl:if test="$dobExist">
						<xsl:element name="DateofBirth">
							<xsl:value-of select="../../../@OpenClinica:DateOfBirth"/>
						</xsl:element>
					</xsl:if>
					<xsl:if test="$dobYearExist">
						<xsl:element name="YearofBirth">
							<xsl:value-of select="../../../@OpenClinica:YearOfBirth"/>
						</xsl:element>
					</xsl:if>
					<xsl:variable name="vStudyEventOID">
						<xsl:value-of select="../../@StudyEventOID"/>
					</xsl:variable>
					<xsl:element name="StudyEvent">
						<xsl:value-of select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID=$vStudyEventOID]/@Name"/>
					</xsl:element>
					<xsl:element name="StudyEventRepeatKey">
						<xsl:value-of select="../../@StudyEventRepeatKey"/>
					</xsl:element>
					<xsl:element name="Form">
                        <xsl:value-of select="../@OpenClinica:FormName"/>
                    </xsl:element>
                    <xsl:element name="FormVersion">
                        <xsl:value-of select="../@OpenClinica:FormLayoutOID"/>
                    </xsl:element>
                    <xsl:element name="FormStatus">
                        <xsl:value-of select="../@OpenClinica:Status"/>
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
