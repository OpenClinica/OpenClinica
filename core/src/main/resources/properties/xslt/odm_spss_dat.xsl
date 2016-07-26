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
	<xsl:variable name="itemNameAndEventSep" select="'x@x'"/>
	
	<xsl:variable name="matchSep" select="'M_'"/>
	<xsl:variable name="nonMatchSep" select="'*N'"/>
	
	<xsl:variable name="delimiter" select="$sep" />
	<xsl:variable name="studyEventDefOID" select="//odm:StudyEventDef[@OID]" />

	<xsl:key name="eventCRFs"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData"
		use="@FormOID"></xsl:key>

	<xsl:key name="studyEvents"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
		use="@StudyEventOID"></xsl:key>

	<xsl:key name="form_OID"
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef" use="@OID"></xsl:key>

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
        <xsl:variable name="yearOfBirthExist"
		select="//odm:SubjectData/@OpenClinica:YearOfBirth" />
	<xsl:variable name="subjectStatusExist" select="//odm:SubjectData/@OpenClinica:Status" />
	<xsl:variable name="subjectSecondaryIdExist" select="//odm:SubjectData/@OpenClinica:SecondaryID"/>
	
	<xsl:variable name="allEventDefs"
		select="//odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef" />
    <xsl:variable name="allStudyEventDataElements"
		select="//odm:StudyEventData" />	
	<xsl:variable name="allItemGrpDataDataElements" select="//odm:ItemGroupData"/>
	<xsl:variable name="allFormRefElements"
		select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef/odm:FormRef" />
		<xsl:variable name="allItemDataElements" select="//odm:ItemData"/>	
		<xsl:variable name="allItemDefs"
		select="//odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:ItemDef" />
		
	<xsl:variable name="eventColHeaders">
		<xsl:apply-templates
			select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef"
			mode="studyEventInfoHeaders" >		
		</xsl:apply-templates>
	</xsl:variable>
	<xsl:variable name="tokenizedEventHeaders" select="tokenize($eventColHeaders,'_E')"/>
	<xsl:variable name="crfAndDataItemsHeaders">
		<xsl:apply-templates
			select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef"
			mode="studyFormAndDataItemsHeaders" >
				<xsl:with-param name="generateIntHeadersList" select="'Yes'"/>
		</xsl:apply-templates>
	</xsl:variable>
	<xsl:variable name="tokenizedcrfAndDataItemsHeaders" select="tokenize($crfAndDataItemsHeaders,$itemNameAndEventSep)"/>
	<xsl:variable name="mValSeparator1" select="'_][_1'"/>	
	<xsl:variable name="mValSeparator2" select="'_][_2'"/>
	<xsl:variable name="mValSeparator3" select="'_][_3'"/>
	<xsl:variable name="mValSeparator4" select="'_][_4'"/>
	<xsl:variable name="mValSeparator5" select="'_][_5'"/>
	
	<xsl:variable name="err-msg-invalid-date" select="'Invalid value for date'" />

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
			select="count(/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef)" />

		

		<!-- <xsl:apply-templates -->
		<!-- select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID]" -->
		<!-- mode="itemGroupDataHeaderTempl"></xsl:apply-templates> -->
		<!-- JN: Commenting out fix for 0012071: SPSS *.dat file has blank row at the beginning-->
		<!-- xsl:value-of select="$eol"></xsl:value-of--><!-- Subject Data, item 
			data etc -->
		<xsl:text>StudySubjectID</xsl:text>
		<xsl:value-of select="$sep"/>
		<xsl:text>Protocol ID</xsl:text>
		<xsl:value-of select="$sep"/>
		
		<xsl:if test="$uniqueIdExist">
			<xsl:text>PersonID</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<xsl:if test="$subjectSecondaryIdExist">
			<xsl:text>Secondary ID</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<xsl:if test="$subjectStatusExist">
			<xsl:text>SubjectStatus</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<xsl:if test="$sexExist">
			<xsl:text>Sex</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<xsl:if test="$dobExist">
			<xsl:text>DateofBirth</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
                <xsl:if test="$yearOfBirthExist">
			<xsl:text>YearofBirth</xsl:text>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		
		<xsl:apply-templates
			select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef"
			mode="studyEventInfoHeaders" />				
							
			<xsl:apply-templates
			select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef"
			mode="studyFormAndDataItemsHeaders" />
		
		<xsl:value-of select="$eol"></xsl:value-of>

		<xsl:apply-templates select="/odm:ODM/odm:ClinicalData/odm:SubjectData"
			mode="allSubjectData">
			<xsl:with-param name="tokenizedEventHeaders" select="$tokenizedEventHeaders"/>
			<xsl:with-param name="tokenizedcrfAndDataItemsHeaders" select="$tokenizedcrfAndDataItemsHeaders"/>
		</xsl:apply-templates>

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
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef"
		mode="studyEventDefinition">
		<xsl:variable name="eventPosition" select="position()" />
		<xsl:variable name="OID" select="@OID" />
		<xsl:variable name="studyName" select="@Name" />
		<xsl:variable name="oid" select="$OID" />

		<xsl:variable name="isRepeating" select="@Repeating" />

		<xsl:for-each
			select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]">

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
				<xsl:value-of select="$eventPosition" />
				<xsl:value-of select="$eol"></xsl:value-of>
			</xsl:if>
		</xsl:for-each>

	</xsl:template>


	<xsl:template priority="1" mode="formDataTemplate"
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef">
		<xsl:param name="eventOID"/>
		
		<xsl:variable name="OID" select="@OID" />
		<xsl:variable name="formName" select="@Name" />
		<xsl:variable name="oid" select="$OID" />
		<xsl:value-of select="position()" />

		<xsl:apply-templates
			select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() = generate-id(key('eventCRFs',$oid)[1])  and ../@StudyEventOID = $eventOID]"
			mode="CrfInfo">
			<xsl:with-param name="oid" select="$oid" />
			<xsl:with-param name="formName" select="$formName" />
		</xsl:apply-templates>
	</xsl:template>


	<xsl:template mode="CrfInfo"
		match="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<xsl:param name="oid"></xsl:param>
		<xsl:param name="formName"></xsl:param>
		<xsl:variable name="formOid" select="@FormOID" />

		<xsl:variable name="crfPosition" select="position()" />

		<xsl:for-each
			select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',$formOid)[1])]">


			<xsl:text>CRF</xsl:text>
			<xsl:value-of select="$delimiter" />

			<xsl:value-of select="$formName" />
			<xsl:value-of select="$delimiter" />
			<xsl:value-of select="$C" />
			<xsl:value-of select="$crfPosition" />

			<xsl:text>&#xa;</xsl:text>


		</xsl:for-each>
	</xsl:template>

	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData"
		mode="allSubjectData">
		<xsl:param name="tokenizedEventHeaders"/>
		<xsl:param name="tokenizedcrfAndDataItemsHeaders"/>	
		
		<xsl:variable name="studyOID" select="../@StudyOID"/>
		<xsl:variable name="studyElement" select="//odm:Study[@OID = $studyOID]"/>
		<xsl:variable name="protocolName" select="$studyElement/odm:GlobalVariables/odm:ProtocolName"/>
		
		<xsl:apply-templates select="@OpenClinica:StudySubjectID" />
		<xsl:value-of select="$sep"/>
		<xsl:apply-templates select="$protocolName" />
		<xsl:value-of select="$sep"/>
		
		<xsl:if test="$uniqueIdExist">
			<xsl:value-of select="@OpenClinica:UniqueIdentifier"/>
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<xsl:if test="$subjectSecondaryIdExist">
			<xsl:value-of select="@OpenClinica:SecondaryID"/>
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
			<!-- @pgawade 21-Mar-2012 #12213 convert ISO 8601 format date into american date -->
			<!--<xsl:value-of select="@OpenClinica:DateOfBirth"/>>-->
			<xsl:call-template name="convert-date-ISO8601-to-ADate">
				<xsl:with-param name="dateSrc" select="@OpenClinica:DateOfBirth"/>
			</xsl:call-template>			
			<xsl:value-of select="$delimiter" />
		</xsl:if>
                
                <xsl:if test="$yearOfBirthExist">
			<!-- @jrousseau 21-Mar-2014 OC-4783: Year of Birth at subject level not exported   -->
			<xsl:apply-templates select="(@OpenClinica:YearOfBirth)" />			
			<xsl:value-of select="$delimiter" />
		</xsl:if>
	<!--<xsl:apply-templates mode="studyEventInfoData" select="odm:StudyEventData"/>-->
								
		<xsl:variable name="subjectEvents" select="./odm:StudyEventData" />
		<!-- <xsl:apply-templates mode="studyEventsData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 
			= generate-id(key('studyEvents',@StudyEventOID)[1])]"> <xsl:with-param name="subjectEvents" 
			select="$subjectEvents"></xsl:with-param> </xsl:apply-templates> -->
		<xsl:variable name="subjectItems"
			select="./odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData" />
		
		<xsl:variable name="subjectForms" select="./odm:StudyEventData/odm:FormData"/>
		
		<xsl:call-template name="studyEventInfoData2">
			<xsl:with-param name="subjectEvents" select="$subjectEvents"/>			
			<xsl:with-param name="tokenizedEventHeaders" select="$tokenizedEventHeaders"/>
		</xsl:call-template>	
		
		<xsl:call-template name="studyCRFAndItemsData">
			<xsl:with-param name="subjectEvents" select="$subjectEvents"/>	
			<xsl:with-param name="subjectForms" select="$subjectForms"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>		
			<xsl:with-param name="tokenizedcrfAndDataItemsHeaders" select="$tokenizedcrfAndDataItemsHeaders"/>
		</xsl:call-template>
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

	</xsl:template>





	<xsl:template
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData"
		mode="itemGroupDataHeader">

		<xsl:param name="crfPosition"></xsl:param>
		<xsl:param name="currentFormOID"></xsl:param>
		<xsl:variable name="itemData" select="." />
		<xsl:variable name="itemOID" select="@ItemOID" />
		<xsl:apply-templates
			select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef" mode="itemDef">
			<xsl:with-param name="crfPosition" select="$crfPosition"></xsl:with-param>
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
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$interviewerNameExist">
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
	
		
	<xsl:template mode="studyEventInfoHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
	
	<xsl:variable name="eventDefOID" select="@OID"/>
	<xsl:variable name="isRepeating" select="@Repeating"/>
	<xsl:variable name="allStudyEventDataElements" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID = $eventDefOID]"/>	
	<!--<xsl:variable name="eventLocationExist" select="count($allStudyEventDataElements[@OpenClinica:StudyEventLocation]) &gt; 0"/>
	<xsl:variable name="eventStartDateExist" select="count($allStudyEventDataElements[@OpenClinica:StartDate]) &gt; 0"/>
	<xsl:variable name="eventStatusExist" select="count($allStudyEventDataElements[@OpenClinica:Status]) &gt; 0"/>
	<xsl:variable name="eventEndDateExist" select="count($allStudyEventDataElements[@OpenClinica:EndDate]) &gt; 0"/>
	<xsl:variable name="ageExist" select="count($allStudyEventDataElements[@OpenClinica:SubjectAgeAtEvent]) &gt; 0"/>	-->
		
	<xsl:variable name="eventPosition">		
		<xsl:copy-of select="position()" />
	</xsl:variable>
	
	<!-- maximum value of StudyEventRepeatKey for an event -->
		<xsl:variable name="MaxEventRepeatKey">
			<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/@StudyEventRepeatKey">
				<xsl:sort data-type="number"/>
				<xsl:if test="position() = last()">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		
	<xsl:choose>
		<xsl:when test="$isRepeating = 'Yes'">
			<!-- write event data header columns for repeating event -->
			<xsl:apply-templates
				select="."
				mode="createColForRepeatingEvent" >
				<xsl:with-param name="eventRepeatCnt" select="1"/>
				<xsl:with-param name="eventOID"  select="$eventDefOID"/>
				<xsl:with-param name="eventPosition" select="$eventPosition" />
				<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
				<!-- <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>-->
			</xsl:apply-templates>	
		</xsl:when>
		<xsl:otherwise>	
			<!-- write event data header columns for non repeating event -->
			<xsl:apply-templates select="." mode="createColForNonRepeatingEvent">
				<xsl:with-param name="eventPosition" select="$eventPosition"/>
			  <!-- <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>-->
				<xsl:with-param name="eventOID" select="$eventDefOID"/>
			</xsl:apply-templates>
			</xsl:otherwise>					
	</xsl:choose>
	</xsl:template>
	
   <xsl:template name="createColForRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" mode="createColForRepeatingEvent" >
		<xsl:param name="eventOID" />   
	   <xsl:param name="eventPosition"/>
	   <xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist" />
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>		
		<xsl:param name="eventRepeatCnt" />
		<xsl:param name="MaxEventRepeatKey"/>
		
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:StudyEventLocation]) &gt; 0">
			
			<!--<xsl:if test="$eventLocationExist">	-->	
													
				<xsl:text>Location_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />								
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />																				
				<xsl:value-of select="$delimiter" />
			<!--</xsl:if>-->
			</xsl:if>
			<!--<xsl:if test="$eventStartDateExist">					-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:StartDate]) &gt; 0">
				<xsl:text>StartDate_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />						
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:value-of select="$delimiter" />
			<!--</xsl:if>-->
			</xsl:if>
			<!--<xsl:if test="$eventEndDateExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:EndDate]) &gt; 0">
				<xsl:text>EndDate_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />						
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:value-of select="$delimiter" />	
			</xsl:if>
			<!--<xsl:if test="$eventStatusExist">					-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:Status]) &gt; 0">
				<xsl:text>EventStatus_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />							
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:value-of select="$delimiter" />
			<!--</xsl:if>-->
			</xsl:if>
			<!--<xsl:if test="$ageExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:SubjectAgeAtEvent]) &gt; 0">
				<xsl:text>Age_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />						
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />	
				<xsl:value-of select="$delimiter" />							
			<!--</xsl:if>-->
			</xsl:if>
			
			<!--<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = 
					($eventRepeatCnt+1)]) &gt; 0">-->
			<!-- fix for issue 11832: corrected to repeat the process for next incremental event repeat key until it reaches the value of "MaxEventRepeatKey" -->
			<xsl:if test="($eventRepeatCnt+1) &lt;= number($MaxEventRepeatKey)">
				<xsl:call-template name="createColForRepeatingEvent">
					<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
					<xsl:with-param name="eventOID"  select="$eventOID"/>
					<xsl:with-param name="eventPosition" select="$eventPosition" />	
					<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
					<!--<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
					<xsl:with-param name="eventStartDateExist" select="$eventStartDateExist"/>
					<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
					<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
					<xsl:with-param name="ageExist" select="$ageExist"/>	-->					
				</xsl:call-template>
		    </xsl:if>	
   </xsl:template>
   
   <xsl:template mode="createColForNonRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" >
	   <xsl:param name="eventPosition"/>
	   <!--<xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist" />
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>	-->
		<xsl:param name="eventOID"/>
	  
	   <!--<xsl:if test="$eventLocationExist">-->
	   <xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:StudyEventLocation]) &gt; 0">
			<xsl:text>Location_</xsl:text>
			<xsl:value-of select="$E" />
			<xsl:value-of select="$eventPosition" />						
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<!--<xsl:if test="$eventStartDateExist">-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:StartDate]) &gt; 0">
			<xsl:text>StartDate_</xsl:text>
			<xsl:value-of select="$E" />
			<xsl:value-of select="$eventPosition" />	
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<!--<xsl:if test="$eventEndDateExist">			-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:EndDate]) &gt; 0">
			<xsl:text>EndDate_</xsl:text>
			<xsl:value-of select="$E" />
			<xsl:value-of select="$eventPosition" />
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<!--<xsl:if test="$eventStatusExist">			-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:Status]) &gt; 0">
			<xsl:text>EventStatus_</xsl:text>
			<xsl:value-of select="$E" />
			<xsl:value-of select="$eventPosition" />
			<xsl:value-of select="$delimiter" />
		</xsl:if>
		<!--<xsl:if test="$ageExist">			-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:SubjectAgeAtEvent]) &gt; 0">
			<xsl:text>Age_</xsl:text>
			<xsl:value-of select="$E" />
			<xsl:value-of select="$eventPosition" />
			<xsl:value-of select="$delimiter" />
		</xsl:if>
   </xsl:template>

