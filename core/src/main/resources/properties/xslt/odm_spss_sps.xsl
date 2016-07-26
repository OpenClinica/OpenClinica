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
        <xsl:variable name="yearOfBirthExist"
		select="//odm:SubjectData/@OpenClinica:YearOfBirth" />
	<xsl:variable name="subjectStatusExist" select="//odm:SubjectData/@OpenClinica:Status" />
	<xsl:variable name="subjectSecondaryIdExist" select="//odm:SubjectData/@OpenClinica:SecondaryID"/>
	
	<xsl:variable name="allStudyEventDataElements"
		select="//odm:StudyEventData" />
	<xsl:variable name="allFormRefElements"
		select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef/odm:FormRef" />
		<xsl:variable name="allSubjects"
		select="//odm:ODM/odm:ClinicalData/odm:SubjectData" />
		<xsl:variable name="allItemGrpDataDataElements" select="//odm:ItemGroupData"/>
		<xsl:variable name="allItemDataElements" select="//odm:ItemData"/>			
	
	<xsl:template match="/">
<xsl:variable name="crfPosition" select="position()"/>
		<xsl:variable name="seperator" select="'&#xa;'"></xsl:variable>
		<xsl:text>* NOTE: If you have put this file in a different folder </xsl:text><xsl:value-of select="$seperator" />
		<xsl:text>* from the associated data file, you will have to change the FILE </xsl:text><xsl:value-of select="$seperator" />
		<xsl:text>* location on the line below to point to the physical location of your data file.</xsl:text>
		<xsl:value-of select="$seperator"/>
		<xsl:text>GET DATA  /TYPE = TXT/FILE = 'SPSS_DAT</xsl:text>
		<!--<xsl:text>GET DATA  /TYPE = TXT/FILE = '</xsl:text>-->
		<!--'All_Items_SPSS_data_spss.dat'-->
		<!-- @pgawade 29-Feb-2012 fix for issue #11796 Do not include data timestamp in the name of .dat file name -->		
		<!-- <xsl:variable name="currentDate" select="current-date()"/>
		<xsl:variable name="currentDateTime" select="current-dateTime()"/>		
		<xsl:value-of select="format-dateTime($currentDateTime, '[Y0001]-[M01]-[D01]-[H01][m01][s00001]')"/> -->

