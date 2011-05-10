<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet version="2.0"
	xmlns:odm="http://www.cdisc.org/ns/odm/v1.3" xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
	xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance" xmlns:def="http://www.cdisc.org/ns/def/v1.0"
	xmlns:xlink="http://www.w3c.org/1999/xlink" xmlns:exsl="http://exslt.org/common"
	extension-element-prefixes="exsl"
	xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1"
	xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
	xsi:schemaLocation="http://www.cdisc.org/ns/odm/v1.3 ">
	<xsl:output method="text" version="4.0" encoding="utf-8"
		indent="no" />
	<xsl:variable name="delimiter" select="'&#x09;'" />
	<!--E to represent Events -->
	<xsl:variable name="E" select="'E'" />
	<!--C to represent CRFS -->
	<xsl:variable name="C" select="'C'" />
	<xsl:variable name="A" select="A" />
	<xsl:variable name="ADATE" select="ADATE" />
	<xsl:key name="studyEvents" match="odm:StudyEventData" use="@StudyEventOID"></xsl:key>
	<xsl:key name="eventCRFs" match="odm:FormData" use="@FormOID"></xsl:key>

	<xsl:variable name="crfVersionExist" select="//odm:FormData/@OpenClinica:Version" />
	<xsl:variable name="interviewerNameExist"
		select="//odm:FormData/@OpenClinica:InterviewerName" />
	<xsl:variable name="interviewDateExist"
		select="//odm:FormData/@OpenClinica:InterviewDate" />
	<xsl:variable name="crfStatusExist" select="//odm:FormData/@OpenClinica:Status" />
	
	<xsl:variable name="sexExist" select="//odm:SubjectData/@OpenClinica:Sex" />
	<xsl:variable name="uniqueIdExist"
		select="//odm:SubjectData/@OpenClinica:UniqueIdentifier" />
	<xsl:variable name="dobExist"
		select="//odm:SubjectData/@OpenClinica:DateOfBirth" />
	<xsl:variable name="subjectStatusExist" select="//odm:SubjectData/@OpenClinica:Status" />
	
	<xsl:variable name="allStudyEventDataElements"
		select="//odm:StudyEventData" />
	<xsl:variable name="allFormRefElements"
		select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef/odm:FormRef" />
		<xsl:variable name="allSubjects"
		select="//odm:ODM/odm:ClinicalData/odm:SubjectData" />
	<xsl:template match="/">
<xsl:variable name="crfPosition" select="position()"/>
		<xsl:variable name="seperator" select="'&#xa;'"></xsl:variable>
		<xsl:text>* NOTE: If you have put this file in a different folder </xsl:text><xsl:value-of select="$seperator" />
		<xsl:text>* from the associated data file, you will have to change the FILE </xsl:text><xsl:value-of select="$seperator" />
		<xsl:text>* location on the line below to point to the physical location of your data file.</xsl:text>
		<xsl:value-of select="$seperator"/>
		<xsl:text>GET DATA  /TYPE = TXT/FILE = 'SPSS_DAT</xsl:text>
		<!--'All_Items_SPSS_data_spss.dat'-->
		<!--<xsl:value-of select="current-dateTime()"/>-->
		<xsl:variable name="currentDate" select="current-date()"/>
		<xsl:variable name="currentDateTime" select="current-dateTime()"/>
		<!--<xsl:value-of select="format-date($currentDate, '[Y0001]-[M01]-[D01]')"/>-->
		<xsl:value-of select="format-dateTime($currentDateTime, '[Y0001]-[M01]-[D01]-[H01][m01][s00001]')"/>

		<xsl:text>.dat' /DELCASE = LINE /DELIMITERS = "\t" /ARRANGEMENT = DELIMITED /FIRSTCASE = 2 /IMPORTCASE = ALL /VARIABLES =</xsl:text>
		<xsl:value-of select="$seperator" />
		
		<xsl:call-template name="subjectDataColumnHeaders"/>
		<!--<xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[1]"
			mode="studyDataSPSS"></xsl:apply-templates>-->
			<xsl:apply-templates mode="studyDataSPSS2" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef"/>

		<!--<xsl:apply-templates mode="formDataSPSS" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID)[1])]">
			<xsl:with-param name="crfPosition" select="$crfPosition"></xsl:with-param>
			</xsl:apply-templates>-->
		<xsl:apply-templates mode="studyFormAndDataItemsHeaders" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef"/>
			
		<xsl:text>.</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>VARIABLE LABELS</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<xsl:call-template name="subjectDataColumnSPSS"></xsl:call-template>
		<!--xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]"
			mode="studyEventDataColumnSPSS" />-->
	
			<xsl:apply-templates
			select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef"
			mode="studyEventDataColumnSPSS2" />

		<!--<xsl:apply-templates mode="itemDataValuesSPSS" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID)[1])]"/>-->
		<xsl:apply-templates mode="studyFormAndDataItemsHeaders" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
			<xsl:with-param name="calledFor" select="'itemDataValuesSPSS'"/>
		</xsl:apply-templates>
		
		<xsl:text>.</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>VALUE LABELS</xsl:text>
		<xsl:text>&#xa;</xsl:text>
					
		<xsl:apply-templates mode="studyItemDataColumnHeaders" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">			
			<xsl:with-param name="eventOID"/>	
			<xsl:with-param name="isEventRepeating" select="@Repeating"/>	
			<xsl:with-param name="calledFor" select="'forCodeListsOnly'"/>
		</xsl:apply-templates>	
<!-- 
		<xsl:text>.</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>VALUE LABELS</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<xsl:apply-templates select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData" mode="allColumnsandLabels"/>-->
		  <xsl:text>.</xsl:text>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text>EXECUTE.</xsl:text>
	</xsl:template>