<xsl:template
		match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]"
		mode="studyFormAndDataItemsHeaders">
		<xsl:param name="generateIntHeadersList"/>
		<!--<xsl:variable name="formRefOID" select="@FormOID"/>-->
		<xsl:variable name="eventOID" select="@OID" />
		<xsl:variable name="isEventRepeating" select="@Repeating" />
		<xsl:variable name="isRepeatingEvent" select="@Repeating" />
		<!-- calculate event position -->
		<xsl:variable name="eventPosition">
			<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $eventOID">
					<xsl:copy-of select="position()" />
				</xsl:if>	
			</xsl:for-each>
		</xsl:variable>
		<!-- calculate form def position in metadata -->		
		<!--<xsl:variable name="formRefNodeId" select="generate-id()"/>
		<xsl:variable name="crfPosition">
			<xsl:for-each select="$allFormRefElements">
				<xsl:if test="@FormOID = $formRefOID">
					<xsl:if test="$formRefNodeId = generate-id()">
						<xsl:copy-of select="position()" />
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>-->
		<!-- maximum value of StudyEventRepeatKey for an event -->
		<xsl:variable name="MaxEventRepeatKey">
			<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/@StudyEventRepeatKey">
				<xsl:sort data-type="number"/>
				<xsl:if test="position() = last()">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:apply-templates
			select="."
			mode="studyFormColumnHeaders">
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="isRepeatingEvent" select="$isRepeatingEvent"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>
			<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>	
			<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>	
		</xsl:apply-templates>
		<!-- apply template for item data columns -->
		<xsl:apply-templates
			select="."
			mode="studyItemDataColumnHeaders">			
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>		
			<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
			<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
		</xsl:apply-templates>		
	</xsl:template>
	
	<xsl:template mode="studyFormColumnHeaders"
		match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
		<xsl:param name="generateIntHeadersList"/>
		<!--<xsl:template name="studyEventData2">-->
		<!--<xsl:param name="crfPosition"/>-->
		<xsl:param name="eventOID"/>		
		<xsl:param name="eventPosition"/>
		<xsl:param name="isRepeatingEvent"/>	
		<xsl:param name="MaxEventRepeatKey"/>
		<!--<xsl:variable name="formRefOID" select="@FormOID"/>-->
		<!--
		<xsl:variable name="crfVersionExist" select="count(//odm:FormData[@FormOID = $formRefOID and @OpenClinica:Version]) &gt; 0"/>
		<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[@FormOID = $formRefOID and @OpenClinica:InterviewerName]) &gt; 0"/>
		<xsl:variable name="interviewDateExist" select="count(//odm:FormData[@FormOID = $formRefOID and @OpenClinica:InterviewDate]) &gt; 0"/>
		<xsl:variable name="crfStatusExist" select="count(//odm:FormData[@FormOID = $formRefOID and @OpenClinica:Status]) &gt; 0"/>
		
		-->
		<xsl:choose>
			<xsl:when test="$isRepeatingEvent = 'Yes'">			
				<!-- create CRF columns for repeating event -->		
				<xsl:apply-templates select="." mode="createCRFColForRepeatingEvent">
					<xsl:with-param name="eventOID" select="$eventOID"/>   
				   <xsl:with-param name="eventPosition" select="$eventPosition"/>
				   <xsl:with-param name="eventRepeatCnt" select="1"/>
				   <xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>	
				   <xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>			   						
				</xsl:apply-templates>			
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="odm:FormRef">
					<xsl:variable name="formRefOID" select="@FormOID"/>
					
					<!-- calculate form def position in metadata -->		
					<xsl:variable name="formRefNodeId" select="generate-id()"/>
					<xsl:variable name="crfPosition">
						<xsl:for-each select="$allFormRefElements">
							<xsl:if test="@FormOID = $formRefOID">
								<xsl:if test="$formRefNodeId = generate-id()">
									<xsl:copy-of select="position()" />
								</xsl:if>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
					@OpenClinica:Version])&gt; 0"/>
					
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewerName]) &gt; 0"/>
						
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewDate]) &gt; 0"/>
						
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
					@OpenClinica:Status]) &gt; 0"/>
						
					<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and odm:FormData/@FormOID = 
						$formRefOID]) &gt; 0">
						<xsl:if test="$interviewerNameExist">
							
								<xsl:value-of select="' '"	/>
								<xsl:text>Interviewer</xsl:text>
								<xsl:choose>
									<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
										<xsl:value-of select="$itemNameAndEventSep"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>_</xsl:text>				
										<xsl:value-of select="$E"/>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C" />
								<xsl:value-of select="$crfPosition" />
								<xsl:value-of select="$delimiter" />
						</xsl:if>
	
						<xsl:if test="$interviewDateExist">
							
								<xsl:value-of select="' '"	/>
								<xsl:text>InterviewDate</xsl:text>
								<xsl:choose>
									<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
										<xsl:value-of select="$itemNameAndEventSep"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>_</xsl:text>				
										<xsl:value-of select="$E"/>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C" />
								<xsl:value-of select="$crfPosition" />
								<xsl:value-of select="$delimiter" />
						</xsl:if>
		
						<xsl:if test="$crfStatusExist">
							
							<xsl:value-of select="' '"	/>
							<xsl:text>CRFVersionStatus</xsl:text>
								<xsl:choose>
									<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
										<xsl:value-of select="$itemNameAndEventSep"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>_</xsl:text>				
										<xsl:value-of select="$E"/>
									</xsl:otherwise>
								</xsl:choose>
							<xsl:value-of select="$eventPosition" />
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C" />
							<xsl:value-of select="$crfPosition" />
							<xsl:value-of select="$delimiter" />
						</xsl:if>
		
						<xsl:if test="$crfVersionExist">
							
								<xsl:value-of select="' '"	/>	
								<xsl:text>VersionName</xsl:text>
								<xsl:choose>
									<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
										<xsl:value-of select="$itemNameAndEventSep"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>_</xsl:text>				
										<xsl:value-of select="$E"/>
									</xsl:otherwise>
								</xsl:choose>
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C" />
								<xsl:value-of select="$crfPosition" />
								<xsl:value-of select="$delimiter" />
						</xsl:if>
				</xsl:if>
			</xsl:for-each>	
			</xsl:otherwise>
		</xsl:choose>
		<!--
		<xsl:apply-templates select="odm:ItemGroupData/odm:ItemData"
			mode="itemDataColumnHeaders">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
		</xsl:apply-templates>
		-->
	</xsl:template>
	
