<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Copyright (C) 2010, Akaza Research, LLC. -->
<xsl:stylesheet version="2.0"
	xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance" xmlns:def="http://www.cdisc.org/ns/def/v1.0"
	xmlns:xlink="http://www.w3c.org/1999/xlink" xmlns:exsl="http://exslt.org/common"
	extension-element-prefixes="exsl"
	xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions">


	<xsl:output method="text" indent="yes" encoding="utf-8"
		standalone="yes" />
	<xsl:strip-space elements="*" />

	<!-- Parameterized separator/end of line characters for flexibility -->
	<xsl:param name="sep" select="'&#x09;'" />
	<xsl:param name="eol" select="'&#10;'" />
	<!--E to represent Events -->
	<xsl:variable name="E" select="'E'" />
	<xsl:variable name="C" select="'C'" />
	<xsl:variable name="delimiter" select="$sep" />
	<xsl:variable name="studyEventDefOID" select="//odm:StudyEventDef[@OID]" />

	<xsl:key name="eventCRFs"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData"
		use="@FormOID" />

	<xsl:key name="studyEvents"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
		use="@StudyEventOID" />

	<xsl:key name="form_OID"
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef" use="@OID" />

	<xsl:strip-space elements="*" />
	<xsl:variable name="fileName" select="/odm:ODM/@FileOID" />
	<xsl:variable name="year"
		select="substring(/odm:ODM/@CreationDateTime, 1, 4)" />
	<xsl:variable name="D_year" select="concat('D', $year)" />
	<xsl:variable name="datasetName" select="substring-before($fileName, $D_year)" />
	<xsl:variable name="desc" select="/odm:ODM/@Description" />
	<xsl:variable name="subject_count"
		select="count(/odm:ODM/odm:ClinicalData/odm:SubjectData)" />
	<xsl:variable name="study" select="/odm:ODM/odm:Study[1]" />
	<xsl:variable name="protocolNameStudy"
		select="$study/odm:GlobalVariables/odm:ProtocolName" />

	<xsl:variable name="formOID"
		select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[@FormOID]" />
	<xsl:variable name="eventOID"
		select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID]" />

	<xsl:variable name="sexExist" select="//odm:SubjectData/@OpenClinica:Sex" />
	<xsl:variable name="uniqueIdExist"
		select="//odm:SubjectData/@OpenClinica:UniqueIdentifier" />
	<xsl:variable name="dobExist"
		select="//odm:SubjectData/@OpenClinica:DateOfBirth" />
	<xsl:variable name="subjectStatusExist" select="//odm:SubjectData/@OpenClinica:Status" />






	<xsl:template match="/odm:ODM">

		<xsl:variable name="fileName" select="/odm:ODM/@FileOID" />
		<xsl:variable name="year"
			select="substring(/odm:ODM/@CreationDateTime, 1, 4)" />
		<xsl:variable name="D_year" select="concat('D', $year)" />
		<xsl:variable name="datasetName" select="substring-before($fileName, $D_year)" />
		<xsl:variable name="desc" select="/odm:ODM/@Description" />
		<xsl:variable name="subject_count"
			select="count(/odm:ODM/odm:ClinicalData/odm:SubjectData)" />
		<xsl:variable name="study" select="/odm:ODM/odm:Study[1]" />
		<xsl:variable name="protocolNameStudy"
			select="$study/odm:GlobalVariables/odm:ProtocolName" />



		<xsl:variable name="eventDefCount"
			select="count(//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])])" />



		<xsl:text>Dataset Name:</xsl:text>
		<xsl:text >&#x9;</xsl:text>
		<xsl:value-of select="$delimiter" />
		<xsl:value-of select="$datasetName" />
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>Dataset Description: </xsl:text>
		<xsl:value-of select="$desc" />
		<xsl:value-of select="$delimiter" />
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>Item Status: </xsl:text>
		<xsl:value-of select="$delimiter" />
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>Study Name: </xsl:text>
		<xsl:value-of select="$delimiter" />
		<xsl:value-of select="$study/odm:GlobalVariables/odm:StudyName" />
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>Protocol ID: </xsl:text>
		<xsl:value-of select="$delimiter" />
		<xsl:value-of select="$protocolNameStudy" />
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>Date: </xsl:text>
		<xsl:value-of select="$delimiter" />
		<xsl:call-template name="FormatDate">
			<xsl:with-param name="DateTime" select="/odm:ODM/@CreationDateTime" />
		</xsl:call-template>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>Subjects: </xsl:text>
		<xsl:value-of select="$delimiter" />
		<xsl:value-of select="$subject_count" />
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>Study Events Definitions</xsl:text>
		<xsl:value-of select="$delimiter" />
		<xsl:value-of select="$eventDefCount" />
		<xsl:text>&#xa;</xsl:text>



		<xsl:apply-templates
			select="//odm:ODM/odm:Study/odm:MetaDataVersion"
			 mode="metadataDisplay"/>