<!--	
	<xsl:template mode="studyDataSPSS" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:variable name="eventOID" select="@StudyEventOID" />
		<xsl:variable name="eventPosition" select="position()" />
		<xsl:variable name="locationLen"
			select="string-length(@OpenClinica:StudyEventLocation)" />
		<xsl:variable name="eventStatusLen" select="string-length(@OpenClinica:Status)" />
		<xsl:variable name="ageLen"
			select="string-length(@OpenClinica:SubjectAgeAtEvent)" />
			
			<xsl:variable name="eventLocationExist"
		select="@OpenClinica:StudyEventLocation" />
	<xsl:variable name="eventStartDateExist"
		select="@OpenClinica:StartDate" />
	<xsl:variable name="eventEndDateExist"
		select="@OpenClinica:EndDate" />
	<xsl:variable name="eventStatusExist"
		select="@OpenClinica:Status" />
	<xsl:variable name="ageExist"
		select="@OpenClinica:SubjectAgeAtEvent" />
		<xsl:choose>
			<xsl:when test="@StudyEventRepeatKey">
				<xsl:variable name="allStudyEvents">
					<xsl:for-each select="//odm:StudyEventData[@StudyEventOID=$eventOID]">
						<xsl:sort select="@StudyEventRepeatKey" data-type="number" />
						<xsl:copy-of select="." />
					</xsl:for-each>
				</xsl:variable>

				<xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
					<xsl:choose>
						<xsl:when test="position()=1">
							<xsl:if test="$eventLocationExist">
								<xsl:text>Location_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> A</xsl:text>
								<xsl:choose>
									<xsl:when test="$locationLen &gt; 8">
										<xsl:text>8</xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$locationLen" />
									</xsl:otherwise>
								</xsl:choose>
								<xsl:text>&#xa;</xsl:text>
						</xsl:if>

							<xsl:if test="$eventStartDateExist">
								<xsl:text>StartDate_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> ADATE10</xsl:text>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>

							<xsl:if test="$eventEndDateExist">
								<xsl:text>EndDate_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> ADATE10</xsl:text>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>

							<xsl:if test="$eventStatusExist">
								<xsl:text>Event Status_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> A</xsl:text>
								<xsl:choose>
									<xsl:when test="$eventStatusLen &gt; 8">
										<xsl:text>8</xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$eventStatusLen" />
									</xsl:otherwise>
								</xsl:choose>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>

							<xsl:if test="$ageExist">
								<xsl:text>Age_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> A</xsl:text>
								<xsl:choose>
									<xsl:when test="$ageLen &gt; 8">
										<xsl:text>8</xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$ageLen" />
									</xsl:otherwise>
								</xsl:choose>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if
								test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">
								
								<xsl:if test="$eventLocationExist">
									<xsl:text>Location_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> A</xsl:text>
									<xsl:choose>
										<xsl:when test="$locationLen &gt; 8">
											<xsl:text>8</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$locationLen" />
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text>&#xa;</xsl:text>
							</xsl:if>

								<xsl:if test="$eventStartDateExist">
									<xsl:text>StartDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> ADATE10</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$eventEndDateExist">
									<xsl:text>EndDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> ADATE10</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$eventStatusExist">
									<xsl:text>Event Status_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> A</xsl:text>
									<xsl:choose>
										<xsl:when test="$eventStatusLen &gt; 8">
											<xsl:text>8</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$eventStatusLen" />
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$ageExist">
									<xsl:text>Age_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> A</xsl:text>
									<xsl:choose>
										<xsl:when test="$ageLen &gt; 8">
											<xsl:text>8</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$ageLen" />
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="$eventLocationExist">
					<xsl:text>Location_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> A</xsl:text>
					<xsl:choose>
						<xsl:when test="$locationLen &gt; 8">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$locationLen" />
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>
				

				<xsl:if test="$eventStartDateExist">
					<xsl:text>StartDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> ADATE10</xsl:text>
					<xsl:text>&#xa;</xsl:text>
					<xsl:text>StartDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> ADATE10</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$eventEndDateExist">
					<xsl:text>EndDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> ADATE10</xsl:text>
					<xsl:text>&#xa;</xsl:text>
					<xsl:text>EndDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> ADATE10</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$eventStatusExist">
					<xsl:text>Event Status_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> A</xsl:text>
					<xsl:choose>
						<xsl:when test="$eventStatusLen &gt; 8">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$eventStatusLen" />
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$ageExist">
					<xsl:text>Age_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> A</xsl:text>
					<xsl:choose>
						<xsl:when test="$ageLen &gt; 8">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$ageLen" />
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
-->
	<!--
	<xsl:template mode="formDataSPSS" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<xsl:param name="crfPosition"/>
		<xsl:variable name="parentEvent"
			select="../odm:StudyEventData[odm:FormData[@FormOID=@FormOID]]" />
		<xsl:variable name="interLen"
			select="string-length(@OpenClinica:InterviewerName)" />
		<xsl:variable name="interStatusLen" select="string-length(@OpenClinica:Status)" />
		<xsl:variable name="versionLen" select="string-length(@OpenClinica:Version)" />
		<xsl:apply-templates mode="firstStudyEventData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="parentEvent" select="$parentEvent"></xsl:with-param>
			<xsl:with-param name="interLen" select="$interLen"></xsl:with-param>
			<xsl:with-param name="interStatusLen" select="$interStatusLen"></xsl:with-param>
			<xsl:with-param name="versionLen" select="$versionLen"></xsl:with-param>

		</xsl:apply-templates>
		<xsl:apply-templates select="odm:ItemGroupData/odm:ItemData"
			mode="itemDataSPSS">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="@FormOID" />
			<xsl:with-param name="parentEvent" select="$parentEvent"></xsl:with-param>
	
			
		</xsl:apply-templates>
		
	</xsl:template>
	
	<xsl:template mode="itemDataSPSS" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">
		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
	
		<xsl:param name="parentEvent" />
		<xsl:variable name="itemData" select="." />
		<xsl:variable name="itemOID" select="@ItemOID" />
		<xsl:variable name="valueLength" select="string-length(@Value)" />
		<xsl:variable name="anotherPost" select="position()"></xsl:variable>
		<xsl:apply-templates
			select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]"
			mode="itemDefSPSS">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
			<xsl:with-param name="itemData" select="$itemData" />
			<xsl:with-param name="itemOID" select="$itemOID" />
			<xsl:with-param name="valueLength" select="$valueLength"></xsl:with-param>
			<xsl:with-param name="parentEvent" select="$parentEvent"/>
			
		</xsl:apply-templates>

	</xsl:template>

	<xsl:template mode="itemDefSPSS" match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID]">
		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
		<xsl:param name="itemData" />
		<xsl:param name="itemOID" />
		<xsl:param name="valueLength" />
		<xsl:param name="eventPosition"/>
		<xsl:variable name="formOID"
			select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID" />
		<xsl:if test="$currentFormOID = $formOID">
			<xsl:value-of select="@Name" />
			<xsl:text>_</xsl:text>
			
			<xsl:value-of select="$C" />
			<xsl:value-of select="$crfPosition" />
			<xsl:variable name="group" select="$itemData/parent::node()" />
			<xsl:variable name="groupOID" select="$group/@ItemGroupOID" />
			<xsl:if test="$group/@ItemGroupRepeatKey">
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$group/@ItemGroupRepeatKey" />
			</xsl:if>
			<xsl:variable name="dataType" select="@DataType" />
			<xsl:choose>
				<xsl:when test="$dataType='date'">
					<xsl:text> ADATE10</xsl:text>
				</xsl:when>
				<xsl:when test="$dataType='floating'">
					<xsl:text> F8.2</xsl:text>
				</xsl:when>
				<xsl:when test="$dataType='integer'">
					<xsl:text> F8.0</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text> A</xsl:text>
					<xsl:choose>
						<xsl:when test="$valueLength &gt; 8 ">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$valueLength" />
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xa;</xsl:text>

		</xsl:if>
	</xsl:template>
	<xsl:template mode="itemGroupDefSPSS" match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef[@OID]">

		<xsl:if test="@Name !='Ungrouped'">
			<xsl:value-of select="@Name" />
		</xsl:if>
	</xsl:template>

	<xsl:template mode="firstStudyEventData" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:param name="crfPosition" />
		<xsl:param name="parentEvent" />
		<xsl:param name="interLen" />
		<xsl:param name="interStatusLen" />
		<xsl:param name="versionLen" />
	<xsl:variable name="crfVersionExist" select="odm:FormData[@OpenClinica:Version]" />
	<xsl:variable name="interviewerNameExist"
		select="odm:FormData[@OpenClinica:InterviewerName]" />
	<xsl:variable name="interviewDateExist"
		select="odm:FormData[@OpenClinica:InterviewDate]" />
	<xsl:variable name="crfStatusExist" select="odm:FormData/@OpenClinica:Status" />

		<xsl:variable name="eventPosition" select="position()" />
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
								<xsl:if test="$interviewerNameExist">
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
									<xsl:text> A</xsl:text>
									<xsl:choose>
										<xsl:when test="$interLen &gt; 8">
											<xsl:text>8</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$interLen" />
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text>&#xa;</xsl:text>
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
									<xsl:text> ADATE10</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$crfStatusExist">
									<xsl:text>CRF Version Status_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
									</xsl:if>
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:text> A</xsl:text>
									<xsl:choose>
										<xsl:when test="$interStatusLen &gt; 8">
											<xsl:text>8</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$interStatusLen" />
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$crfVersionExist">
									<xsl:text>Version Name_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
									</xsl:if>
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:text> A</xsl:text>
									<xsl:choose>
										<xsl:when test="$versionLen &gt; 8">
											<xsl:text>8</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$versionLen" />
										</xsl:otherwise>
									</xsl:choose>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if
									test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">
									<xsl:text>Interviewer_</xsl:text>
									<xsl:if test="$interviewerNameExist">
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
										<xsl:text> A</xsl:text>
										<xsl:choose>
											<xsl:when test="$interLen &gt; 8">
												<xsl:text>8</xsl:text>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="$interLen" />
											</xsl:otherwise>
										</xsl:choose>
										<xsl:text>&#xa;</xsl:text>
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
										<xsl:text> ADATE10</xsl:text>
										<xsl:text>&#xa;</xsl:text>
									</xsl:if>

									<xsl:if test="$crfStatusExist">
										<xsl:text>CRF Version Status_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:text> A</xsl:text>
										<xsl:choose>
											<xsl:when test="$interStatusLen &gt; 8">
												<xsl:text>8</xsl:text>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="$interStatusLen" />
											</xsl:otherwise>
										</xsl:choose>
										<xsl:text>&#xa;</xsl:text>
									</xsl:if>

									<xsl:if test="$crfVersionExist">
										<xsl:text>Version Name_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:text> A</xsl:text>
										<xsl:choose>
											<xsl:when test="$versionLen &gt; 8">
												<xsl:text>8</xsl:text>
											</xsl:when>
											<xsl:otherwise>
												<xsl:value-of select="$versionLen" />
											</xsl:otherwise>
										</xsl:choose>
										<xsl:text>&#xa;</xsl:text>
									</xsl:if>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$interviewerNameExist">
						<xsl:text>Interviewer_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> A</xsl:text>
						<xsl:choose>
							<xsl:when test="$interLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$interLen" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
					<xsl:if test="$interviewDateExist">
						<xsl:text>Interviewer date_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> ADATE10</xsl:text>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
					<xsl:if test="$crfStatusExist">
						<xsl:text>CRF Version Status_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> A</xsl:text>
						<xsl:choose>
							<xsl:when test="$interStatusLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$interStatusLen" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>

					<xsl:if test="$crfVersionExist">
						<xsl:text>Version Name_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> A</xsl:text>
						<xsl:choose>
							<xsl:when test="$versionLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$versionLen" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		
		
	</xsl:template>
-->
	<xsl:template mode="studyDataSPSS2" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
	
	<xsl:variable name="eventDefOID" select="@OID"/>
	<xsl:variable name="isRepeating" select="@Repeating"/>
	<xsl:variable name="allStudyEventDataElements" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID = $eventDefOID]"/>	
	<xsl:variable name="eventLocationExist" select="count($allStudyEventDataElements[@OpenClinica:StudyEventLocation]) &gt; 0"/>
	<xsl:variable name="eventStartDateExist" select="count($allStudyEventDataElements[@OpenClinica:StartDate]) &gt; 0"/>
	<xsl:variable name="eventStatusExist" select="count($allStudyEventDataElements[@OpenClinica:Status]) &gt; 0"/>
	<xsl:variable name="eventEndDateExist" select="count($allStudyEventDataElements[@OpenClinica:EndDate]) &gt; 0"/>
	<xsl:variable name="ageExist" select="count($allStudyEventDataElements[@OpenClinica:SubjectAgeAtEvent]) &gt; 0"/>	
	<xsl:variable name="studyEventData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID = $eventDefOID]"/>
	
