<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Copyright (C) 2010, Akaza Research, LLC.  -->
<xsl:stylesheet version="1.0" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance"
                xmlns:def="http://www.cdisc.org/ns/def/v1.0" xmlns:xlink="http://www.w3c.org/1999/xlink"
                xmlns:OpenClinica="http://www.openclinica.org/ns/openclinica_odm/v1.3"
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
                xsi:schemaLocation="http://www.cdisc.org/ns/odm/v1.3 ">
    <xsl:output method="text" version="4.0" encoding="utf-8" indent="no"/>
    <xsl:variable name="delimiter" select="','"/>
    <xsl:template match="/">
        <xsl:text>Subject ID</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Unique ID</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Subject Status</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Sex</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Location</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>StartDate</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Event Status</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Interviewer</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Interviewer date</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>CRF Version Status</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Version Name</xsl:text>
        <xsl:text>&#xa;</xsl:text>
        <xsl:for-each select="/odm:ODM/odm:ClinicalData/odm:SubjectData">
            <xsl:value-of select="@OpenClinica:StudySubjectId"></xsl:value-of>
            <xsl:value-of select="$delimiter" />
            <xsl:value-of select="@OpenClinica:UniqueIdentifier"></xsl:value-of>
            <xsl:value-of select="$delimiter" />
            <xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
            <xsl:value-of select="$delimiter" />
            <xsl:value-of select="@OpenClinica:Sex"></xsl:value-of>
            <xsl:value-of select="$delimiter" />
            <xsl:for-each select="./odm:StudyEventData">
                <xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
                <xsl:value-of select="$delimiter" />
                <xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
                <xsl:value-of select="$delimiter" />
                <xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
                <xsl:value-of select="$delimiter" />
                <xsl:for-each select="./odm:FormData">
                    <xsl:value-of select="@OpenClinica:InterviewerName"></xsl:value-of>
                    <xsl:value-of select="$delimiter" />
                    <xsl:value-of select="@OpenClinica:InterviewDate"></xsl:value-of>
                    <xsl:value-of select="$delimiter" />
                    <xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
                    <xsl:value-of select="$delimiter" />
                    <xsl:value-of select="@OpenClinica:Version"></xsl:value-of>
                    <xsl:value-of select="$delimiter" />
                </xsl:for-each>
            </xsl:for-each>

            <xsl:text>&#xa;</xsl:text>
        </xsl:for-each>
        <xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef">
            
        </xsl:for-each>
    </xsl:template>

</xsl:stylesheet>