<xsl:template name="createCRFColForRepeatingEvent" mode="createCRFColForRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
		<xsl:param name="eventOID" />   
	   <xsl:param name="eventPosition"/>
	   <xsl:param name="eventRepeatCnt" />
	   <xsl:param name="generateIntHeadersList"/>
	   <xsl:param name="MaxEventRepeatKey"/>
	   <!--<xsl:param name="crfPosition" />	   
	   <xsl:param name="crfVersionExist"/>
		<xsl:param name="interviewerNameExist"/>
		<xsl:param name="interviewDateExist"/>
		<xsl:param name="crfStatusExist"/>		
		
		<xsl:variable name="formRefOID" select="@FormOID"/>-->
		<xsl:for-each select="odm:FormRef">
			<xsl:variable name="formRefOID" select="@FormOID"/>
			
			<!-- calculate form def position in metadata -->		
			<xsl:variable name="formRefNodeId" select="generate-id()"/>
			<xsl:variable name="crfPosition">
				<xsl:for-each select="$allFormRefElements">
					<xsl:if test="@FormOID = $formRefOID">
						<xsl:if test="$formRefNodeId = generate-id()">
							<xsl:copy-of select="position()" />
						</xsl:if>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:variable name="crfVersionExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and @OpenClinica:Version 
			and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
			
			<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
				@OpenClinica:InterviewerName  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
				
			<xsl:variable name="interviewDateExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
				@OpenClinica:InterviewDate  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
				
			<xsl:variable name="crfStatusExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and @OpenClinica:Status  and 
			../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					
					<xsl:if test="$interviewerNameExist">
						
							<xsl:value-of select="' '"	/><!-- added for tokenization when displaying crf data -->
							<xsl:text>Interviewer</xsl:text>
								<xsl:choose>
									<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
										<xsl:value-of select="$itemNameAndEventSep"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>_</xsl:text>				
										<xsl:value-of select="$E"/>
									</xsl:otherwise>
								</xsl:choose>
							<xsl:value-of select="$eventPosition" />										
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$eventRepeatCnt" />										
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C" />
							<xsl:value-of select="$crfPosition" />
							<xsl:value-of select="$delimiter" />
					</xsl:if>
		
					<xsl:if test="$interviewDateExist">
						
							<xsl:value-of select="' '"	/>	
							<xsl:text>InterviewDate</xsl:text>
								<xsl:choose>
									<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
										<xsl:value-of select="$itemNameAndEventSep"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>_</xsl:text>				
										<xsl:value-of select="$E"/>
									</xsl:otherwise>
								</xsl:choose>
							<xsl:value-of select="$eventPosition" />										
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$eventRepeatCnt" />										
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C" />
							<xsl:value-of select="$crfPosition" />
							<xsl:value-of select="$delimiter" />
					</xsl:if>
		
					<xsl:if test="$crfStatusExist">
						
							<xsl:value-of select="' '"	/>
							<xsl:text>CRFVersionStatus</xsl:text>
								<xsl:choose>
									<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
										<xsl:value-of select="$itemNameAndEventSep"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>_</xsl:text>				
										<xsl:value-of select="$E"/>
									</xsl:otherwise>
								</xsl:choose>
							<xsl:value-of select="$eventPosition" />
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$eventRepeatCnt" />
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C" />
							<xsl:value-of select="$crfPosition" />
							<xsl:value-of select="$delimiter" />									
					</xsl:if>
		
					<xsl:if test="$crfVersionExist">
						
							<xsl:value-of select="' '"	/>
							<xsl:text>VersionName</xsl:text>
								<xsl:choose>
									<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
										<xsl:value-of select="$itemNameAndEventSep"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:text>_</xsl:text>				
										<xsl:value-of select="$E"/>
									</xsl:otherwise>
								</xsl:choose>
							<xsl:value-of select="$eventPosition" />
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$eventRepeatCnt" />
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C" />
							<xsl:value-of select="$crfPosition" />
							<xsl:value-of select="$delimiter" />
					</xsl:if>				
				
		</xsl:for-each>	
		
		<!--<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = 
					($eventRepeatCnt+1)]) &gt; 0">-->
		<!-- fix for issue 11832: corrected to repeat the process for next incremental event repeat key until it reaches the value of "MaxEventRepeatKey" -->
		<xsl:if test="($eventRepeatCnt+1) &lt;= number($MaxEventRepeatKey)">				
		<xsl:call-template name="createCRFColForRepeatingEvent">
			<xsl:with-param name="eventOID" select="$eventOID"/>   
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
			<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
			<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
		</xsl:call-template>
		</xsl:if>
   </xsl:template>
   
   <xsl:template  mode="studyItemDataColumnHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
		<xsl:param name="eventOID"/> 
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="generateIntHeadersList"/>
		<xsl:param name="MaxEventRepeatKey"/>
		
	  <!-- <xsl:variable name="formRefOID" select="@FormOID"/>	  formRefOID - <xsl:value-of select="$formRefOID" />
	   <xsl:variable name="formRefNodeId" select="generate-id()"/>
		<xsl:variable name="crfPosition">
			<xsl:for-each select="$allFormRefElements">
				<xsl:if test="@FormOID = $formRefOID">
					<xsl:if test="$formRefNodeId = generate-id()">
						<xsl:copy-of select="position()" />
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>-->
	<xsl:variable name="eventPosition">
			<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $eventOID">
					<xsl:copy-of select="position()" />
				</xsl:if>	
			</xsl:for-each>
		</xsl:variable>  	
	
    <!--<xsl:variable name="StudyEventRepeatKey" select="1"/>--><!--temp hardcoded-->
    <!--
	<xsl:apply-templates select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID = $formRefOID]" mode="formRefToDefTemplateForHeaders">
		<xsl:with-param name="crfPosition" select="$crfPosition"/>
		<xsl:with-param name="eventPosition" select="$eventPosition"/>
		<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
		<xsl:with-param name="eventOID" select="$eventOID"/>
		<xsl:with-param name="StudyEventRepeatKey" select="$MaxEventRepeatKey"/>
	</xsl:apply-templates>-->
	<xsl:choose>
		<xsl:when test="$isEventRepeating = 'Yes'">			
			<!-- create item data columns for repeating event -->		
			<xsl:apply-templates select="." mode="createItemDataColForRepeatingEvent">
				<xsl:with-param name="eventOID" select="$eventOID"/>   
			   <xsl:with-param name="eventPosition" select="$eventPosition"/>
			   <xsl:with-param name="eventRepeatCnt" select="1"/>				   						
				<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
				<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/><!-- this is just need to pass on to further template which is common to repeating and non-repeating events -->
				<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
			</xsl:apply-templates>			
		</xsl:when>
		<xsl:otherwise>
			<xsl:for-each select="odm:FormRef">
			<xsl:variable name="formRefOID" select="@FormOID"/>
			
			<xsl:variable name="formRefNodeId" select="generate-id()"/>
			
			<xsl:variable name="crfPosition">
				<xsl:for-each select="$allFormRefElements">
					<xsl:if test="@FormOID = $formRefOID">
						<xsl:if test="$formRefNodeId = generate-id()">
							<xsl:copy-of select="position()" />
						</xsl:if>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:apply-templates  mode="formRefToDefTemplateForHeaders" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID = $formRefOID]">
				<xsl:with-param name="crfPosition" select="$crfPosition"/>
				<xsl:with-param name="eventPosition" select="$eventPosition"/>
				<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
				<xsl:with-param name="eventOID" select="$eventOID"/>
				<xsl:with-param name="StudyEventRepeatKey" select="$MaxEventRepeatKey"/><!-- this param is of no use for non-repeating column further when creating the columns -->
				<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
			</xsl:apply-templates>
								
		</xsl:for-each>	
		</xsl:otherwise>
	</xsl:choose>
  </xsl:template>
  
  <xsl:template name="createItemDataColForRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]" mode="createItemDataColForRepeatingEvent" >
		<xsl:param name="eventOID" />   
	   <xsl:param name="eventPosition"/>
	   <xsl:param name="eventRepeatCnt" />
	   <xsl:param name="MaxEventRepeatKey"/>
	   <xsl:param name="isEventRepeating"/>
	   <xsl:param name="generateIntHeadersList"/>
	 
	   <xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = 
					$eventRepeatCnt]) &gt; 0">
			<xsl:for-each select="odm:FormRef">
				<xsl:variable name="formRefOID" select="@FormOID"/>
				
				
				<xsl:variable name="formRefNodeId" select="generate-id()"/>
				<xsl:variable name="crfPosition">
					<xsl:for-each select="$allFormRefElements">
						<xsl:if test="@FormOID = $formRefOID">
							<xsl:if test="$formRefNodeId = generate-id()">
								<xsl:copy-of select="position()" />
							</xsl:if>
						</xsl:if>
					</xsl:for-each>
				</xsl:variable>
						
				<xsl:apply-templates select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID = $formRefOID]" mode="formRefToDefTemplateForHeaders">
					<xsl:with-param name="crfPosition" select="$crfPosition"/>
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
					<xsl:with-param name="eventOID" select="$eventOID"/>
					<xsl:with-param name="StudyEventRepeatKey" select="$eventRepeatCnt"/>
					<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
				</xsl:apply-templates>						
					
			</xsl:for-each>	
		</xsl:if>	
		
		<!-- fix for issue 11832: corrected to repeat the process for next incremental event repeat key until it reaches the value of "MaxEventRepeatKey" -->
		<!--<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = 
					($eventRepeatCnt+1)]) &gt; 0">-->
		<xsl:if test="($eventRepeatCnt+1) &lt;= number($MaxEventRepeatKey)">			
		
		
		<xsl:apply-templates select="." mode="createItemDataColForRepeatingEvent">
			<xsl:with-param name="eventOID" select="$eventOID"/>   
		   <xsl:with-param name="eventPosition" select="$eventPosition"/>
		   <xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>	
		    <xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>	
		    <xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>	
		    <xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>   						
		</xsl:apply-templates>
		</xsl:if>
   </xsl:template>
   
  <xsl:template mode="formRefToDefTemplateForHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID]">
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition" />
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="eventOID"/>	
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="generateIntHeadersList"/>
		
		<xsl:variable name="formOID" select="@OID"/>
		<xsl:apply-templates select="odm:ItemGroupRef" mode="ItemGrpRefs">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
			<xsl:with-param name="formOID" select="$formOID"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
			<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
			
		</xsl:apply-templates>
  </xsl:template>
  
   <xsl:template mode="ItemGrpRefs" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef/odm:ItemGroupRef[@ItemGroupOID]" >
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition" />
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="formOID"/>
		<xsl:param name="eventOID"/>	
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="generateIntHeadersList"/>
			
		<xsl:variable name="grpOID" select="@ItemGroupOID"/>
		<xsl:apply-templates select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef[@OID = $grpOID]" mode="ItemGrpRefToDefTemplateForHeaders">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
			<xsl:with-param name="formOID" select="$formOID"/>
			<xsl:with-param name="grpOID" select="$grpOID"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
			<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
		</xsl:apply-templates>
  </xsl:template>
  
  <xsl:template mode="ItemGrpRefToDefTemplateForHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef[@OID]">
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition" />
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="formOID"/>
		<xsl:param name="grpOID"/>		
		<xsl:param name="eventOID"/>
		<xsl:param name="StudyEventRepeatKey"/>	
		<xsl:param name="generateIntHeadersList"/>
			
		<xsl:variable name="isGrpRepeating" select="@Repeating"/>
		<!--<xsl:variable name="itemGrpRepeatKey" select="1"/>-->
		
		<xsl:choose>
			<xsl:when test="$isGrpRepeating = 'Yes'">
				<xsl:apply-templates mode="createItemDataColForRepeatingGrps" select=".">
					<xsl:with-param name="crfPosition" select="$crfPosition"/>
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
					<xsl:with-param name="formOID" select="$formOID"/>
					<xsl:with-param name="grpOID" select="$grpOID"/>		
					<xsl:with-param name="eventOID" select="$eventOID"/>
					<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>	
					<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/> 
					<xsl:with-param name="itemGrpRepeatKey" select="1"/>
					<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/> 
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates select="odm:ItemRef" mode="GrpItemRefs">
					<xsl:with-param name="crfPosition" select="$crfPosition"/>
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
					<xsl:with-param name="formOID" select="$formOID"/>
					<xsl:with-param name="grpOID" select="$grpOID"/>
					<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
					<xsl:with-param name="eventOID" select="$eventOID"/>	
					<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
					<xsl:with-param name="isLastItem" select="position()=last()" />
					<!--<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>-->
					<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
			</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
		<!--<xsl:apply-templates select="odm:ItemRef" mode="GrpItemRefs">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
			<xsl:with-param name="formOID" select="$formOID"/>
			<xsl:with-param name="grpOID" select="$grpOID"/>
			<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
			<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
		</xsl:apply-templates>-->
  </xsl:template>
  
  <xsl:template mode="createItemDataColForRepeatingGrps" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef[@OID]">
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition" />
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="formOID"/>
		<xsl:param name="grpOID"/>		
		<xsl:param name="eventOID"/>
		<xsl:param name="StudyEventRepeatKey"/>	
		<xsl:param name="itemGrpRepeatKey"/> 
		<xsl:param name="isGrpRepeating"/>
		<xsl:param name="generateIntHeadersList"/>
		<!--
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $StudyEventRepeatKey 
			and odm:FormData/@FormOID = $formOID and odm:FormData/odm:ItemGroupData/@ItemGroupOID = $grpOID 
			and odm:FormData/odm:ItemGroupData/@ItemGroupRepeatKey = $itemGrpRepeatKey]) &gt; 0">
			<xsl:apply-templates select="odm:ItemRef" mode="GrpItemRefs">
				<xsl:with-param name="crfPosition" select="$crfPosition"/>
				<xsl:with-param name="eventPosition" select="$eventPosition"/>
				<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
				<xsl:with-param name="formOID" select="$formOID"/>
				<xsl:with-param name="grpOID" select="$grpOID"/>
				<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
				<xsl:with-param name="eventOID" select="$eventOID"/>	
				<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
				<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
				<xsl:with-param name="isLastItem" select="position()=last()" />
			</xsl:apply-templates> 
			
			<xsl:apply-templates mode="createItemDataColForRepeatingGrps" select=".">
				<xsl:with-param name="crfPosition" select="$crfPosition"/>
				<xsl:with-param name="eventPosition" select="$eventPosition"/>
				<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
				<xsl:with-param name="formOID" select="$formOID"/>
				<xsl:with-param name="grpOID" select="$grpOID"/>		
				<xsl:with-param name="eventOID" select="$eventOID"/>
				<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>	
				<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey+1"/> 
				<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
			</xsl:apply-templates>
		</xsl:if>
	    -->
	    <!--<xsl:variable name="maxGrpRepeatKey">
				<xsl:for-each select="$allItemGrpDataDataElements[../../@StudyEventOID = $eventOID and ../../@StudyEventRepeatKey = $StudyEventRepeatKey 
						and ../@FormOID = $formOID and @ItemGroupOID = $grpOID ]/@ItemGroupRepeatKey">
						
				<xsl:sort data-type="number"/>
				   <xsl:if test="position() = last()">
					 <xsl:value-of select="."/>
				   </xsl:if>
				  </xsl:for-each>
			</xsl:variable>-->			
			<!--grpOID: <xsl:value-of select="$grpOID"/>	
			maxGrpRepeatKey:<xsl:value-of select="$maxGrpRepeatKey"/>
			StudyEventRepeatKey:<xsl:value-of select="$StudyEventRepeatKey"/>
			itemGrpRepeatKey:<xsl:value-of select="$itemGrpRepeatKey"/>
			cnt: <xsl:value-of select="count($allItemGrpDataDataElements[../../@StudyEventOID = $eventOID and ../../@StudyEventRepeatKey = $StudyEventRepeatKey 
						and ../@FormOID = $formOID and @ItemGroupOID = $grpOID 
						and @ItemGroupRepeatKey = $itemGrpRepeatKey])"/>-->
	    <xsl:choose>
				<xsl:when test="$isEventRepeating = 'Yes'">
					<xsl:variable name="maxGrpRepeatKey">
						<xsl:for-each select="$allItemGrpDataDataElements[../../@StudyEventOID = $eventOID and ../../@StudyEventRepeatKey = $StudyEventRepeatKey 
							and ../@FormOID = $formOID and @ItemGroupOID = $grpOID ]/@ItemGroupRepeatKey">
							<xsl:sort data-type="number"/>
							<xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<xsl:if test="count($allItemGrpDataDataElements[../../@StudyEventOID = $eventOID and ../../@StudyEventRepeatKey = $StudyEventRepeatKey 
						and ../@FormOID = $formOID and @ItemGroupOID = $grpOID 
						and @ItemGroupRepeatKey = $itemGrpRepeatKey]) &gt; 0">
							<xsl:apply-templates select="odm:ItemRef" mode="GrpItemRefs">
								<xsl:with-param name="crfPosition" select="$crfPosition"/>
								<xsl:with-param name="eventPosition" select="$eventPosition"/>
								<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
								<xsl:with-param name="formOID" select="$formOID"/>
								<xsl:with-param name="grpOID" select="$grpOID"/>
								<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
								<xsl:with-param name="eventOID" select="$eventOID"/>	
								<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
								<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
								<xsl:with-param name="isLastItem" select="position()=last()" />
								<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
							</xsl:apply-templates> 
					</xsl:if>
					<xsl:if test="($itemGrpRepeatKey+1) &lt;= number($maxGrpRepeatKey)">		
							<xsl:apply-templates mode="createItemDataColForRepeatingGrps" select=".">
								<xsl:with-param name="crfPosition" select="$crfPosition"/>
								<xsl:with-param name="eventPosition" select="$eventPosition"/>
								<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
								<xsl:with-param name="formOID" select="$formOID"/>
								<xsl:with-param name="grpOID" select="$grpOID"/>		
								<xsl:with-param name="eventOID" select="$eventOID"/>
								<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>	
								<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey+1"/> 
								<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
								<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
							</xsl:apply-templates>
					</xsl:if>		
				</xsl:when>
				
				<xsl:otherwise>
					<xsl:variable name="maxGrpRepeatKey">
						<xsl:for-each select="$allItemGrpDataDataElements[../../@StudyEventOID = $eventOID  
							and ../@FormOID = $formOID and @ItemGroupOID = $grpOID ]/@ItemGroupRepeatKey">
							<xsl:sort data-type="number"/>
							<xsl:if test="position() = last()">
							<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<xsl:if test="count($allItemGrpDataDataElements[../../@StudyEventOID = $eventOID 
						and ../@FormOID = $formOID and @ItemGroupOID = $grpOID 
						and @ItemGroupRepeatKey = $itemGrpRepeatKey]) &gt; 0">
							<xsl:apply-templates select="odm:ItemRef" mode="GrpItemRefs">
								<xsl:with-param name="crfPosition" select="$crfPosition"/>
								<xsl:with-param name="eventPosition" select="$eventPosition"/>
								<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
								<xsl:with-param name="formOID" select="$formOID"/>
								<xsl:with-param name="grpOID" select="$grpOID"/>
								<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
								<xsl:with-param name="eventOID" select="$eventOID"/>	
								<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
								<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
								<xsl:with-param name="isLastItem" select="position()=last()" />
								<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
							</xsl:apply-templates> 
						</xsl:if>
						<xsl:if test="($itemGrpRepeatKey+1) &lt;= number($maxGrpRepeatKey)">	
							<xsl:apply-templates mode="createItemDataColForRepeatingGrps" select=".">
								<xsl:with-param name="crfPosition" select="$crfPosition"/>
								<xsl:with-param name="eventPosition" select="$eventPosition"/>
								<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
								<xsl:with-param name="formOID" select="$formOID"/>
								<xsl:with-param name="grpOID" select="$grpOID"/>		
								<xsl:with-param name="eventOID" select="$eventOID"/>
								<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>	
								<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey+1"/> 
								<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
								<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
							</xsl:apply-templates>
						</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
  </xsl:template>
  
  
  <xsl:template mode="GrpItemRefs" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef/odm:ItemRef[@ItemOID]" >
	  <xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition" />
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="formOID"/>
		<xsl:param name="grpOID"/>
		<xsl:param name="isGrpRepeating"/>
		<xsl:param name="eventOID"/>	
		<xsl:param name="itemGrpRepeatKey"/>	
		<xsl:param name="isLastItem"/>
		<xsl:param name="generateIntHeadersList"/>
		
		<xsl:variable name="itemOID" select="@ItemOID"/>
		<xsl:choose>
			<xsl:when test="$isEventRepeating = 'Yes'">
				<!--<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $StudyEventRepeatKey and odm:FormData/@FormOID = 
						$formOID and odm:FormData/odm:ItemGroupData/@ItemGroupOID = $grpOID and odm:FormData/odm:ItemGroupData/odm:ItemData/@ItemOID = $itemOID]) &gt; 0">
					<xsl:apply-templates
						select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]"
						mode="ItemDefColHeaders2">
						<xsl:with-param name="crfPosition" select="$crfPosition" />
						<xsl:with-param name="eventPosition" select="$eventPosition"/>
						<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
						<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating" />
						
						<xsl:with-param name="itemOID" select="$itemOID" />
						<xsl:with-param name="eventOID" select="$eventOID"/>	
						<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
						<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
						<xsl:with-param name="isLastItem" select="$isLastItem" />
						<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
					</xsl:apply-templates>
				</xsl:if>	-->
		        <xsl:choose>
				<xsl:when test="$isGrpRepeating = 'Yes'">
				 
						
					<xsl:if test="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../@ItemGroupRepeatKey =$itemGrpRepeatKey and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID and ../../../@StudyEventRepeatKey = $StudyEventRepeatKey]) &gt; 0">
					<xsl:apply-templates select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]" mode="ItemDefColHeaders2">
						<xsl:with-param name="crfPosition" select="$crfPosition"/>
						<xsl:with-param name="eventPosition" select="$eventPosition"/>
						<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
						<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>						
						<xsl:with-param name="itemOID" select="$itemOID"/>
						<xsl:with-param name="eventOID" select="$eventOID"/>
						<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
						<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
						<xsl:with-param name="isLastItem" select="$isLastItem"/>
						<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
					</xsl:apply-templates>
				</xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID and ../../../@StudyEventRepeatKey = $StudyEventRepeatKey]) &gt; 0">
					<xsl:apply-templates select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]" mode="ItemDefColHeaders2">
						<xsl:with-param name="crfPosition" select="$crfPosition"/>
						<xsl:with-param name="eventPosition" select="$eventPosition"/>
						<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
						<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>						
						<xsl:with-param name="itemOID" select="$itemOID"/>
						<xsl:with-param name="eventOID" select="$eventOID"/>
						<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
						<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
						<xsl:with-param name="isLastItem" select="$isLastItem"/>
						<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
					</xsl:apply-templates>
				</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
				<xsl:when test="$isGrpRepeating = 'Yes'"><!--repeating grp-->
					<xsl:if test="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../@ItemGroupRepeatKey =$itemGrpRepeatKey and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID]) &gt; 0">						
				<xsl:apply-templates
					select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]"
					mode="ItemDefColHeaders2">
					<xsl:with-param name="crfPosition" select="$crfPosition" />
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
					<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating" />
					<!--<xsl:with-param name="currentFormOID" select="$currentFormOID" />-->
					<!--<xsl:with-param name="itemData" select="$itemData" />-->
					<xsl:with-param name="itemOID" select="$itemOID" />
					<xsl:with-param name="eventOID" select="$eventOID"/>
					<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>	
					<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
					<xsl:with-param name="isLastItem" select="$isLastItem" />
					<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
				</xsl:apply-templates>
		        </xsl:if>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID]) &gt; 0">
				<xsl:apply-templates
					select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]"
					mode="ItemDefColHeaders2">
					<xsl:with-param name="crfPosition" select="$crfPosition" />
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
					<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating" />
					<!--<xsl:with-param name="currentFormOID" select="$currentFormOID" />-->
					<!--<xsl:with-param name="itemData" select="$itemData" />-->
					<xsl:with-param name="itemOID" select="$itemOID" />
					<xsl:with-param name="eventOID" select="$eventOID"/>
					<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>	
					<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>
					<xsl:with-param name="isLastItem" select="$isLastItem" />
					<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
				</xsl:apply-templates>
		</xsl:if>
				</xsl:otherwise>
			</xsl:choose>	
					
			</xsl:otherwise>
		</xsl:choose>		
  </xsl:template>
  
  	<xsl:template mode="ItemDefColHeaders2"
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID]">
		<xsl:param name="crfPosition" />
		<xsl:param name="eventPosition"/>
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="isGrpRepeating"/>
		<xsl:param name="itemOID" />
		<xsl:param name="eventOID"/>	
		<xsl:param name="StudyEventRepeatKey"/>	
		<xsl:param name="grpRepeatKey"/>	
		<xsl:param name="itemGrpRepeatKey"/>
		<xsl:param name="isLastItem"/>
		<xsl:param name="generateIntHeadersList"/>		
				
		 
			<xsl:value-of select="' '"/>
			<!--<xsl:value-of select="@Name" />-->
			
			<!-- @pgawade 11-May-2012 Fix for issue #13613 -->
			<xsl:variable name="itemName" select="@Name"/>
			<xsl:variable name="itemNameValidated">
				<xsl:choose>
					<xsl:when test='matches(substring($itemName, 1, 1), "[^A-Za-z]")'>
						<xsl:value-of select="concat('v$', normalize-space($itemName))"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$itemName"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			
			<!--<xsl:value-of select='replace(normalize-space(@Name), "\s", "_")'/>-->
			<xsl:value-of select='replace(normalize-space($itemNameValidated), "\s", "_")'/>
			
			<xsl:choose>
				<xsl:when test="$generateIntHeadersList = 'Yes'"><!-- Use special constants here than '_E' for internal processing -->
					<xsl:value-of select="$itemNameAndEventSep"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text>_</xsl:text>				
					<xsl:value-of select="$E"/>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:value-of select="$eventPosition"/>
			<xsl:if test="$isEventRepeating = 'Yes'">
				<xsl:text>_</xsl:text><xsl:value-of select="$StudyEventRepeatKey"/>
			</xsl:if>
			<xsl:text>_</xsl:text>
			<xsl:value-of select="$C" /><xsl:value-of select="$crfPosition" />
			<xsl:if test="$isGrpRepeating ='Yes'">
				<xsl:text>_</xsl:text><xsl:value-of select="$itemGrpRepeatKey" />
			</xsl:if>
			<xsl:if test="$isLastItem">
				<xsl:value-of select="' '"/>
			</xsl:if>
			<xsl:value-of select="$delimiter" />
	</xsl:template>	

