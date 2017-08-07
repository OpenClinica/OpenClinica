<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="2.0" xsi:schemaLocation="http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC1.xsd">
    <!-- Output types for result-document instructions. -->
    <xsl:output encoding="utf-8" indent="yes" method="xml" name="xml"/>
    <xsl:output encoding="utf-8" indent="no" method="text" name="plain" omit-xml-declaration="yes"/>
    <!-- Suppress unmatched nodes. -->
    <xsl:template match="text()"/>
    <!-- Get the parent study oid, which is listed first. -->
    <xsl:variable name="study_oid" select="substring(concat('S',substring(//odm:Study[position()=1]/@OID, 3)),1,8)"/>
    <!-- Index of objects for lookup. -->
    <xsl:key name="event-name" match="odm:StudyEventDef" use="@OID"/>
    <xsl:key name="item-name" match="odm:ItemDef" use="@OID"/>
    <xsl:key name="form-name" match="odm:FormDef" use="@OID"/>
    <!-- Catch-all template that feeds 3 result documents.
         HTML-encoded characters in use: &#xa; (newline), &#9; (tab),
         &quot; (double quote),  &lt; (less than), &gt; (greater than).
         -->
    <xsl:template match="/*">
        <!-- map.xml : instructions for how to read the raw data. -->
        <xsl:element name="SXLEMAP">
            <xsl:attribute name="version">
                <xsl:value-of select="'1.2'"/>
            </xsl:attribute>
            <xsl:apply-templates mode="map" select="odm:Study/odm:MetaDataVersion/odm:ItemGroupDef"/>
        </xsl:element>
    </xsl:template>
    <!-- map.xml templates.-->
    <!-- Row header data for item group data, PATH node is set dynamically.-->
    <xsl:variable name="sas_rowheaders">
        <row name="SubjectID">
            <TYPE>character</TYPE>
            <DATATYPE>string</DATATYPE>
            <LENGTH>50</LENGTH>
        </row>
        <row name="StudyEvent">
            <TYPE>character</TYPE>
            <DATATYPE>string</DATATYPE>
            <LENGTH>255</LENGTH>
        </row>
        <row name="StudyEventRepeatKey">
            <TYPE>numeric</TYPE>
            <DATATYPE>integer</DATATYPE>
        </row>
        <row name="ItemGroupRepeatKey">
            <TYPE>numeric</TYPE>
            <DATATYPE>integer</DATATYPE>
        </row>
    </xsl:variable>
    <!-- Return the ItemGroup TABLE map elements. -->
    <xsl:template match="odm:ItemGroupDef" mode="map">
        <xsl:variable name="curatedOID">
            <xsl:variable name="formdef" select="key('form-name', OpenClinica:ItemGroupDetails/OpenClinica:PresentInForm[1]/@FormOID)"/>
            <xsl:variable name="noprefixoid" select="replace(@OID, '^IG_', '')"/>
            <xsl:variable name="noprefixoidtokenized" select="tokenize($noprefixoid,'_')"/>
            <xsl:if test="string-length(@OID) &gt; 35 ">
                <xsl:value-of select="concat('_',substring(string-join(subsequence($noprefixoidtokenized,1,count($noprefixoidtokenized)-1),'_'),1,26),'_',$noprefixoidtokenized[count($noprefixoidtokenized)])"/>
            </xsl:if>
            <xsl:if test="string-length(@OID) &lt; 36 and not(contains(@OID, 'UNGROUPED'))">
                <xsl:value-of select="concat('_',substring($noprefixoid,1,31))"/>
            </xsl:if>
            <xsl:if test="contains(@OID, 'UNGROUPED')">
                <xsl:variable name="isFormNameAvailable">
                    <xsl:call-template name="isFormBasedTableNameAvailable">
                        <xsl:with-param name="formDef" select="$formdef"/>
                        <xsl:with-param name="ungroupedOID" select="@OID"/>
                    </xsl:call-template>
                </xsl:variable>
                <xsl:choose>
                    <xsl:when test="$isFormNameAvailable= 'false'">
                        <xsl:value-of select="concat('_',substring($noprefixoid,1,31))"/>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:value-of select="replace($formdef/OpenClinica:FormDetails/@ParentFormOID, '^F_', '_')"/>
                    </xsl:otherwise>
                </xsl:choose>
            </xsl:if>
        </xsl:variable>
        <xsl:element name="TABLE">
            <xsl:attribute name="name">
                <xsl:value-of select="$curatedOID"/>
            </xsl:attribute>
            <xsl:element name="TABLE-PATH">
                <xsl:attribute name="syntax">
                    <xsl:value-of select="'XPATH'"/>
                </xsl:attribute>
                <xsl:value-of select="concat('/', $study_oid, '/', @OID)"/>
            </xsl:element>
            <xsl:apply-templates select="$sas_rowheaders/row" mode="map">
                <xsl:with-param name="itemgroup" select="@OID"/>
            </xsl:apply-templates>
            <xsl:apply-templates select="odm:ItemRef" mode="map"/>
        </xsl:element>
    </xsl:template>


    <!-- Determine if the form based name of the ungrouped table conflicts with another table name -->
    <!-- Return 'false' if name is not usable.  Empty string otherwise.-->
    <xsl:template name="isFormBasedTableNameAvailable">
        <xsl:param name="formDef"/>
        <xsl:param name="ungroupedOID"/>
        <xsl:variable name="ungroupedCuratedName" select="replace($formDef/OpenClinica:FormDetails/@ParentFormOID, '^F_', '_')"/>
        <xsl:variable name="formOID" select="$formDef/@OID"/>
        <xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef[.//OpenClinica:PresentInForm/@FormOID=$formOID]">
            <xsl:variable name="noprefixoid" select="replace(@OID, '^IG_', '')"/>
            <xsl:variable name="noprefixoidtokenized" select="tokenize($noprefixoid,'_')"/>
            <xsl:if test="not(@OID = $ungroupedOID)">
                <xsl:variable name="curatedGroupName">
                    <xsl:if test="string-length(@OID) &gt; 35 ">
                        <xsl:value-of select="concat('_',substring(string-join(subsequence($noprefixoidtokenized,1,count($noprefixoidtokenized)-1),'_'),1,26),'_',$noprefixoidtokenized[count($noprefixoidtokenized)])"/>
                    </xsl:if>
                    <xsl:if test="string-length(@OID) &lt; 36">
                        <xsl:value-of select="concat('_',substring($noprefixoid,1,31))"/>
                    </xsl:if>
                </xsl:variable>
                <xsl:if test="$ungroupedCuratedName = $curatedGroupName">
                    <xsl:value-of select="false()"/>
                </xsl:if>
            </xsl:if>
        </xsl:for-each>
    </xsl:template>
    
    <!-- Return row header data using above data.-->
    <xsl:template match="row" mode="map">
        <xsl:param name="itemgroup"/>
        <xsl:element name="COLUMN">
            <xsl:attribute name="Name">
                <xsl:value-of select="@name"/>
            </xsl:attribute>
            <xsl:element name="PATH">
                <xsl:value-of select="concat('/', $study_oid, '/', $itemgroup, '/', @name)"/>
            </xsl:element>
            <xsl:copy-of copy-namespaces="no" select="*"/>
        </xsl:element>
    </xsl:template>
    <!-- Mapping of OpenClinica datatypes to SAS types and datatypes.-->
    <xsl:variable name="sas_typemap">
        <row oc="date">
            <TYPE>character</TYPE>
            <DATATYPE>date</DATATYPE>
        </row>
        <row oc="float">
            <TYPE>numeric</TYPE>
            <DATATYPE>double</DATATYPE>
        </row>
        <row oc="integer">
            <TYPE>numeric</TYPE>
            <DATATYPE>integer</DATATYPE>
        </row>
        <row oc="partialDate">
            <TYPE>character</TYPE>
            <DATATYPE>string</DATATYPE>
            <LENGTH>20</LENGTH>
        </row>
        <row oc="text">
            <TYPE>character</TYPE>
            <DATATYPE>string</DATATYPE>
        </row>
    </xsl:variable>
    <!-- Return the Item COLUMN map elements. -->
    <xsl:template match="odm:ItemRef" mode="map">
        <xsl:variable name="item_group_oid" select="ancestor::odm:ItemGroupDef/@OID"/>
        <xsl:variable name="itemdef" select="key('item-name', @ItemOID)"/>
        <xsl:variable name="typemap" select="$sas_typemap"/>
        <xsl:element name="COLUMN">
            <xsl:attribute name="Name">
                <xsl:variable name="curatedItemOID" select="replace($itemdef/@OID, '^I_[A-Z0-9]*_', '')"/>
                <xsl:variable name="noprefixTokenizedItemOid" select="tokenize($curatedItemOID,'_')"/>
                <xsl:if test="string-length($curatedItemOID) &gt; 31 ">
                    <xsl:value-of select="concat('_',substring(string-join(subsequence($noprefixTokenizedItemOid,1,count($noprefixTokenizedItemOid)-1),'_'),1,26),'_',$noprefixTokenizedItemOid[count($noprefixTokenizedItemOid)])"/>
                </xsl:if>
                <xsl:if test="string-length($curatedItemOID) &lt; 32 ">
                    <xsl:value-of select="concat('_',$curatedItemOID)"/>
                </xsl:if>
            </xsl:attribute>
            <xsl:element name="PATH">
                <xsl:value-of select="concat('/', $study_oid, '/', $item_group_oid, '/', @ItemOID)"/>
            </xsl:element>
            <xsl:copy-of copy-namespaces="no" select="$typemap/row[@oc=$itemdef/@DataType]/*"/>
            <xsl:choose>
                <xsl:when test="$itemdef/@DataType='text' and $itemdef/@Length &gt; 255">
                    <xsl:element name="LENGTH">
                        <xsl:value-of select="255"/>
                    </xsl:element>
                </xsl:when>
                <xsl:when test="$itemdef/@DataType='text' and $itemdef/@Length &lt; 256">
                    <xsl:element name="LENGTH">
                        <xsl:value-of select="$itemdef/@Length"/>
                    </xsl:element>
                </xsl:when>
            </xsl:choose>
        </xsl:element>
    </xsl:template>
</xsl:stylesheet>