<!--	<xsl:variable name="locationLen" select="string-length(@OpenClinica:StudyEventLocation)" />-->
<!-- maximum length of event location -->
	<xsl:variable name="locationLen">
		<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/string-length(@OpenClinica:StudyEventLocation)">
			<xsl:sort data-type="number"/>
		   <xsl:if test="position() = last()">
			 <xsl:value-of select="."/>
		   </xsl:if>
		 </xsl:for-each>
    </xsl:variable>
	<!--	<xsl:variable name="eventStatusLen" select="string-length($studyEventData/@OpenClinica:Status)" />-->
	<xsl:variable name="eventStatusLen">
		<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/string-length(@OpenClinica:Status)">
			<xsl:sort data-type="number"/>
		   <xsl:if test="position() = last()">
			 <xsl:value-of select="."/>
		   </xsl:if>
		 </xsl:for-each>
    </xsl:variable>
	<!--	<xsl:variable name="ageLen" select="string-length($studyEventData/@OpenClinica:SubjectAgeAtEvent)" />-->
	<xsl:variable name="ageLen">
		<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/string-length(@OpenClinica:SubjectAgeAtEvent)">
			<xsl:sort data-type="number"/>
		   <xsl:if test="position() = last()">
			 <xsl:value-of select="."/>
		   </xsl:if>
		 </xsl:for-each>
    </xsl:variable>		
		<!--	<xsl:variable name="eventLocationExist"
		select="@OpenClinica:StudyEventLocation" />-->
	
		
	<xsl:variable name="eventPosition">		
		<xsl:copy-of select="position()" />
	</xsl:variable>
	
	<xsl:choose>
		<xsl:when test="$isRepeating = 'Yes'"><!--{repeating event}-->
			<!-- write event data header columns for repeating event -->
			<xsl:apply-templates
				select="."
				mode="studyDataSPSSForRepeatingEvent" >
				<xsl:with-param name="eventRepeatCnt" select="1"/>
				<xsl:with-param name="eventOID"  select="$eventDefOID"/>
				<xsl:with-param name="eventPosition" select="$eventPosition" />
				 <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>
				<xsl:with-param name="locationLen" select="$locationLen"/>
				<xsl:with-param name="ageLen" select="$ageLen"/>
				<xsl:with-param name="eventStatusLen" select="$eventStatusLen"/>
			</xsl:apply-templates>	
		</xsl:when>
		<xsl:otherwise>	
			<!-- write event data header columns for non repeating event -->
			<xsl:apply-templates select="." mode="studyDataSPSSForNonRepeatingEvent">
				<xsl:with-param name="eventPosition" select="$eventPosition"/>
			   <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>
				<xsl:with-param name="locationLen" select="$locationLen"/>
				<xsl:with-param name="ageLen" select="ageLen"/>
				<xsl:with-param name="eventStatusLen" select="$eventStatusLen"/>
			</xsl:apply-templates>
			</xsl:otherwise>					
	</xsl:choose>
	</xsl:template>
	
   <xsl:template name="studyDataSPSSForRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" mode="studyDataSPSSForRepeatingEvent" >
		<xsl:param name="eventOID" />   
	   <xsl:param name="eventPosition"/>
	   <xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist" />
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>		
		<xsl:param name="eventRepeatCnt" />
		<xsl:param name="locationLen"/>
		<xsl:param name="ageLen"/>
		<xsl:param name="eventStatusLen"/>
		<!--{studyDataSPSSForRepeatingEvent}-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0">		
		<!--{cnt greater than 0}	-->
			<xsl:if test="$eventLocationExist">
				<xsl:text>Location_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> A</xsl:text>
				<xsl:choose>
					<xsl:when test="number($locationLen) &gt; 8">
						<xsl:text>8</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$locationLen" />
					</xsl:otherwise>
				</xsl:choose>
				<xsl:text>&#xa;</xsl:text>
		</xsl:if>

			<xsl:if test="$eventStartDateExist">
				<xsl:text>StartDate_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> ADATE10</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<xsl:if test="$eventEndDateExist">
				<xsl:text>EndDate_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="@StudyEventRepeatKey" />
				<xsl:text> ADATE10</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<xsl:if test="$eventStatusExist">
				<xsl:text>EventStatus_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> A</xsl:text>
				<xsl:choose>
					<xsl:when test="number($eventStatusLen) &gt; 8">
						<xsl:text>8</xsl:text>
					</xsl:when>
					<xsl:otherwise>
						<xsl:value-of select="$eventStatusLen" />
					</xsl:otherwise>
				</xsl:choose>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<xsl:if test="$ageExist">
									<xsl:text>Age_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$eventRepeatCnt" />
									<xsl:text> A8</xsl:text>
									<!--<xsl:choose>
										<xsl:when test="number($ageLen) &gt; 8">
											<xsl:text>8</xsl:text>
										</xsl:when>
										<xsl:otherwise>
											<xsl:value-of select="$ageLen" />
										</xsl:otherwise>
									</xsl:choose>-->
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>
			<xsl:call-template name="studyDataSPSSForRepeatingEvent">
				<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
				<xsl:with-param name="eventOID"  select="$eventOID"/>
				<xsl:with-param name="eventPosition" select="$eventPosition" />	
				<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist" select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>
				<xsl:with-param name="locationLen" select="$locationLen"/>
				<xsl:with-param name="ageLen" select="$ageLen"/>
				<xsl:with-param name="eventStatusLen" select="$eventStatusLen"/>						
			</xsl:call-template>
		</xsl:if>	
   </xsl:template>
   
   <xsl:template mode="studyDataSPSSForNonRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" >
	   <xsl:param name="eventPosition"/>
	   <xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist" />
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>	  
		<xsl:param name="locationLen"/>
		<xsl:param name="ageLen"/>	  
		<xsl:param name="eventStatusLen"/>
	   <xsl:if test="$eventLocationExist">
					<xsl:text>Location_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> A</xsl:text>
					<xsl:choose>
						<xsl:when test="number($locationLen) &gt; 8">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$locationLen" />
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>
				

				<xsl:if test="$eventStartDateExist">
					<xsl:text>StartDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> ADATE10</xsl:text>
					<xsl:text>&#xa;</xsl:text>
					<!--<xsl:text>StartDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> ADATE10</xsl:text>
					<xsl:text>&#xa;</xsl:text>-->
				</xsl:if>

				<xsl:if test="$eventEndDateExist">
					<xsl:text>EndDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> ADATE10</xsl:text>
					<xsl:text>&#xa;</xsl:text>
					<!--<xsl:text>EndDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> ADATE10</xsl:text>
					<xsl:text>&#xa;</xsl:text>-->
				</xsl:if>

				<xsl:if test="$eventStatusExist">
					<xsl:text>EventStatus_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> A</xsl:text>
					<xsl:choose>
						<xsl:when test="number($eventStatusLen) &gt; 8">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$eventStatusLen" />
						</xsl:otherwise>
					</xsl:choose>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$ageExist">
					<xsl:text>Age_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> A8</xsl:text>
					<!--<xsl:choose>
						<xsl:when test="number($ageLen) &gt; 8">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$ageLen" />
						</xsl:otherwise>
					</xsl:choose>-->
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>
   </xsl:template>

	<xsl:template mode="studyEventDataColumnSPSS2" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
	
	<xsl:variable name="eventDefOID" select="@OID"/>
	<xsl:variable name="isRepeating" select="@Repeating"/>
	<xsl:variable name="allStudyEventDataElements" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID = $eventDefOID]"/>	
	<xsl:variable name="eventLocationExist" select="count($allStudyEventDataElements[@OpenClinica:StudyEventLocation]) &gt; 0"/>
	<xsl:variable name="eventStartDateExist" select="count($allStudyEventDataElements[@OpenClinica:StartDate]) &gt; 0"/>
	<xsl:variable name="eventStatusExist" select="count($allStudyEventDataElements[@OpenClinica:Status]) &gt; 0"/>
	<xsl:variable name="eventEndDateExist" select="count($allStudyEventDataElements[@OpenClinica:EndDate]) &gt; 0"/>
	<xsl:variable name="ageExist" select="count($allStudyEventDataElements[@OpenClinica:SubjectAgeAtEvent]) &gt; 0"/>	
	<xsl:variable name="studyEventData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID = $eventDefOID]"/>
	<xsl:variable name="eventName" select="@Name"/>
	<!--<xsl:variable name="locationLen"
			select="string-length(@OpenClinica:StudyEventLocation)" />
		<xsl:variable name="eventStatusLen" select="string-length($studyEventData/@OpenClinica:Status)" />
		<xsl:variable name="ageLen"
			select="string-length($studyEventData/@OpenClinica:SubjectAgeAtEvent)" />
			
			<xsl:variable name="eventLocationExist"
		select="@OpenClinica:StudyEventLocation" />-->
	<!--{EventDataColumnSPSS2}-->
		
	<xsl:variable name="eventPosition">		
		<xsl:copy-of select="position()" />
	</xsl:variable>
	
	<xsl:choose>
		<xsl:when test="$isRepeating = 'Yes'">
			<!-- write event data header columns for repeating event -->
			<xsl:apply-templates
				select="."
				mode="studyEventDataColumnSPSSForRepeatingEvent" >
				<xsl:with-param name="eventRepeatCnt" select="1"/>
				<xsl:with-param name="eventOID"  select="$eventDefOID"/>
				<xsl:with-param name="eventPosition" select="$eventPosition" />
				 <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>
				<!--<xsl:with-param name="locationLen" select="$locationLen"/>
				<xsl:with-param name="ageLen" select="$ageLen"/>
				<xsl:with-param name="eventStatusLen" select="$eventStatusLen"/>-->
				<xsl:with-param name="eventName" select="$eventName"/>
			</xsl:apply-templates>	
		</xsl:when>
		<xsl:otherwise>	
			<!-- write event data header columns for non repeating event -->
			<xsl:apply-templates select="." mode="studyEventDataColumnSPSSForNonRepeatingEvent">
				<xsl:with-param name="eventPosition" select="$eventPosition"/>
			   <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>
				<!--<xsl:with-param name="locationLen" select="$locationLen"/>
				<xsl:with-param name="ageLen" select="$ageLen"/>
				<xsl:with-param name="eventStatusLen" select="$eventStatusLen"/>-->
				<xsl:with-param name="eventName" select="$eventName"/>
			</xsl:apply-templates>
			</xsl:otherwise>					
	</xsl:choose>
	</xsl:template>
	
   <xsl:template name="studyEventDataColumnSPSSForRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" mode="studyEventDataColumnSPSSForRepeatingEvent" >
		<xsl:param name="eventOID" />   
	   <xsl:param name="eventPosition"/>
	   <xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist" />
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>		
		<xsl:param name="eventRepeatCnt" />
		<!--<xsl:param name="locationLen"/>
		<xsl:param name="ageLen"/>
		<xsl:param name="eventStatusLen"/>-->
		
		<xsl:param name="eventName"/>
		
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0">		
			
			<xsl:if test="$eventLocationExist">
				<xsl:text>Location_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> "Location For </xsl:text>
				<xsl:value-of select="$eventName" />
				<xsl:text>(</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text>)"</xsl:text>
				<xsl:text> /</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<xsl:if test="$eventStartDateExist">
				<xsl:text>StartDate_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> "Start Date For </xsl:text>
				<xsl:value-of select="$eventName" />
				<xsl:text>(</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text>)"</xsl:text>
				<xsl:text> /</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<xsl:if test="$eventEndDateExist">
				<xsl:text>EndDate_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> "End Date For </xsl:text>
				<xsl:value-of select="$eventName" />
				<xsl:text>(</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text>)"</xsl:text>
				<xsl:text> /</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<xsl:if test="$eventStatusExist">
				<xsl:text>Event Status_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> "Event Status For </xsl:text>
				<xsl:value-of select="$eventName" />
				<xsl:text>(</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text>)"</xsl:text>
				<xsl:text> /</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<xsl:if test="$ageExist">
				<xsl:text>Age_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> "Age For </xsl:text>
				<xsl:value-of select="$eventName" />
				<xsl:text>(</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text>)"</xsl:text>
				<xsl:text> /</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>
			<xsl:call-template name="studyEventDataColumnSPSSForRepeatingEvent">
				<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
				<xsl:with-param name="eventOID"  select="$eventOID"/>
				<xsl:with-param name="eventPosition" select="$eventPosition" />	
				<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist" select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>	
				<xsl:with-param name="eventName"  select="$eventName"/>	
							
			</xsl:call-template>
		</xsl:if>	
   </xsl:template>
   
   <xsl:template mode="studyEventDataColumnSPSSForNonRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" >
	   <xsl:param name="eventPosition"/>
	   <xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist" />
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>	  
		<!--<xsl:param name="locationLen"/>
		<xsl:param name="ageLen"/>	  
		<xsl:param name="eventStatusLen"/>-->
		<xsl:param name="eventName"/>
		   <xsl:if test="$eventLocationExist">
					<xsl:text>Location_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "Location For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text> /</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$eventStartDateExist">
					<xsl:text>StartDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "Start Date For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text> /</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$eventEndDateExist">
					<xsl:text>EndDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "End Date For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text> /</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$eventStatusExist">
					<xsl:text>Event Status_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "Event Status For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text> /</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$ageExist">
					<xsl:text>Age_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "Age For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text> /</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>
   </xsl:template>	