<xsl:template name="studyEventInfoData2">		
		<xsl:param name="subjectEvents" />
		
		<xsl:param name="tokenizedEventHeaders"/>
		
		<xsl:for-each select="$tokenizedEventHeaders">
		<xsl:variable name="currentPos" select="position()"/><!--currentPos: <xsl:value-of select="$currentPos"/>-->
		<xsl:variable name="currentToken" select="."/>
		<!--{T<xsl:value-of select="position()"/>:<xsl:value-of select="."/>}-->
			<xsl:if test=". != $tokenizedEventHeaders[last()]"><!--not last	-->
				
					<!-- get which event this is -->
					<xsl:variable name="nextToken" select="$tokenizedEventHeaders[$currentPos+1]"/>
					<!--{currentToken:*<xsl:value-of select="$currentToken"/>*
					next token:*<xsl:value-of select="$nextToken"/>*}-->
					<xsl:variable name="numericStart">
						<xsl:if test="ends-with($nextToken,'Location')">
							<xsl:value-of select="substring-before($nextToken,concat($delimiter, 'Location'))"/>
						</xsl:if>
						<xsl:if test="ends-with($nextToken,'StartDate')">
							<xsl:value-of select="substring-before($nextToken,concat($delimiter, 'StartDate'))"/>
						</xsl:if>
						<xsl:if test="ends-with($nextToken,'EndDate')">
							<xsl:value-of select="substring-before($nextToken,concat($delimiter, 'EndDate'))"/>
						</xsl:if>
						<xsl:if test="ends-with($nextToken,'EventStatus')">
							<xsl:value-of select="substring-before($nextToken,concat($delimiter, 'EventStatus'))"/>
						</xsl:if>
						<xsl:if test="ends-with($nextToken,'Age')">
							<xsl:value-of select="substring-before($nextToken,concat($delimiter, 'Age'))"/>
						</xsl:if>
						<xsl:if test="$currentToken = $tokenizedEventHeaders[last()-1]">
							<xsl:value-of select="$nextToken"/>
						</xsl:if>
					</xsl:variable>
					<!--{numeric start: <xsl:value-of select="$numericStart"/>}-->
					<xsl:variable name="colEventPosition" >
						<xsl:choose>
							<xsl:when test="contains($numericStart, '_')">
								<xsl:value-of select="substring-before($numericStart,'_')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$numericStart"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>					
					
					<xsl:variable name="isColForRepeatingEvent" select="contains($numericStart, '_')"/><!--isColForRepeatingEvent<xsl:value-of select="$isColForRepeatingEvent"/>-->
					<xsl:variable name="colRepeatEventKey">
						<xsl:if test="contains($numericStart, '_')">
							<xsl:value-of select="substring-after($numericStart,'_')"/>
						</xsl:if>
					</xsl:variable><!--colRepeatEventKey: <xsl:value-of select="$colRepeatEventKey"/>-->
					
					<xsl:variable name="colType">
						<xsl:if test="ends-with($currentToken,'Location')">
							<xsl:text>Location</xsl:text>
						</xsl:if>
						<xsl:if test="ends-with($currentToken,'StartDate')">
							<xsl:text>StartDate</xsl:text>
						</xsl:if>
						<xsl:if test="ends-with($currentToken,'EndDate')">
							<xsl:text>EndDate</xsl:text>
						</xsl:if>
						<xsl:if test="ends-with($currentToken,'EventStatus')">
							<xsl:text>Status</xsl:text>
						</xsl:if>
						<xsl:if test="ends-with($currentToken,'Age')">
							<xsl:text>Age</xsl:text>
						</xsl:if>
					</xsl:variable>
					
					<!--{colType:*<xsl:value-of select="$colType"/>*-->
					<xsl:variable name="ifMatch">
					<xsl:for-each select="$subjectEvents">
						<xsl:variable name="eventOID" select="@StudyEventOID" />					
						<xsl:variable name="eventPosition">
							<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
								<xsl:if test="@OID = $eventOID">
									<xsl:copy-of select="position()" />
								</xsl:if>	
							</xsl:for-each>				
						</xsl:variable>
						<!--{colEventPosition:*<xsl:value-of select="$colEventPosition"/>*-->
						<xsl:choose>
							<xsl:when test="normalize-space($colEventPosition) = $eventPosition"><!--{event matched}-->
								<xsl:choose>
									<xsl:when test="$isColForRepeatingEvent"> 
										<xsl:choose>
											<xsl:when test="@StudyEventRepeatKey = normalize-space($colRepeatEventKey)">
												<!--<xsl:text>M</xsl:text>--><xsl:value-of select="$matchSep"/><xsl:value-of select="position()"/><!--_<xsl:value-of select="@StudyEventRepeatKey"/>-->
											</xsl:when>
											<xsl:otherwise><!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
											</xsl:otherwise>
										</xsl:choose>										
									</xsl:when>									
									<xsl:otherwise><!--<xsl:text>M</xsl:text>--><xsl:value-of select="$matchSep"/><xsl:value-of select="position()"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
							</xsl:otherwise>			
						</xsl:choose>
					</xsl:for-each>	
				</xsl:variable>
									
					<!--ifMatch: *<xsl:value-of select="$ifMatch"/>*}-->
				<xsl:choose>
					<xsl:when test="contains($ifMatch, $matchSep)">
					 
							<xsl:variable name="StrAfterM" select="substring-after($ifMatch,$matchSep)"/>							
							<xsl:variable name="evenPos">
								<xsl:choose>
									<xsl:when test="contains($StrAfterM,$nonMatchSep)">
										<xsl:value-of select="substring-before($StrAfterM,$nonMatchSep)"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$StrAfterM"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable> 
							<!--evenPos:<xsl:value-of select="$evenPos"/>-->
							<xsl:variable name="event" select="$subjectEvents[position() = number($evenPos)]"/>
							<!-- write data -->	
							<xsl:if test="$colType = 'Location'">
								<xsl:choose>
									<xsl:when test="$event/@OpenClinica:StudyEventLocation">
										<xsl:value-of select="$event/@OpenClinica:StudyEventLocation"></xsl:value-of>
										<xsl:value-of select="$delimiter" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$delimiter" />		
									</xsl:otherwise>
								</xsl:choose>
							</xsl:if>
							<xsl:if test="$colType = 'StartDate'">
								<xsl:choose>
									<xsl:when test="$event/@OpenClinica:StartDate">
									<!-- @pgawade 21-Mar-2012 #12213 convert ISO 8601 format date into american date -->
											<!--<xsl:value-of select="$event/@OpenClinica:StartDate"></xsl:value-of>-->
											<xsl:call-template name="convert-date-ISO8601-to-ADate">
												<xsl:with-param name="dateSrc" select="$event/@OpenClinica:StartDate"/>
											</xsl:call-template>
										<xsl:value-of select="$delimiter" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$delimiter" />		
									</xsl:otherwise>
								</xsl:choose>									
							</xsl:if>
							<xsl:if test="$colType = 'EndDate'">	
								<xsl:choose>
									<xsl:when test="$event/@OpenClinica:EndDate">
									<!-- @pgawade 21-Mar-2012 #12213 convert ISO 8601 format date into american date -->
											<!--<xsl:value-of select="$event/@OpenClinica:EndDate"></xsl:value-of>-->
											<xsl:call-template name="convert-date-ISO8601-to-ADate">
												<xsl:with-param name="dateSrc" select="$event/@OpenClinica:EndDate"/>
											</xsl:call-template>
										<xsl:value-of select="$delimiter" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$delimiter" />
									</xsl:otherwise>
								</xsl:choose>						
							</xsl:if>
							<xsl:if test="$colType = 'Status'">
								<xsl:choose>
									<xsl:when test="$event/@OpenClinica:Status">
										<xsl:value-of select="$event/@OpenClinica:Status"></xsl:value-of>
										<xsl:value-of select="$delimiter" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$delimiter" />
									</xsl:otherwise>
								</xsl:choose>										
							</xsl:if>
							<xsl:if test="$colType = 'Age'">
								<xsl:choose>
									<xsl:when test="$event/@OpenClinica:SubjectAgeAtEvent">
										<xsl:value-of select="$event/@OpenClinica:SubjectAgeAtEvent"></xsl:value-of>
										<xsl:value-of select="$delimiter" />
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$delimiter" />
									</xsl:otherwise>
								</xsl:choose>																				
						</xsl:if>
							
						
					
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="$colType = 'Location'">
							<xsl:value-of select="$delimiter" />										
						</xsl:if>
						<xsl:if test="$colType = 'StartDate'">							
							<xsl:value-of select="$delimiter" />															
						</xsl:if>
						<xsl:if test="$colType = 'EndDate'">
							<xsl:value-of select="$delimiter" />
						</xsl:if>
						<xsl:if test="$colType = 'Status'">							
							<xsl:value-of select="$delimiter" />
						</xsl:if>
						<xsl:if test="$colType = 'Age'">					
							<xsl:value-of select="$delimiter" />
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
		</xsl:if>	
		</xsl:for-each>
							
	</xsl:template>
	
	<xsl:template name="studyCRFAndItemsData">
		<xsl:param name="subjectForms"/>
		<xsl:param name="subjectItems" />
		
		<xsl:param name="subjectEvents" />
		
		<xsl:param name="tokenizedcrfAndDataItemsHeaders"/>		
		<xsl:for-each select="$tokenizedcrfAndDataItemsHeaders">
		<xsl:variable name="currentPos" select="position()"/><!--currentPos: <xsl:value-of select="$currentPos"/>-->
		<xsl:variable name="currentToken" select="."/>
		<!--{T<xsl:value-of select="position()"/>:<xsl:value-of select="."/>}-->
			<xsl:if test=". != $tokenizedcrfAndDataItemsHeaders[last()]"><!--not last-->	
			<!-- ************** Steps *************************-->
					<!-- get event posiotn and event repeat key (if repeating event) from next token.-->
					<!-- get if this is crf or item column -->
					<!-- if crf -->
						<!-- extract event position, event repeat key (if repeating event) if not fecthed commonly, crf position form column name -->
						<!-- iterate study events. for each study event; if repeating - check if crf in column name is present and event info matched as well. If yes write the data. 
																						same for non-repeating except the event repeat key will not be matched. if matched write data else write empty column-->
					<!-- if item -->
					<!-- extract event position, event repeat key (if repeating event) if not fecthed commonly, crf position, grp repeat key (if repeating grp) -->
					<!-- iterate study events. for each study event; if repeating - check if item name, crf, grp repeat key in column name is present and event info matched as well. If yes write the data. 
																						same for non-repeating except the event repeat key will not be matched. if matched write data else write empty column-->
			<!-- ***************************************-->																			
			<!-- get event posiotn and event repeat key (if repeating event) from next token.-->		
			<xsl:variable name="nextToken" select="$tokenizedcrfAndDataItemsHeaders[$currentPos+1]"/>
			<!--CurrentToken:*<xsl:value-of select="$currentToken"/>*
			next token:<xsl:value-of select="$nextToken"/>-->
			<xsl:variable name="numericStart">
				<xsl:choose>
					<xsl:when test="ends-with($nextToken,'Interviewer')">
						<xsl:value-of select="substring-before($nextToken,concat(concat($delimiter, ' '), 'Interviewer'))"/>
					</xsl:when>
					<xsl:when test="ends-with($nextToken,'InterviewDate')">
						<xsl:value-of select="substring-before($nextToken,concat(concat($delimiter, ' '), 'InterviewDate'))"/>
					</xsl:when>
					<xsl:when test="ends-with($nextToken,'CRFVersionStatus')">
						<xsl:value-of select="substring-before($nextToken,concat(concat($delimiter, ' '), 'CRFVersionStatus'))"/>
					</xsl:when>
					<xsl:when test="ends-with($nextToken,'VersionName')">
						<xsl:value-of select="substring-before($nextToken,concat(concat($delimiter, ' '), 'VersionName'))"/>
					</xsl:when>
					<xsl:otherwise>
						<!-- @pgawade: Added following conditional logic in order to meet display of both data item values as well as CRF attributes -->
						<!--<xsl:value-of select="substring-before($nextToken,concat(' ', $delimiter))"/>-->
						<xsl:choose>
							<xsl:when test="contains($nextToken, concat(' ', $delimiter))">
								<xsl:value-of select="substring-before($nextToken,concat(' ', $delimiter))"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="substring-before($nextToken,concat($delimiter, ' '))"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:variable>
			<!--{numeric start: <xsl:value-of select="$numericStart"/>}-->
			<xsl:variable name="numericB4_C" select="substring-before($numericStart, '_C')"/>
			<!--numericB4_C: <xsl:value-of select="$numericB4_C"/>-->
			<xsl:variable name="colEventPosition" >
			<xsl:choose>
				<xsl:when test="contains($numericB4_C, '_')">
					<xsl:value-of select="substring-before($numericStart,'_')"/>
				</xsl:when>
				<xsl:otherwise>
					<xsl:value-of select="$numericB4_C"/>
				</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<!--colEventPosition: *<xsl:value-of select="$colEventPosition"/>*-->
			
			<xsl:variable name="isColForRepeatingEvent" select="contains($numericB4_C, '_')"/><!--isColForRepeatingEvent<xsl:value-of select="$isColForRepeatingEvent"/>-->
			<xsl:variable name="colRepeatEventKey">
				<xsl:if test="contains($numericB4_C, '_')">
					<xsl:value-of select="substring-after($numericB4_C,'_')"/>
				</xsl:if>
			</xsl:variable><!--colRepeatEventKey: <xsl:value-of select="$colRepeatEventKey"/>-->
			<!-- get if this is crf or item column -->		
			<xsl:variable name="colType"> 
				<xsl:choose>
					<xsl:when test="ends-with($currentToken,'Interviewer')">
						<xsl:text>Interviewer</xsl:text>
					</xsl:when>
					<xsl:when test="ends-with($currentToken,'InterviewDate')">
						<xsl:text>InterviewDate</xsl:text>
					</xsl:when>
					<xsl:when test="ends-with($currentToken,'CRFVersionStatus')">
						<xsl:text>CRFVersionStatus</xsl:text>
					</xsl:when>
					<xsl:when test="ends-with($currentToken,'VersionName')">
						<xsl:text>VersionName</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:text>ItemData</xsl:text>
					</xsl:otherwise>
				</xsl:choose>				
			</xsl:variable>
			<!--{colType: <xsl:value-of select="$colType"/>	}-->
			<xsl:variable name="numericAfter_C" select="substring-after($numericStart, '_C')"/>
			<!--numericAfter_C:<xsl:value-of select="$numericAfter_C"/>	-->
			<xsl:variable name="colCrfPosition" >
				<xsl:choose>
					<xsl:when test="contains($numericAfter_C, '_')">
						<xsl:value-of select="substring-before($numericAfter_C,'_')"/>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$numericAfter_C"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:variable>
			<!--colCrfPosition: *<xsl:value-of select="$colCrfPosition"/>*	-->
			<xsl:variable name="colItemName">
				<xsl:if test="$colType = 'ItemData'">
					<xsl:value-of select="substring-after($currentToken, ' ')"/>
				</xsl:if>
			</xsl:variable>
			<!--{colItemName: *<xsl:value-of select="$colItemName"/>*}-->
			<xsl:variable name="isColForRepeatingGrp" select="contains($numericAfter_C, '_')"/><!--isColForRepeatingGrp<xsl:value-of select="$isColForRepeatingGrp"/>-->
			<xsl:variable name="colRepeatGrpKey">
				<xsl:if test="contains($numericAfter_C, '_')">
					<xsl:value-of select="substring-after($numericAfter_C,'_')"/>
				</xsl:if>
			</xsl:variable>
				<!--colRepeatGrpKey: *<xsl:value-of select="$colRepeatGrpKey"/>*-->
			<xsl:choose>
				<xsl:when test="$colType = 'ItemData'"><!--data column -->
					<xsl:variable name="ifMatch" >
						<xsl:for-each select="$subjectEvents">
							<xsl:variable name="eventOID" select="@StudyEventOID" /><!--{eventOID:<xsl:value-of select="$eventOID"/>}-->
							<xsl:variable name="eventRepeatKey" select="@StudyEventRepeatKey"/><!--{eventRepeatKey:<xsl:value-of select="$eventRepeatKey"/>}-->
							<xsl:variable name="eventPosition">
								<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
									<xsl:if test="@OID = $eventOID">
										<xsl:copy-of select="position()" />
									</xsl:if>	
								</xsl:for-each>				
							</xsl:variable>
							<!--{eventPosition:<xsl:value-of select="$eventPosition"/>}-->
							<xsl:choose>
								<xsl:when test="$colEventPosition = $eventPosition"><!--{event matched}-->
								<xsl:choose>
									<xsl:when test="$isColForRepeatingEvent"><!--{repeating event }-->
										<xsl:choose>
											<xsl:when test="$colRepeatEventKey = $eventRepeatKey"><!--{event repeat match}-->
												<xsl:for-each select="./odm:FormData">
													<xsl:variable name="formOID" select="@FormOID"/><!--{formOID:<xsl:value-of select="$formOID"/>}-->
													<!-- find crf position -->
													<xsl:variable name="matchingCRFRef" select="$allEventDefs/odm:FormRef[@FormOID = $formOID and ../@OID = $eventOID]"/>
													<xsl:variable name="formRefNodeId" select="generate-id($matchingCRFRef)"/>
													<xsl:variable name="crfPosition">
														<xsl:for-each select="$allFormRefElements">
															<xsl:if test="@FormOID = $formOID">
																<xsl:if test="$formRefNodeId = generate-id()">
																	<xsl:copy-of select="position()" />
																</xsl:if>
															</xsl:if>
														</xsl:for-each>
													</xsl:variable>
													<!--{crfPosition:<xsl:value-of select="$crfPosition"/>}-->
													<xsl:choose>
													<xsl:when test="$crfPosition = normalize-space($colCrfPosition)"><!--{crf matched}-->
														<xsl:for-each select="./odm:ItemGroupData">
															<xsl:variable name="grpOID" select="@ItemGroupOID"/><!--{grp OID<xsl:value-of select="$grpOID"/>}-->
															<xsl:variable name="grpRepeatKey" select="@ItemGroupRepeatKey"/>
															<xsl:choose>
															<xsl:when test="$isColForRepeatingGrp"><!--{grp repeating}{grp RepeatKey:*<xsl:value-of select="$grpRepeatKey"/>*}{colRepeatGrpKey:*<xsl:value-of select="$colRepeatGrpKey"/>*}-->
															<xsl:choose>
																<xsl:when test="$grpRepeatKey = normalize-space($colRepeatGrpKey)"><!--{both event and grp repeating }-->
																		<!-- check item name -->
																		<xsl:for-each select="./odm:ItemData">
																			<xsl:variable name="itemOID" select="@ItemOID"/>
																			<xsl:variable name="itemName" select="//odm:ItemDef[@OID = $itemOID]/@Name"/>
																			<!-- @pgawade 14-May-2012 #13613 prepend the 'v$' before item name if it starts with some thing other then alphabet -->
																			<xsl:variable name="itemNameValidated">
																				<xsl:choose>
																					<xsl:when test='matches(substring($itemName, 1, 1), "[^A-Za-z]")'>
																						<xsl:value-of select="concat('v$', normalize-space($itemName))"/>
																					</xsl:when>
																					<xsl:otherwise>
																						<xsl:value-of select="$itemName"/>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:variable>
																			<xsl:choose>																			
																				<xsl:when test="normalize-space($colItemName) = $itemNameValidated"><!--{item name matched}-->
																					<!--<xsl:text>M_</xsl:text>--><xsl:value-of select="$matchSep"/><xsl:value-of select="$eventOID"/><xsl:value-of select="$mValSeparator1"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$formOID"/><xsl:value-of select="$mValSeparator2"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$grpOID"/><xsl:value-of select="$mValSeparator3"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$itemOID"/><xsl:value-of select="$mValSeparator4"/>	
																					<xsl:text>_</xsl:text><xsl:value-of select="$colRepeatEventKey"/><xsl:value-of select="$mValSeparator5"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$grpRepeatKey"/>																					
																				</xsl:when>
																				<xsl:otherwise>
																					<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
																				</xsl:otherwise>
																			</xsl:choose>
																		</xsl:for-each>
																</xsl:when>
																<xsl:otherwise>
																	<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
																</xsl:otherwise>
															</xsl:choose>													
															</xsl:when>
															<xsl:otherwise> 
																<!-- check item name -->
																<xsl:for-each select="./odm:ItemData">
																	<xsl:variable name="itemOID" select="@ItemOID"/>
																	<xsl:variable name="itemName" select="//odm:ItemDef[@OID = $itemOID]/@Name"/>
																	<!-- @pgawade 14-May-2012 #13613 prepend the 'v$' before item name if it starts with some thing other then alphabet -->
																			<xsl:variable name="itemNameValidated">
																				<xsl:choose>
																					<xsl:when test='matches(substring($itemName, 1, 1), "[^A-Za-z]")'>
																						<xsl:value-of select="concat('v$', normalize-space($itemName))"/>
																					</xsl:when>
																					<xsl:otherwise>
																						<xsl:value-of select="$itemName"/>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:variable>
																	<xsl:choose>
																		<xsl:when test="normalize-space($colItemName) = $itemNameValidated">
																			<!--<xsl:text>M_</xsl:text>--><xsl:value-of select="$matchSep"/><xsl:value-of select="$eventOID"/><xsl:value-of select="$mValSeparator1"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$formOID"/><xsl:value-of select="$mValSeparator2"/>																																										
																					<xsl:text>_</xsl:text><xsl:value-of select="$grpOID"/><xsl:value-of select="$mValSeparator3"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$itemOID"/>	<xsl:value-of select="$mValSeparator4"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$colRepeatEventKey"/>
																		</xsl:when>
																		<xsl:otherwise>
																			<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
																		</xsl:otherwise>
																	</xsl:choose>
																</xsl:for-each>
															</xsl:otherwise>
													</xsl:choose>
												</xsl:for-each>
													</xsl:when>
													<xsl:otherwise>
														<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
													</xsl:otherwise>
										</xsl:choose>
									
							</xsl:for-each>
											</xsl:when>
											<xsl:otherwise>
												<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>
									<xsl:otherwise><!--non-repeating event-->
										
										<xsl:for-each select="./odm:FormData">
										<xsl:variable name="formOID" select="@FormOID"/><!--formOID:<xsl:value-of select="$formOID"/>-->
										<!-- find crf position -->
										<!--<xsl:variable name="matchingEventDef" select="$allEventDefs[@OID = $eventOID]"/>	
										<xsl:variable name="matchingCRFRef" select="$matchingEventDef/odm:FormRef[@FormOID = $formOID]"/>-->
										<xsl:variable name="matchingCRFRef" select="$allEventDefs/odm:FormRef[@FormOID = $formOID and ../@OID = $eventOID]"/>
										
										<xsl:variable name="formRefNodeId" select="generate-id($matchingCRFRef)"/>
										<xsl:variable name="crfPosition">
											<xsl:for-each select="$allFormRefElements">
												<xsl:if test="@FormOID = $formOID">
													<xsl:if test="$formRefNodeId = generate-id()">
														<xsl:copy-of select="position()" />
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</xsl:variable>
										<!--crfPosition:<xsl:value-of select="$crfPosition"/>-->
										<xsl:choose>
											<xsl:when test="$crfPosition = normalize-space($colCrfPosition)"><!--crf matched-->
												<xsl:for-each select="./odm:ItemGroupData">
													<xsl:variable name="grpOID" select="@ItemGroupOID"/><!--grpOID:<xsl:value-of select="$grpOID"/>-->
													<xsl:variable name="grpRepeatKey" select="@ItemGroupRepeatKey"/><!--grpRepeatKey:*<xsl:value-of select="$grpRepeatKey"/>*-->
													<xsl:choose>
														<xsl:when test="$isColForRepeatingGrp">
															<xsl:choose>
																<xsl:when test="$grpRepeatKey = normalize-space($colRepeatGrpKey)">
																		<!-- check item name -->
																		<xsl:for-each select="./odm:ItemData">
																			<xsl:variable name="itemOID" select="@ItemOID"/>
																			<xsl:variable name="itemName" select="//odm:ItemDef[@OID = $itemOID]/@Name"/>
																			<!-- @pgawade 14-May-2012 #13613 prepend the 'v$' before item name if it starts with some thing other then alphabet -->
																			<xsl:variable name="itemNameValidated">
																				<xsl:choose>
																					<xsl:when test='matches(substring($itemName, 1, 1), "[^A-Za-z]")'>
																						<xsl:value-of select="concat('v$', normalize-space($itemName))"/>
																					</xsl:when>
																					<xsl:otherwise>
																						<xsl:value-of select="$itemName"/>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:variable>
																			<xsl:choose>
																				<xsl:when test="normalize-space($colItemName) = $itemNameValidated"><!-- only grp repeating --> 
																					<!--<xsl:text>M_</xsl:text>--><xsl:value-of select="$matchSep"/><xsl:value-of select="$eventOID"/><xsl:value-of select="$mValSeparator1"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$formOID"/><xsl:value-of select="$mValSeparator2"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$grpOID"/><xsl:value-of select="$mValSeparator3"/>
																					<xsl:text>_</xsl:text><xsl:value-of select="$itemOID"/><xsl:value-of select="$mValSeparator5"/>	
																					<xsl:text>_</xsl:text><xsl:value-of select="$grpRepeatKey"/>
																				</xsl:when>
																				<xsl:otherwise>
																					<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
																				</xsl:otherwise>
																			</xsl:choose>
																		</xsl:for-each>
																</xsl:when>
																<xsl:otherwise>
																	<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
																</xsl:otherwise>
															</xsl:choose>													
														</xsl:when>
														<xsl:otherwise>
															<!-- check item name -->
																<xsl:for-each select="./odm:ItemData">
																	<xsl:variable name="itemOID" select="@ItemOID"/><!--itemOID:<xsl:value-of select="$itemOID"/>-->
																	<xsl:variable name="itemName" select="//odm:ItemDef[@OID = $itemOID]/@Name"/><!--itemName:<xsl:value-of select="$itemName"/>-->
																	<!-- @pgawade 14-May-2012 #13613 prepend the 'v$' before item name if it starts with some thing other then alphabet -->
																			<xsl:variable name="itemNameValidated">
																				<xsl:choose>
																					<xsl:when test='matches(substring($itemName, 1, 1), "[^A-Za-z]")'>
																						<xsl:value-of select="concat('v$', normalize-space($itemName))"/>
																					</xsl:when>
																					<xsl:otherwise>
																						<xsl:value-of select="$itemName"/>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:variable>
																	<xsl:choose>
																		<xsl:when test="normalize-space($colItemName) = $itemNameValidated"><!-- nothing repeating -->
																			<!--<xsl:text>M_</xsl:text>--><xsl:value-of select="$matchSep"/><xsl:value-of select="$eventOID"/><xsl:value-of select="$mValSeparator1"/>
																			<xsl:text>_</xsl:text><xsl:value-of select="$formOID"/><xsl:value-of select="$mValSeparator2"/>
																			<xsl:text>_</xsl:text><xsl:value-of select="$grpOID"/><xsl:value-of select="$mValSeparator3"/>
																			<xsl:text>_</xsl:text><xsl:value-of select="$itemOID"/>																					
																		</xsl:when>
																		<xsl:otherwise>
																			<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
																		</xsl:otherwise>
																	</xsl:choose>
																</xsl:for-each>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:for-each>
											</xsl:when>
											<xsl:otherwise>
												<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
											</xsl:otherwise>
										</xsl:choose>
									
							</xsl:for-each>
									</xsl:otherwise>
								</xsl:choose>									
								</xsl:when>
								<xsl:otherwise>
									<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
								</xsl:otherwise>
							</xsl:choose>
							
						</xsl:for-each>	<!-- subjectEvents-->
						</xsl:variable>
						
					<!--ifMatch:<xsl:value-of select="$ifMatch"/>-->
					<xsl:choose>
					<xsl:when test="contains($ifMatch, $matchSep)">
							
							<xsl:variable name="eventOID" select="substring-before(substring-after($ifMatch, $matchSep), $mValSeparator1)"/>
							<!--eventOID:*<xsl:value-of select="$eventOID"/>*-->
							<!--<xsl:variable name="formOID" select="$ifMatchTokenized[3]"/>-->
							
							<xsl:variable name="formOID" select="substring-before(substring-after($ifMatch, concat($mValSeparator1,'_')), concat($mValSeparator2,'_'))"/>
							<!--formOID: *<xsl:value-of select="$formOID"/>*-->
							<xsl:variable name="grpOID" select="substring-before(substring-after($ifMatch, concat($mValSeparator2,'_')), concat($mValSeparator3,'_'))"/>
							<!--grpOID: *<xsl:value-of select="$grpOID"/>*-->
							<!--<xsl:variable name="itemOID" select="substring-before(substring-after($ifMatch, concat($mValSeparator3,'_')), concat($mValSeparator4,'_'))"/>-->
							
							<xsl:variable name="itemOID">
								<xsl:choose>
									<xsl:when test="$isColForRepeatingEvent"><!-- only event repeating or both event and grp repeating -->
										<xsl:value-of select="substring-before(substring-after($ifMatch, concat($mValSeparator3,'_')), concat($mValSeparator4,'_'))"/>
									</xsl:when>
									<xsl:when test="not($isColForRepeatingEvent) and $isColForRepeatingGrp">
										<xsl:value-of select="substring-before(substring-after($ifMatch, concat($mValSeparator3,'_')), concat($mValSeparator5,'_'))"/>
									</xsl:when>
									<xsl:otherwise><!-- nothing repeating -->
										<xsl:variable name="afterSep3" select="substring-after($ifMatch, concat($mValSeparator3,'_'))"/>
										<xsl:choose>
											<xsl:when test="contains($afterSep3,$nonMatchSep)">
												<xsl:value-of select="substring-before($afterSep3,$nonMatchSep)"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="$afterSep3"/>
											</xsl:otherwise>
										</xsl:choose>										
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<!--itemOID: *<xsl:value-of select="$itemOID"/>*-->
							<xsl:variable name="itemDataType" select="$allItemDefs[@OID = $itemOID]/@DataType"/>
							
							<xsl:variable name="eventRepeatKey">
								<xsl:if test="contains($ifMatch, $mValSeparator4)">
									<xsl:variable name="afterSep4" select="substring-after($ifMatch, concat($mValSeparator4,'_'))"/>
									<xsl:choose>
										<xsl:when test="contains($afterSep4, $nonMatchSep)">
											<xsl:variable name="beforeN" select="substring-before($afterSep4, $nonMatchSep)"/>
											<xsl:choose>
												<xsl:when test="contains($beforeN, $mValSeparator5)">
													<xsl:value-of select="substring-before($beforeN, $mValSeparator5)"/>
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of select="$beforeN"/>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:when>
										<xsl:otherwise>
											<xsl:choose>
												<xsl:when test="contains($afterSep4, $mValSeparator5)">
													<xsl:value-of select="substring-before($afterSep4, concat($mValSeparator5,'_'))"/>
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of select="$afterSep4"/>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>								
							</xsl:variable>
							<!-- *<xsl:value-of select="$eventRepeatKey"/>*-->
							<xsl:variable name="grpRepeatKey">
								<xsl:if test="contains($ifMatch, $mValSeparator5)">
									<xsl:variable name="afterSep5" select="substring-after($ifMatch, concat($mValSeparator5,'_'))"/>
									<xsl:choose>
										<xsl:when test="contains($afterSep5, $nonMatchSep)">
											<xsl:value-of select="substring-before($afterSep5, $nonMatchSep)"/>											
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$afterSep5"/>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>								
							</xsl:variable>
							<!--grpRepeatKey: *<xsl:value-of select="$grpRepeatKey"/>*-->
							
							<xsl:choose>	
								<xsl:when test="$isColForRepeatingEvent and $isColForRepeatingGrp">
									<xsl:variable name="itemData" select="$subjectItems[@ItemOID = $itemOID  
									 and ../@ItemGroupOID=$grpOID  and ../@ItemGroupRepeatKey = $grpRepeatKey
									 and ../../@FormOID = $formOID 
									 and ../../../@StudyEventOID = $eventOID and ../../../@StudyEventRepeatKey = $eventRepeatKey]"/>
									 <!--itemData oid:<xsl:value-of select="$itemData/@ItemOID"/>-->
									<xsl:choose>
										<xsl:when test="$itemData/@Value">
											<!-- @pgawade 22-Mar-2012 #12213 convert ISO 8601 format date into american date -->
											<xsl:choose>
												<xsl:when test="$itemDataType = 'date'">
													<xsl:call-template name="convert-date-ISO8601-to-ADate">
														<xsl:with-param name="dateSrc" select="$itemData/@Value"/>
													</xsl:call-template>
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of select="$itemData/@Value" />
												</xsl:otherwise>
											</xsl:choose>
											
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:when test="$itemData/@OpenClinica:ReasonForNull">
											<xsl:value-of select="$itemData/@OpenClinica:ReasonForNull" />
											<xsl:value-of select="$delimiter" />												
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose> 
								</xsl:when>						
								<xsl:when test="$isColForRepeatingEvent and not($isColForRepeatingGrp)">
									<xsl:variable name="itemData" select="$subjectItems[@ItemOID = $itemOID  
									 and ../@ItemGroupOID=$grpOID
									 and ../../@FormOID = $formOID 
									 and ../../../@StudyEventOID = $eventOID and ../../../@StudyEventRepeatKey = $eventRepeatKey]"/>
									 <!--itemData oid:<xsl:value-of select="$itemData/@ItemOID"/>-->
									<xsl:choose>
										<xsl:when test="$itemData/@Value">										
											<!-- @pgawade 22-Mar-2012 #12213 convert ISO 8601 format date into american date -->
											<xsl:choose>
												<xsl:when test="$itemDataType = 'date'">
													<xsl:call-template name="convert-date-ISO8601-to-ADate">
														<xsl:with-param name="dateSrc" select="$itemData/@Value"/>
													</xsl:call-template>
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of select="$itemData/@Value" />
												</xsl:otherwise>
											</xsl:choose>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:when test="$itemData/@OpenClinica:ReasonForNull">
											<xsl:value-of select="$itemData/@OpenClinica:ReasonForNull" />
											<xsl:value-of select="$delimiter" />												
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose> 
								</xsl:when>
								<xsl:when test="not($isColForRepeatingEvent) and $isColForRepeatingGrp">
									<xsl:variable name="itemData" select="$subjectItems[@ItemOID = $itemOID  
									 and ../@ItemGroupOID=$grpOID  and ../@ItemGroupRepeatKey = $grpRepeatKey
									 and ../../@FormOID = $formOID 
									 and ../../../@StudyEventOID = $eventOID]"/>
									<!-- itemData oid:<xsl:value-of select="$itemData/@ItemOID"/>-->
									<xsl:choose>
										<xsl:when test="$itemData/@Value">
											<!-- @pgawade 22-Mar-2012 #12213 convert ISO 8601 format date into american date -->
											<xsl:choose>
												<xsl:when test="$itemDataType = 'date'">
													<xsl:call-template name="convert-date-ISO8601-to-ADate">
														<xsl:with-param name="dateSrc" select="$itemData/@Value"/>
													</xsl:call-template>
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of select="$itemData/@Value" />
												</xsl:otherwise>
											</xsl:choose>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:when test="$itemData/@OpenClinica:ReasonForNull">
											<xsl:value-of select="$itemData/@OpenClinica:ReasonForNull" />
											<xsl:value-of select="$delimiter" />												
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose> 	
								</xsl:when>
								<xsl:otherwise>
									<xsl:variable name="itemData" select="$subjectItems[@ItemOID = $itemOID  
									 and ../@ItemGroupOID=$grpOID 
									 and ../../@FormOID = $formOID 
									 and ../../../@StudyEventOID = $eventOID]"/>
									<!-- itemData oid:<xsl:value-of select="$itemData/@ItemOID"/>-->
									<xsl:choose>
										<xsl:when test="$itemData/@Value">
											<!-- @pgawade 22-Mar-2012 #12213 convert ISO 8601 format date into american date -->
											<xsl:choose>
												<xsl:when test="$itemDataType = 'date'">
													<xsl:call-template name="convert-date-ISO8601-to-ADate">
														<xsl:with-param name="dateSrc" select="$itemData/@Value"/>
													</xsl:call-template>
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of select="$itemData/@Value" />
												</xsl:otherwise>
											</xsl:choose>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:when test="$itemData/@OpenClinica:ReasonForNull">
											<xsl:value-of select="$itemData/@OpenClinica:ReasonForNull" />
											<xsl:value-of select="$delimiter" />												
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose> 
								</xsl:otherwise>
							</xsl:choose>
							
								
							
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$delimiter" />					
					</xsl:otherwise>
					</xsl:choose>	
				</xsl:when>
				<xsl:otherwise><!--crf column -->
				<!-- iterate study events. for each study event; if repeating - check if crf in column name is present and event info matched as well. If yes write the data. 
																						same for non-repeating except the event repeat key will not be matched. if matched write data else write empty column-->	
					<xsl:variable name="ifMatch">
						<xsl:for-each select="$subjectEvents">
							<xsl:variable name="eventOID" select="@StudyEventOID" /><!--eventOID:<xsl:value-of select="$eventOID"/>-->
							<xsl:variable name="eventPosition">
								<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
									<xsl:if test="@OID = $eventOID">
										<xsl:copy-of select="position()" />
									</xsl:if>	
								</xsl:for-each>				
							</xsl:variable><!--eventPosition: *<xsl:value-of select="$eventPosition"/>*-->
							<xsl:choose>
								<xsl:when test="$colEventPosition = $eventPosition"><!--event matched-->
									<xsl:for-each select="./odm:FormData">
										<xsl:variable name="formOID" select="@FormOID"/><!--formOID:<xsl:value-of select="$formOID"/>-->
										<!-- find crf position -->
										<xsl:variable name="matchingCRFRef" select="$allEventDefs/odm:FormRef[@FormOID = $formOID and ../@OID = $eventOID]"/>
										<!--matchingCRFRef OID: <xsl:value-of select="$matchingCRFRef/@FormOID"/>-->
										<xsl:variable name="formRefNodeId" select="generate-id($matchingCRFRef)"/>
										<xsl:variable name="crfPosition">
											<xsl:for-each select="$allFormRefElements">
												<xsl:if test="@FormOID = $formOID">
													<xsl:if test="$formRefNodeId = generate-id()">
														<xsl:copy-of select="position()" />
													</xsl:if>
												</xsl:if>
											</xsl:for-each>
										</xsl:variable><!--crfPosition: *<xsl:value-of select="$crfPosition"/>*-->
										
										<xsl:choose>
											<xsl:when test="$crfPosition = normalize-space($colCrfPosition)"><!--crf matched-->
												<xsl:choose>
													<xsl:when test="$isColForRepeatingEvent"><!--col for repeating event -->
														<xsl:choose>
															<xsl:when test="../@StudyEventRepeatKey = normalize-space($colRepeatEventKey)">event repeat key matched
																<!--<xsl:text>M_</xsl:text>--><xsl:value-of select="$matchSep"/><xsl:value-of select="$eventOID"/><xsl:value-of select="$mValSeparator1"/><xsl:value-of select="$formOID"/><xsl:value-of select="$mValSeparator2"/><xsl:value-of select="../@StudyEventRepeatKey"/>
															</xsl:when>
															<xsl:otherwise><!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/><!--event repeat key mismatch-->
															</xsl:otherwise>
														</xsl:choose>										
													</xsl:when>									
													<xsl:otherwise><!--<xsl:text>M_</xsl:text>--><xsl:value-of select="$matchSep"/><xsl:value-of select="$eventOID"/><xsl:value-of select="$mValSeparator1"/><xsl:value-of select="$formOID"/>
														<!--match for non-repeating event-->
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
											<xsl:otherwise><!--crf mismatch-->
												<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/>
											</xsl:otherwise>			
										</xsl:choose>
									</xsl:for-each>
								</xsl:when>
								<xsl:otherwise>
									<!--<xsl:text>N</xsl:text>--><xsl:value-of select="$nonMatchSep"/><!--event mismatch-->
								</xsl:otherwise>
							</xsl:choose>
							
						</xsl:for-each>	
					</xsl:variable>
					<!--<xsl:variable name="ifMatch" select="'NN'"/>-->
				<!--{ifMatch: <xsl:value-of select="$ifMatch"/>}-->
					<xsl:choose>
						<xsl:when test="contains($ifMatch, $matchSep)">					 
							<!--<xsl:variable name="ifMatchTokenized" select="tokenize($ifMatch,'_')"/>-->
							<!--<xsl:variable name="eventOID" select="$ifMatchTokenized[2]"/>-->
							<xsl:variable name="eventOID" select="substring-before(substring-after($ifMatch, $matchSep), $mValSeparator1)"/>
							<!--eventOID:*<xsl:value-of select="$eventOID"/>*-->
							<!--<xsl:variable name="formOID" select="$ifMatchTokenized[3]"/>-->
							<xsl:variable name="formOID">
								<xsl:choose>
									<xsl:when test="$isColForRepeatingEvent">
										<xsl:value-of select="substring-before(substring-after($ifMatch, $mValSeparator1), $mValSeparator2)"/>
									</xsl:when>
									<xsl:otherwise>
										<xsl:variable name="afterSep1" select="substring-after($ifMatch, $mValSeparator1)"/>
										<xsl:choose>
											<xsl:when test="contains($afterSep1, $nonMatchSep)">
												<xsl:value-of select="substring-before($afterSep1, $nonMatchSep)"/>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="$afterSep1"/>
											</xsl:otherwise>
										</xsl:choose>
										
									</xsl:otherwise>
								</xsl:choose>
							</xsl:variable>
							<!--formOID: *<xsl:value-of select="$formOID"/>*-->
							<xsl:variable name="eventRepeatKey" >	
								<xsl:if test="$isColForRepeatingEvent">
									<xsl:variable name="afterSep1" select="substring-after($ifMatch,$mValSeparator2 )"/>
									<xsl:choose>
										<xsl:when test="contains($afterSep1, $nonMatchSep)">
											<xsl:value-of select="substring-before($afterSep1, $nonMatchSep)"/>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$afterSep1"/>
										</xsl:otherwise>
									</xsl:choose>
									
								</xsl:if>
							</xsl:variable><!--eventRepeatKey: <xsl:value-of  select="$eventRepeatKey"/>-->
							<xsl:variable name="eventT" select="$subjectEvents[@StudyEventOID = $eventOID]"/>
							
						<xsl:choose>
							<xsl:when test="$isColForRepeatingEvent">
							<xsl:variable name="formData" select="$subjectEvents/odm:FormData[@FormOID = $formOID and ../@StudyEventOID = $eventOID and ../@StudyEventRepeatKey = $eventRepeatKey]"/>
								<xsl:if test="$colType = 'Interviewer'">
									<xsl:choose>
										<xsl:when test="$formData/@OpenClinica:InterviewerName">
											<xsl:value-of select="$formData/@OpenClinica:InterviewerName"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />	
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>
								<xsl:if test="$colType = 'InterviewDate'">
									<xsl:choose>
										<xsl:when test="$formData/@OpenClinica:InterviewDate">
											<!-- @pgawade 21-Mar-2012 #12213 convert ISO 8601 format date into american date -->
											<!--<xsl:value-of select="$formData/@OpenClinica:InterviewDate"></xsl:value-of>-->
											<xsl:call-template name="convert-date-ISO8601-to-ADate">
												<xsl:with-param name="dateSrc" select="$formData/@OpenClinica:InterviewDate"/>
											</xsl:call-template>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />	
										</xsl:otherwise>
									</xsl:choose>									
								</xsl:if>
								<xsl:if test="$colType = 'CRFVersionStatus'">	
									<xsl:choose> 
										<xsl:when test="$formData/@OpenClinica:Status">	
											<xsl:value-of select="$formData/@OpenClinica:Status"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose>						
								</xsl:if>
								<xsl:if test="$colType = 'VersionName'">
									<xsl:choose>
										<xsl:when test="$formData/@OpenClinica:Version">
											<xsl:value-of select="$formData/@OpenClinica:Version"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />	
										</xsl:otherwise>
									</xsl:choose>										
								</xsl:if>	
							</xsl:when>
							<xsl:otherwise>
								<xsl:variable name="formData" select="$subjectEvents/odm:FormData[@FormOID = $formOID and ../@StudyEventOID = $eventOID]"/>
								
								<!--formData oid: *<xsl:value-of select="$formData/@FormOID"/>*-->
								<xsl:if test="$colType = 'Interviewer'">
									<xsl:choose>
										<xsl:when test="$formData/@OpenClinica:InterviewerName">
											<xsl:value-of select="$formData/@OpenClinica:InterviewerName"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>
								<xsl:if test="$colType = 'InterviewDate'">
									<xsl:choose>
										<xsl:when test="$formData/@OpenClinica:InterviewDate">
										<!-- @pgawade 21-Mar-2012 #12213 convert ISO 8601 format date into american date -->
										<!--<xsl:value-of select="$formData/@OpenClinica:InterviewDate"></xsl:value-of>-->
										<xsl:call-template name="convert-date-ISO8601-to-ADate">
											<xsl:with-param name="dateSrc" select="$formData/@OpenClinica:InterviewDate"/>
										</xsl:call-template>
											
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose>									
								</xsl:if>
								<xsl:if test="$colType = 'CRFVersionStatus'">	
									<xsl:choose> 
										<xsl:when test="$formData/@OpenClinica:Status">	
											<xsl:value-of select="$formData/@OpenClinica:Status"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose>						
								</xsl:if>
								<xsl:if test="$colType = 'VersionName'">
									<xsl:choose>
										<xsl:when test="$formData/@OpenClinica:Version">
											<xsl:value-of select="$formData/@OpenClinica:Version"></xsl:value-of>
											<xsl:value-of select="$delimiter" />
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$delimiter" />
										</xsl:otherwise>
									</xsl:choose>										
								</xsl:if>	
							</xsl:otherwise>
						</xsl:choose>
									
														
						
