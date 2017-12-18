<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC1.xsd">
    <xsl:output method="text" omit-xml-declaration="yes"/>
    <xsl:key name="form-name" match="odm:FormDef" use="@OID"/>
    <xsl:template match="/">
        <!-- Get the parent study oid, which is listed first. -->
        <xsl:variable name="vStudyName" select="substring(concat('S',substring(//odm:Study[position()=1]/@OID, 3)),1,8)"/>
        FILENAME <xsl:value-of select="$vStudyName"/> "~/SAS_DATA.xml";
        FILENAME map "~/SAS_MAP.xml";
        LIBNAME <xsl:value-of select="$vStudyName"/> xml xmlmap=map access=readonly;
        proc datasets library=<xsl:value-of select="$vStudyName"/>;
        copy out=work;
        run;
        proc format;
        <xsl:for-each select="odm:ODM/odm:Study/odm:MetaDataVersion/odm:CodeList"><xsl:if test="@DataType='text'">value $<xsl:value-of select="@OID"/>_ <xsl:for-each select="odm:CodeListItem/odm:Decode">"<xsl:value-of select="../@CodedValue"/>"="<xsl:value-of select="odm:TranslatedText"/>" </xsl:for-each>;
            </xsl:if><xsl:if test="@DataType='integer'">value <xsl:value-of select="@OID"/>_ <xsl:for-each select="odm:CodeListItem/odm:Decode"><xsl:value-of select="../@CodedValue"/>="<xsl:value-of select="odm:TranslatedText"/>" </xsl:for-each>;
            </xsl:if></xsl:for-each>
        run;
        <xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef">
            <xsl:variable name="vitemgrouprefOID"><xsl:value-of select="@OID"/></xsl:variable>
            <xsl:variable name="vFormName">
                <xsl:value-of select="substring-before(/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef/odm:ItemGroupRef[@ItemGroupOID=$vitemgrouprefOID]/../@Name,' -')"/>
            </xsl:variable>
            <!-- KK variable -->
            <xsl:variable name="curatedOID">
                <xsl:variable name="formdef" select="key('form-name', OpenClinica:ItemGroupDetails/OpenClinica:PresentInForm[1]/@FormOID)"/>
                <xsl:variable name="noprefixoid" select="replace(@OID, '^IG_', '')"/>
                <xsl:variable name="noprefixoidtokenized" select="tokenize($noprefixoid,'_')"/>
                <xsl:if test="string-length(@OID) &gt; 35 ">
                    <xsl:value-of select="
                        concat('_',substring(string-join(subsequence($noprefixoidtokenized,1,count($noprefixoidtokenized)-1),'_'),1,26),'_',$noprefixoidtokenized[count($noprefixoidtokenized)])"/>
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

            <xsl:variable name="vTableName">
                <xsl:call-template name="get_tablename">
                    <xsl:with-param name="formname" select="$vFormName"/>
                    <xsl:with-param name="groupname" select="@Name"/>
                    <xsl:with-param name="groupid" select="$curatedOID"/>
                </xsl:call-template>
            </xsl:variable>
                <xsl:call-template name="processtable">
                    <xsl:with-param name="TableName" select="$vTableName"/>
                    <xsl:with-param name="ItemGroupOID" select="@OID"/>
                </xsl:call-template>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="processtable">
        <xsl:param name="TableName"/>
        <xsl:param name="ItemGroupOID"/>
        <xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef[@OID=$ItemGroupOID]/odm:ItemRef">
            <xsl:variable name="vitemOID">
                <xsl:value-of select="@ItemOID"/>
            </xsl:variable>
            <xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$vitemOID]/odm:CodeListRef">
                data <xsl:value-of select="$TableName"/>;
                set <xsl:value-of select="$TableName"/>;
                format <xsl:call-template name="get_item_oid"><xsl:with-param name="oid" select="../@OID"/></xsl:call-template> <xsl:text> </xsl:text> <xsl:if test="../@DataType = 'text'">$</xsl:if><xsl:value-of select="@CodeListOID"/>_.;
                run;
            </xsl:for-each>
        </xsl:for-each>
    </xsl:template>
    <xsl:template name="get_tablename">
        <xsl:param name="formname"/>
        <xsl:param name="groupname"/>
        <xsl:param name="groupid"/>
        <xsl:value-of select="$groupid"/>
    </xsl:template>
    <xsl:template name="get_item_oid">
        <xsl:param name="oid"/>
        <xsl:variable name="curatedItemOID" select="replace($oid, '^I_[A-Z0-9]*_', '')"/>
        <xsl:variable name="noprefixTokenizedItemOid" select="tokenize($curatedItemOID,'_')"/>
            <xsl:if test="string-length($curatedItemOID) &gt; 31 ">
                <xsl:value-of select="
                        concat('_',substring(string-join(subsequence($noprefixTokenizedItemOid,1,count($noprefixTokenizedItemOid)-1),'_'),1,26),'_',$noprefixTokenizedItemOid[count($noprefixTokenizedItemOid)])"/>
            </xsl:if>
            <xsl:if test="string-length($curatedItemOID) &lt; 32 ">
                    <xsl:value-of select="concat('_',$curatedItemOID)"/>
            </xsl:if>
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
</xsl:stylesheet>