<xsl:template mode="studyFormAndDataItemsHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
		<xsl:param name="calledFor" />
		<!--<xsl:variable name="formRefOID" select="@FormOID"/>-->
		<xsl:variable name="eventOID" select="@OID" />
		<!--<xsl:variable name="isEventRepeating" select="@Repeating" />-->
		<xsl:variable name="isRepeatingEvent" select="@Repeating" />
		<!-- calculate event position -->
		<xsl:variable name="eventPosition">
			<xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $eventOID">
					<xsl:copy-of select="position()" />
				</xsl:if>	
			</xsl:for-each>
		</xsl:variable>
		<!-- calculate form def position in metadata -->		
		
		
		<xsl:apply-templates mode="studyFormColumnHeaders" select=".">
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="isRepeatingEvent" select="$isRepeatingEvent"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="calledFor" select="$calledFor"/>		
		</xsl:apply-templates>
		<!-- apply template for item data columns -->
		<xsl:apply-templates mode="studyItemDataColumnHeaders" select=".">			
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="isEventRepeating" select="$isRepeatingEvent"/>	
			<xsl:with-param name="calledFor" select="$calledFor"/>			
		</xsl:apply-templates>	
		<!--<xsl:apply-templates mode="studyItemDataColumnHeaders" select=".">			
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="isEventRepeating" select="$isRepeatingEvent"/>	
			<xsl:with-param name="calledFor" select="'forCodeListsOnly'"/>
		</xsl:apply-templates>-->	
	</xsl:template>
	
	<xsl:template mode="studyFormColumnHeaders"
		match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
		<!--<xsl:template name="studyEventData2">-->
		<!--<xsl:param name="crfPosition"/>-->
		<xsl:param name="calledFor" />
		<xsl:param name="eventOID"/>		
		<xsl:param name="eventPosition"/>
		<xsl:param name="isRepeatingEvent"/>	
		
		<xsl:variable name="eventName" select="@Name"/>
		<xsl:choose>
			<xsl:when test="$isRepeatingEvent = 'Yes'">			
				<!-- create CRF columns for repeating event -->		
				<xsl:apply-templates select="." mode="createCRFColForRepeatingEvent">
					<xsl:with-param name="eventOID" select="$eventOID"/>   
				   <xsl:with-param name="eventPosition" select="$eventPosition"/>
				   <xsl:with-param name="eventRepeatCnt" select="1"/>		
				   	<xsl:with-param name="calledFor" select="$calledFor"/>	   						
				</xsl:apply-templates>			
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="$calledFor = 'itemDataValuesSPSS'"><!--{studyFormColumnHeaders called for}-->
					
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
					
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
					@OpenClinica:Version]) 	
						&gt; 0"/>
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
						@OpenClinica:InterviewerName]) &gt; 0"/>
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
						@OpenClinica:InterviewDate]) &gt; 0"/>
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
					@OpenClinica:Status]) &gt; 
						0"/>
					<!--	
					<xsl:variable name="interLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/@OpenClinica:InterviewerName">
							<xsl:sort data-type="number"/>
						    <xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
							
					<xsl:variable name="interStatusLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/@OpenClinica:Status">
							<xsl:sort data-type="number"/>
						    <xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
								
					<xsl:variable name="versionLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/@OpenClinica:Version">
							<xsl:sort data-type="number"/>
						    <xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>	
					-->
					<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and odm:FormData/@FormOID = 
						$formRefOID]) &gt; 0"><!--{data exists for crf}-->
						<xsl:if test="$interviewerNameExist">
						<xsl:text>Interviewer_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> "Interviewer Name for </xsl:text>
						<xsl:value-of select="$eventName" /><xsl:text>"</xsl:text>
						<xsl:text> /</xsl:text>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>

					<xsl:if test="$interviewDateExist">
						<xsl:text>InterviewDate_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> "Interviewer Date For </xsl:text>
						<xsl:value-of select="$eventName" /><xsl:text>"</xsl:text>
						<xsl:text> /</xsl:text>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>

					<xsl:if test="$crfStatusExist">
						<xsl:text>CRFVersionStatus_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> "CRF Version Status For </xsl:text>
						<xsl:value-of select="$eventName" /><xsl:text>"</xsl:text>
						<xsl:text> /</xsl:text>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>

					<xsl:if test="$crfVersionExist">
						<xsl:text>VersionName_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> "Version Name For </xsl:text>
						<xsl:value-of select="$eventName" /><xsl:text>"</xsl:text>
						<xsl:text> /</xsl:text>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
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
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
					@OpenClinica:Version]) 	
						&gt; 0"/>
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
						@OpenClinica:InterviewerName]) &gt; 0"/>
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
						@OpenClinica:InterviewDate]) &gt; 0"/>
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
					@OpenClinica:Status]) &gt; 
						0"/>
					<!-- maximum length of InterviewerName for CRF -->					
					<xsl:variable name="interLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/string-length(@OpenClinica:InterviewerName)">
							<xsl:sort data-type="number"/>
						    <xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<!-- maximum length of Status for CRF -->				
					<xsl:variable name="interStatusLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/string-length(@OpenClinica:Status)">
							<xsl:sort data-type="number"/>
						    <xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<!-- maximum length of Version for CRF -->				
					<xsl:variable name="versionLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/string-length(@OpenClinica:Version)">
							<xsl:sort data-type="number"/>
						    <xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>	
					
					<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and odm:FormData/@FormOID = 
						$formRefOID]) &gt; 0">
						<xsl:if test="$interviewerNameExist">
						<xsl:text>Interviewer_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> A</xsl:text>
						<xsl:choose>
							<xsl:when test="$interLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$interLen" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
					<xsl:if test="$interviewDateExist">
						<xsl:text>InterviewDate_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> ADATE10</xsl:text>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
					<xsl:if test="$crfStatusExist">
						<xsl:text>CRFVersionStatus_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> A</xsl:text>
						<xsl:choose>
							<xsl:when test="$interStatusLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$interStatusLen" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>

					<xsl:if test="$crfVersionExist">
						<xsl:text>VersionName_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> A</xsl:text>
						<xsl:choose>
							<xsl:when test="$versionLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="$versionLen" />
							</xsl:otherwise>
						</xsl:choose>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>	
					</xsl:otherwise>
				</xsl:choose>
				
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>
	
