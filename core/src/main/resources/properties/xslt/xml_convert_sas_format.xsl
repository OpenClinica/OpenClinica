<?xml version="1.0"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" version="1.0" xsi:schemaLocation="http://www.cdisc.org/ns/odm/v1.3 OpenClinica-ODM1-3-0-OC1.xsd">
	<xsl:output method="text" omit-xml-declaration="yes"/>
	<xsl:key name="form-name" match="odm:FormDef" use="@OID"/>
	<xsl:template match="/">
		<!-- Get the parent study oid, which is listed first. -->
		<xsl:variable name="vStudyName" select="substring(concat('S',substring(//odm:Study[position()=1]/@OID, 3)),1,8)"/>
		FILENAME <xsl:value-of select="$vStudyName"/> "data.xml";
        FILENAME map "map.xml";
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
            	<xsl:variable name="noprefixoid" select="replace(@OID, 'IG_', '')"/>
            	<xsl:variable name="noprefixoidtokenized" select="tokenize($noprefixoid,'_')"/>
            	<xsl:if test="string-length(@OID) &gt; 35 ">
                	<xsl:value-of select="
                    	concat(substring(string-join(subsequence($noprefixoidtokenized,1,count($noprefixoidtokenized)-1),'_'),1,27),'_',$noprefixoidtokenized[count($noprefixoidtokenized)])"/>
            	</xsl:if>
            	<xsl:if test="string-length(@OID) &lt; 36 and not(contains(@OID, 'UNGROUPED'))">
                	<xsl:value-of select="$noprefixoid"/>
            	</xsl:if>
            	<xsl:if test="contains(@OID, 'UNGROUPED')">
                	<xsl:value-of select="replace($formdef/OpenClinica:FormDetails/@ParentFormOID, 'F_', '')"/>
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
				format <xsl:value-of select="../@Name"/><xsl:text> </xsl:text><xsl:if test="../@DataType = 'text'">$</xsl:if><xsl:value-of select="@CodeListOID"/>_.;
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
</xsl:stylesheet>