<!--		<xsl:apply-templates-->
<!--			select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID]"-->
<!--			mode="formDataTemplate"></xsl:apply-templates>-->

		<!-- <xsl:apply-templates -->
		<!-- select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID]" -->
		<!-- mode="itemGroupDataHeaderTempl"></xsl:apply-templates> -->
		<xsl:value-of select="$eol"/><!-- Subject Data, item 
			data etc -->
		<xsl:text>SubjectId</xsl:text>
		<xsl:value-of select="$sep"/>
		<xsl:if test="$uniqueIdExist">
			<xsl:text>Unique ID</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<xsl:if test="$subjectStatusExist">
			<xsl:text>Subject Status</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<xsl:if test="$sexExist">
			<xsl:text>Sex</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<xsl:if test="$dobExist">
			<xsl:text>Date of Birth</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		
			<xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
			mode="studyEventInfo" />
	
		<xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
			mode="studyEventHeader" />

		<xsl:value-of select="$eol"/>

		<xsl:apply-templates select="/odm:ODM/odm:ClinicalData/odm:SubjectData"
			mode="allSubjectData" />

	</xsl:template>



	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData" mode="studyEventInfo">
		<xsl:variable name="eventPosition">
		<xsl:for-each select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 	= generate-id(key('studyEvents',@StudyEventOID)[1])]">
		<xsl:copy-of select="position()" />
		</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="crfStatusExist" select="odm:FormData/@OpenClinica:Status" />
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation" />
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate" />

		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status" />
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent" />
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate" />
				<xsl:if test="$eventLocationExist">
									<xsl:text>Location_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$eventStartDateExist">
									<xsl:text>StartDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$eventEndDateExist">
									<xsl:text>EndDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$eventStatusExist">
									<xsl:text>Event Status_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$ageExist">
									<xsl:text>Age_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>
	</xsl:template>

	<xsl:template
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID]"
		mode="studyEventHeader">
		<xsl:variable name="eventOID" select="@StudyEventOID" />
		<xsl:variable name="eventPosition">
		<xsl:for-each select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 	= generate-id(key('studyEvents',@StudyEventOID)[1])]">
		<xsl:copy-of select="position()" />
		</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation" />
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate" />

		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status" />
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent" />
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate" />
		<xsl:variable name="studyEventRepeatKey" select="@StudyEventRepeatKey"/>
		

		
		<xsl:apply-templates
			select="odm:FormData"
			mode="formDataHeader">
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
		</xsl:apply-templates>
	</xsl:template>

<xsl:template match="//odm:ODM/odm:Study/odm:MetaDataVersion" mode="metadataDisplay">

<xsl:apply-templates select="odm:StudyEventDef" mode="studyEventDefinition"/>
<xsl:value-of select="$eol"/>
<xsl:apply-templates select="odm:FormDef" mode="formDataTemplate"/>

</xsl:template>


	<xsl:template
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef"
		mode="studyEventDefinition">
		<xsl:variable name="eventPosition" select="position()" />
		<xsl:variable name="OID" select="@OID" />
		<xsl:variable name="studyName" select="@Name" />
		<xsl:variable name="oid" select="$OID" />

		<xsl:variable name="isRepeating" select="@Repeating" />

<xsl:apply-templates select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]" mode="studyEventDefList">
<xsl:with-param name="oid" select="$oid"/>
<xsl:with-param name="studyName" select="$studyName"/>
</xsl:apply-templates>
	</xsl:template>
	