<xsl:template name="createCRFColForRepeatingEvent" mode="createCRFColForRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
		<xsl:param name="eventOID" />   
	   <xsl:param name="eventPosition"/>
	   <xsl:param name="eventRepeatCnt" />
	    <xsl:param name="calledFor" />
	    
	    <xsl:variable name="eventName" select="@Name"/>
	    <xsl:choose>
			<xsl:when test="$calledFor = 'itemDataValuesSPSS'">
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
					<xsl:if test="$crfPosition = 1 ">
						<xsl:text>.</xsl:text>
						<xsl:text>&#xa;</xsl:text>
						<xsl:text>VALUE LABELS</xsl:text>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>	
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[@FormOID = $formRefOID and @OpenClinica:Version 
					and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
						@OpenClinica:InterviewerName  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
						@OpenClinica:InterviewDate  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[@FormOID = $formRefOID and @OpenClinica:Status  and 
					../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					<!--				
					<xsl:variable name="interLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/@OpenClinica:InterviewerName">
							<xsl:sort data-type="number"/>
							<xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
									
					<xsl:variable name="interStatusLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/@OpenClinica:Status">
							<xsl:sort data-type="number"/>
							<xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
								
					<xsl:variable name="versionLen">
								<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/@OpenClinica:Version">
									<xsl:sort data-type="number"/>
									<xsl:if test="position() = last()">
										<xsl:value-of select="."/>
									</xsl:if>
								</xsl:for-each>
							</xsl:variable>			
					-->		
							<xsl:if test="$interviewerNameExist">
									<xsl:text>Interviewer_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />									
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$eventRepeatCnt" />
									
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:text> "Interviewer Name For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text> /</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$interviewDateExist">
									<xsl:text>InterviewDate</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
										<xsl:value-of select="$eventRepeatCnt" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:text> "Interviewer Date For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text> /</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$crfStatusExist">
									<xsl:text>CRFVersionStatus_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
										<xsl:value-of select="$eventRepeatCnt" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:text> "CRF Version Status For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text> /</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$crfVersionExist">
									<xsl:text>VersionName_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
										<xsl:value-of select="$eventRepeatCnt" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:text> "Version Name For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text> /</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>
						
				</xsl:for-each>	
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = 
					$eventRepeatCnt]) &gt; 0">	
					<xsl:call-template name="createCRFColForRepeatingEvent">
						<xsl:with-param name="eventOID" select="$eventOID"/>   
						<xsl:with-param name="eventPosition" select="$eventPosition"/>
						<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
						 <xsl:with-param name="calledFor" select="$calledFor"/>
					</xsl:call-template>
				</xsl:if>
				
								
				
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
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[@FormOID = $formRefOID and @OpenClinica:Version 
					and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
						@OpenClinica:InterviewerName  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[@FormOID = $formRefOID and 
						@OpenClinica:InterviewDate  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[@FormOID = $formRefOID and @OpenClinica:Status  and 
					../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					<!-- maximum length of InterviewerName for CRF -->					
					<xsl:variable name="interLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/string-length(@OpenClinica:InterviewerName)">
							<xsl:sort data-type="number"/>
							<xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<!-- maximum length of Status for CRF -->				
					<xsl:variable name="interStatusLen">
						<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/string-length(@OpenClinica:Status)">
							<xsl:sort data-type="number"/>
							<xsl:if test="position() = last()">
								<xsl:value-of select="."/>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<!-- maximum length of Version for CRF -->				
					<xsl:variable name="versionLen">
								<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/string-length(@OpenClinica:Version)">
									<xsl:sort data-type="number"/>
									<xsl:if test="position() = last()">
										<xsl:value-of select="."/>
									</xsl:if>
								</xsl:for-each>
							</xsl:variable>			
							<xsl:if test="$interviewerNameExist">
								<xsl:text>Interviewer_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />									
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$eventRepeatCnt" />									
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C" />
								<xsl:value-of select="$crfPosition" />
								<xsl:text> A</xsl:text>
								<xsl:choose>
									<xsl:when test="$interLen &gt; 8">
										<xsl:text>8</xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$interLen" />
									</xsl:otherwise>
								</xsl:choose>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>
		
							<xsl:if test="$interviewDateExist">
								<xsl:text>InterviewDate</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$eventRepeatCnt" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C" />
								<xsl:value-of select="$crfPosition" />
								<xsl:text> ADATE10</xsl:text>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>
		
							<xsl:if test="$crfStatusExist">
								<xsl:text>CRFVersionStatus_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$eventRepeatCnt" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C" />
								<xsl:value-of select="$crfPosition" />
								<xsl:text> A</xsl:text>
								<xsl:choose>
									<xsl:when test="$interStatusLen &gt; 8">
										<xsl:text>8</xsl:text>
									</xsl:when>
									<xsl:otherwise>
										<xsl:value-of select="$interStatusLen" />
									</xsl:otherwise>
								</xsl:choose>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>
		
							<xsl:if test="$crfVersionExist">
											<xsl:text>VersionName_</xsl:text>
											<xsl:value-of select="$E" />
											<xsl:value-of select="$eventPosition" />
											<xsl:text>_</xsl:text>
											<xsl:value-of select="$eventRepeatCnt" />
											<xsl:text>_</xsl:text>
											<xsl:value-of select="$C" />
											<xsl:value-of select="$crfPosition" />
											<xsl:text> A</xsl:text>
											<xsl:choose>
												<xsl:when test="$versionLen &gt; 8">
													<xsl:text>8</xsl:text>
												</xsl:when>
												<xsl:otherwise>
													<xsl:value-of select="$versionLen" />
												</xsl:otherwise>
											</xsl:choose>
											<xsl:text>&#xa;</xsl:text>
										</xsl:if>
						
				</xsl:for-each>	
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = 
					$eventRepeatCnt]) &gt; 0">	
		<xsl:call-template name="createCRFColForRepeatingEvent">
			<xsl:with-param name="eventOID" select="$eventOID"/>   
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
		</xsl:call-template>
		</xsl:if>
			</xsl:otherwise>
		</xsl:choose>		
   </xsl:template>
   
   <xsl:template  mode="studyItemDataColumnHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
	   <xsl:param name="calledFor" />
		<xsl:param name="eventOID" /> 
		<xsl:param name="isEventRepeating"/>
			<!-- {studyItemDataColumnHeaders, eventOID:<xsl:value-of select="./@OID"/>}-->
		<xsl:variable name="currentEventOID"	 select="./@OID"/>
		<xsl:variable name="currentEventIsRepeating"	 select="./@Repeating"/>
	<xsl:variable name="eventPosition">
			<xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $currentEventOID">
					<xsl:copy-of select="position()" />
				</xsl:if>	
			</xsl:for-each>
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
    <!--<xsl:variable name="StudyEventRepeatKey" select="1"/>--><!--temp hardcoded-->
    
	<xsl:choose>
		<xsl:when test="$currentEventIsRepeating = 'Yes'">			
			<!-- create item data columns for repeating event -->		
			<xsl:apply-templates select="." mode="createItemDataColForRepeatingEvent">
				<xsl:with-param name="eventOID" select="$currentEventOID"/>   
			   <xsl:with-param name="eventPosition" select="$eventPosition"/>
			   <xsl:with-param name="eventRepeatCnt" select="1"/>				   						
				<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
				<xsl:with-param name="isEventRepeating" select="$currentEventIsRepeating"/><!-- this is just need to pass on to further template which is common to repeating and non-repeating events -->
				<xsl:with-param name="calledFor" select="$calledFor"/>
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
				<xsl:with-param name="isEventRepeating" select="$currentEventIsRepeating"/>
				<xsl:with-param name="eventOID" select="$currentEventOID"/>
				<xsl:with-param name="StudyEventRepeatKey" select="$MaxEventRepeatKey"/><!-- this param is of no use for non-repeating column further when creating the columns -->
				<xsl:with-param name="calledFor" select="$calledFor"/>
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
	   <xsl:param name="calledFor"/>
	   
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
					
			<xsl:apply-templates mode="formRefToDefTemplateForHeaders" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID = $formRefOID]" >
				<xsl:with-param name="crfPosition" select="$crfPosition"/>
				<xsl:with-param name="eventPosition" select="$eventPosition"/>
				<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
				<xsl:with-param name="eventOID" select="$eventOID"/>
				<xsl:with-param name="StudyEventRepeatKey" select="$eventRepeatCnt"/>
				<xsl:with-param name="calledFor" select="$calledFor"/>
			</xsl:apply-templates>						
				
		</xsl:for-each>	
		
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = 
					$eventRepeatCnt]) &gt; 0">	
					
		
		
		<xsl:apply-templates select="." mode="createItemDataColForRepeatingEvent">
			<xsl:with-param name="eventOID" select="$eventOID"/>   
		   <xsl:with-param name="eventPosition" select="$eventPosition"/>
		   <xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>	
		    <xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>	
		    <xsl:with-param name="calledFor" select="$calledFor"/>	   						
		</xsl:apply-templates>
		</xsl:if>
   </xsl:template>
   
  <xsl:template mode="formRefToDefTemplateForHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID]">
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition" />
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="eventOID"/>	
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="calledFor"/>
		<!--{formRefToDefTemplateForHeaders, eventOID: <xsl:value-of select="$eventOID"/>}-->
		<xsl:variable name="formOID" select="@OID"/>
		<xsl:apply-templates select="odm:ItemGroupRef" mode="ItemGrpRefs">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
			<xsl:with-param name="formOID" select="$formOID"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
			<xsl:with-param name="calledFor" select="$calledFor"/>
		</xsl:apply-templates>
  </xsl:template>
  
   <xsl:template mode="ItemGrpRefs" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef/odm:ItemGroupRef[@ItemGroupOID]" >
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition" />
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="formOID"/>
		<xsl:param name="eventOID"/>	
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="calledFor"/>
			
		<xsl:variable name="grpOID" select="@ItemGroupOID"/>
		<xsl:apply-templates select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef[@OID = $grpOID]" mode="ItemGrpRefToDefTemplateForHeaders">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
			<xsl:with-param name="formOID" select="$formOID"/>
			<xsl:with-param name="grpOID" select="$grpOID"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="StudyEventRepeatKey" select="$StudyEventRepeatKey"/>
			<xsl:with-param name="calledFor" select="$calledFor"/>
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
		<xsl:param name="calledFor"/><!--{ItemGrpRefToDefTemplateForHeaders}-->
			
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
					<xsl:with-param name="calledFor" select="$calledFor"/> 
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
					<xsl:with-param name="calledFor" select="$calledFor"/>
					<!--<xsl:with-param name="itemGrpRepeatKey" select="$itemGrpRepeatKey"/>-->
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
		<xsl:param name="calledFor"/>
		<xsl:choose>
			<xsl:when test="$isEventRepeating = 'Yes'">
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
				<xsl:with-param name="calledFor" select="$calledFor"/>
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
				<xsl:with-param name="calledFor" select="$calledFor"/>
			</xsl:apply-templates>
		</xsl:if>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID 
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
				<xsl:with-param name="calledFor" select="$calledFor"/>
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
				<xsl:with-param name="calledFor" select="$calledFor"/>
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
		<xsl:param name="calledFor"/>
		<!--{GrpItemRefs}-->
		<xsl:variable name="itemOID" select="@ItemOID"/>
		<xsl:choose>
			<xsl:when test="$isEventRepeating = 'Yes'">
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $StudyEventRepeatKey and odm:FormData/@FormOID = 
						$formOID and odm:FormData/odm:ItemGroupData/@ItemGroupOID = $grpOID and odm:FormData/odm:ItemGroupData/odm:ItemData/@ItemOID = $itemOID]) &gt; 0">
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
				<!--<xsl:with-param name="isLastItem" select="$isLastItem" />-->
				<xsl:with-param name="calledFor" select="$calledFor"/>
				<xsl:with-param name="groupOID" select="$grpOID"/>
				<xsl:with-param name="formOID" select="$formOID"/>
			</xsl:apply-templates>
		</xsl:if>	
			</xsl:when>
			<xsl:otherwise><!--{otherwise}-->
			<!--eventOID:<xsl:value-of select="$eventOID"/>
			formOID:<xsl:value-of select="$formOID"/>
			grpOID:<xsl:value-of select="$grpOID"/>
			itemOID:<xsl:value-of select="$itemOID"/>-->
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and odm:FormData/@FormOID = 
						$formOID and odm:FormData/odm:ItemGroupData/@ItemGroupOID = $grpOID and odm:FormData/odm:ItemGroupData/odm:ItemData/@ItemOID = $itemOID]) &gt; 0">
				<!--call template for items-->
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
					<!--<xsl:with-param name="isLastItem" select="$isLastItem" />-->
					<xsl:with-param name="calledFor" select="$calledFor"/>
					<xsl:with-param name="groupOID" select="$grpOID"/>
					<xsl:with-param name="formOID" select="$formOID"/>
				</xsl:apply-templates>
		</xsl:if>	
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
		<xsl:param name="calledFor"/>
		<xsl:param name="groupOID"/>
		<xsl:param name="formOID"/>	
		
		<xsl:variable name="dataType" select="@DataType" />
	<!--{ItemDefColHeaders2: <xsl:value-of select="$calledFor"/>}-->
		<xsl:choose>
			<xsl:when test="$calledFor = 'itemDataValuesSPSS'">
			<xsl:variable name="comment" select="@Comment"/>
				<!--<xsl:value-of select="@Name" />-->
				<xsl:value-of select='replace(normalize-space(@Name), "\s", "_")'/>
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$E"/>	<xsl:value-of select="$eventPosition"/>
				<xsl:if test="$isEventRepeating = 'Yes'">
					<xsl:text>_</xsl:text><xsl:value-of select="$StudyEventRepeatKey"/>
				</xsl:if>
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$C" />
				<xsl:value-of select="$crfPosition" />
				<xsl:text>_</xsl:text>
				<!--<xsl:variable name="group" select="$itemData/parent::node()" />
				<xsl:variable name="groupOID" select="$group/@ItemGroupOID" />-->
			<xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
				<xsl:if test="@Name !='Ungrouped'">
					<xsl:value-of select="@Name" />
				</xsl:if>
			</xsl:for-each>
			<xsl:if test="$isGrpRepeating">
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$itemGrpRepeatKey" />
			</xsl:if>
			<!-- item label -->
			<xsl:text> "</xsl:text>
			<xsl:value-of select="$comment" /><xsl:text>"</xsl:text>
			<xsl:text> /</xsl:text>
			<xsl:text>&#xa;</xsl:text>
			<!--<xsl:variable name="codeListOID" select="./odm:CodeListRef/@CodeListOID" />
			<xsl:for-each select="//odm:CodeList[@OID=$codeListOID]">
				<xsl:for-each select="./odm:CodeListItem">
					<xsl:value-of select="@CodedValue" />
					<xsl:text> "</xsl:text>
					<xsl:value-of select="./odm:Decode/odm:TranslatedText" />
					<xsl:text>"</xsl:text>
					<xsl:if test="position()!=last()">
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
			<xsl:text>&#xa;</xsl:text>
			<xsl:text>/</xsl:text>
			<xsl:text>&#xa;</xsl:text>-->
			</xsl:when>
			<xsl:when test="$calledFor = 'forCodeListsOnly'">
			
			<xsl:variable name="codeListOID" select="./odm:CodeListRef/@CodeListOID" />
			<xsl:if test="$codeListOID">
				<!--<xsl:value-of select="@Name" />-->
				<xsl:value-of select='replace(normalize-space(@Name), "\s", "_")'/>
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$E"/>	<xsl:value-of select="$eventPosition"/>
				<xsl:if test="$isEventRepeating = 'Yes'">
					<xsl:text>_</xsl:text><xsl:value-of select="$StudyEventRepeatKey"/>
				</xsl:if>
				<xsl:text>_</xsl:text>			
				<xsl:value-of select="$C" />
				<xsl:value-of select="$crfPosition" />
				<!--<xsl:variable name="group" select="$itemData/parent::node()" />
				<xsl:variable name="groupOID" select="$group/@ItemGroupOID" />-->
				<xsl:if test="$isGrpRepeating = 'Yes'">
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$itemGrpRepeatKey" />
				</xsl:if>	
				<xsl:text>&#xa;</xsl:text>
				
				<xsl:for-each select="//odm:CodeList[@OID=$codeListOID]">
					<xsl:for-each select="./odm:CodeListItem">
						<xsl:value-of select="@CodedValue" />
						<xsl:text> "</xsl:text>
						<xsl:value-of select="./odm:Decode/odm:TranslatedText" />
						<xsl:text>"</xsl:text>
						<xsl:if test="position()!=last()">
							<xsl:text>&#xa;</xsl:text>
						</xsl:if>
					</xsl:for-each>
				</xsl:for-each>
				<xsl:text>&#xa;</xsl:text>
				<xsl:text>/</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>	
			</xsl:when>	
			<xsl:otherwise>
				
			<xsl:value-of select='replace(normalize-space(@Name), "\s", "_")'/>
			<xsl:text>_</xsl:text>
			<xsl:value-of select="$E"/>	<xsl:value-of select="$eventPosition"/>
			<xsl:if test="$isEventRepeating = 'Yes'">
				<xsl:text>_</xsl:text><xsl:value-of select="$StudyEventRepeatKey"/>
			</xsl:if>
			<xsl:text>_</xsl:text>			
			<xsl:value-of select="$C" />
			<xsl:value-of select="$crfPosition" />
			<!--<xsl:variable name="group" select="$itemData/parent::node()" />
			<xsl:variable name="groupOID" select="$group/@ItemGroupOID" />-->
			<xsl:if test="$isGrpRepeating = 'Yes'">
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$itemGrpRepeatKey" />
			</xsl:if>
			
			<xsl:choose>
				<xsl:when test="$dataType='date'">
					<xsl:text> ADATE10</xsl:text>
				</xsl:when>
				<xsl:when test="$dataType='float'">
					<!--<xsl:text> F8.2</xsl:text>-->
					<xsl:text> F</xsl:text><xsl:value-of select="@Length"/><xsl:text>.</xsl:text><xsl:value-of select="@SignificantDigits"/>
				</xsl:when>
				<!--<xsl:when test="$dataType='integer'">
					<xsl:text> F8.0</xsl:text>
				</xsl:when>-->
				<xsl:otherwise>
					<xsl:text> A</xsl:text>
					<xsl:variable name="valueLength" select="@Length"/>
					<!--<xsl:choose>
						<xsl:when test="$valueLength &gt; 8 ">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>-->
							<xsl:value-of select="$valueLength" />
						<!--</xsl:otherwise>
					</xsl:choose>-->
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xa;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>	