<!--<xsl:text>C:\tmp\oc_extract_issues\temp</xsl:text>-->
		<xsl:text>.dat' /DELCASE = LINE /DELIMITERS = "\t" /ARRANGEMENT = DELIMITED /FIRSTCASE = 2 /IMPORTCASE = ALL /VARIABLES =</xsl:text>
		<xsl:value-of select="$seperator" />
		
		<xsl:call-template name="subjectDataColumnHeaders"/>
		<!--<xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[1]"
			mode="studyDataSPSS"></xsl:apply-templates>-->
			<xsl:apply-templates mode="studyDataSPSS2" select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef"/>

		<!--<xsl:apply-templates mode="formDataSPSS" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID)[1])]">
			<xsl:with-param name="crfPosition" select="$crfPosition"></xsl:with-param>
			</xsl:apply-templates>-->
		<xsl:apply-templates mode="studyFormAndDataItemsHeaders" select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef"/>
		
		<xsl:text>.</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>VARIABLE LABELS</xsl:text>		
		<xsl:text>&#xa;</xsl:text>
		<xsl:call-template name="subjectDataColumnSPSS"></xsl:call-template>
		<!--xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]"
			mode="studyEventDataColumnSPSS" />-->
	
			<xsl:apply-templates
			select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef"
			mode="studyEventDataColumnSPSS2" />

		<!--<xsl:apply-templates mode="itemDataValuesSPSS" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID)[1])]"/>-->
		<xsl:apply-templates mode="studyFormAndDataItemsHeaders" select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
			<xsl:with-param name="calledFor" select="'itemDataValuesSPSS'"/>
		</xsl:apply-templates>
		
		<xsl:text>.</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		<xsl:text>VALUE LABELS</xsl:text>
		<xsl:text>&#xa;</xsl:text>
					
		<xsl:apply-templates mode="studyItemDataColumnHeaders" select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">			
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

	<xsl:template mode="studyDataSPSS2" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
	
	<xsl:variable name="eventDefOID" select="@OID"/>
	<xsl:variable name="isRepeating" select="@Repeating"/>
	<xsl:variable name="allStudyEventDataElements" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID = $eventDefOID]"/>	
	<!--<xsl:variable name="eventLocationExist" select="count($allStudyEventDataElements[@OpenClinica:StudyEventLocation]) &gt; 0"/>
	<xsl:variable name="eventStartDateExist" select="count($allStudyEventDataElements[@OpenClinica:StartDate]) &gt; 0"/>
	<xsl:variable name="eventStatusExist" select="count($allStudyEventDataElements[@OpenClinica:Status]) &gt; 0"/>
	<xsl:variable name="eventEndDateExist" select="count($allStudyEventDataElements[@OpenClinica:EndDate]) &gt; 0"/>
	<xsl:variable name="ageExist" select="count($allStudyEventDataElements[@OpenClinica:SubjectAgeAtEvent]) &gt; 0"/>-->
		
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
		<xsl:when test="$isRepeating = 'Yes'"><!--{repeating event}-->
			<!-- write event data header columns for repeating event -->
			<xsl:apply-templates
				select="."
				mode="studyDataSPSSForRepeatingEvent" >
				<xsl:with-param name="eventRepeatCnt" select="1"/>
				<xsl:with-param name="eventOID"  select="$eventDefOID"/>
				<xsl:with-param name="eventPosition" select="$eventPosition" />
				<!-- <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>-->
				<xsl:with-param name="locationLen" select="$locationLen"/>
				<xsl:with-param name="ageLen" select="$ageLen"/>
				<xsl:with-param name="eventStatusLen" select="$eventStatusLen"/>
				<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
			</xsl:apply-templates>	
		</xsl:when>
		<xsl:otherwise>	
			<!-- write event data header columns for non repeating event -->
			<xsl:apply-templates select="." mode="studyDataSPSSForNonRepeatingEvent">
				<xsl:with-param name="eventPosition" select="$eventPosition"/>
				<xsl:with-param name="eventOID"  select="$eventDefOID"/>
			  <!-- <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>-->
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
	   <!--<xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist" />
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>		-->
		<xsl:param name="eventRepeatCnt" />
		<xsl:param name="locationLen"/>
		<xsl:param name="ageLen"/>
		<xsl:param name="eventStatusLen"/>
		<xsl:param name="MaxEventRepeatKey"/><!--studyDataSPSSForRepeatingEvent, eventPosition:<xsl:value-of select="$eventPosition"/>-->
		<!--{studyDataSPSSForRepeatingEvent}-->
		<!--<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0">		-->
		<!--{cnt greater than 0}	-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:StudyEventLocation]) &gt; 0">
				<!--<xsl:if test="$eventLocationExist">-->
				<xsl:text>Location_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> A</xsl:text>
				<!--<xsl:choose>
					<xsl:when test="number($locationLen) &gt; 8">
						<xsl:text>8</xsl:text>
					</xsl:when>
					<xsl:otherwise>-->
						<xsl:value-of select="$locationLen" />
					<!--</xsl:otherwise>
				</xsl:choose>-->
				<xsl:text>&#xa;</xsl:text>
		</xsl:if>

			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:StartDate]) &gt; 0"><!--col for event startdate--><!--start date present-->
			<!--<xsl:if test="$eventStartDateExist">-->
				<xsl:text>StartDate_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> ADATE10</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<!--<xsl:if test="$eventEndDateExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:EndDate]) &gt; 0"><!--end date presen-->
				<xsl:text>EndDate_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<!-- @pgawade 14-Mar-2012 Corrected the variable used for event repeat -->
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> ADATE10</xsl:text>
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<!--<xsl:if test="$eventStatusExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:Status]) &gt; 0">
				<xsl:text>EventStatus_</xsl:text>
				<xsl:value-of select="$E" />
				<xsl:value-of select="$eventPosition" />
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$eventRepeatCnt" />
				<xsl:text> A</xsl:text>
				<!--<xsl:choose>
					<xsl:when test="number($eventStatusLen) &gt; 8">
						<xsl:text>8</xsl:text>
					</xsl:when>
					<xsl:otherwise>-->
						<xsl:value-of select="$eventStatusLen" />
					<!--sl:otherwise>
				</xsl:choose>-->
				<xsl:text>&#xa;</xsl:text>
			</xsl:if>

			<!--<xsl:if test="$ageExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:SubjectAgeAtEvent]) &gt; 0">
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
			
			<!-- fix for issue 11832: corrected to repeat the process for next incremental event repeat key until it reaches the value of "MaxEventRepeatKey" -->
			<xsl:if test="($eventRepeatCnt+1) &lt;= number($MaxEventRepeatKey)">			
				<xsl:call-template name="studyDataSPSSForRepeatingEvent">
					<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
					<xsl:with-param name="eventOID"  select="$eventOID"/>
					<xsl:with-param name="eventPosition" select="$eventPosition" />	
					<!--<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
					<xsl:with-param name="eventStartDateExist" select="$eventStartDateExist"/>
					<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
					<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
					<xsl:with-param name="ageExist" select="$ageExist"/>-->
					<xsl:with-param name="locationLen" select="$locationLen"/>
					<xsl:with-param name="ageLen" select="$ageLen"/>
					<xsl:with-param name="eventStatusLen" select="$eventStatusLen"/>
					<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>						
				</xsl:call-template>
			</xsl:if>	
	<!--	</xsl:if>	-->
   </xsl:template>
   
   <xsl:template mode="studyDataSPSSForNonRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" >
	   <xsl:param name="eventPosition"/>
	   <!--<xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist" />
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>	  -->
		<xsl:param name="eventOID"/>
		<xsl:param name="locationLen"/>
		<xsl:param name="ageLen"/>	  
		<xsl:param name="eventStatusLen"/>
		
		<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
	   <!--<xsl:if test="$eventLocationExist">-->
	   <xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:StudyEventLocation]) &gt; 0">
					<xsl:text>Location_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> A</xsl:text>					
							<xsl:value-of select="$locationLen" />
						<!--</xsl:otherwise>
					</xsl:choose>-->
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>
				
				<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
				<!--<xsl:if test="$eventStartDateExist">-->
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:StartDate]) &gt; 0">
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
				
				<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
				<!--<xsl:if test="$eventEndDateExist">-->
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:EndDate]) &gt; 0">
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
				
				<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
				<!-- <xsl:if test="$eventStatusExist">-->
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:Status]) &gt; 0">
					<xsl:text>EventStatus_</xsl:text>
					<xsl:value-of select="$E" />
					<xsl:value-of select="$eventPosition" />
					<xsl:text> A</xsl:text>
					<!--<xsl:choose>
						<xsl:when test="number($eventStatusLen) &gt; 8">
							<xsl:text>8</xsl:text>
						</xsl:when>
						<xsl:otherwise>-->
							<xsl:value-of select="$eventStatusLen" />
						<!--</xsl:otherwise>
					</xsl:choose>-->
					<xsl:text>&#xa;</xsl:text>
				</xsl:if>
				
				<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
				<!--<xsl:if test="$ageExist">-->
				<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:SubjectAgeAtEvent]) &gt; 0">
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
	<!--<xsl:variable name="eventLocationExist" select="count($allStudyEventDataElements[@OpenClinica:StudyEventLocation]) &gt; 0"/>
	<xsl:variable name="eventStartDateExist" select="count($allStudyEventDataElements[@OpenClinica:StartDate]) &gt; 0"/>
	<xsl:variable name="eventStatusExist" select="count($allStudyEventDataElements[@OpenClinica:Status]) &gt; 0"/>
	<xsl:variable name="eventEndDateExist" select="count($allStudyEventDataElements[@OpenClinica:EndDate]) &gt; 0"/>
	<xsl:variable name="ageExist" select="count($allStudyEventDataElements[@OpenClinica:SubjectAgeAtEvent]) &gt; 0"/>	-->
	<xsl:variable name="studyEventData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID = $eventDefOID]"/>
	<xsl:variable name="eventName" select="@Name"/>
	
		
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
				<!-- <xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>-->
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
				<xsl:with-param name="eventOID"  select="$eventDefOID"/>
			   <!--<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist"  select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>
				<xsl:with-param name="locationLen" select="$locationLen"/>
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
		<xsl:param name="eventRepeatCnt" />
		<!--<xsl:param name="locationLen"/>
		<xsl:param name="ageLen"/>
		<xsl:param name="eventStatusLen"/>-->
		
		<xsl:param name="eventName"/>
		
		<!--<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0">		-->
			<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
			<!--<xsl:if test="$eventLocationExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:StudyEventLocation]) &gt; 0">
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
			
			<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->	
			<!--<xsl:if test="$eventStartDateExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:StartDate]) &gt; 0">
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

			<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->		
			<!--<xsl:if test="$eventEndDateExist">eventEndDateExist-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:EndDate]) &gt; 0">
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

			<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
			<!--<xsl:if test="$eventStatusExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:Status]) &gt; 0">
			<!-- @pgawade 14-Mar-2012 #13052 Removed the unwanted space within value label for event status -->
				<xsl:text>EventStatus_</xsl:text>
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

			<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
			<!--<xsl:if test="$ageExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:SubjectAgeAtEvent]) &gt; 0">
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
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = ($eventRepeatCnt+1)]) &gt; 0">				
			<xsl:call-template name="studyEventDataColumnSPSSForRepeatingEvent">
				<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
				<xsl:with-param name="eventOID"  select="$eventOID"/>
				<xsl:with-param name="eventPosition" select="$eventPosition" />	
				<!--<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
				<xsl:with-param name="eventStartDateExist" select="$eventStartDateExist"/>
				<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
				<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
				<xsl:with-param name="ageExist" select="$ageExist"/>	-->
				<xsl:with-param name="eventName"  select="$eventName"/>	
							
			</xsl:call-template>
		</xsl:if>	
   </xsl:template>
   
   <xsl:template mode="studyEventDataColumnSPSSForNonRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" >
	   <xsl:param name="eventPosition"/>	   
		<xsl:param name="eventName"/>
		<xsl:param name="eventOID"/>
		
		<!-- @pgawade 15-May-2012 fix for issue 14279 consider the presense of event attribute specific to ordinal -->
		<!--   <xsl:if test="$eventLocationExist">-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:StudyEventLocation]) &gt; 0">
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

			<!--	<xsl:if test="$eventStartDateExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:StartDate]) &gt; 0">
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

			<!--	<xsl:if test="$eventEndDateExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:EndDate]) &gt; 0">
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

			<!--	<xsl:if test="$eventStatusExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:Status]) &gt; 0">
					<!-- @pgawade 14-Mar-2012 #13052 Removed the unwanted space within value label for event status -->
				<xsl:text>EventStatus_</xsl:text>
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

			<!--	<xsl:if test="$ageExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:SubjectAgeAtEvent]) &gt; 0">
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
			<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $eventOID">
					<xsl:copy-of select="position()" />
				</xsl:if>	
			</xsl:for-each>
		</xsl:variable>
		<!-- calculate form def position in metadata -->		
		
		<!-- maximum value of StudyEventRepeatKey for an event -->
		<xsl:variable name="MaxEventRepeatKey">
			<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/@StudyEventRepeatKey">
				<xsl:sort data-type="number"/>
				<xsl:if test="position() = last()">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:apply-templates mode="studyFormColumnHeaders" select=".">
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="isRepeatingEvent" select="$isRepeatingEvent"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="calledFor" select="$calledFor"/>	
			<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>	
		</xsl:apply-templates>
		<!-- apply template for item data columns -->
		<xsl:apply-templates mode="studyItemDataColumnHeaders" select=".">			
			<xsl:with-param name="eventOID" select="$eventOID"/>	
			<xsl:with-param name="isEventRepeating" select="$isRepeatingEvent"/>	
			<xsl:with-param name="calledFor" select="$calledFor"/>	
			<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>		
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
		<xsl:param name="MaxEventRepeatKey"/>
		
		<xsl:variable name="eventName" select="@Name"/>
		<xsl:choose>
			<xsl:when test="$isRepeatingEvent = 'Yes'">			
				<!-- create CRF columns for repeating event -->		
				<xsl:apply-templates select="." mode="createCRFColForRepeatingEvent">
					<xsl:with-param name="eventOID" select="$eventOID"/>   
				   <xsl:with-param name="eventPosition" select="$eventPosition"/>
				   <xsl:with-param name="eventRepeatCnt" select="1"/>		
				   	<xsl:with-param name="calledFor" select="$calledFor"/>	
				   	<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>  						
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
					
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
					@OpenClinica:Version]) 	
						&gt; 0"/>
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewerName]) &gt; 0"/>
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewDate]) &gt; 0"/>
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
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
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
					@OpenClinica:Version]) 	
						&gt; 0"/>
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewerName]) &gt; 0"/>
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewDate]) &gt; 0"/>
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
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
						<!--<xsl:choose>
							<xsl:when test="$interLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>-->
								<xsl:value-of select="$interLen" />
							<!--</xsl:otherwise>
						</xsl:choose>-->
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
						<!--<xsl:choose>
							<xsl:when test="$interStatusLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>-->
								<xsl:value-of select="$interStatusLen" />
							<!--</xsl:otherwise>
						</xsl:choose>-->
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
						<!--<xsl:choose>
							<xsl:when test="$versionLen &gt; 8">
								<xsl:text>8</xsl:text>
							</xsl:when>
							<xsl:otherwise>-->
								<xsl:value-of select="$versionLen" />
							<!--</xsl:otherwise>
						</xsl:choose>-->
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
	    <xsl:param name="MaxEventRepeatKey"/>
	    
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
				<!-- @pgawade 14-Mar-2012 commented out display of text "VALUE LABELS" which seems unwanted here. Probably it should be "VARIABLE LABELS" -->
				<!--	<xsl:if test="$crfPosition = 1 ">
						<xsl:text>.</xsl:text>
						<xsl:text>&#xa;</xsl:text>
						<xsl:text>VALUE LABELS</xsl:text>
						<xsl:text>&#xa;</xsl:text>
					</xsl:if>	-->
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and @OpenClinica:Version 
					and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewerName  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
						
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewDate  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
						
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and @OpenClinica:Status  and 
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
									<xsl:value-of select="$eventRepeatCnt" />
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
									<xsl:value-of select="$eventRepeatCnt" />
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
									<xsl:value-of select="$eventRepeatCnt" />
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
									<xsl:value-of select="$eventRepeatCnt" />
									<xsl:text>)"</xsl:text>
									<xsl:text> /</xsl:text>
									<xsl:text>&#xa;</xsl:text>
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
						 <xsl:with-param name="calledFor" select="$calledFor"/>
						 <xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
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
					<xsl:variable name="crfVersionExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and @OpenClinica:Version 
					and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
					
					<xsl:variable name="interviewerNameExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewerName  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
						
					<xsl:variable name="interviewDateExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and 
						@OpenClinica:InterviewDate  and ../@StudyEventRepeatKey = $eventRepeatCnt]) &gt; 0"/>
						
					<xsl:variable name="crfStatusExist" select="count(//odm:FormData[../@StudyEventOID = $eventOID and @FormOID = $formRefOID and @OpenClinica:Status  and 
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
								<!--<xsl:choose>
									<xsl:when test="$interLen &gt; 8">
										<xsl:text>8</xsl:text>
									</xsl:when>
									<xsl:otherwise>-->
										<xsl:value-of select="$interLen" />
								<!--	</xsl:otherwise>
								</xsl:choose>-->
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
								<!--<xsl:choose>
									<xsl:when test="$interStatusLen &gt; 8">
										<xsl:text>8</xsl:text>
									</xsl:when>
									<xsl:otherwise>-->
										<xsl:value-of select="$interStatusLen" />
									<!--</xsl:otherwise>
								</xsl:choose>-->
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
											<!--<xsl:choose>
												<xsl:when test="$versionLen &gt; 8">
													<xsl:text>8</xsl:text>
												</xsl:when>
												<xsl:otherwise>-->
													<xsl:value-of select="$versionLen" />
											<!--	</xsl:otherwise>
											</xsl:choose>-->
											<xsl:text>&#xa;</xsl:text>
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
						<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
					</xsl:call-template>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>		
   </xsl:template>
   
   <xsl:template  mode="studyItemDataColumnHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
	   <xsl:param name="calledFor" />
		<xsl:param name="eventOID" /> 
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="MaxEventRepeatKey"/>
		
			<!-- {studyItemDataColumnHeaders, eventOID:<xsl:value-of select="./@OID"/>}-->
		<xsl:variable name="currentEventOID"	 select="./@OID"/>
		<xsl:variable name="currentEventIsRepeating"	 select="./@Repeating"/>
	<xsl:variable name="eventPosition">
			<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $currentEventOID">
					<xsl:copy-of select="position()" />
				</xsl:if>	
			</xsl:for-each>
		</xsl:variable>  	
    
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
						
				<xsl:apply-templates mode="formRefToDefTemplateForHeaders" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID = $formRefOID]" >
					<xsl:with-param name="crfPosition" select="$crfPosition"/>
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
					<xsl:with-param name="eventOID" select="$eventOID"/>
					<xsl:with-param name="StudyEventRepeatKey" select="$eventRepeatCnt"/>
					<xsl:with-param name="calledFor" select="$calledFor"/>
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
				<xsl:with-param name="calledFor" select="$calledFor"/>	   
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
		<xsl:param name="calledFor"/>
		<!--{ItemGrpRefToDefTemplateForHeaders}
		grpOID:<xsl:value-of select="$grpOID"/>	-->
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
		<!--{createItemDataColForRepeatingGrps}
		eventOID:*<xsl:value-of select="$eventOID"/>*
		formOID:*<xsl:value-of select="$formOID"/>*
		grpOID:*<xsl:value-of select="$grpOID"/>*
		itemGrpRepeatKey:*<xsl:value-of select="$itemGrpRepeatKey"/>*
		
		cnt21:<xsl:value-of select="count($allItemGrpDataDataElements[../../@StudyEventOID = $eventOID 
						and ../@FormOID = $formOID and @ItemGroupOID = $grpOID 
						and @ItemGroupRepeatKey = $itemGrpRepeatKey])"/>-->
		<!--<xsl:variable name="maxGrpRepeatKey">
				<xsl:for-each select="$allItemGrpDataDataElements[../../@StudyEventOID = $eventOID and ../../@StudyEventRepeatKey = $StudyEventRepeatKey 
						and ../@FormOID = $formOID and @ItemGroupOID = $grpOID ]/@ItemGroupRepeatKey">
						
				<xsl:sort data-type="number"/>
				   <xsl:if test="position() = last()">
					 <xsl:value-of select="."/>
				   </xsl:if>
				  </xsl:for-each>
			</xsl:variable>	-->		
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
								<xsl:with-param name="calledFor" select="$calledFor"/>
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
								<xsl:with-param name="calledFor" select="$calledFor"/>
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
						and @ItemGroupRepeatKey = $itemGrpRepeatKey]) &gt; 0"><!--go ahead-->
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
		<!--{GrpItemRefs}grpOID:<xsl:value-of select="$grpOID"/>-->
		<xsl:variable name="itemOID" select="@ItemOID"/>
		<xsl:choose>
			<xsl:when test="$isEventRepeating = 'Yes'">
			<!--	<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $StudyEventRepeatKey and odm:FormData/@FormOID = 
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
				<xsl:with-param name="calledFor" select="$calledFor"/>
				<xsl:with-param name="groupOID" select="$grpOID"/>
				<xsl:with-param name="formOID" select="$formOID"/>
			</xsl:apply-templates>
		</xsl:if>	-->
		<xsl:choose>
				<xsl:when test="$isGrpRepeating = 'Yes'"><!--repeating grp-->
				 
				<!--cnt123:<xsl:value-of select="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../@ItemGroupRepeatKey =$itemGrpRepeatKey and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID and ../../../@StudyEventRepeatKey = $StudyEventRepeatKey]) "/>-->
						
					<xsl:if test="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../@ItemGroupRepeatKey =$itemGrpRepeatKey and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID and ../../../@StudyEventRepeatKey = $StudyEventRepeatKey]) &gt; 0"><!--create col-->
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
						<!--<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>-->
						<!-- @pgawade 14-Mar-2012 Added the "calledFor" parameter to pass on to print the codelists in "VALUE LABELS" section correctly -->
						<xsl:with-param name="calledFor" select="$calledFor"/>
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
						<!--<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>-->
						<!-- @pgawade 14-Mar-2012 Added the "calledFor" parameter to pass on to print the codelists in "VALUE LABELS" section correctly -->
						<xsl:with-param name="calledFor" select="$calledFor"/>
					</xsl:apply-templates>
				</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
			</xsl:when>
			<xsl:otherwise><!--{otherwise}-->
				<xsl:choose>
				<xsl:when test="$isGrpRepeating = 'Yes'"><!--repeating grp-->
					<!--eventOID:<xsl:value-of select="$eventOID"/>
			formOID:<xsl:value-of select="$formOID"/>
			grpOID:<xsl:value-of select="$grpOID"/>
			itemOID:<xsl:value-of select="$itemOID"/>	
			cnt2: <xsl:value-of select="count($allItemDataElements[../../../@StudyEventOID = $eventOID and ../../@FormOID = 
						$formOID and ../@ItemGroupOID = $grpOID and ../@ItemGroupRepeatKey = $itemGrpRepeatKey and @ItemOID = $itemOID])"/>
			-->			
				<xsl:if test="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../@ItemGroupRepeatKey =$itemGrpRepeatKey and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID])&gt; 0">
						
						
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
					<xsl:with-param name="isLastItem" select="$isLastItem" />
					<xsl:with-param name="calledFor" select="$calledFor"/>
					<xsl:with-param name="groupOID" select="$grpOID"/>
					<xsl:with-param name="formOID" select="$formOID"/>
				</xsl:apply-templates>
		</xsl:if>
				</xsl:when>
				<xsl:otherwise>
			<!--		eventOID:<xsl:value-of select="$eventOID"/>
			formOID:<xsl:value-of select="$formOID"/>
			grpOID:<xsl:value-of select="$grpOID"/>
			itemOID:<xsl:value-of select="$itemOID"/>	
			cnt2: <xsl:value-of select="count($allItemDataElements[../../../@StudyEventOID = $eventOID and ../../@FormOID = 
						$formOID and ../@ItemGroupOID = $grpOID and @ItemOID = $itemOID])"/>
			-->
				<xsl:if test="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID]) &gt; 0">
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
					<xsl:with-param name="isLastItem" select="$isLastItem" />
					<xsl:with-param name="calledFor" select="$calledFor"/>
					<xsl:with-param name="groupOID" select="$grpOID"/>
					<xsl:with-param name="formOID" select="$formOID"/>
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
		<xsl:param name="calledFor"/>
		<xsl:param name="groupOID"/>
		<xsl:param name="formOID"/>	
		<xsl:param name="isLastItem"/>	
		
		<xsl:variable name="dataType" select="@DataType" />
		<xsl:variable name="length" select="@Length"/>
		<xsl:variable name="significantDigits" select="@SignificantDigits"/>
		
	<!--{ItemDefColHeaders2: <xsl:value-of select="$calledFor"/>}-->
	<!-- @pgawade 11-May-2012 Fix for issue #13613 -->
	<xsl:variable name="itemName" select="@Name"/>
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
			<xsl:when test="$calledFor = 'itemDataValuesSPSS'">
			<xsl:variable name="comment" select="@Comment"/>
				<xsl:value-of select="' '"/>
				<!--<xsl:value-of select="@Name" />-->
				<!--<xsl:value-of select='replace(normalize-space(@Name), "\s", "_")'/>-->
				<xsl:value-of select='replace(normalize-space($itemNameValidated), "\s", "_")'/>
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$E"/>	<xsl:value-of select="$eventPosition"/>
				<xsl:if test="$isEventRepeating = 'Yes'">
					<xsl:text>_</xsl:text><xsl:value-of select="$StudyEventRepeatKey"/>
				</xsl:if>
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$C" />
				<xsl:value-of select="$crfPosition" />
				<!--<xsl:text>_</xsl:text>-->
				<!--<xsl:variable name="group" select="$itemData/parent::node()" />
				<xsl:variable name="groupOID" select="$group/@ItemGroupOID" />-->
			<!--<xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
				<xsl:if test="@Name !='Ungrouped'">
					<xsl:value-of select="@Name" />
				</xsl:if>
			</xsl:for-each>-->
			<xsl:if test="$isGrpRepeating = 'Yes'">
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$itemGrpRepeatKey" />
			</xsl:if>
			<xsl:if test="$isLastItem">
				<xsl:value-of select="' '"/>
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
				<!--<xsl:value-of select='replace(normalize-space(@Name), "\s", "_")'/>-->
				<xsl:value-of select='replace(normalize-space($itemNameValidated), "\s", "_")'/>
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
					<!-- @pgawade 20-Mar-2012 Added the response option value inside single quotes to work properly with SPSS tool -->
						<xsl:text>'</xsl:text><xsl:value-of select="@CodedValue" /><xsl:text>'</xsl:text>
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
			<xsl:value-of select="' '"/>
			<!--<xsl:value-of select='replace(normalize-space(@Name), "\s", "_")'/>-->
			<xsl:value-of select='replace(normalize-space($itemNameValidated), "\s", "_")'/>
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
			<xsl:if test="$isLastItem">
				<xsl:value-of select="' '"/>
			</xsl:if>
			<!-- @pgawade 20-Mar-2012  #13109 Fixes to get the correct mapping between OpenClinica and SPSS data types -->
			<xsl:choose>
				<xsl:when test="$dataType='date'">
					<xsl:text> ADATE10</xsl:text>
				</xsl:when>
				<xsl:when test="$dataType='float'">									
					<xsl:text> F</xsl:text><xsl:value-of select="$length"/><xsl:text>.</xsl:text><xsl:value-of select="$significantDigits"/>
				</xsl:when>				
				<xsl:when test="$dataType='integer'">
					<xsl:text> F</xsl:text><xsl:value-of select="$length"/><xsl:text>.0</xsl:text>
				</xsl:when>
				<xsl:when test="$dataType='partialDate'">									
					<xsl:text> A</xsl:text><xsl:text>10.0</xsl:text>
				</xsl:when>
				<xsl:otherwise>
					<xsl:text> A</xsl:text>				
					<xsl:value-of select="$length" />										
				</xsl:otherwise>
			</xsl:choose>
			<xsl:text>&#xa;</xsl:text>
			</xsl:otherwise>
		</xsl:choose>
		
	</xsl:template>	