<xsl:template	mode="studyEventDefList"		match="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
<xsl:param name="oid"/>
<xsl:param name="studyName"/>
			<xsl:variable name="studyEventOID" select="@StudyEventOID" />
			<xsl:if test="$oid=$studyEventOID">
				<xsl:text>Study Event Definition </xsl:text>
				<xsl:value-of select="position()" />
				<xsl:variable name="isRepeating" select="@Repeating" />
				<xsl:if test="$isRepeating='Yes'">
					<xsl:text>(Repeating)</xsl:text>
				</xsl:if>
				<xsl:value-of select="$delimiter" />
				<xsl:value-of select="$studyName" />
				<xsl:value-of select="$delimiter" />
				<xsl:value-of select="$E" />
				<!-- <xsl:value-of select="$eventPosition" />-->
					<xsl:value-of select="position()" />
<!--				<xsl:value-of select="$eol"></xsl:value-of>-->
			
			</xsl:if>
		</xsl:template>


	<xsl:template priority="1" mode="formDataTemplate"
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef">

		<xsl:variable name="FormOID" select="@OID" />
		<xsl:variable name="formName" select="@Name" />
		
		<xsl:variable name="crfPosition" select="position()"/>


		<xsl:apply-templates
			select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() = generate-id(key('eventCRFs',$FormOID)[1])]"
			mode="CrfInfo">
			<xsl:with-param name="oid" select="$FormOID" />
			<xsl:with-param name="formName" select="$formName" />
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
		</xsl:apply-templates>
	</xsl:template>


	<xsl:template mode="CrfInfo"
		match="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<xsl:param name="oid"/>
		<xsl:param name="formName"/>
		<xsl:param name="crfPosition"/>
		<xsl:variable name="formOid" select="@FormOID" />

		<xsl:if test="$oid=@FormOID">
			<xsl:text>CRF</xsl:text>
			<xsl:value-of select="$delimiter" />

			<xsl:value-of select="$formName" />
			<xsl:value-of select="$delimiter" />
			<xsl:value-of select="$C" />
			<xsl:value-of select="count(preceding-sibling::*) + 1" />
			
			<xsl:text>&#xa;</xsl:text>