<xsl:template name="subjectDataColumnHeaders" mode="subjectDataColumnHeaders" match="/odm:ODM/odm:ClinicalData/odm:SubjectData" >
		
		<xsl:text>StudySubjectID</xsl:text>
		<xsl:variable name="valueLength">
			<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/string-length(@OpenClinica:StudySubjectID)">
				<xsl:sort data-type="number"/>
				<xsl:if test="position() = last()">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:text> A</xsl:text><xsl:value-of select="$valueLength" />
		<!--<xsl:text> "Study Subject ID"</xsl:text>-->
		<xsl:text>&#xa;</xsl:text>
		<!--<xsl:text>ProtocolID</xsl:text>
		<xsl:text> "Protocol ID_Site ID"</xsl:text>
		<xsl:text>&#xa;</xsl:text>-->
		<xsl:if test="$uniqueIdExist">
			<xsl:variable name="valueLength">
				<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/string-length(@OpenClinica:UniqueIdentifier)">
					<xsl:sort data-type="number"/>
					<xsl:if test="position() = last()">
						<xsl:value-of select="."/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:text>PersonID</xsl:text>
			<xsl:text> A</xsl:text><xsl:value-of select="$valueLength" />
			<!--<xsl:text> "Unique ID"</xsl:text>-->
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>

		<xsl:if test="$subjectStatusExist">
			<xsl:text>SubjectStatus</xsl:text>
			<xsl:variable name="valueLength">
				<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/string-length(@OpenClinica:Status)">
					<xsl:sort data-type="number"/>
					<xsl:if test="position() = last()">
						<xsl:value-of select="."/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:text> A</xsl:text><xsl:value-of select="$valueLength" />
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>

		<xsl:if test="$sexExist">
			<xsl:text>Sex</xsl:text>
			<xsl:variable name="valueLength">
				<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/string-length(@OpenClinica:Sex)">
					<xsl:sort data-type="number"/>
					<xsl:if test="position() = last()">
						<xsl:value-of select="."/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:text> A</xsl:text><xsl:value-of select="$valueLength" />
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>

		<xsl:if test="$dobExist">
			<xsl:text>DateofBirth</xsl:text>
			<xsl:variable name="valueLength">
				<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/string-length(@OpenClinica:DateOfBirth)">
					<xsl:sort data-type="number"/>
					<xsl:if test="position() = last()">
						<xsl:value-of select="."/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:text> A</xsl:text><xsl:value-of select="$valueLength" />	
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>
	</xsl:template>