<xsl:template name="subjectDataColumnHeaders" mode="subjectDataColumnHeaders" match="/odm:ODM/odm:ClinicalData/odm:SubjectData" >
		
		<xsl:variable name="studyOID" select="../@StudyOID"/>
		<xsl:variable name="studyElement" select="//odm:Study[@OID = $studyOID]"/>
		<xsl:variable name="protocolName" select="$studyElement/odm:GlobalVariables/odm:ProtocolName"/>
		
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
		
		<xsl:text>ProtocolID</xsl:text>
		<xsl:variable name="valueLengthProtocolID">
			<xsl:for-each select="//odm:ODM/odm:Study/odm:GlobalVariables/odm:ProtocolName/string-length(.)">
				<xsl:sort data-type="number"/>
				<xsl:if test="position() = last()">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:text> A</xsl:text><xsl:value-of select="$valueLengthProtocolID" />
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
		
		<xsl:if test="$subjectSecondaryIdExist">
			<xsl:variable name="valueLength">
				<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/string-length(@OpenClinica:SecondaryID)">
					<xsl:sort data-type="number"/>
					<xsl:if test="position() = last()">
						<xsl:value-of select="."/>
					</xsl:if>
				</xsl:for-each>
			</xsl:variable>
			<xsl:text>SecondaryID</xsl:text>
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
			<!-- @pgawade 21-Mar-2012 #12213 - Changing data type for date of birth to ADATE10 -->			
			<xsl:text> ADATE10</xsl:text>
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>
                
                <xsl:if test="$yearOfBirthExist">
			<xsl:text>YearofBirth</xsl:text>
			<!-- @jrousseau 21-Mar-2014 OC-4783 - Check if the year of birth is imported correctly by SPSS with type F4 -->			
			<xsl:text> F4</xsl:text>
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
		
		<xsl:text>ProtocolID</xsl:text>
		<xsl:text> "Protocol ID"</xsl:text><xsl:text> /</xsl:text>
		<xsl:text>&#xa;</xsl:text>
		
		<!--<xsl:text>ProtocolID</xsl:text>
		<xsl:text> "Protocol ID_Site ID"</xsl:text>
		<xsl:text>&#xa;</xsl:text>-->
		<xsl:if test="$uniqueIdExist">
			<xsl:text>PersonID</xsl:text>
			<xsl:text> "Person ID"</xsl:text><xsl:text> /</xsl:text>
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>
		
		<xsl:if test="$subjectSecondaryIdExist">
			<xsl:text>SecondaryID</xsl:text>
			<xsl:text> "Secondary ID"</xsl:text><xsl:text> /</xsl:text>
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
                
                <xsl:if test="$yearOfBirthExist">
			<xsl:text>YearofBirth</xsl:text>
			<xsl:text> "Year of Birth"</xsl:text><xsl:text> /</xsl:text>
			<xsl:text>&#xa;</xsl:text>
		</xsl:if>
	</xsl:template>

	<!-- template to replace all invalid SPSS characters (characters other than any letter, any digit, a period, or the symbols @, #, _, or $) with # 
		This is a template meant for future use to handle the special case of if SPSS variables generated do not start with a letter. Associated issue number is 13583
-->
	
	<xsl:template name="replace-invalid-char">
		<xsl:param name="text"/>		
		<xsl:choose>
			<xsl:when test='matches($text, "[^0-9A-Za-z.@#_$]")'>
				<xsl:value-of select='replace(normalize-space($text), "[^0-9A-Za-z.@#_$]", "#")'/>
			</xsl:when>
			<xsl:otherwise>
				<xsl:value-of select="normalize-space($text)"/>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
</xsl:stylesheet>