</xsl:when>
						<xsl:otherwise>
						<xsl:if test="$colType = 'Interviewer'">										
							<xsl:value-of select="$delimiter" />											
						</xsl:if>
						<xsl:if test="$colType = 'InterviewDate'">							
							<xsl:value-of select="$delimiter" />																	
						</xsl:if>
						<xsl:if test="$colType = 'CRFVersionStatus'">
							<xsl:value-of select="$delimiter" />														
						</xsl:if>
						<xsl:if test="$colType = 'VersionName'">							
							<xsl:value-of select="$delimiter" />																			
						</xsl:if>						
					</xsl:otherwise>
					</xsl:choose>																	
				
				</xsl:otherwise>
				
			</xsl:choose>
		</xsl:if>	
		</xsl:for-each>
							
	</xsl:template>
	
	<!-- @pagwade 21-Mar-2012 #12213 template to convert ISO 8601 (YYYY-MM-DD) date value to american date (MM/DD/YYYY) -->
	<xsl:template name="convert-date-ISO8601-to-ADate">
		<xsl:param name="dateSrc"/>
		
		<xsl:variable name="sepDateSrc" select="'-'" />
		<xsl:variable name="sepDateOp" select="'/'" />
		
		<xsl:choose>
			<xsl:when test="contains($dateSrc, $sepDateSrc)">				
				<xsl:variable name="year">
					<xsl:value-of select="substring-before($dateSrc,$sepDateSrc)"/>
				</xsl:variable>
				<xsl:variable name="afterSepDateSrc">
					<xsl:value-of select="substring-after($dateSrc,$sepDateSrc)"/>
				</xsl:variable>
				<xsl:choose>
					<xsl:when test="contains($afterSepDateSrc, $sepDateSrc)">							
							<xsl:variable name="month" select="substring-before($afterSepDateSrc,$sepDateSrc)"/>
							<xsl:variable name="date" select="substring-after($afterSepDateSrc,$sepDateSrc)"/>			
							<xsl:value-of select="$month"/><xsl:value-of select="$sepDateOp"/><xsl:value-of select="$date"/><xsl:value-of select="$sepDateOp"/><xsl:value-of select="$year"/>											
					</xsl:when>					
					<xsl:otherwise >
						<xsl:value-of select="$err-msg-invalid-date"/>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>	
			<xsl:otherwise >
				<xsl:value-of select="$err-msg-invalid-date"/>
			</xsl:otherwise>
		</xsl:choose>
		
		
	</xsl:template>	
</xsl:stylesheet>