<!--****************************************************************************************************** -->
	<!-- Starting Columns and its labels -->
	<!--****************************************************************************************************** -->



	<xsl:template name="subjectDataColumnSPSS">
		
		<xsl:text>StudySubjectID</xsl:text>
		<xsl:text> "Study Subject ID"</xsl:text><xsl:text> /</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<!--<xsl:text>ProtocolID</xsl:text>
		<xsl:text> "Protocol ID_Site ID"</xsl:text>
		<xsl:text>&#xa;</xsl:text>-->
		<xsl:if test="$uniqueIdExist">
			<xsl:text>PersonID</xsl:text>
			<xsl:text> "Person ID"</xsl:text><xsl:text> /</xsl:text>
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>

		<xsl:if test="$subjectStatusExist">
			<xsl:text>SubjectStatus</xsl:text>
			<xsl:text> "Subject Status"</xsl:text><xsl:text> /</xsl:text>
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>

		<xsl:if test="$sexExist">
			<xsl:text>Sex</xsl:text>
			<xsl:text> "Sex"</xsl:text><xsl:text> /</xsl:text>
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>

		<xsl:if test="$dobExist">
			<xsl:text>DateofBirth</xsl:text>
			<xsl:text> "Date of Birth"</xsl:text><xsl:text> /</xsl:text>
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>
	</xsl:template>
<!--
	<xsl:template
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
		mode="studyEventDataColumnSPSS">
		<xsl:variable name="eventOID" select="@StudyEventOID" />
		<xsl:variable name="eventPosition" select="position()" />
		<xsl:variable name="eventName"
			select="//odm:StudyEventDef[@OID=$eventOID]/@Name" />
			<xsl:variable name="eventLocationExist"
		select="@OpenClinica:StudyEventLocation" />
	<xsl:variable name="eventStartDateExist"
		select="@OpenClinica:StartDate" />
	<xsl:variable name="eventEndDateExist"
		select="@OpenClinica:EndDate" />
	<xsl:variable name="eventStatusExist"
		select="@OpenClinica:Status" />
	<xsl:variable name="ageExist"
		select="@OpenClinica:SubjectAgeAtEvent" />
		<xsl:choose>
			<xsl:when test="@StudyEventRepeatKey">
				<xsl:variable name="allStudyEvents">
					<xsl:for-each select="//odm:StudyEventData[@StudyEventOID=$eventOID]">
						<xsl:sort select="@StudyEventRepeatKey" data-type="number" />
						<xsl:copy-of select="." />
					</xsl:for-each>
				</xsl:variable>
				<xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
					<xsl:choose>
						<xsl:when test="position()=1">

							<xsl:if test="$eventLocationExist">
								<xsl:text>Location_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> "Location For </xsl:text>
								<xsl:value-of select="$eventName" />
								<xsl:text>(</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text>)"</xsl:text>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>

							<xsl:if test="$eventStartDateExist">
								<xsl:text>StartDate_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> "Start Date For </xsl:text>
								<xsl:value-of select="$eventName" />
								<xsl:text>(</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text>)"</xsl:text>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>

							<xsl:if test="$eventEndDateExist">
								<xsl:text>EndDate_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> "End Date For </xsl:text>
								<xsl:value-of select="$eventName" />
								<xsl:text>(</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text>)"</xsl:text>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>

							<xsl:if test="$eventStatusExist">
								<xsl:text>Event Status_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> "Event Status For </xsl:text>
								<xsl:value-of select="$eventName" />
								<xsl:text>(</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text>)"</xsl:text>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>

							<xsl:if test="$ageExist">
								<xsl:text>Age_</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text> "Age For </xsl:text>
								<xsl:value-of select="$eventName" />
								<xsl:text>(</xsl:text>
								<xsl:value-of select="$E" />
								<xsl:value-of select="$eventPosition" />
								<xsl:text>_</xsl:text>
								<xsl:value-of select="@StudyEventRepeatKey" />
								<xsl:text>)"</xsl:text>
								<xsl:text>&#xa;</xsl:text>
							</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if
								test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">
								
								<xsl:if test="$eventLocationExist">
									<xsl:text>Location_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> "Location For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$eventStartDateExist">
									<xsl:text>StartDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> "Start Date For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$eventEndDateExist">
									<xsl:text>EndDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> "End Date For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$eventStatusExist">
									<xsl:text>Event Status_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> "Event Status For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$ageExist">
									<xsl:text>Age_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text> "Age For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:for-each>
			</xsl:when>
			<xsl:otherwise>
				<xsl:if test="$eventLocationExist">
					<xsl:text>Location_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "Location For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$eventStartDateExist">
					<xsl:text>StartDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "Start Date For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$eventEndDateExist">
					<xsl:text>EndDate_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "End Date For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$eventStatusExist">
					<xsl:text>Event Status_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "Event Status For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>

				<xsl:if test="$ageExist">
					<xsl:text>Age_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> "Age For </xsl:text>
					<xsl:value-of select="$eventName" />
					<xsl:text>(</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text>)"</xsl:text>
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
-->