</xsl:if>

	</xsl:template>






	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData"
		mode="allSubjectData">
		<xsl:apply-templates select="@OpenClinica:StudySubjectID" />
		<xsl:value-of select="$sep"/>
		<xsl:if test="$uniqueIdExist">
			<xsl:value-of select="@OpenClinica:UniqueIdentifier"/>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<!--<xsl:apply-templates select="@OpenClinica:UniqueIdentifier"/> <xsl:value-of 
			select="$sep"></xsl:value-of> <xsl:apply-templates select="(@OpenClinica:Subject)" 
			/> <xsl:value-of select="$sep"></xsl:value-of> -->
		<xsl:if test="$subjectStatusExist">
			<xsl:apply-templates select="(@OpenClinica:Status)" />
			<xsl:value-of select="$sep"/>
		</xsl:if>

		<xsl:if test="$sexExist">
			<xsl:apply-templates select="(@OpenClinica:Sex)" />
			<xsl:value-of select="$sep"/>
		</xsl:if>
		<xsl:if test="$dobExist">
			<xsl:value-of select="@OpenClinica:DateOfBirth"/>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
	<xsl:apply-templates mode="studyEventInfoData" select="odm:StudyEventData"/>
								
		<xsl:variable name="subjectEvents" select="./odm:StudyEventData" />
		<!-- <xsl:apply-templates mode="studyEventsData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 
			= generate-id(key('studyEvents',@StudyEventOID)[1])]"> <xsl:with-param name="subjectEvents" 
			select="$subjectEvents"></xsl:with-param> </xsl:apply-templates> -->
		<xsl:variable name="subjectItems"
			select="./odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData" />
		<xsl:apply-templates
			select="odm:StudyEventData/odm:FormData"
			mode="eventCRFData">
			<xsl:with-param name="subjectForms"
				select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData"/>
			<xsl:with-param name="subjectEvents" select="$subjectEvents"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>


		<xsl:value-of select="$eol" />
	</xsl:template>

	<xsl:template mode="studyEventInfoData" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
	<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation" />
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate" />

		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status" />
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent" />
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate" />
	
				<xsl:if test="$eventLocationExist">
											<xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:if>

										<xsl:if test="$eventStartDateExist">
											<xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:if>

										<xsl:if test="$eventEndDateExist">
											<xsl:value-of select="@OpenClinica:EndDate"></xsl:value-of>
											<xsl:value-of select="$delimiter" />

										</xsl:if>
										<xsl:if test="$eventStatusExist">
											<xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$ageExist">
											<xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:if>
	</xsl:template>

	<xsl:template mode="itemDataColumnHeaders"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">

		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
		<xsl:param name="studyEventRepeatKey" />
				<xsl:param name="eventPosition" />
		<xsl:variable name="itemData" select="." />
		<xsl:variable name="itemOID" select="@ItemOID" />
		
		<xsl:apply-templates
			select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]"
			mode="ItemDefColHeaders">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
			<xsl:with-param name="itemData" select="$itemData" />
			<xsl:with-param name="itemOID" select="$itemOID" />
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
			<xsl:with-param name="ePosition" select="$eventPosition"/>
		</xsl:apply-templates>
	</xsl:template>


	<xsl:template mode="ItemDefColHeaders"
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID]">
		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
		<xsl:param name="itemData" />
		<xsl:param name="itemOID" />
		<xsl:param name="studyEventRepeatKey"/>
		<xsl:param name="ePosition"/>
		<xsl:variable name="formOID"
			select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID" />

		<xsl:if test="$currentFormOID = $formOID"> <!-- Changed from$currentFormOID = $formOID -->
			<xsl:value-of select="@Name" />
			<xsl:text>_</xsl:text>
			<xsl:value-of select="$C" />
			<xsl:value-of select="$crfPosition" />
		
			
		
		
		<xsl:variable name="group" select="$itemData/parent::node()" />
			<xsl:variable name="groupOID" select="$group/@ItemGroupOID" />
			<!-- JN: Commenting out the logic for now, not sure if this is right as per Paul's suggestion -->
			<!--<xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
				<xsl:if test="@Name !='Ungrouped'">
					<xsl:value-of select="@Name" />
				</xsl:if>
			</xsl:for-each>
			--><xsl:if test="$group/@ItemGroupRepeatKey">
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$group/@ItemGroupRepeatKey" />
			</xsl:if>
		<xsl:text>_</xsl:text>	<xsl:value-of select="$E"/>	<xsl:value-of select="$ePosition"/>
			<xsl:if test="$studyEventRepeatKey">
			<xsl:text>_</xsl:text>
			<xsl:value-of select="$studyEventRepeatKey"/>
			</xsl:if>
			<xsl:value-of select="$delimiter" />
			
		</xsl:if>
	</xsl:template>
	<xsl:template mode="formDataHeader"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[@FormOID]">
		<!-- <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]"> -->
		<xsl:param name="eventPosition"/>
		<xsl:param name="studyEventRepeatKey"/>
		<xsl:variable name="crfPosition" select="position()" />
		<xsl:variable name="parentEvent" select=".." />
		<xsl:variable name="currentFormOID" select="@FormOID" />
		
		
		<xsl:apply-templates mode="studyEventData"
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 	= generate-id(key('studyEvents',@StudyEventOID)[1])]">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="parentEvent" select="$parentEvent" />
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			
		</xsl:apply-templates>
		
		<xsl:apply-templates select="odm:ItemGroupData/odm:ItemData"
			mode="itemDataColumnHeaders">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
		</xsl:apply-templates>

		<!-- </xsl:for-each> -->

	</xsl:template>


	<xsl:template mode="itemGroupDataHeaderTempl"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<!-- <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]"> -->
		<xsl:variable name="crfPosition" select="position()" />
		<xsl:variable name="parentEvent" select=".." />

		<xsl:apply-templates mode="itemGroupDataHeader"
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="@FormOID" />
		</xsl:apply-templates>


		<!-- </xsl:for-each> -->

	</xsl:template>





	<xsl:template
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData"
		mode="itemGroupDataHeader">

		<xsl:param name="crfPosition"/>
		<xsl:param name="currentFormOID"/>
		<xsl:variable name="itemData" select="." />
		<xsl:variable name="itemOID" select="@ItemOID" />
		<xsl:apply-templates
			select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef" mode="itemDef">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
		</xsl:apply-templates>
	</xsl:template>


	<!-- event crf Header Data -->
	<xsl:template mode="studyEventData"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:param name="crfPosition"/>
		<xsl:param name="parentEvent"/>
		<xsl:param name="eventPosition"/>
		<xsl:variable name="eventOID" select="@StudyEventOID" />
		<!-- <xsl:variable name="eventPosition" select="position()" /> -->
		<xsl:variable name="crfVersionExist" select="odm:FormData/@OpenClinica:Version" />
		<xsl:variable name="interviewerNameExist"
			select="odm:FormData/@OpenClinica:InterviewerName" />
		<xsl:variable name="interviewDateExist"
			select="odm:FormData/@OpenClinica:InterviewDate" />
		<xsl:variable name="crfStatusExist" select="odm:FormData/@OpenClinica:Status" />
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation" />
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate" />

		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status" />
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent" />
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate" />


		<xsl:if test="@StudyEventOID = $parentEvent/@StudyEventOID">
			<xsl:choose>
				<xsl:when test="@StudyEventRepeatKey">
					<xsl:variable name="allStudyEvents">
						<xsl:for-each select="//odm:StudyEventData">
							<xsl:sort select="@StudyEventRepeatKey" data-type="number" />
							<xsl:copy-of select="." />
						</xsl:for-each>
					</xsl:variable>

					<xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
						<xsl:choose>
							<xsl:when test="position()=1">
								<!--<xsl:if test="$eventLocationExist">
									<xsl:text>Location_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$eventStartDateExist">
									<xsl:text>StartDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$eventEndDateExist">
									<xsl:text>EndDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$eventStatusExist">
									<xsl:text>Event Status_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$ageExist">
									<xsl:text>Age_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									<xsl:value-of select="$delimiter" />
								</xsl:if>
								--><xsl:if test="$interviewerNameExist">
									<xsl:text>Interviewer_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
									</xsl:if>
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$interviewDateExist">
									<xsl:text>Interviewer date</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
									</xsl:if>
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$crfStatusExist">
									<xsl:text>CRF Version Status_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:value-of select="$delimiter" />
								</xsl:if>

								<xsl:if test="$crfVersionExist">
									<xsl:text>Version Name_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:value-of select="$delimiter" />
								</xsl:if>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if
									test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey"><!--
									<xsl:if test="$eventLocationExist">
										<xsl:text>Location_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>

									<xsl:if test="$eventStartDateExist">
										<xsl:text>StartDate_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>

									<xsl:if test="$eventEndDateExist">
										<xsl:text>EndDate_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
							<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>

									<xsl:if test="$eventStatusExist">
										<xsl:text>Event Status_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>

									<xsl:if test="$ageExist">
										<xsl:text>Age_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>
									--><xsl:if test="$interviewerNameExist">
										<xsl:text>Interviewer_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>

									<xsl:if test="$interviewDateExist">
										<xsl:text>Interviewer date</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>

									<xsl:if test="$crfStatusExist">
										<xsl:text>CRF Version Status_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>

									<xsl:if test="$crfVersionExist">
										<xsl:text>Version Name_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
									</xsl:if>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise><!--
					<xsl:if test="$eventLocationExist">
						<xsl:text>Location_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
					</xsl:if>

					<xsl:if test="$eventStartDateExist">
						<xsl:text>StartDate_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
					</xsl:if>

					<xsl:if test="$eventEndDateExist">
						<xsl:text>EndDate_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
					</xsl:if>

					<xsl:if test="$eventStatusExist">
						<xsl:text>Event Status_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
					</xsl:if>

					<xsl:if test="$ageExist">
						<xsl:text>Age_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:value-of select="$delimiter" />
					</xsl:if>
					--><xsl:if test="$interviewerNameExist">
						<xsl:text>Interviewer_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:value-of select="$delimiter" />
					</xsl:if>

					<xsl:if test="$interviewDateExist">
						<xsl:text>Interviewer date</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:value-of select="$delimiter" />
					</xsl:if>

					<xsl:if test="$crfStatusExist">
						<xsl:text>CRF Version Status_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:value-of select="$delimiter" />
					</xsl:if>

					<xsl:if test="$crfVersionExist">
						<xsl:text>Version Name_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:value-of select="$delimiter" />
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>



	<xsl:template mode="eventCRFData"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[@FormOID]">
		<xsl:param name="subjectEvents" />
		<xsl:param name="subjectForms"/>
		<xsl:param name="subjectItems"/>
		<xsl:variable name="currentForm" select="current()" />
		<xsl:variable name="subjectFormData"
			select="$subjectForms[@FormOID=$currentForm/@FormOID]" />
		<xsl:variable name="subjectEvent" select="$subjectFormData/.." />
		<xsl:variable name="parentEvent" select=".." />
		<xsl:apply-templates mode="allEventCrfData"
			select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]">
			<xsl:with-param name="subjectForms" select="$subjectForms"/>
			<xsl:with-param name="currentForm" select="$currentForm"/>
			<xsl:with-param name="subjectEvent" select="$subjectEvent"/>
			<xsl:with-param name="parentEvent" select="$parentEvent"/>
			<xsl:with-param name="subjectFormData" select="$subjectFormData"/>
		</xsl:apply-templates>


		<xsl:apply-templates mode="allItemData"
			select="odm:ItemGroupData/odm:ItemData">
			<xsl:with-param name="currentFormOID" select="@FormOID"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>
	</xsl:template>






	<xsl:template mode="allEventCrfData"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:param name="subjectForms"/>
		<xsl:param name="currentForm"/>
		<xsl:param name="subjectEvent"/>
		<xsl:param name="parentEvent"/>
		<xsl:param name="subjectFormData"/>


		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation" />
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate" />

		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status" />
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent" />
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate" />
		<xsl:variable name="crfVersionExist" select="odm:FormData/@OpenClinica:Version" />
		<xsl:variable name="interviewerNameExist"
			select="odm:FormData/@OpenClinica:InterviewerName" />
		<xsl:variable name="interviewDateExist"
			select="odm:FormData/@OpenClinica:InterviewDate" />
		<xsl:variable name="crfStatusExist" select="odm:FormData/@OpenClinica:Status" />
	
		<xsl:if test="@StudyEventOID = $parentEvent/@StudyEventOID">

			<xsl:choose>
				<xsl:when test="@StudyEventRepeatKey">
					<xsl:variable name="allStudyEvents">
						<xsl:for-each select="//odm:StudyEventData[@StudyEventOID]">
							<xsl:sort select="@StudyEventRepeatKey" data-type="number" />
							<xsl:copy-of select="." />
						</xsl:for-each>
					</xsl:variable>
						
					<xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
						<xsl:choose>
							<xsl:when test="position()=1">

								<!-- Subjects event Data --><!--

								<xsl:choose>
									<xsl:when test="$subjectEvent/node()">
										<xsl:if test="$eventLocationExist">
											<xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:if>

										<xsl:if test="$eventStartDateExist">
											<xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:if>

										<xsl:if test="$eventEndDateExist">
											<xsl:value-of select="@OpenClinica:EndDate"></xsl:value-of>
											<xsl:value-of select="$delimiter" />

										</xsl:if>
										<xsl:if test="$eventStatusExist">
											<xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$ageExist">
											<xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:if>
									</xsl:when>
									<xsl:otherwise>
										<xsl:if test="$eventLocationExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$eventStartDateExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$eventEndDateExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$eventStatusExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$ageExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>
									</xsl:otherwise>

								</xsl:choose>



								--><xsl:choose>

									<xsl:when
										test="$subjectFormData/node()
                                                    and $subjectEvent/@StudyEventOID=@StudyEventOID
                                                    and $subjectEvent/@StudyEventRepeatKey=@StudyEventRepeatKey">
										<xsl:if test="$interviewerNameExist">
											<xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"/>
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$interviewDateExist">
											<xsl:value-of select="$currentForm/@OpenClinica:InterviewDate"/>
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$crfStatusExist">
											<xsl:value-of select="$currentForm/@OpenClinica:Status"/>
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$crfVersionExist">
											<xsl:value-of select="$currentForm/@OpenClinica:Version"/>
											<xsl:value-of select="$delimiter" />
										</xsl:if>

									</xsl:when>
									<xsl:otherwise>
										<xsl:if test="$interviewerNameExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$interviewDateExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$crfVersionExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>
										<xsl:if test="$crfStatusExist">
											<xsl:value-of select="$delimiter" />
										</xsl:if>

									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if
									test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">
									<!--<xsl:choose>
										<xsl:when test="$subjectEvent/node()">
											<xsl:if test="$eventLocationExist">
												<xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
												<xsl:value-of select="$delimiter" />
											</xsl:if>

											<xsl:if test="$eventStartDateExist">
												<xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
												<xsl:value-of select="$delimiter" />
											</xsl:if>

											<xsl:if test="$eventEndDateExist">
												<xsl:value-of select="@OpenClinica:EndDate"></xsl:value-of>
												<xsl:value-of select="$delimiter" />
											</xsl:if>

											<xsl:if test="$eventStatusExist">
												<xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$ageExist">
												<xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"></xsl:value-of>
												<xsl:value-of select="$delimiter" />
											</xsl:if>
										</xsl:when>
										<xsl:otherwise>
											<xsl:if test="$eventLocationExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$eventStartDateExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$eventEndDateExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$eventStatusExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$ageExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>

									--><xsl:choose>
										<xsl:when
											test="$subjectFormData/node()
                                                        and $subjectEvent/@StudyEventOID=@StudyEventOID
                                                        and $subjectEvent/@StudyEventRepeatKey=@StudyEventRepeatKey">
											<xsl:if test="$interviewerNameExist">
												<xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"/>
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$interviewDateExist">
												<xsl:value-of select="$currentForm/@OpenClinica:InterviewDate"/>
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$crfStatusExist">
												<xsl:value-of select="$currentForm/@OpenClinica:Status"/>
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$crfVersionExist">
												<xsl:value-of select="$currentForm/@OpenClinica:Version"/>
												<xsl:value-of select="$delimiter" />
											</xsl:if>
										</xsl:when>
										<xsl:otherwise>
											<xsl:if test="$interviewerNameExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$interviewDateExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$crfVersionExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
											<xsl:if test="$crfStatusExist">
												<xsl:value-of select="$delimiter" />
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>

					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="$subjectFormData/node()"><!--
							<xsl:if test="$eventLocationExist">
								<xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
								<xsl:value-of select="$delimiter" />
							</xsl:if>

							<xsl:if test="$eventStartDateExist">
								<xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
								<xsl:value-of select="$delimiter" />
							</xsl:if>

							<xsl:if test="$eventEndDateExist">
								<xsl:value-of select="@OpenClinica:EndDate"></xsl:value-of>
								<xsl:value-of select="$delimiter" />

							</xsl:if>
							<xsl:if test="$eventStatusExist">
								<xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$ageExist">
								<xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"></xsl:value-of>
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							--><xsl:if test="$interviewerNameExist">
								<xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"/>
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$interviewDateExist">
								<xsl:value-of select="$currentForm/@OpenClinica:InterviewDate"/>
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$crfStatusExist">
								<xsl:value-of select="$currentForm/@OpenClinica:Status"/>
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$crfVersionExist">
								<xsl:value-of select="$currentForm/@OpenClinica:Version"/>
								<xsl:value-of select="$delimiter" />
							</xsl:if>
						</xsl:when>
						<xsl:otherwise><!--
							<xsl:if test="$eventLocationExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$eventStartDateExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$eventEndDateExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$eventStatusExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$ageExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							--><xsl:if test="$interviewerNameExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$interviewDateExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$crfVersionExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
							<xsl:if test="$crfStatusExist">
								<xsl:value-of select="$delimiter" />
							</xsl:if>
						</xsl:otherwise>

					</xsl:choose>

				</xsl:otherwise>
			</xsl:choose>

		</xsl:if>
	</xsl:template>
	<!-- <xsl:template mode="allItemData" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData"> 
		<xsl:param name="subjectItems"/> <xsl:param name="currentFormOID"/> <xsl:variable 
		name="itemData" select="current()"/> <xsl:variable name="itemFormOID" select="$itemData/../../@FormOID"/> 
		<xsl:variable name="itemOID" select="@ItemOID"/> <xsl:variable name="eventRepeatKey" 
		select="$itemData/../../../@StudyEventRepeatKey"/> <xsl:variable name="subjectItemRepeating" 
		select="$subjectItems[@ItemOID = $itemOID and $itemFormOID =../../@FormOID 
		and $eventRepeatKey=../../../@StudyEventRepeatKey]"/> <xsl:variable name="subjectItemSingle" 
		select="$subjectItems[@ItemOID = $itemOID and $itemFormOID =../../@FormOID]"/> 
		<xsl:apply-templates name="allItemDefData" select=""></xsl:apply-templates> 
		</xsl:template> <xsl:template mode="itemDataTemplate" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData"> 
		<xsl:variable name="currentFormOID" select="@FormOID"/> <xsl:apply-templates 
		mode="allItemData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData"> 
		<xsl:with-param name="currentFormOID" select="$currentFormOID"></xsl:with-param> 
		</xsl:apply-templates> <xsl:text>&#xa;</xsl:text> </xsl:template> <xsl:variable 
		name="subjectItems" select="./odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData"/> -->
	<xsl:template mode="allItemData"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">
		<xsl:param name="subjectItems" />
		<xsl:param name="currentFormOID" />
		<xsl:variable name="itemData" select="current()" />
		<xsl:variable name="itemFormOID" select="$itemData/../../@FormOID" />
		<xsl:variable name="itemOID" select="@ItemOID" />
		<xsl:variable name="eventRepeatKey"
			select="$itemData/../../../@StudyEventRepeatKey" />
		<xsl:variable name="subjectItemRepeating"
			select="$subjectItems[@ItemOID = $itemOID
                                                                            and $itemFormOID =../../@FormOID
                                                                            and $eventRepeatKey=../../../@StudyEventRepeatKey]" />
		<xsl:variable name="subjectItemSingle"
			select="$subjectItems[@ItemOID = $itemOID and $itemFormOID =../../@FormOID]" />
		<xsl:apply-templates mode="printItemData"
			select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]">
			<xsl:with-param name="itemData" select="$itemData"/>
			<xsl:with-param name="itemFormOID" select="$itemFormOID" />
			<xsl:with-param name="itemOID" select="$itemOID" />
			<xsl:with-param name="eventRepeatKey" select="$eventRepeatKey" />
			<xsl:with-param name="subjectItemRepeating" select="$subjectItemRepeating" />
			<xsl:with-param name="subjectItemSingle" select="$subjectItemSingle" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
		</xsl:apply-templates>

	</xsl:template>

	<xsl:template match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef"
		mode="printItemData">

		<xsl:param name="itemData"/>
		<xsl:param name="itemFormOID" />
		<xsl:param name="itemOID" />
		<xsl:param name="eventRepeatKey" />
		<xsl:param name="subjectItemRepeating" />
		<xsl:param name="subjectItemSingle" />
		<xsl:param name="currentFormOID" />
		<xsl:variable name="formOID"
			select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemFormOID]/@FormOID" />
		<xsl:if test="$currentFormOID = $formOID">
			<xsl:choose>
				<xsl:when test="$eventRepeatKey">
					<xsl:choose>
						<xsl:when test="count($subjectItemRepeating) &gt; 0">
							<xsl:value-of select="$itemData/@Value" />
							<xsl:value-of select="$delimiter" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$delimiter" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="count($subjectItemSingle) &gt; 0">
							<xsl:value-of select="$itemData/@Value" />
							<xsl:value-of select="$delimiter" />
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$delimiter" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	<xsl:template name="FormatDate">
		<xsl:param name="DateTime" />
		<xsl:variable name="month">
			<xsl:value-of select="substring($DateTime, 6, 2)" />
		</xsl:variable>
		<xsl:variable name="days">
			<xsl:value-of select="substring($DateTime, 9, 2)" />
		</xsl:variable>

		<xsl:variable name="year_of_date">
			<xsl:value-of select="substring($DateTime, 1, 4)" />
		</xsl:variable>

		<xsl:value-of select="$year_of_date"
			disable-output-escaping="yes" />
		<xsl:value-of select="'-'" />
		<xsl:choose>
			<xsl:when test="$month = '01'">
				<xsl:text>Jan</xsl:text>
			</xsl:when>
			<xsl:when test="$month = '02'">
				<xsl:text>Feb</xsl:text>

			</xsl:when>
			<xsl:when test="$month = '03'">
				<xsl:text>Mar</xsl:text>

			</xsl:when>
			<xsl:when test="$month = '04'">
				<xsl:text>Apr</xsl:text>

			</xsl:when>
			<xsl:when test="$month = '05'">

				<xsl:text>May</xsl:text>
			</xsl:when>
			<xsl:when test="$month = '06'">

				<xsl:text>Jun</xsl:text>
			</xsl:when>
			<xsl:when test="$month = '07'">

				<xsl:text>Jul</xsl:text>
			</xsl:when>
			<xsl:when test="$month = '08'">

				<xsl:text>Aug</xsl:text>
			</xsl:when>
			<xsl:when test="$month = '09'">

				<xsl:text>Sep</xsl:text>
			</xsl:when>
			<xsl:when test="$month = '10'">

				<xsl:text>Oct</xsl:text>
			</xsl:when>
			<xsl:when test="$month = '11'">

				<xsl:text>Nov</xsl:text>
			</xsl:when>
			<xsl:when test="$month = '12'">

				<xsl:text>Dec</xsl:text>
			</xsl:when>
		</xsl:choose>

		<xsl:value-of select="'-'" />
		<xsl:if test="(string-length($days) &lt; 2)">
			<xsl:value-of select="0" />
		</xsl:if>

		<xsl:value-of select="$days" />
	</xsl:template>
</xsl:stylesheet>