<!--
	<xsl:template mode="itemDataValuesSPSS" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<xsl:variable name="crfPosition" select="position()" />
		<xsl:variable name="parentEvent" select=".." />
		<xsl:variable name="currentFormOID" select="@FormOID" />
		<xsl:apply-templates mode="studyEventDataColumnSPSS1" select="../odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="parentEvent" select="$parentEvent" />
		</xsl:apply-templates>
		<xsl:if test="$crfPosition = 1 ">
		<xsl:text>.</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>VALUE LABELS</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		</xsl:if>
		<xsl:apply-templates mode="itemColumnsData1" select="odm:ItemGroupData/odm:ItemData" >
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="studyEventDataColumnSPSS1" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:param name="crfPosition" />
		<xsl:param name="parentEvent" />
		<xsl:variable name="eventOID" select="@StudyEventOID" />
		<xsl:variable name="eventPosition" select="position()" />
		<xsl:variable name="eventName"
			select="//odm:StudyEventDef[@OID=$eventOID]/@Name" />
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
						<xsl:for-each select="//odm:StudyEventData">
							<xsl:sort select="@StudyEventRepeatKey" data-type="number" />
							<xsl:copy-of select="." />
						</xsl:for-each>
					</xsl:variable>
					<xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
						<xsl:choose>
							<xsl:when test="position()=1">
								<xsl:if test="$interviewerNameExist">
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
									<xsl:text> "Interviewer Name For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
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
									<xsl:text> "Interviewer Date For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$crfStatusExist">
									<xsl:text>CRF Version Status_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
									</xsl:if>
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:text> "CRF Version Status For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>

								<xsl:if test="$crfVersionExist">
									<xsl:text>Version Name_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
									</xsl:if>
									<xsl:text>_</xsl:text>
									<xsl:value-of select="$C" />
									<xsl:value-of select="$crfPosition" />
									<xsl:text> "Version Name For </xsl:text>
									<xsl:value-of select="$eventName" />
									<xsl:text>(</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:text>_</xsl:text>
									<xsl:value-of select="@StudyEventRepeatKey" />
									<xsl:text>)"</xsl:text>
									<xsl:text>&#xa;</xsl:text>
								</xsl:if>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if
									test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">
									<xsl:text>Interviewer_</xsl:text>
									<xsl:if test="$interviewerNameExist">
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
										<xsl:text> "Interviewer Name For </xsl:text>
										<xsl:value-of select="$eventName" />
										<xsl:text>(</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
										<xsl:text>)"</xsl:text>
										<xsl:text>&#xa;</xsl:text>
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
										<xsl:text> "Interviewer Date For </xsl:text>
										<xsl:value-of select="$eventName" />
										<xsl:text>(</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
										<xsl:text>)"</xsl:text>
										<xsl:text>&#xa;</xsl:text>
									</xsl:if>

									<xsl:if test="$crfStatusExist">
										<xsl:text>CRF Version Status_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:text> "CRF Version Status For </xsl:text>
										<xsl:value-of select="$eventName" />
										<xsl:text>(</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
										<xsl:text>)"</xsl:text>
										<xsl:text>&#xa;</xsl:text>
									</xsl:if>

									<xsl:if test="$crfVersionExist">
										<xsl:text>Version Name_</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
										<xsl:value-of select="$C" />
										<xsl:value-of select="$crfPosition" />
										<xsl:text> "Version Name For </xsl:text>
										<xsl:value-of select="$eventName" />
										<xsl:text>(</xsl:text>
										<xsl:value-of select="$E" />
										<xsl:value-of select="$eventPosition" />
										<xsl:text>_</xsl:text>
										<xsl:value-of select="@StudyEventRepeatKey" />
										<xsl:text>)"</xsl:text>
										<xsl:text>&#xa;</xsl:text>
									</xsl:if>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$interviewerNameExist">
						<xsl:text>Interviewer_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> "Interviewer Name for </xsl:text>
						<xsl:value-of select="$eventName" />
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>

					<xsl:if test="$interviewDateExist">
						<xsl:text>Interviewer date_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> "Interviewer Date For </xsl:text>
						<xsl:value-of select="$eventName" />
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>

					<xsl:if test="$crfStatusExist">
						<xsl:text>CRF Version Status_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> "CRF Version Status For </xsl:text>
						<xsl:value-of select="$eventName" />
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>

					<xsl:if test="$crfVersionExist">
						<xsl:text>Version Name_</xsl:text>
						<xsl:value-of select="$E" />
						<xsl:value-of select="$eventPosition" />
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$C" />
						<xsl:value-of select="$crfPosition" />
						<xsl:text> "Version Name For </xsl:text>
						<xsl:value-of select="$eventName" />
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
		
		
	</xsl:template>
-->
<!--
	<xsl:template mode="itemDataForItemDef" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">
		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
		<xsl:variable name="itemData" select="." />
		<xsl:variable name="itemOID" select="@ItemOID" />
		<xsl:apply-templates mode="itemDataDef1"
			select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]">
			<xsl:with-param name="itemData" select="$itemData" />
			<xsl:with-param name="itemOID" select="$itemOID" />
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
		</xsl:apply-templates>
	</xsl:template>
-->	
<!--
	<xsl:template mode="itemDataDef1" match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef">
		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
		<xsl:param name="itemData" />
		<xsl:param name="itemOID" />
		<xsl:variable name="formOID"
			select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID" />
		<xsl:if test="$currentFormOID = $formOID">
			<xsl:value-of select="@Name" />
			<xsl:text>_</xsl:text>
			<xsl:value-of select="$C" />
			<xsl:value-of select="$crfPosition" />
			<xsl:text>_</xsl:text>
			<xsl:variable name="group" select="$itemData/parent::node()" />
			<xsl:variable name="groupOID" select="$group/@ItemGroupOID" />
			<xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
				<xsl:if test="@Name !='Ungrouped'">
					<xsl:value-of select="@Name" />
				</xsl:if>
			</xsl:for-each>
			<xsl:if test="$group/@ItemGroupRepeatKey">
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$group/@ItemGroupRepeatKey" />
			</xsl:if>
			<xsl:text> "</xsl:text>
			<xsl:value-of select="@Comment" />
			<xsl:text>"</xsl:text>
			<xsl:text>&#xa;</xsl:text>

		</xsl:if>
	</xsl:template>
	-->
<!--****************************************************************************************************** -->
	<!-- Starting Columns and its labels -->
	<!--****************************************************************************************************** -->
	<!--
	<xsl:template mode="allColumnsandLabels" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<xsl:variable name="crfPosition" select="position()" />
		<xsl:variable name="currentFormOID" select="@FormOID" />
		<xsl:apply-templates select="odm:ItemGroupData/odm:ItemData"
			mode="itemColumnsData1">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />

		</xsl:apply-templates>
	</xsl:template>
	-->
<!--
	<xsl:template mode="itemColumnsData1" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">
		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
		<xsl:variable name="itemData" select="." />
		<xsl:variable name="itemOID" select="@ItemOID" />
		<xsl:apply-templates mode="itemDefColumnValues" select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
			<xsl:with-param name="itemData" select="$itemData" />
			<xsl:with-param name="itemOID" select="$itemOID" />
		</xsl:apply-templates>
	</xsl:template>
	
	<xsl:template mode="itemDefColumnValues" match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef">
			<xsl:param name="crfPosition" />
			<xsl:param name="currentFormOID" />
			<xsl:param name="itemData"/>
			<xsl:param name="itemOID"  />
		<xsl:variable name="formOID"
			select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID" />
		<xsl:if test="$currentFormOID = $formOID">
			<xsl:value-of select="@Name" />
			<xsl:text>_</xsl:text>
			<xsl:value-of select="$C" />
			<xsl:value-of select="$crfPosition" />
			<xsl:text>_</xsl:text>
			<xsl:variable name="group" select="$itemData/parent::node()" />
			<xsl:variable name="groupOID" select="$group/@ItemGroupOID" />
			<xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
				<xsl:if test="@Name !='Ungrouped'">
					<xsl:value-of select="@Name" />
				</xsl:if>
			</xsl:for-each>
			<xsl:if test="$group/@ItemGroupRepeatKey">
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$group/@ItemGroupRepeatKey" />
			</xsl:if>
			<xsl:text>&#xa;</xsl:text>
			<xsl:variable name="codeListOID" select="./odm:CodeListRef/@CodeListOID" />
			<xsl:for-each select="//odm:CodeList[@OID=$codeListOID]">
				<xsl:for-each select="./odm:CodeListItem">
					<xsl:value-of select="@CodedValue" />
					<xsl:text> "</xsl:text>
					<xsl:value-of select="./odm:Decode/odm:TranslatedText" />
					<xsl:text>"</xsl:text>
					<xsl:if test="position()!=last()">
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>
				</xsl:for-each>
			</xsl:for-each>
			<xsl:text>&#xa;</xsl:text>
			<xsl:text>/</xsl:text>
			<xsl:text>&#xa;</xsl:text>

		</xsl:if>
	</xsl:template>
-->
</xsl:stylesheet>