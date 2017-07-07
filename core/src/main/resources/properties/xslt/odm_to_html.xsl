<?xml version="1.0" encoding="ISO-8859-1"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:exsl="http://exslt.org/common" extension-element-prefixes="exsl" xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3">
	<xsl:variable name="language">
		<xsl:text>en</xsl:text>
	</xsl:variable>
	<xsl:variable name="E" select="'E'"/>
	
	<xsl:variable name="itemNameAndEventSep" select="'x@x'"/>
	<xsl:variable name="C" select="'C'"/>
	<xsl:variable name="matchSep" select="'M_'"/>
	<xsl:variable name="nonMatchSep" select="'*N'"/>
	<xsl:variable name="datasetDesc" select="/odm:ODM/@Description"/>
	<xsl:variable name="study" select="/odm:ODM/odm:Study[1]"/>
	<xsl:variable name="protocolNameStudy" select="$study/odm:GlobalVariables/odm:ProtocolName"/>
	<xsl:variable name="studyName" select="$study/odm:GlobalVariables/odm:StudyName"/>
	<xsl:variable name="crfDetails" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef"/>
	<xsl:variable name="groupDetails" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef"/>
	<xsl:variable name="itemDef" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef"/>
	<xsl:variable name="codeList" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:CodeList"/>
	<xsl:variable name="allEventDefs" select="//odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef"/>
	<!-- fix for issue #12238 -->
	<!--<xsl:variable name="eventDefCount" select="count(/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef)"/>-->
	<xsl:variable name="eventDefCount"
			select="count(//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])])" />
			
	<xsl:variable name="allStudyEventDataElements" select="//odm:StudyEventData"/>
	<xsl:variable name="allItemGrpDataDataElements" select="//odm:ItemGroupData"/>
	<xsl:variable name="allFormRefElements" select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef/odm:FormRef"/>
	<xsl:variable name="allItemDataElements" select="//odm:ItemData"/>
	<!-- Getting the Dataset Name -->
	<xsl:variable name="fileName" select="/odm:ODM/@FileOID"/>
	<xsl:variable name="year" select="substring(/odm:ODM/@CreationDateTime, 1, 4)"/>
	<xsl:variable name="D_year" select="concat('D', $year)"/>
	<xsl:variable name="datasetName" select="substring-before($fileName, $D_year)"/>
	<xsl:variable name="desc" select="/odm:ODM/@Description"/>
	<xsl:variable name="subject_count" select="count(/odm:ODM/odm:ClinicalData/odm:SubjectData)"/>
	<xsl:key name="studyEvents" match="odm:StudyEventData" use="@StudyEventOID"/>
	<xsl:key name="eventCRFs" match="odm:FormData" use="@FormOID"/>
	<xsl:key name="itemDataKey" match="odm:ItemData" use="@ItemOID"/>
	
	<xsl:variable name="sexExist" select="//odm:SubjectData/@OpenClinica:Sex"/>
	<xsl:variable name="uniqueIdExist" select="//odm:SubjectData/@OpenClinica:UniqueIdentifier"/>
	<xsl:variable name="dobExist" select="//odm:SubjectData/@OpenClinica:DateOfBirth"/>
        <xsl:variable name="yearOfBirthExist" select="//odm:SubjectData/@OpenClinica:YearOfBirth"/>
	<xsl:variable name="subjectStatusExist" select="//odm:SubjectData/@OpenClinica:Status"/>
	<xsl:variable name="subjectSecondaryIdExist" select="//odm:SubjectData/@OpenClinica:SecondaryID"/>
	
	<xsl:variable name="eventLocationExist" select="//odm:StudyEventData/@OpenClinica:StudyEventLocation"/>
	<xsl:variable name="eventStartDateExist" select="//odm:StudyEventData/@OpenClinica:StartDate"/>
	<xsl:variable name="eventEndDateExist" select="//odm:StudyEventData/@OpenClinica:EndDate"/>
	<xsl:variable name="eventStatusExist" select="//odm:StudyEventData/@OpenClinica:Status"/>
	<xsl:variable name="ageExist" select="//odm:StudyEventData/@OpenClinica:SubjectAgeAtEvent"/>
	<xsl:variable name="crfVersionExist" select="//odm:FormData/@OpenClinica:Version"/>
	<xsl:variable name="interviewerNameExist" select="//odm:FormData/@OpenClinica:InterviewerName"/>
	<xsl:variable name="interviewDateExist" select="//odm:FormData/@OpenClinica:InterviewDate"/>
	<xsl:variable name="crfStatusExist" select="//odm:FormData/@OpenClinica:Status"/>
	<!-- Tokenization of column headers-->
	<xsl:variable name="eventColHeaders">
		<xsl:apply-templates select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef" mode="studyEventInfoHeaders">		
		</xsl:apply-templates>
	</xsl:variable>
	<xsl:variable name="tokenizedEventHeaders" select="tokenize($eventColHeaders,'_E')"/>
	<xsl:variable name="crfAndDataItemsHeaders">
		<xsl:apply-templates select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef" mode="studyFormAndDataItemsHeaders">
			<xsl:with-param name="generateIntHeadersList" select="'Yes'"/>
		</xsl:apply-templates>
	</xsl:variable>
	
	<xsl:variable name="tokenizedcrfAndDataItemsHeaders" select="tokenize($crfAndDataItemsHeaders,$itemNameAndEventSep)"/>
	
	<xsl:variable name="mValSeparator1" select="'_][_1'"/>
	<xsl:variable name="mValSeparator2" select="'_][_2'"/>
	<xsl:variable name="mValSeparator3" select="'_][_3'"/>
	<xsl:variable name="mValSeparator4" select="'_][_4'"/>
	<xsl:variable name="mValSeparator5" select="'_][_5'"/>
	<xsl:strip-space elements="*"/>
	<xsl:template match="/">
		<html>
			<script language="JavaScript1.2">
				function openWin(ele) {
				
				newwin =
				window.open('', 'windowname1', 'width=900', 'height=500','scrollbars=1');
				var divTxt = document.getElementById(ele);
				var newdiv
				= newwin.document.createElement('div');
				newdiv.innerHTML =
				divTxt.innerHTML;
				newwin.document.body.appendChild(newdiv);
				}

            </script>
			<body>
				<br/>
				<h1>
					View Dataset
					<xsl:value-of select="$datasetName"/>
				</h1>
				<div class="tablebox_center" align="left">
					<table border="1" cellpadding="0" cellspacing="0">
						<!-- Fix for 0007973 -->
						<!--
						<tr valign="top">
							<td class="table_header_column_top">Database Export Header Metadata</td>
							<td class="table_cell_top">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
							<td class="table_cell_top">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</tr>
						-->
						<tr>
							<td class="table_header_column">Dataset Name:</td>
							<td class="table_cell">
								<xsl:value-of select="$datasetName"/>
							</td>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</tr>
						<tr>
							<td class="table_header_column">Dataset Description:</td>
							<td class="table_cell">
								<xsl:value-of select="$desc"/>
							</td>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</tr>
						<tr>
							<td class="table_header_column">Study Name:</td>
							<td class="table_cell">
								<xsl:value-of select="$study/odm:GlobalVariables/odm:StudyName"/>
							</td>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</tr>
						<tr>
							<td class="table_header_column">Protocol ID:</td>
							<td class="table_cell">
								<xsl:value-of select="$protocolNameStudy"/>
							</td>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</tr>
						<tr>
							<td class="table_header_column">Date:</td>
							<td class="table_cell">
								<xsl:call-template name="FormatDate">
									<xsl:with-param name="DateTime" select="/odm:ODM/@CreationDateTime"/>
								</xsl:call-template>
							</td>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</tr>
						<tr>
							<td class="table_header_column">Subjects:</td>
							<td class="table_cell">
								<xsl:value-of select="$subject_count"/>
							</td>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</tr>
						<tr>
							<td class="table_header_column">Study Event Definitions:</td>
							<td class="table_cell">
								<xsl:value-of select="$eventDefCount"/>
							</td>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</tr>
						<xsl:apply-templates select="//odm:ODM/odm:Study[1]/odm:MetaDataVersion" mode="metadataDisplay"/>
						<!-- placeholder 1 studyeventdata -->
						<!--<xsl:apply-templates
							select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]"
							mode="studyEventData1" />

						-->
						<!-- End of Study Event Data -->
					</table>
				</div>
				<br/>
				<!--crfAndDataItemsHeaders: <xsl:value-of select="$crfAndDataItemsHeaders"/>-->
				<!--<xsl:for-each select="$tokenizedcrfAndDataItemsHeaders">
					token<xsl:value-of select="position()"/>:<xsl:value-of select="."/>
				</xsl:for-each>-->
				<!--tokenizedcrfAndDataItemsHeaders: <xsl:value-of select="$tokenizedcrfAndDataItemsHeaders"/>-->
				
				<!-- Main Table Starting -->
				<div class="tablebox_center" align="center">
					<table border="1" cellpadding="0" cellspacing="0">
						<tr valign="top">
							<td class="table_header_row">
								<xsl:text>Study Subject ID</xsl:text>
							</td>							
							<td class="table_header_row">
								<xsl:text>Protocol ID</xsl:text>
							</td>
							<xsl:if test="$uniqueIdExist">
								<td class="table_header_row">
									<xsl:text>Person ID</xsl:text>
								</td>
							</xsl:if>
							<xsl:if test="$subjectSecondaryIdExist">
								<td class="table_header_row">
									<xsl:text>Secondary ID</xsl:text>
								</td>
							</xsl:if>
							<xsl:if test="$subjectStatusExist">
								<td class="table_header_row">
									<xsl:text>Subject Status</xsl:text>
								</td>
							</xsl:if>
							<xsl:if test="$sexExist">
								<td class="table_header_row">
									<xsl:text>Sex</xsl:text>
								</td>
							</xsl:if>
							<xsl:if test="$dobExist">
								<td class="table_header_row">
									<xsl:text>Date of Birth</xsl:text>
								</td>
							</xsl:if>
                                                        <xsl:if test="$yearOfBirthExist">
								<td class="table_header_row">
									<xsl:text>Year of Birth</xsl:text>
								</td>
							</xsl:if>
							<!--Starting Study Event Column Headers -->
							<!-- place holder 2 -->
							<!--
							<xsl:apply-templates
								select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]"
								mode="studyeventData2" />
								
							-->
							<!-- Selecting Event CRF column headers -->
							<!--			<xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
			mode="studyEventInfo" />-->
							<xsl:apply-templates select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef" mode="studyEventInfoHeaders"/>
							<!--	<xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
			mode="studyEventHeader" />-->
							<xsl:apply-templates select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef" mode="studyFormAndDataItemsHeaders">
								<xsl:with-param name="generateIntHeadersList" select="'No'"/>
							</xsl:apply-templates>
							<!-- place holder 3 -->
							<!-- Item Headers -->
							<!-- placeholder 4 -->
							<!-- <xsl:apply-templates select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() 
								= generate-id(key('eventCRFs',@FormOID)[1])]" mode="itemHeader4" /> -->
						</tr>
						<!-- ====================================================================================================================== -->
						<!-- Place holder 5 -->
						
						<xsl:apply-templates select="/odm:ODM/odm:ClinicalData/odm:SubjectData" mode="allSubjectData">
							<xsl:with-param name="tokenizedEventHeaders" select="$tokenizedEventHeaders"/>
							<xsl:with-param name="tokenizedcrfAndDataItemsHeaders" select="$tokenizedcrfAndDataItemsHeaders"/>
						</xsl:apply-templates>
					</table>
				</div>
				<!-- ====================================================================================================================== -->
				<!-- For the MetaData PopUp -->
				<br/>
				<!-- place holder 6 -->
				<xsl:apply-templates select="/odm:ODM/odm:ClinicalData" mode="clinicalMetadata">
			
				</xsl:apply-templates>
				<!-- Clinical For Each -->
				<!-- End For the MetaData PopUp -->
			</body>
		</html>
	</xsl:template>
	<!--
		<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData"
		mode="allSubjectData">
	<tr valign="top">
			<td class="table_cell_left">
				<xsl:value-of select="@OpenClinica:StudySubjectID"></xsl:value-of>
			</td>
			<xsl:if test="$uniqueIdExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:UniqueIdentifier">
							<xsl:value-of select="@OpenClinica:UniqueIdentifier"></xsl:value-of>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			<xsl:if test="$subjectStatusExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:Status">
							<xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			<xsl:if test="$sexExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:Sex">
							<xsl:value-of select="@OpenClinica:Sex"></xsl:value-of>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			<xsl:if test="$dobExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:DateOfBirth">
							<xsl:value-of select="@OpenClinica:DateOfBirth"></xsl:value-of>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			
			<xsl:variable name="subjectEvents" select="./odm:StudyEventData" />
			<xsl:variable name="subjectItems"
			select="./odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData" />
	
	<xsl:for-each select="$allStudyEventDataElements">
		<xsl:variable name="eventOIDLoop" select="@StudyEventOID"/>
		<xsl:variable name="eventRepeatKeyLoop" select="@StudyEventRepeatKey"/>
		<xsl:choose>
			<xsl:when test="$eventRepeatKeyLoop">
				<xsl:variable name="subjectEventDataElementInOrder" select="$subjectEvents[$eventOIDLoop= @StudyEventOID and $eventRepeatKeyLoop = @StudyEventRepeatKey]" />
				<xsl:if test="$subjectEventDataElementInOrder">
					<xsl:apply-templates mode="studyEventInfoData2" select="$subjectEventDataElementInOrder">
						<xsl:with-param name="subjectForms"
								select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData"/>
						<xsl:with-param name="subjectKey" select="@SubjectKey"	 />
						<xsl:with-param name="subjectEvents" select="$subjectEvents" />
						<xsl:with-param name="subjectItems"  select="$subjectItems"/>
					</xsl:apply-templates>
				</xsl:if>
			</xsl:when>	
			<xsl:otherwise>
				<xsl:variable name="subjectEventDataElementInOrder" select="$subjectEvents[$eventOIDLoop= @StudyEventOID]" />
				<xsl:if test="$subjectEventDataElementInOrder">
					<xsl:apply-templates mode="studyEventInfoData2" select="odm:StudyEventData">
						<xsl:with-param name="subjectForms"
								select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData"/>
						<xsl:with-param name="subjectKey" select="@SubjectKey"	 />
						<xsl:with-param name="subjectEvents" select="$subjectEvents" />
						<xsl:with-param name="subjectItems"  select="$subjectItems"/>
					</xsl:apply-templates>
				</xsl:if>
			</xsl:otherwise>
		</xsl:choose>		
	</xsl:for-each>


		
		<xsl:apply-templates
			select="odm:StudyEventData/odm:FormData"
			mode="eventCRFData">
			<xsl:with-param name="subjectForms"
				select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData"/>
			<xsl:with-param name="subjectEvents" select="$subjectEvents"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>
		</tr>
		
	</xsl:template>
	-->
	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData" mode="allSubjectData">
		<xsl:param name="tokenizedEventHeaders"/>
		<xsl:param name="tokenizedcrfAndDataItemsHeaders"/>
		<xsl:variable name="studyOID" select="../@StudyOID"/>
		<xsl:variable name="studyElement" select="//odm:Study[@OID = $studyOID]"/>
		<xsl:variable name="protocolName" select="$studyElement/odm:GlobalVariables/odm:ProtocolName"/>
		<tr valign="top">
			<td class="table_cell_left">
				<xsl:value-of select="@OpenClinica:StudySubjectID"/>
			</td>
			<td class="table_cell_left">
				<xsl:value-of select="$protocolName"/>
			</td>
			<xsl:if test="$uniqueIdExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:UniqueIdentifier">
							<xsl:value-of select="@OpenClinica:UniqueIdentifier"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			<xsl:if test="$subjectSecondaryIdExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:SecondaryID">
							<xsl:value-of select="@OpenClinica:SecondaryID"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			<xsl:if test="$subjectStatusExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:Status">
							<xsl:value-of select="@OpenClinica:Status"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			<xsl:if test="$sexExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:Sex">
							<xsl:value-of select="@OpenClinica:Sex"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			<xsl:if test="$dobExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:DateOfBirth">
							<xsl:value-of select="@OpenClinica:DateOfBirth"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
                        <xsl:if test="$yearOfBirthExist">
				<td class="table_cell">
					<xsl:choose>
						<xsl:when test="@OpenClinica:YearOfBirth">
							<xsl:value-of select="@OpenClinica:YearOfBirth"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</td>
			</xsl:if>
			<!--<xsl:apply-templates mode="studyEventInfoData" select="odm:StudyEventData"/>-->
			<xsl:variable name="subjectEvents" select="./odm:StudyEventData"/>
			<!-- <xsl:apply-templates mode="studyEventsData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 
			= generate-id(key('studyEvents',@StudyEventOID)[1])]"> <xsl:with-param name="subjectEvents" 
			select="$subjectEvents"></xsl:with-param> </xsl:apply-templates> -->
			<xsl:variable name="subjectItems" select="./odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData"/>
			<xsl:variable name="subjectForms" select="./odm:StudyEventData/odm:FormData"/>
			<xsl:call-template name="studyEventInfoData2">
				<xsl:with-param name="subjectEvents" select="$subjectEvents"/>
				<xsl:with-param name="tokenizedEventHeaders" select="$tokenizedEventHeaders"/>
			</xsl:call-template>
			<!--<xsl:for-each select="$tokenizedEventHeaders">
		  {token<xsl:value-of select="position()"/>: <xsl:value-of select="."/>}
		  <xsl:text>! </xsl:text>
		</xsl:for-each>-->
			<!--
		<xsl:apply-templates
			select="odm:StudyEventData/odm:FormData"
			mode="eventCRFData">
			<xsl:with-param name="subjectForms"
				select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData"/>
			<xsl:with-param name="subjectEvents" select="$subjectEvents"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>-->
			<xsl:call-template name="studyCRFAndItemsData">
				<xsl:with-param name="subjectEvents" select="$subjectEvents"/>
				<xsl:with-param name="subjectForms" select="$subjectForms"/>
				<xsl:with-param name="subjectItems" select="$subjectItems"/>
				<xsl:with-param name="tokenizedcrfAndDataItemsHeaders" select="$tokenizedcrfAndDataItemsHeaders"/>
			</xsl:call-template>
		</tr>
	</xsl:template>
	<xsl:template mode="eventCRFData" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[@FormOID]">
		<xsl:param name="subjectEvents"/>
		<xsl:param name="subjectForms"/>
		<xsl:param name="subjectItems"/>
		<xsl:variable name="currentForm" select="current()"/>
		<xsl:variable name="subjectFormData" select="$subjectForms[@FormOID=$currentForm/@FormOID]"/>
		<xsl:variable name="subjectEvent" select="$subjectFormData/.."/>
		<xsl:variable name="parentEvent" select=".."/>
		<xsl:apply-templates mode="allEventCrfData" select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]">
			<xsl:with-param name="subjectForms" select="$subjectForms"/>
			<xsl:with-param name="currentForm" select="$currentForm"/>
			<xsl:with-param name="subjectEvent" select="$subjectEvent"/>
			<xsl:with-param name="parentEvent" select="$parentEvent"/>
			<xsl:with-param name="subjectFormData" select="$subjectFormData"/>
		</xsl:apply-templates>
		<xsl:apply-templates mode="allItemData" select="odm:ItemGroupData/odm:ItemData">
			<xsl:with-param name="currentFormOID" select="@FormOID"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template mode="eventCRFData2" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<xsl:param name="subjectEvents"/>
		<xsl:param name="subjectForms"/>
		<xsl:param name="subjectItems"/>
		<xsl:variable name="currentForm" select="current()"/>
		<xsl:variable name="subjectFormData" select="$subjectForms[@FormOID=$currentForm/@FormOID]"/>
		<xsl:variable name="subjectEvent" select="$subjectFormData/.."/>
		<xsl:variable name="parentEvent" select=".."/>
		<xsl:apply-templates mode="allEventCrfData2" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[@FormOID]">
			<xsl:with-param name="subjectForms" select="$subjectForms"/>
			<xsl:with-param name="currentForm" select="$currentForm"/>
			<xsl:with-param name="subjectEvent" select="$subjectEvent"/>
			<xsl:with-param name="parentEvent" select="$parentEvent"/>
			<xsl:with-param name="subjectFormData" select="$subjectFormData"/>
			<xsl:with-param name="currentFormOID" select="@FormOID"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>
		<!--
		<xsl:apply-templates mode="allItemData"
			select="odm:ItemGroupData/odm:ItemData">
			<xsl:with-param name="currentFormOID" select="@FormOID"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>
		-->
	</xsl:template>
	<xsl:template mode="allEventCrfData" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:param name="subjectForms"/>
		<xsl:param name="currentForm"/>
		<xsl:param name="subjectEvent"/>
		<xsl:param name="parentEvent"/>
		<xsl:param name="subjectFormData"/>
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation"/>
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate"/>
		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status"/>
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent"/>
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate"/>
		<xsl:variable name="crfVersionExist" select="odm:FormData/@OpenClinica:Version"/>
		<xsl:variable name="interviewerNameExist" select="odm:FormData/@OpenClinica:InterviewerName"/>
		<xsl:variable name="interviewDateExist" select="odm:FormData/@OpenClinica:InterviewDate"/>
		<xsl:variable name="crfStatusExist" select="odm:FormData/@OpenClinica:Status"/>
		<xsl:if test="@StudyEventOID = $parentEvent/@StudyEventOID">
			<xsl:choose>
				<xsl:when test="@StudyEventRepeatKey">
					<xsl:variable name="allStudyEvents">
						<xsl:for-each select="//odm:StudyEventData[@StudyEventOID]">
							<xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
							<xsl:copy-of select="."/>
						</xsl:for-each>
					</xsl:variable>
					<!--	
					<xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
						<xsl:choose>
							<xsl:when test="position()=1">

								<xsl:choose>

									<xsl:when
										test="$subjectFormData/node()
                                                    and $subjectEvent/@StudyEventOID=@StudyEventOID
                                                    and $subjectEvent/@StudyEventRepeatKey=@StudyEventRepeatKey">
										<xsl:if test="$interviewerNameExist">
										<td class="table_cell">
											<xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"/>
										</td>
										</xsl:if>
										<xsl:if test="$interviewDateExist">
										<td class="table_cell">
											<xsl:value-of select="$currentForm/@OpenClinica:InterviewDate"/>
										</td>
										</xsl:if>
										<xsl:if test="$crfStatusExist">
										<td class="table_cell">
											<xsl:value-of select="$currentForm/@OpenClinica:Status"/>
										</td>
										</xsl:if>
										<xsl:if test="$crfVersionExist">
										<td class="table_cell">
											<xsl:value-of select="$currentForm/@OpenClinica:Version"/>
										</td>
										</xsl:if>

									</xsl:when>
									<xsl:otherwise>
									<xsl:if test="$eventLocationExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									<xsl:if test="$eventStartDateExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									<xsl:if test="$eventEndDateExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									<xsl:if test="$eventStatusExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									<xsl:if test="$ageExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if
									test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">
								<xsl:choose>
										<xsl:when
											test="$subjectFormData/node()
                                                        and $subjectEvent/@StudyEventOID=@StudyEventOID
                                                        and $subjectEvent/@StudyEventRepeatKey=@StudyEventRepeatKey">
										<xsl:if test="$interviewerNameExist">
										<td class="table_cell">
											<xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"/>
										</td>
										</xsl:if>
										<xsl:if test="$interviewDateExist">
										<td class="table_cell">
											<xsl:value-of select="$currentForm/@OpenClinica:InterviewDate"/>
										</td>
										</xsl:if>
										<xsl:if test="$crfStatusExist">
										<td class="table_cell">
											<xsl:value-of select="$currentForm/@OpenClinica:Status"/>
										</td>
										</xsl:if>
										<xsl:if test="$crfVersionExist">
										<td class="table_cell">
											<xsl:value-of select="$currentForm/@OpenClinica:Version"/>
										</td>
										</xsl:if>
										</xsl:when>
										<xsl:otherwise>
											<xsl:if test="$eventLocationExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									<xsl:if test="$eventStartDateExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									<xsl:if test="$eventEndDateExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									<xsl:if test="$eventStatusExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
									<xsl:if test="$ageExist">
										<td class="table_cell">
											<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
										</td>
									</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>

					</xsl:for-each>-->
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="$subjectFormData/node()">
							<!--
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
							-->
							<xsl:if test="$interviewerNameExist">
								<td class="table_cell">
									<xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"/>
								</td>
							</xsl:if>
							<xsl:if test="$interviewDateExist">
								<td class="table_cell">
									<xsl:value-of select="$currentForm/@OpenClinica:InterviewDate"/>
								</td>
							</xsl:if>
							<xsl:if test="$crfStatusExist">
								<td class="table_cell">
									<xsl:value-of select="$currentForm/@OpenClinica:Status"/>
								</td>
							</xsl:if>
							<xsl:if test="$crfVersionExist">
								<td class="table_cell">
									<xsl:value-of select="$currentForm/@OpenClinica:Version"/>
								</td>
							</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<!--
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
							-->
							<xsl:if test="$eventLocationExist">
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>
							</xsl:if>
							<xsl:if test="$eventStartDateExist">
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>
							</xsl:if>
							<xsl:if test="$eventEndDateExist">
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>
							</xsl:if>
							<xsl:if test="$eventStatusExist">
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>
							</xsl:if>
							<xsl:if test="$ageExist">
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	<!-- **************** -->
	<xsl:template mode="allEventCrfData2" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<xsl:param name="subjectForms"/>
		<xsl:param name="currentForm"/>
		<xsl:param name="subjectEvent"/>
		<xsl:param name="parentEvent"/>
		<xsl:param name="subjectFormData"/>
		<xsl:param name="currentFormOID"/>
		<xsl:param name="subjectItems"/>
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation"/>
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate"/>
		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status"/>
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent"/>
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate"/>
		<xsl:variable name="crfVersionExist" select="@OpenClinica:Version"/>
		<xsl:variable name="interviewerNameExist" select="@OpenClinica:InterviewerName"/>
		<xsl:variable name="interviewDateExist" select="@OpenClinica:InterviewDate"/>
		<xsl:variable name="crfStatusExist" select="@OpenClinica:Status"/>
		<xsl:if test="$interviewerNameExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:InterviewerName"/>
			</td>
		</xsl:if>
		<xsl:if test="$interviewerNameExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:InterviewerName"/>
			</td>
		</xsl:if>
		<xsl:if test="$interviewDateExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:InterviewDate"/>
			</td>
		</xsl:if>
		<xsl:if test="$crfStatusExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:Status"/>
			</td>
		</xsl:if>
		<xsl:if test="$crfVersionExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:Version"/>
			</td>
		</xsl:if>
		<xsl:apply-templates mode="allItemData" select="odm:ItemGroupData/odm:ItemData">
			<xsl:with-param name="currentFormOID" select="$currentFormOID"/>
			<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template mode="allItemData" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">
		<xsl:param name="subjectItems"/>
		<xsl:param name="currentFormOID"/>
		<xsl:variable name="itemData" select="current()"/>
		<xsl:variable name="itemFormOID" select="$itemData/../../@FormOID"/>
		<xsl:variable name="itemOID" select="@ItemOID"/>
		<xsl:variable name="eventRepeatKey" select="$itemData/../../../@StudyEventRepeatKey"/>
		<xsl:variable name="subjectItemRepeating" select="$subjectItems[@ItemOID = $itemOID
                                                                            and $itemFormOID =../../@FormOID
                                                                            and $eventRepeatKey=../../../@StudyEventRepeatKey]"/>
		<xsl:variable name="subjectItemSingle" select="$subjectItems[@ItemOID = $itemOID and $itemFormOID =../../@FormOID]"/>
		<xsl:apply-templates mode="printItemData" select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]">
			<xsl:with-param name="itemData" select="$itemData"/>
			<xsl:with-param name="itemFormOID" select="$itemFormOID"/>
			<xsl:with-param name="itemOID" select="$itemOID"/>
			<xsl:with-param name="eventRepeatKey" select="$eventRepeatKey"/>
			<xsl:with-param name="subjectItemRepeating" select="$subjectItemRepeating"/>
			<xsl:with-param name="subjectItemSingle" select="$subjectItemSingle"/>
			<xsl:with-param name="currentFormOID" select="$currentFormOID"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef" mode="printItemData">
		<xsl:param name="itemData"/>
		<xsl:param name="itemFormOID"/>
		<xsl:param name="itemOID"/>
		<xsl:param name="eventRepeatKey"/>
		<xsl:param name="subjectItemRepeating"/>
		<xsl:param name="subjectItemSingle"/>
		<xsl:param name="currentFormOID"/>
		<xsl:variable name="formOID" select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemFormOID]/@FormOID"/>
		<xsl:if test="$currentFormOID = $formOID">
			<xsl:choose>
				<xsl:when test="$eventRepeatKey">
					<xsl:choose>
						<xsl:when test="count($subjectItemRepeating) &gt; 0">
							<td class="table_cell">
								<xsl:value-of select="$itemData/@Value"/>
							</td>
						</xsl:when>
						<xsl:otherwise>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					<xsl:choose>
						<xsl:when test="count($subjectItemSingle) &gt; 0">
							<td class="table_cell">
								<xsl:value-of select="$itemData/@Value"/>
							</td>
						</xsl:when>
						<xsl:otherwise>
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	<xsl:template mode="studyEventInfoData" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:param name="subjectForms"/>
		<xsl:param name="subjectEvents"/>
		<xsl:param name="subjectItems"/>
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation"/>
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate"/>
		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status"/>
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent"/>
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate"/>
		<!-- place the event information under appropriate columns -->
		<xsl:if test="$eventLocationExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:StudyEventLocation"/>
			</td>
		</xsl:if>
		<xsl:if test="$eventStartDateExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:StartDate"/>
			</td>
		</xsl:if>
		<xsl:if test="$eventEndDateExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:EndDate"/>
			</td>
		</xsl:if>
		<xsl:if test="$eventStatusExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:Status"/>
			</td>
		</xsl:if>
		<xsl:if test="$ageExist">
			<td class="table_cell">
				<xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"/>
			</td>
		</xsl:if>
		<!--<xsl:apply-templates
			select="odm:FormData"
			mode="eventCRFData2">
				<xsl:with-param name="subjectForms" select="$subjectForms"/>
				<xsl:with-param name="subjectEvents" select="$subjectEvents"/>
				<xsl:with-param name="subjectItems" select="$subjectItems"/>
		</xsl:apply-templates>	-->
	</xsl:template>
	<xsl:template name="studyEventInfoData2">
		<!--<xsl:param name="subjectForms"/>
<xsl:param name="subjectItems" />
	-->
		<xsl:param name="subjectEvents"/>
		<xsl:param name="tokenizedEventHeaders"/>
		<!--
		-subjectEvents:
		<xsl:for-each select="$subjectEvents">
			<xsl:variable name="eventOID" select="@StudyEventOID" />eventOID:<xsl:value-of select="$eventOID"/>					
			<xsl:variable name="eventPosition">
				<xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
					<xsl:if test="@OID = $eventOID">
						<xsl:copy-of select="position()" />
					</xsl:if>	
				</xsl:for-each>				
			</xsl:variable>eventPosition:<xsl:value-of select="$eventPosition"/>
			StudyEventRepeatKey:<xsl:value-of select="@StudyEventRepeatKey"/>			
			</xsl:for-each>
		-->
		<xsl:for-each select="$tokenizedEventHeaders">
			<xsl:variable name="currentPos" select="position()"/>
			<!--currentPos: <xsl:value-of select="$currentPos"/>-->
			<xsl:variable name="currentToken" select="."/>
			
			
			<!--<xsl:variable name="currentToken" select="."/>-->
			<!--{T<xsl:value-of select="position()"/>:<xsl:value-of select="."/>}-->
			<xsl:if test=". != $tokenizedEventHeaders[last()]">
				<!--not last	-->
				<!-- get which event this is -->
				<xsl:variable name="nextToken" select="$tokenizedEventHeaders[$currentPos+1]"/>
				<!--currentToken:*<xsl:value-of select="$currentToken"/>*
					next token:<xsl:value-of select="$nextToken"/>-->
				<xsl:variable name="numericStart">
					<xsl:if test="ends-with($nextToken,'Location')">
						<xsl:value-of select="substring-before($nextToken,'Location')"/>
					</xsl:if>
					<xsl:if test="ends-with($nextToken,'StartDate')">
						<xsl:value-of select="substring-before($nextToken,'StartDate')"/>
					</xsl:if>
					<xsl:if test="ends-with($nextToken,'EndDate')">
						<xsl:value-of select="substring-before($nextToken,'EndDate')"/>
					</xsl:if>
					<xsl:if test="ends-with($nextToken,'Event Status')">
						<xsl:value-of select="substring-before($nextToken,'Event Status')"/>
					</xsl:if>
					<xsl:if test="ends-with($nextToken,'Age')">
						<xsl:value-of select="substring-before($nextToken,'Age')"/>
					</xsl:if>
					<xsl:if test="$currentToken = $tokenizedEventHeaders[last()-1]">
						<xsl:value-of select="$nextToken"/>
					</xsl:if>
				</xsl:variable>
				<!--numeric start: <xsl:value-of select="$numericStart"/>-->
				<xsl:variable name="colEventPosition">
					<xsl:choose>
						<xsl:when test="contains($numericStart, '_')">
							<xsl:value-of select="substring-before($numericStart,'_')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$numericStart"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<!--<xsl:variable name="colEventPosition">
						<xsl:choose>
							<xsl:when test="contains(., '_')">
								<xsl:value-of select="substring-before(.,'_')"/>
							</xsl:when>
							<xsl:otherwise>
								<xsl:value-of select="."/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:variable>-->
				<xsl:variable name="isColForRepeatingEvent" select="contains($numericStart, '_')"/>
				<!--isColForRepeatingEvent<xsl:value-of select="$isColForRepeatingEvent"/>-->
				<xsl:variable name="colRepeatEventKey">
					<xsl:if test="contains($numericStart, '_')">
						<xsl:value-of select="substring-after($numericStart,'_')"/>
					</xsl:if>
				</xsl:variable>
				<!--colRepeatEventKey: <xsl:value-of select="$colRepeatEventKey"/>-->
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
					<xsl:if test="ends-with($currentToken,'Event Status')">
						<xsl:text>Status</xsl:text>
					</xsl:if>
					<xsl:if test="ends-with($currentToken,'Age')">
						<xsl:text>Age</xsl:text>
					</xsl:if>
				</xsl:variable>
				<!--colType:<xsl:value-of select="$colType"/>-->
				<xsl:variable name="ifMatch">
					<xsl:for-each select="$subjectEvents">
						<xsl:variable name="eventOID" select="@StudyEventOID"/>
						<xsl:variable name="eventPosition">
							<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
								<xsl:if test="@OID = $eventOID">
									<xsl:copy-of select="position()"/>
								</xsl:if>
							</xsl:for-each>
						</xsl:variable>
						<xsl:choose>
							<xsl:when test="$colEventPosition = $eventPosition">
								<xsl:choose>
									<xsl:when test="$isColForRepeatingEvent">
										<xsl:choose>
											<xsl:when test="@StudyEventRepeatKey = $colRepeatEventKey">
												<!--<xsl:text>M</xsl:text>-->
												<xsl:value-of select="$matchSep"/>
												<xsl:value-of select="position()"/>
												<!--_<xsl:value-of select="@StudyEventRepeatKey"/>-->
											</xsl:when>
											<xsl:otherwise>
												<!--<xsl:text>N</xsl:text>-->
												<xsl:value-of select="$nonMatchSep"/>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>
									<xsl:otherwise>
										<!--<xsl:text>M</xsl:text>-->
										<xsl:value-of select="$matchSep"/>
										<xsl:value-of select="position()"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<!--<xsl:text>N</xsl:text>-->
								<xsl:value-of select="$nonMatchSep"/>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:for-each>
				</xsl:variable>
				<!--ifMatch: *<xsl:value-of select="$ifMatch"/>*-->
				<xsl:choose>
					<xsl:when test="contains($ifMatch, $matchSep)">
						<xsl:variable name="StrAfterM" select="substring-after($ifMatch,$matchSep)"/>
						<!--<xsl:variable name="StrB4N" select="substring-before($StrAfterM,'N')"/>
							<xsl:variable name="evenPos" select="substring-before($StrB4N, '_')"/>
							<xsl:variable name="evenRepeatKey" select="substring-aftere($StrB4N, '_')"/>-->
						<!--<xsl:variable name="StrAfterM" select="substring-after($ifMatch,'M')"/>-->
						<!--<xsl:variable name="StrB4N" select="substring-before($StrAfterM,'N')"/>-->
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
									<td class="table_cell">
										<xsl:value-of select="$event/@OpenClinica:StudyEventLocation"/>
									</td>
								</xsl:when>
								<xsl:otherwise>
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<xsl:if test="$colType = 'StartDate'">
							<xsl:choose>
								<xsl:when test="$event/@OpenClinica:StartDate">
									<td class="table_cell">
										<xsl:value-of select="$event/@OpenClinica:StartDate"/>
									</td>
								</xsl:when>
								<xsl:otherwise>
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<xsl:if test="$colType = 'EndDate'">
							<xsl:choose>
								<xsl:when test="$event/@OpenClinica:EndDate">
									<td class="table_cell">
										<xsl:value-of select="$event/@OpenClinica:EndDate"/>
									</td>
								</xsl:when>
								<xsl:otherwise>
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<xsl:if test="$colType = 'Status'">
							<xsl:choose>
								<xsl:when test="$event/@OpenClinica:Status">
									<td class="table_cell">
										<xsl:value-of select="$event/@OpenClinica:Status"/>
									</td>
								</xsl:when>
								<xsl:otherwise>
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
						<xsl:if test="$colType = 'Age'">
							<xsl:choose>
								<xsl:when test="$event/@OpenClinica:SubjectAgeAtEvent">
									<td class="table_cell">
										<xsl:value-of select="$event/@OpenClinica:SubjectAgeAtEvent"/>
									</td>
								</xsl:when>
								<xsl:otherwise>
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="$colType = 'Location'">
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</xsl:if>
						<xsl:if test="$colType = 'StartDate'">
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</xsl:if>
						<xsl:if test="$colType = 'EndDate'">
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</xsl:if>
						<xsl:if test="$colType = 'Status'">
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</xsl:if>
						<xsl:if test="$colType = 'Age'">
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
				<!--<xsl:choose>
						<xsl:when test="$colEventPosition = $eventPosition">even pos matched
							<xsl:choose>
								<xsl:when test="$isColForRepeatingEvent"> repeating event column
									<xsl:choose>
										<xsl:when test="@StudyEventRepeatKey = $colRepeatEventKey">
											writing data for repeatKey: <xsl:value-of select="@StudyEventRepeatKey"/>
											<xsl:if test="$colType = 'Location'">
												<xsl:choose>
													<xsl:when test="@OpenClinica:StudyEventLocation">
														<td class="table_cell">
															<xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
														</td>
													</xsl:when>
													<xsl:otherwise>
														<td class="table_cell">
															<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
														</td>	
													</xsl:otherwise>
												</xsl:choose>
											</xsl:if>
											<xsl:if test="$colType = 'StartDate'">
												<xsl:choose>
													<xsl:when test="@OpenClinica:StartDate">
														<td class="table_cell">
															<xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
														</td>
													</xsl:when>
													<xsl:otherwise>
														<td class="table_cell">
															<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
														</td>	
													</xsl:otherwise>
												</xsl:choose>									
											</xsl:if>
											<xsl:if test="$colType = 'EndDate'">		
												<xsl:choose>
													<xsl:when test="@OpenClinica:EndDate">
														<td class="table_cell">
															<xsl:value-of select="@OpenClinica:EndDate"></xsl:value-of>
														</td>
													</xsl:when>
													<xsl:otherwise>
														<td class="table_cell">
															<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
														</td>	
													</xsl:otherwise>
												</xsl:choose>						
											</xsl:if>
											<xsl:if test="$colType = 'Event Status'">
												<xsl:choose>
													<xsl:when test="@OpenClinica:Status">
														<td class="table_cell">
															<xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
														</td>
													</xsl:when>
													<xsl:otherwise>
														<td class="table_cell">
															<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
														</td>	
													</xsl:otherwise>
												</xsl:choose>										
											</xsl:if>
											<xsl:if test="$colType = 'Age'">
												<xsl:choose>
													<xsl:when test="@OpenClinica:SubjectAgeAtEvent">
														<td class="table_cell">
															<xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"></xsl:value-of>
														</td>
													</xsl:when>
													<xsl:otherwise>
														<td class="table_cell">
															<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
														</td>	
													</xsl:otherwise>
												</xsl:choose>																				
										</xsl:if>
										</xsl:when>
										<xsl:otherwise>even repeat key not matched - no data
											<xsl:if test="$colType = 'Location'">										
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>											
											</xsl:if>
											<xsl:if test="$colType = 'StartDate'">								
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>																	
											</xsl:if>
											<xsl:if test="$colType = 'EndDate'">
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>														
											</xsl:if>
											<xsl:if test="$colType = 'Event Status'">								
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>																			
											</xsl:if>
											<xsl:if test="$colType = 'Age'">							
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>																												
											</xsl:if>
										</xsl:otherwise>
									</xsl:choose>
								</xsl:when>
								<xsl:otherwise>									
									<xsl:if test="$colType = 'Location'">
										<xsl:choose>
											<xsl:when test="@OpenClinica:StudyEventLocation">
												<td class="table_cell">
													<xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>	
											</xsl:otherwise>
										</xsl:choose>
									</xsl:if>
									<xsl:if test="$colType = 'StartDate'">
										<xsl:choose>
											<xsl:when test="@OpenClinica:StartDate">
												<td class="table_cell">
													<xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>	
											</xsl:otherwise>
										</xsl:choose>									
									</xsl:if>
									<xsl:if test="$colType = 'EndDate'">		
										<xsl:choose>
											<xsl:when test="@OpenClinica:EndDate">
												<td class="table_cell">
													<xsl:value-of select="@OpenClinica:EndDate"></xsl:value-of>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>	
											</xsl:otherwise>
										</xsl:choose>						
									</xsl:if>
									<xsl:if test="$colType = 'Event Status'">
										<xsl:choose>
											<xsl:when test="@OpenClinica:Status">
												<td class="table_cell">
													<xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>	
											</xsl:otherwise>
										</xsl:choose>										
									</xsl:if>
									<xsl:if test="$colType = 'Age'">
									<xsl:choose>
											<xsl:when test="@OpenClinica:SubjectAgeAtEvent">
												<td class="table_cell">
													<xsl:value-of select="@OpenClinica:SubjectAgeAtEvent"></xsl:value-of>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>	
											</xsl:otherwise>
										</xsl:choose>																				
									</xsl:if>
								</xsl:otherwise>
							</xsl:choose>
						</xsl:when>
						<xsl:otherwise>even pos not matched - no data
							<xsl:if test="$colType = 'Location'">										
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>											
							</xsl:if>
							<xsl:if test="$colType = 'StartDate'">								
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>																	
							</xsl:if>
							<xsl:if test="$colType = 'EndDate'">
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>														
							</xsl:if>
							<xsl:if test="$colType = 'Event Status'">								
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>																			
							</xsl:if>
							<xsl:if test="$colType = 'Age'">							
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>																												
							</xsl:if>
						</xsl:otherwise>
					</xsl:choose>-->
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="studyCRFAndItemsData">
		<xsl:param name="subjectForms"/>
		<xsl:param name="subjectItems"/>
		<xsl:param name="subjectEvents"/>
		<xsl:param name="tokenizedcrfAndDataItemsHeaders"/>
		<!--
		-subjectEvents:
		<xsl:for-each select="$subjectEvents">
			<xsl:variable name="eventOID" select="@StudyEventOID" />eventOID:<xsl:value-of select="$eventOID"/>					
			<xsl:variable name="eventPosition">
				<xsl:for-each select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
					<xsl:if test="@OID = $eventOID">
						<xsl:copy-of select="position()" />
					</xsl:if>	
				</xsl:for-each>				
			</xsl:variable>eventPosition:<xsl:value-of select="$eventPosition"/>
			StudyEventRepeatKey:<xsl:value-of select="@StudyEventRepeatKey"/>			
			</xsl:for-each>
		-->
		<xsl:for-each select="$tokenizedcrfAndDataItemsHeaders">
			<xsl:variable name="currentPos" select="position()"/>
			<!--currentPos: <xsl:value-of select="$currentPos"/>-->
			<xsl:variable name="currentToken" select="."/>
			<!--{T<xsl:value-of select="position()"/>:<xsl:value-of select="."/>}-->
			<xsl:if test=". != $tokenizedcrfAndDataItemsHeaders[last()]">
				<!--not last-->
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
				<!--currentToken:*<xsl:value-of select="$currentToken"/>*
			next token:<xsl:value-of select="$nextToken"/>-->
				<xsl:variable name="numericStart">
					<xsl:choose>
						<xsl:when test="ends-with($nextToken,'Interviewer')">
							<xsl:value-of select="substring-before($nextToken,'Interviewer')"/>
						</xsl:when>
						<xsl:when test="ends-with($nextToken,'InterviewDate')">
							<xsl:value-of select="substring-before($nextToken,'InterviewDate')"/>
						</xsl:when>
						<xsl:when test="ends-with($nextToken,'CRF Version Status')">
							<xsl:value-of select="substring-before($nextToken,'CRF Version Status')"/>
						</xsl:when>
						<xsl:when test="ends-with($nextToken,'Version Name')">
							<xsl:value-of select="substring-before($nextToken,'Version Name')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="substring-before($nextToken,' ')"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<!--numeric start: *<xsl:value-of select="$numericStart"/>*-->
				<xsl:variable name="numericB4_C" select="substring-before($numericStart, '_C')"/>
				<!--numericB4_C: <xsl:value-of select="$numericB4_C"/>-->
				<xsl:variable name="colEventPosition">
					<xsl:choose>
						<xsl:when test="contains($numericB4_C, '_')">
							<xsl:value-of select="substring-before($numericStart,'_')"/>
						</xsl:when>
						<xsl:otherwise>
							<xsl:value-of select="$numericB4_C"/>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<!--colEventPosition: <xsl:value-of select="$colEventPosition"/>-->
				<xsl:variable name="isColForRepeatingEvent" select="contains($numericB4_C, '_')"/>
				<!--isColForRepeatingEvent<xsl:value-of select="$isColForRepeatingEvent"/>-->
				<xsl:variable name="colRepeatEventKey">
					<xsl:if test="contains($numericB4_C, '_')">
						<xsl:value-of select="substring-after($numericB4_C,'_')"/>
					</xsl:if>
				</xsl:variable>
				<!--colRepeatEventKey: <xsl:value-of select="$colRepeatEventKey"/>-->
				<!-- get if this is crf or item column -->
				<xsl:variable name="colType">
					<xsl:choose>
						<xsl:when test="ends-with($currentToken,'Interviewer')">
							<xsl:text>Interviewer</xsl:text>
						</xsl:when>
						<xsl:when test="ends-with($currentToken,'Interview Date')">
							<xsl:text>InterviewDate</xsl:text>
						</xsl:when>
						<xsl:when test="ends-with($currentToken,'CRF Version Status')">
							<xsl:text>CRF Version Status</xsl:text>
						</xsl:when>
						<xsl:when test="ends-with($currentToken,'Version Name')">
							<xsl:text>Version Name</xsl:text>
						</xsl:when>
						<xsl:otherwise>
							<xsl:text>ItemData</xsl:text>
						</xsl:otherwise>
					</xsl:choose>
				</xsl:variable>
				<!--colType: *<xsl:value-of select="$colType"/>*	-->
				<xsl:variable name="numericAfter_C" select="substring-after($numericStart, '_C')"/>
				<!--numericAfter_C:<xsl:value-of select="$numericAfter_C"/>	-->
				<xsl:variable name="colCrfPosition">
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
				<!--colItemName: **<xsl:value-of select="$colItemName"/>**-->
				<xsl:variable name="isColForRepeatingGrp" select="contains($numericAfter_C, '_')"/>
				<!--isColForRepeatingGrp<xsl:value-of select="$isColForRepeatingGrp"/>-->
				<xsl:variable name="colRepeatGrpKey">
					<xsl:if test="contains($numericAfter_C, '_')">
						<xsl:value-of select="substring-after($numericAfter_C,'_')"/>
					</xsl:if>
				</xsl:variable>
				<!--colRepeatGrpKey: <xsl:value-of select="$colRepeatGrpKey"/>-->
				<xsl:choose>
					<xsl:when test="$colType = 'ItemData'">
						<!--data column -->
						<xsl:variable name="ifMatch">
							<xsl:for-each select="$subjectEvents">
								<xsl:variable name="eventOID" select="@StudyEventOID"/>
									<!--eventOID:<xsl:value-of select="$eventOID"/>	-->
								<xsl:variable name="eventRepeatKey" select="@StudyEventRepeatKey"/>
								<!--eventRepeatKey:<xsl:value-of select="$eventRepeatKey"/>-->
								<xsl:variable name="eventPosition">
									<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
										<xsl:if test="@OID = $eventOID">
											<xsl:copy-of select="position()"/>
										</xsl:if>
									</xsl:for-each>
								</xsl:variable>
								<!--eventPosition:<xsl:value-of select="$eventPosition"/>-->
								<xsl:choose>
									<xsl:when test="$colEventPosition = $eventPosition">
										<!--event matched-->
										<xsl:choose>
											<xsl:when test="$isColForRepeatingEvent">
												
												<xsl:choose>
													<xsl:when test="$colRepeatEventKey = $eventRepeatKey">
														
														<xsl:for-each select="./odm:FormData">
															<xsl:variable name="formOID" select="@FormOID"/>
															<!--formOID:<xsl:value-of select="$formOID"/>-->
															<!-- find crf position -->
															<xsl:variable name="matchingCRFRef" select="$allEventDefs/odm:FormRef[@FormOID = $formOID and ../@OID = $eventOID]"/>
															<xsl:variable name="formRefNodeId" select="generate-id($matchingCRFRef)"/>
															<xsl:variable name="crfPosition">
																<xsl:for-each select="$allFormRefElements">
																	<xsl:if test="@FormOID = $formOID">
																		<xsl:if test="$formRefNodeId = generate-id()">
																			<xsl:copy-of select="position()"/>
																		</xsl:if>
																	</xsl:if>
																</xsl:for-each>
															</xsl:variable>
															<!--crfPosition:<xsl:value-of select="$crfPosition"/>-->
															<xsl:choose>
																<xsl:when test="$crfPosition = normalize-space($colCrfPosition)">
																	
																	<xsl:for-each select="./odm:ItemGroupData">
																		<xsl:variable name="grpOID" select="@ItemGroupOID"/>
																		<!--grp OID<xsl:value-of select="$grpOID"/>-->
																		<xsl:variable name="grpRepeatKey" select="@ItemGroupRepeatKey"/>
																		<xsl:choose>
																			<xsl:when test="$isColForRepeatingGrp">
																				<xsl:choose>
																					<xsl:when test="$grpRepeatKey = normalize-space($colRepeatGrpKey)">
																						<!-- both event and grp repeating -->
																						<!-- check item name -->
																						<xsl:for-each select="./odm:ItemData">
																							<xsl:variable name="itemOID" select="@ItemOID"/>
																							<xsl:variable name="itemName" select="//odm:ItemDef[@OID = $itemOID]/@Name"/>
																							<xsl:choose>
																								<xsl:when test="normalize-space($colItemName) = $itemName">
																									<!--<xsl:text>M_</xsl:text>-->
																									<xsl:value-of select="$matchSep"/>
																									<xsl:value-of select="$eventOID"/>
																									<xsl:value-of select="$mValSeparator1"/>
																									<xsl:text>_</xsl:text>
																									<xsl:value-of select="$formOID"/>
																									<xsl:value-of select="$mValSeparator2"/>
																									<xsl:text>_</xsl:text>
																									<xsl:value-of select="$grpOID"/>
																									<xsl:value-of select="$mValSeparator3"/>
																									<xsl:text>_</xsl:text>
																									<xsl:value-of select="$itemOID"/>
																									<xsl:value-of select="$mValSeparator4"/>
																									<xsl:text>_</xsl:text>
																									<xsl:value-of select="$colRepeatEventKey"/>
																									<xsl:value-of select="$mValSeparator5"/>
																									<xsl:text>_</xsl:text>
																									<xsl:value-of select="$grpRepeatKey"/>
																								</xsl:when>
																								<xsl:otherwise>
																									<!--<xsl:text>N</xsl:text>-->
																									<xsl:value-of select="$nonMatchSep"/>
																								</xsl:otherwise>
																							</xsl:choose>
																						</xsl:for-each>
																					</xsl:when>
																					<xsl:otherwise>
																						<!--<xsl:text>N</xsl:text>-->
																						<xsl:value-of select="$nonMatchSep"/>
																					</xsl:otherwise>
																				</xsl:choose>
																			</xsl:when>
																			<xsl:otherwise>
																				<!--only event repeating -->
																				<!-- check item name -->
																				<xsl:for-each select="./odm:ItemData">
																					<xsl:variable name="itemOID" select="@ItemOID"/>
																					<xsl:variable name="itemName" select="//odm:ItemDef[@OID = $itemOID]/@Name"/>
																					<xsl:choose>
																						<xsl:when test="normalize-space($colItemName) = $itemName">
																							<!--<xsl:text>M_</xsl:text>-->
																							<xsl:value-of select="$matchSep"/>
																							<xsl:value-of select="$eventOID"/>
																							<xsl:value-of select="$mValSeparator1"/>
																							<xsl:text>_</xsl:text>
																							<xsl:value-of select="$formOID"/>
																							<xsl:value-of select="$mValSeparator2"/>
																							<xsl:text>_</xsl:text>
																							<xsl:value-of select="$grpOID"/>
																							<xsl:value-of select="$mValSeparator3"/>
																							<xsl:text>_</xsl:text>
																							<xsl:value-of select="$itemOID"/>
																							<xsl:value-of select="$mValSeparator4"/>
																							<xsl:text>_</xsl:text>
																							<xsl:value-of select="$colRepeatEventKey"/>
																						</xsl:when>
																						<xsl:otherwise>
																							<!--<xsl:text>N</xsl:text>-->
																							<xsl:value-of select="$nonMatchSep"/>
																						</xsl:otherwise>
																					</xsl:choose>
																				</xsl:for-each>
																			</xsl:otherwise>
																		</xsl:choose>
																	</xsl:for-each>
																</xsl:when>
																<xsl:otherwise>
																	<!--<xsl:text>N</xsl:text>-->
																	<xsl:value-of select="$nonMatchSep"/>
																</xsl:otherwise>
															</xsl:choose>
														</xsl:for-each>
													</xsl:when>
													<xsl:otherwise>
														<!--<xsl:text>N</xsl:text>-->
														<xsl:value-of select="$nonMatchSep"/>
													</xsl:otherwise>
												</xsl:choose>
											</xsl:when>
											<xsl:otherwise>
												<!--non-repeating event-->
												<xsl:for-each select="./odm:FormData">
													<xsl:variable name="formOID" select="@FormOID"/>
													<!--formOID:<xsl:value-of select="$formOID"/>-->
													<!-- find crf position -->
													<!--<xsl:variable name="matchingEventDef" select="$allEventDefs[@OID = $eventOID]"/>	
										<xsl:variable name="matchingCRFRef" select="$matchingEventDef/odm:FormRef[@FormOID = $formOID]"/>-->
													<xsl:variable name="matchingCRFRef" select="$allEventDefs/odm:FormRef[@FormOID = $formOID and ../@OID = $eventOID]"/>
													<xsl:variable name="formRefNodeId" select="generate-id($matchingCRFRef)"/>
													<xsl:variable name="crfPosition">
														<xsl:for-each select="$allFormRefElements">
															<xsl:if test="@FormOID = $formOID">
																<xsl:if test="$formRefNodeId = generate-id()">
																	<xsl:copy-of select="position()"/>
																</xsl:if>
															</xsl:if>
														</xsl:for-each>
													</xsl:variable>
													<!--crfPosition:<xsl:value-of select="$crfPosition"/>-->
													<xsl:choose>
														<xsl:when test="$crfPosition = normalize-space($colCrfPosition)">
															<!--crf matched-->
															<xsl:for-each select="./odm:ItemGroupData">
																<xsl:variable name="grpOID" select="@ItemGroupOID"/>
																<!--grpOID:<xsl:value-of select="$grpOID"/>-->
																<xsl:variable name="grpRepeatKey" select="@ItemGroupRepeatKey"/>
																<!--grpRepeatKey:*<xsl:value-of select="$grpRepeatKey"/>*
																colRepeatGrpKey:*<xsl:value-of select="$colRepeatGrpKey"/>*-->
																<xsl:choose>
																	<xsl:when test="$isColForRepeatingGrp">
																		<xsl:choose>
																			<xsl:when test="$grpRepeatKey = normalize-space($colRepeatGrpKey)">
																				<!--grp matched-->
																				<!-- check item name -->
																				<xsl:for-each select="./odm:ItemData">
																					<xsl:variable name="itemOID" select="@ItemOID"/>
																					<xsl:variable name="itemName" select="//odm:ItemDef[@OID = $itemOID]/@Name"/>
																					<xsl:choose>
																						<xsl:when test="normalize-space($colItemName) = $itemName">
																							<!-- only grp repeating -->
																							<!--<xsl:text>M_</xsl:text>-->
																							<xsl:value-of select="$matchSep"/>
																							<xsl:value-of select="$eventOID"/>
																							<xsl:value-of select="$mValSeparator1"/>
																							<xsl:text>_</xsl:text>
																							<xsl:value-of select="$formOID"/>
																							<xsl:value-of select="$mValSeparator2"/>
																							<xsl:text>_</xsl:text>
																							<xsl:value-of select="$grpOID"/>
																							<xsl:value-of select="$mValSeparator3"/>
																							<xsl:text>_</xsl:text>
																							<xsl:value-of select="$itemOID"/>
																							<xsl:value-of select="$mValSeparator5"/>
																							<xsl:text>_</xsl:text>
																							<xsl:value-of select="$grpRepeatKey"/>
																						</xsl:when>
																						<xsl:otherwise>
																							<!--<xsl:text>N</xsl:text>-->
																							<xsl:value-of select="$nonMatchSep"/>
																						</xsl:otherwise>
																					</xsl:choose>
																				</xsl:for-each>
																			</xsl:when>
																			<xsl:otherwise>
																				<!--<xsl:text>N</xsl:text>-->
																				<xsl:value-of select="$nonMatchSep"/>
																			</xsl:otherwise>
																		</xsl:choose>
																	</xsl:when>
																	<xsl:otherwise>
																		<!-- check item name -->
																		<xsl:for-each select="./odm:ItemData">
																			<xsl:variable name="itemOID" select="@ItemOID"/>
																			<!--itemOID:<xsl:value-of select="$itemOID"/>-->
																			<xsl:variable name="itemName" select="//odm:ItemDef[@OID = $itemOID]/@Name"/>
																			<!--itemName:<xsl:value-of select="$itemName"/>-->
																			<xsl:choose>
																				<xsl:when test="normalize-space($colItemName) = $itemName">
																					<!-- nothing repeating -->
																					<!--<xsl:text>M_</xsl:text>-->
																					<xsl:value-of select="$matchSep"/>
																					<xsl:value-of select="$eventOID"/>
																					<xsl:value-of select="$mValSeparator1"/>
																					<xsl:text>_</xsl:text>
																					<xsl:value-of select="$formOID"/>
																					<xsl:value-of select="$mValSeparator2"/>
																					<xsl:text>_</xsl:text>
																					<xsl:value-of select="$grpOID"/>
																					<xsl:value-of select="$mValSeparator3"/>
																					<xsl:text>_</xsl:text>
																					<xsl:value-of select="$itemOID"/>
																				</xsl:when>
																				<xsl:otherwise>
																					<!--<xsl:text>N</xsl:text>-->
																					<xsl:value-of select="$nonMatchSep"/>
																				</xsl:otherwise>
																			</xsl:choose>
																		</xsl:for-each>
																	</xsl:otherwise>
																</xsl:choose>
															</xsl:for-each>
														</xsl:when>
														<xsl:otherwise>
															<!--<xsl:text>N</xsl:text>-->
															<xsl:value-of select="$nonMatchSep"/>
														</xsl:otherwise>
													</xsl:choose>
												</xsl:for-each>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>
									<xsl:otherwise>
										<!--<xsl:text>N</xsl:text>-->
										<xsl:value-of select="$nonMatchSep"/>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
							<!-- subjectEvents-->
						</xsl:variable>
						<!--ifMatch:<xsl:value-of select="$ifMatch"/>-->
						<xsl:choose>
							<xsl:when test="contains($ifMatch, $matchSep)">
								<!--	
							<xsl:variable name="ifMatchTokenized" select="tokenize($ifMatch,'_')"/> 
							<xsl:variable name="eventOID" select="$ifMatchTokenized[2]" />
							<xsl:variable name="formOID" select="$ifMatchTokenized[3]" />
							<xsl:variable name="grpOID" select="$ifMatchTokenized[4]" />
							<xsl:variable name="itemOID" select="$ifMatchTokenized[5]" />
							<xsl:variable name="eventRepeatKey">
								<xsl:for-each select="$ifMatchTokenized">
									<xsl:if test="contains(.,'E')">
										<xsl:value-of select="substring-after(.,'E')"/>
									</xsl:if>
								</xsl:for-each>
							</xsl:variable>
							<xsl:variable name="grpRepeatKey">
								<xsl:for-each select="$ifMatchTokenized">
									<xsl:if test="contains(.,'G')">
										<xsl:value-of select="substring-after(.,'G')"/>
									</xsl:if>
								</xsl:for-each>
							</xsl:variable>							
							-->
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
										<xsl:when test="$isColForRepeatingEvent">
											<!-- only event repeating or both event and grp repeating -->
											<xsl:value-of select="substring-before(substring-after($ifMatch, concat($mValSeparator3,'_')), concat($mValSeparator4,'_'))"/>
										</xsl:when>
										<xsl:when test="not($isColForRepeatingEvent) and $isColForRepeatingGrp">
											<xsl:value-of select="substring-before(substring-after($ifMatch, concat($mValSeparator3,'_')), concat($mValSeparator5,'_'))"/>
										</xsl:when>
										<xsl:otherwise>
											<!-- nothing repeating -->
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
								<!--<xsl:variable name="itemData" select="$subjectEvents/odm:FormData/odm:ItemGroupData/odm:ItemData
							[@ItemOID = $itemOID  
							 and ../@ItemGroupOID=$grpOID and ../@ItemGroupRepeatKey = $grpRepeatKey
							 and ../../@FormOID = $formOID 
							 and ../../../@StudyEventOID = $eventOID and ../../../@StudyEventRepeatKey = $eventRepeatKey]"/>-->
								<!-- write data -->
								<xsl:choose>
									<xsl:when test="$isColForRepeatingEvent and $isColForRepeatingGrp">
										<xsl:variable name="itemData" select="$subjectItems[@ItemOID = $itemOID  
									 and ../@ItemGroupOID=$grpOID  and ../@ItemGroupRepeatKey = $grpRepeatKey
									 and ../../@FormOID = $formOID 
									 and ../../../@StudyEventOID = $eventOID and ../../../@StudyEventRepeatKey = $eventRepeatKey]"/>
										<!--itemData oid:<xsl:value-of select="$itemData/@ItemOID"/>-->
										<xsl:choose>
											<xsl:when test="$itemData/@Value">
												<td class="table_cell">
													<xsl:value-of select="$itemData/@Value"/>
												</td>
											</xsl:when>
											<xsl:when test="$itemData/@OpenClinica:ReasonForNull">
												<td class="table_cell">
													<xsl:value-of select="$itemData/@OpenClinica:ReasonForNull"/>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>
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
												<td class="table_cell">
													<xsl:value-of select="$itemData/@Value"/>
												</td>
											</xsl:when>
											<xsl:when test="$itemData/@OpenClinica:ReasonForNull">
												<td class="table_cell">
													<xsl:value-of select="$itemData/@OpenClinica:ReasonForNull"/>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:when>
									<xsl:when test="not($isColForRepeatingEvent) and $isColForRepeatingGrp">
									
										<xsl:variable name="itemData" select="$subjectItems[@ItemOID = $itemOID  
									 and ../@ItemGroupOID=$grpOID  and ../@ItemGroupRepeatKey = $grpRepeatKey
									 and ../../@FormOID = $formOID 
									 and ../../../@StudyEventOID = $eventOID]"/>
									 <!--item data: *<xsl:value-of select="$itemData/@Value" />*-->
										<!--itemData oid:<xsl:value-of select="$itemData/@ItemOID"/>-->
										<xsl:choose>
											<xsl:when test="$itemData/@Value">
												<td class="table_cell">
													<xsl:value-of select="$itemData/@Value"/>
												</td>
											</xsl:when>
											<xsl:when test="$itemData/@OpenClinica:ReasonForNull">
												<td class="table_cell">
													<xsl:value-of select="$itemData/@OpenClinica:ReasonForNull"/>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>
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
												<td class="table_cell">
													<xsl:value-of select="$itemData/@Value"/>
												</td>
											</xsl:when>
											<xsl:when test="$itemData/@OpenClinica:ReasonForNull">
												<td class="table_cell">
													<xsl:value-of select="$itemData/@OpenClinica:ReasonForNull"/>
												</td>
											</xsl:when>
											<xsl:otherwise>
												<td class="table_cell">
													<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
												</td>
											</xsl:otherwise>
										</xsl:choose>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<!--<xsl:if test="$colType = 'Interviewer'">										
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>											
						</xsl:if>
						<xsl:if test="$colType = 'InterviewDate'">							
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>																	
						</xsl:if>
						<xsl:if test="$colType = 'CRF Version Status'">
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>														
						</xsl:if>
						<xsl:if test="$colType = 'Version Name'">							
							<td class="table_cell">
								<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
							</td>																			
						</xsl:if>	-->
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:when>
					<xsl:otherwise>
						<!--crf column -->
						<!-- iterate study events. for each study event; if repeating - check if crf in column name is present and event info matched as well. If yes write the data. 
																						same for non-repeating except the event repeat key will not be matched. if matched write data else write empty column-->
						<xsl:variable name="ifMatch">
							<xsl:for-each select="$subjectEvents">
								<xsl:variable name="eventOID" select="@StudyEventOID"/>
								<!--eventOID:<xsl:value-of select="$eventOID"/>-->
								<xsl:variable name="eventPosition">
									<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
										<xsl:if test="@OID = $eventOID">
											<xsl:copy-of select="position()"/>
										</xsl:if>
									</xsl:for-each>
								</xsl:variable>
								<!--eventPosition: <xsl:value-of select="$eventPosition"/>-->
								<xsl:choose>
									<xsl:when test="$colEventPosition = $eventPosition">
										<!--event matched-->
										<xsl:for-each select="./odm:FormData">
											<xsl:variable name="formOID" select="@FormOID"/>
											<!--formOID:<xsl:value-of select="$formOID"/>-->
											<!-- find crf position -->
											<xsl:variable name="matchingCRFRef" select="$allEventDefs/odm:FormRef[@FormOID = $formOID and ../@OID = $eventOID]"/>
											<!--matchingCRFRef OID: <xsl:value-of select="$matchingCRFRef/@FormOID"/>-->
											<xsl:variable name="formRefNodeId" select="generate-id($matchingCRFRef)"/>
											<xsl:variable name="crfPosition">
												<xsl:for-each select="$allFormRefElements">
													<xsl:if test="@FormOID = $formOID">
														<xsl:if test="$formRefNodeId = generate-id()">
															<xsl:copy-of select="position()"/>
														</xsl:if>
													</xsl:if>
												</xsl:for-each>
											</xsl:variable>
											<!--crfPosition: *<xsl:value-of select="$crfPosition"/>*-->
											<xsl:choose>
												<xsl:when test="$crfPosition = normalize-space($colCrfPosition)">
													<!--crf matched-->
													<xsl:choose>
														<xsl:when test="$isColForRepeatingEvent">
															<!--col for repeating event -->
															<xsl:choose>
																<xsl:when test="../@StudyEventRepeatKey = normalize-space($colRepeatEventKey)">
																	<!--event repeat key matched-->
																	<!--<xsl:text>M_</xsl:text>-->
																	<xsl:value-of select="$matchSep"/>
																	<xsl:value-of select="$eventOID"/>
																	<xsl:value-of select="$mValSeparator1"/>
																	<xsl:value-of select="$formOID"/>
																	<xsl:value-of select="$mValSeparator2"/>
																	<xsl:value-of select="../@StudyEventRepeatKey"/>
																</xsl:when>
																<xsl:otherwise>
																	<!--<xsl:text>N</xsl:text>-->
																	<xsl:value-of select="$nonMatchSep"/>
																	<!--event repeat key mismatch-->
																</xsl:otherwise>
															</xsl:choose>
														</xsl:when>
														<xsl:otherwise>
															<!--<xsl:text>M_</xsl:text>-->
															<xsl:value-of select="$matchSep"/>
															<xsl:value-of select="$eventOID"/>
															<xsl:value-of select="$mValSeparator1"/>
															<xsl:value-of select="$formOID"/>
															<!--match for non-repeating event-->
														</xsl:otherwise>
													</xsl:choose>
												</xsl:when>
												<xsl:otherwise>
													<!--crf mismatch-->
													<!--<xsl:text>N</xsl:text>-->
													<xsl:value-of select="$nonMatchSep"/>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:for-each>
									</xsl:when>
									<xsl:otherwise>
										<!--<xsl:text>N</xsl:text>-->
										<xsl:value-of select="$nonMatchSep"/>
										<!--event mismatch-->
									</xsl:otherwise>
								</xsl:choose>
							</xsl:for-each>
						</xsl:variable>
						<!--<xsl:variable name="ifMatch" select="'NN'"/>-->
						<!--ifMatch: <xsl:value-of select="$ifMatch"/>-->
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
								<xsl:variable name="eventRepeatKey">
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
								</xsl:variable>
								<!--eventRepeatKey: <xsl:value-of  select="$eventRepeatKey"/>-->
								<xsl:variable name="eventT" select="$subjectEvents[@StudyEventOID = $eventOID]"/>
								<xsl:choose>
									<xsl:when test="$isColForRepeatingEvent">
										<xsl:variable name="formData" select="$subjectEvents/odm:FormData[@FormOID = $formOID and ../@StudyEventOID = $eventOID and ../@StudyEventRepeatKey = $eventRepeatKey]"/>
										<xsl:if test="$colType = 'Interviewer'">
											<xsl:choose>
												<xsl:when test="$formData/@OpenClinica:InterviewerName">
													<td class="table_cell">
														<xsl:value-of select="$formData/@OpenClinica:InterviewerName"/>
													</td>
												</xsl:when>
												<xsl:otherwise>
													<td class="table_cell">
														<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
													</td>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
										<xsl:if test="$colType = 'InterviewDate'">
											<xsl:choose>
												<xsl:when test="$formData/@OpenClinica:InterviewDate">
													<td class="table_cell">
														<xsl:value-of select="$formData/@OpenClinica:InterviewDate"/>
													</td>
												</xsl:when>
												<xsl:otherwise>
													<td class="table_cell">
														<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
													</td>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
										<xsl:if test="$colType = 'CRF Version Status'">
											<xsl:choose>
												<xsl:when test="$formData/@OpenClinica:Status">
													<td class="table_cell">
														<xsl:value-of select="$formData/@OpenClinica:Status"/>
													</td>
												</xsl:when>
												<xsl:otherwise>
													<td class="table_cell">
														<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
													</td>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
										<xsl:if test="$colType = 'Version Name'">
											<xsl:choose>
												<xsl:when test="$formData/@OpenClinica:Version">
													<td class="table_cell">
														<xsl:value-of select="$formData/@OpenClinica:Version"/>
													</td>
												</xsl:when>
												<xsl:otherwise>
													<td class="table_cell">
														<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
													</td>
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
													<td class="table_cell">
														<xsl:value-of select="$formData/@OpenClinica:InterviewerName"/>
													</td>
												</xsl:when>
												<xsl:otherwise>
													<td class="table_cell">
														<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
													</td>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
										<xsl:if test="$colType = 'InterviewDate'">
											<xsl:choose>
												<xsl:when test="$formData/@OpenClinica:InterviewDate">
													<td class="table_cell">
														<xsl:value-of select="$formData/@OpenClinica:InterviewDate"/>
													</td>
												</xsl:when>
												<xsl:otherwise>
													<td class="table_cell">
														<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
													</td>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
										<xsl:if test="$colType = 'CRF Version Status'">
											<xsl:choose>
												<xsl:when test="$formData/@OpenClinica:Status">
													<td class="table_cell">
														<xsl:value-of select="$formData/@OpenClinica:Status"/>
													</td>
												</xsl:when>
												<xsl:otherwise>
													<td class="table_cell">
														<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
													</td>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
										<xsl:if test="$colType = 'Version Name'">
											<xsl:choose>
												<xsl:when test="$formData/@OpenClinica:Version">
													<td class="table_cell">
														<xsl:value-of select="$formData/@OpenClinica:Version"/>
													</td>
												</xsl:when>
												<xsl:otherwise>
													<td class="table_cell">
														<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
													</td>
												</xsl:otherwise>
											</xsl:choose>
										</xsl:if>
									</xsl:otherwise>
								</xsl:choose>
							</xsl:when>
							<xsl:otherwise>
								<xsl:if test="$colType = 'Interviewer'">
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:if>
								<xsl:if test="$colType = 'InterviewDate'">
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:if>
								<xsl:if test="$colType = 'CRF Version Status'">
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:if>
								<xsl:if test="$colType = 'Version Name'">
									<td class="table_cell">
										<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
									</td>
								</xsl:if>
							</xsl:otherwise>
						</xsl:choose>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:if>
		</xsl:for-each>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData" mode="studyEventInfo">
		<xsl:variable name="StudyEventDataOID" select="@StudyEventOID"/>
		<xsl:variable name="studyEventRepeatKey" select="@StudyEventRepeatKey"/>
		<!--<xsl:variable name="eventPosition">
		<xsl:for-each select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 	= generate-id(key('studyEvents',@StudyEventOID)[1])]">
		<xsl:copy-of select="position()" />
		</xsl:for-each>
		</xsl:variable>-->
		<xsl:variable name="eventPosition">
			<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $StudyEventDataOID">
					<xsl:copy-of select="position()"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="crfStatusExist" select="odm:FormData/@OpenClinica:Status"/>
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation"/>
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate"/>
		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status"/>
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent"/>
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate"/>
		<xsl:variable name="currentNodeId" select="generate-id()"/>
		<xsl:variable name="currentNodePositon">
			<xsl:for-each select="$allStudyEventDataElements">
				<xsl:choose>
					<xsl:when test="$studyEventRepeatKey">
						<xsl:if test="@StudyEventOID = $StudyEventDataOID and @StudyEventRepeatKey = $studyEventRepeatKey">
							<xsl:if test="$currentNodeId = generate-id()">
								<xsl:copy-of select="position()"/>
							</xsl:if>
						</xsl:if>
					</xsl:when>
					<xsl:otherwise>
						<xsl:if test="@StudyEventOID = $StudyEventDataOID ">
							<xsl:if test="$currentNodeId = generate-id()">
								<xsl:copy-of select="position()"/>
							</xsl:if>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:for-each>
		</xsl:variable>
		<!--currentNodePositon: <xsl:value-of select="$currentNodePositon"/>-->
		<xsl:variable name="priorStudyElementDataElements" select="$allStudyEventDataElements[position() &lt; $currentNodePositon]"/>
		<!--priorStudyElementDataElements<xsl:value-of select="$priorStudyElementDataElements"/>-->
		<xsl:choose>
			<xsl:when test="$studyEventRepeatKey">
				<!--studyEventRepeatKey<xsl:value-of select="$studyEventRepeatKey"/>	
				prior match cnt: <xsl:value-of select="count($priorStudyElementDataElements[@StudyEventOID = $StudyEventDataOID and @StudyEventRepeatKey = $studyEventRepeatKey]) "/>-->
				<xsl:choose>
					<xsl:when test="count($priorStudyElementDataElements[@StudyEventOID = $StudyEventDataOID and @StudyEventRepeatKey = $studyEventRepeatKey]) &gt; 0">
							</xsl:when>
					<xsl:otherwise>
						<!--create column -->
						<xsl:if test="$eventLocationExist">
							<td class="table_header_row">
								<xsl:text>Location_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$studyEventRepeatKey"/>
							</td>
						</xsl:if>
						<xsl:if test="$eventStartDateExist">
							<td class="table_header_row">
								<xsl:text>StartDate_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$studyEventRepeatKey"/>
							</td>
						</xsl:if>
						<xsl:if test="$eventEndDateExist">
							<td class="table_header_row">
								<xsl:text>EndDate_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$studyEventRepeatKey"/>
							</td>
						</xsl:if>
						<xsl:if test="$eventStatusExist">
							<td class="table_header_row">
								<xsl:text>Event Status_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$studyEventRepeatKey"/>
							</td>
						</xsl:if>
						<xsl:if test="$ageExist">
							<td class="table_header_row">
								<xsl:text>Age_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$studyEventRepeatKey"/>
							</td>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:when>
			<xsl:otherwise>
				<xsl:choose>
					<xsl:when test="count($priorStudyElementDataElements[@StudyEventOID = $StudyEventDataOID and @StudyEventRepeatKey = $studyEventRepeatKey]) &gt; 0">
							</xsl:when>
					<xsl:otherwise>
						<!--create column -->
						<xsl:if test="$eventLocationExist">
							<td class="table_header_row">
								<xsl:text>Location_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
							</td>
						</xsl:if>
						<xsl:if test="$eventStartDateExist">
							<td class="table_header_row">
								<xsl:text>StartDate_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
							</td>
						</xsl:if>
						<xsl:if test="$eventEndDateExist">
							<td class="table_header_row">
								<xsl:text>EndDate_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
							</td>
						</xsl:if>
						<xsl:if test="$eventStatusExist">
							<td class="table_header_row">
								<xsl:text>Event Status_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
							</td>
						</xsl:if>
						<xsl:if test="$ageExist">
							<td class="table_header_row">
								<xsl:text>Age_</xsl:text>
								<xsl:value-of select="$E"/>
								<xsl:value-of select="$eventPosition"/>
							</td>
						</xsl:if>
					</xsl:otherwise>
				</xsl:choose>
			</xsl:otherwise>
		</xsl:choose>
		<!--	
				<xsl:if test="$eventLocationExist">								
					<td class="table_header_row">
									<xsl:text>Location_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>										
									</td>
								</xsl:if>

								<xsl:if test="$eventStartDateExist">
									<td class="table_header_row">
									<xsl:text>StartDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
									</td>
								</xsl:if>

								<xsl:if test="$eventEndDateExist">
									<td class="table_header_row">
									<xsl:text>EndDate_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
	</td>
								</xsl:if>

								<xsl:if test="$eventStatusExist">
									<td class="table_header_row">
									<xsl:text>Event Status_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
									<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
	</td>
								</xsl:if>

								<xsl:if test="$ageExist">
									<td class="table_header_row">
									<xsl:text>Age_</xsl:text>
									<xsl:value-of select="$E" />
									<xsl:value-of select="$eventPosition" />
								<xsl:if test="@StudyEventRepeatKey">
											<xsl:text>_</xsl:text>
											<xsl:value-of select="@StudyEventRepeatKey" />
										</xsl:if>
										<xsl:text>_</xsl:text>
							</td>
								</xsl:if>
	-->
		<!--
		<xsl:apply-templates
			select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
			mode="studyEventHeader" />-->
	</xsl:template>
	<xsl:template mode="studyEventInfoHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
		<xsl:variable name="eventDefOID" select="@OID"/>
		<xsl:variable name="isRepeating" select="@Repeating"/>
		<xsl:variable name="allStudyEventDataElements" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID = $eventDefOID]"/>
		<!--<xsl:variable name="eventLocationExist" select="count($allStudyEventDataElements[@OpenClinica:StudyEventLocation]) &gt; 0"/>
		<xsl:variable name="eventStartDateExist" select="count($allStudyEventDataElements[@OpenClinica:StartDate]) &gt; 0"/>
		<xsl:variable name="eventStatusExist" select="count($allStudyEventDataElements[@OpenClinica:Status]) &gt; 0"/>
		<xsl:variable name="eventEndDateExist" select="count($allStudyEventDataElements[@OpenClinica:EndDate]) &gt; 0"/>
		<xsl:variable name="ageExist" select="count($allStudyEventDataElements[@OpenClinica:SubjectAgeAtEvent]) &gt; 0"/>-->
		
		
		<xsl:variable name="eventPosition">
			<xsl:copy-of select="position()"/>
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
				<xsl:apply-templates select="." mode="createColForRepeatingEvent">
					<xsl:with-param name="eventRepeatCnt" select="1"/>
					<xsl:with-param name="eventOID" select="$eventDefOID"/>
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
					<!--<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
					<xsl:with-param name="eventStartDateExist" select="$eventStartDateExist"/>
					<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
					<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
					<xsl:with-param name="ageExist" select="$ageExist"/>-->
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<!-- write event data header columns for non repeating event -->
				<xsl:apply-templates select="." mode="createColForNonRepeatingEvent">
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<!--<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
					<xsl:with-param name="eventStartDateExist" select="$eventStartDateExist"/>
					<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
					<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
					<xsl:with-param name="ageExist" select="$ageExist"/>-->
					<xsl:with-param name="eventOID" select="$eventDefOID"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="createColForRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" mode="createColForRepeatingEvent">
		<xsl:param name="eventOID"/>
		<xsl:param name="eventPosition"/> 
		<xsl:param name="MaxEventRepeatKey"/>
		<!--<xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist"/>
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>-->		
		<xsl:param name="eventRepeatCnt"/><!--createColForRepeatingEvent, eventOID<xsl:value-of select="$eventOID"/>-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:StudyEventLocation]) &gt; 0">
				<!--<xsl:if test="$eventLocationExist">-->
					<td class="table_header_row">
						<xsl:text>Location_</xsl:text>
						<xsl:value-of select="$E"/>
						<xsl:value-of select="$eventPosition"/>
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$eventRepeatCnt"/>
					</td>
				<!--</xsl:if>-->
			</xsl:if>
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:StartDate]) &gt; 0"><!--col for event startdate-->
			<!--<xsl:if test="$eventStartDateExist">-->
				<td class="table_header_row">
					<xsl:text>StartDate_</xsl:text>
					<xsl:value-of select="$E"/>
					<xsl:value-of select="$eventPosition"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$eventRepeatCnt"/>
				</td>
			<!--</xsl:if>-->
			</xsl:if>
			<!--<xsl:if test="$eventEndDateExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:EndDate]) &gt; 0">
				<td class="table_header_row">
					<xsl:text>EndDate_</xsl:text>
					<xsl:value-of select="$E"/>
					<xsl:value-of select="$eventPosition"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$eventRepeatCnt"/>
				</td>
			</xsl:if>
			<!--<xsl:if test="$eventStatusExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:Status]) &gt; 0">
				<td class="table_header_row">
					<xsl:text>Event Status_</xsl:text>
					<xsl:value-of select="$E"/>
					<xsl:value-of select="$eventPosition"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$eventRepeatCnt"/>
				</td>
			<!--</xsl:if>-->
			</xsl:if>
			<!--<xsl:if test="$ageExist">-->
			<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $eventRepeatCnt and @OpenClinica:SubjectAgeAtEvent]) &gt; 0">
				<td class="table_header_row">
					<xsl:text>Age_</xsl:text>
					<xsl:value-of select="$E"/>
					<xsl:value-of select="$eventPosition"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$eventRepeatCnt"/>
				</td>
			<!--</xsl:if>-->
			</xsl:if>
			
			<!--<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = 
					($eventRepeatCnt+1)]) &gt; 0">-->
			<!-- fix for issue 11832: corrected to repeat the process for next incremental event repeat key until it reaches the value of "MaxEventRepeatKey" -->
			<xsl:if test="($eventRepeatCnt+1) &lt;= number($MaxEventRepeatKey)">		
				<xsl:call-template name="createColForRepeatingEvent">
					<xsl:with-param name="eventRepeatCnt" select="$eventRepeatCnt+1"/>
					<xsl:with-param name="eventOID" select="$eventOID"/>
					<xsl:with-param name="eventPosition" select="$eventPosition"/>
					<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
					<!--<xsl:with-param name="eventLocationExist" select="$eventLocationExist"/>
					<xsl:with-param name="eventStartDateExist" select="$eventStartDateExist"/>
					<xsl:with-param name="eventStatusExist" select="$eventStatusExist"/>
					<xsl:with-param name="eventEndDateExist" select="$eventEndDateExist"/>
					<xsl:with-param name="ageExist" select="$ageExist"/>-->
				</xsl:call-template>
			</xsl:if>	
		
	</xsl:template>
	<xsl:template mode="createColForNonRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef">
		<xsl:param name="eventPosition"/>
		<xsl:param name="eventOID"/>
		
		<!--<xsl:param name="eventLocationExist"/>
		<xsl:param name="eventStartDateExist"/>
		<xsl:param name="eventStatusExist"/>
		<xsl:param name="eventEndDateExist"/>
		<xsl:param name="ageExist"/>-->
		<!--createColForNonRepeatingEvent-->
		<!--<xsl:if test="$eventLocationExist">-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:StudyEventLocation]) &gt; 0">
			<td class="table_header_row">
				<xsl:text>Location_</xsl:text>
				<xsl:value-of select="$E"/>
				<xsl:value-of select="$eventPosition"/>
			</td>
		<!--</xsl:if>-->
		</xsl:if>
		<!--<xsl:if test="$eventStartDateExist">-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:StartDate]) &gt; 0">
			<td class="table_header_row">
				<xsl:text>StartDate_</xsl:text>
				<xsl:value-of select="$E"/>
				<xsl:value-of select="$eventPosition"/>
			</td>
		<!--</xsl:if>-->
		</xsl:if>
		<!--<xsl:if test="$eventEndDateExist">-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:EndDate]) &gt; 0">
			<td class="table_header_row">
				<xsl:text>EndDate_</xsl:text>
				<xsl:value-of select="$E"/>
				<xsl:value-of select="$eventPosition"/>
			</td>
		<!--</xsl:if>-->
		</xsl:if>
		<!--<xsl:if test="$eventStatusExist">-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:Status]) &gt; 0">
			<td class="table_header_row">
				<xsl:text>Event Status_</xsl:text>
				<xsl:value-of select="$E"/>
				<xsl:value-of select="$eventPosition"/>
			</td>
		<!--</xsl:if>-->
		</xsl:if>
		<!--<xsl:if test="$ageExist">-->
		<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @OpenClinica:SubjectAgeAtEvent]) &gt; 0">
			<td class="table_header_row">
				<xsl:text>Age_</xsl:text>
				<xsl:value-of select="$E"/>
				<xsl:value-of select="$eventPosition"/>
			</td>
		<!--</xsl:if>-->
		</xsl:if>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:ClinicalData" mode="clinicalMetadata">
		<xsl:apply-templates select="odm:SubjectData" mode="SubjectMetaData"/>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData" mode="SubjectMetaData">
		<xsl:apply-templates select="odm:StudyEventData" mode="studyEventMetadata"/>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData" mode="studyEventMetadata">
		<xsl:apply-templates select="odm:FormData" mode="FormMetaData"/>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData" mode="FormMetaData">
		<xsl:apply-templates select="odm:ItemGroupData" mode="itemGroupMetadata"/>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData" mode="itemGroupMetadata">
		<xsl:apply-templates select="odm:ItemData" mode="itemDataMetadata"/>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData" mode="itemDataMetadata">
		<xsl:variable name="OID_Item" select="@ItemOID"/>
		<xsl:for-each select="$itemDef[@OID=$OID_Item]">
			<xsl:variable name="codeListOID" select="./odm:CodeListRef/@CodeListOID"/>
			<xsl:variable name="phi" select="./OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm/@PHI"/>
			<xsl:variable name="crfOID" select="@OpenClinica:FormOIDs"/>
			<div style="display:none;">
				<xsl:attribute name="id"><xsl:apply-templates select="@OID"/></xsl:attribute>
				<h1>
					<span class="title_manage">Item Metadata: Global Attributes</span>
				</h1>
				<table border="1" cellpadding="0" cellspacing="0">
					<tr valign="top">
						<td class="table_header_column_top">CRF Name:</td>
						<td class="table_cell_top">
							<xsl:for-each select="$crfDetails[@OID=$crfOID]">
								<xsl:value-of select="@Name"/>
							</xsl:for-each>
						</td>
					</tr>
					<tr>
						<td class="table_header_column">Item Name:</td>
						<td class="table_cell">
							<xsl:value-of select="@Name"/>
						</td>
					</tr>
					<tr>
						<td class="table_header_column">OID:</td>
						<td class="table_cell">
							<xsl:value-of select="@OID"/>
						</td>
					</tr>
					<tr>
						<td class="table_header_column">Description:</td>
						<td class="table_cell">
							<xsl:value-of select="@Comment"/>
						</td>
					</tr>
					<tr>
						<td class="table_header_column">Data Type:</td>
						<td class="table_cell">
							<xsl:value-of select="@DataType"/>
						</td>
					</tr>
					<tr>
						<td class="table_header_column">PHI:</td>
						<td class="table_cell">
							<xsl:value-of select="$phi"/>
						</td>
					</tr>
				</table>
				<br/>
				<span class="table_title_Manage">Item Metadata: CRF Version Level
                                                    Attributes
                                                </span>
				<br/>
				<xsl:for-each select="./OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm">
					<xsl:variable name="formOID" select="@FormOID"/>
					<xsl:for-each select="$crfDetails[@OID=$formOID]">
						<br/>
						<span class="expandFormLink">
							<a>
								<xsl:attribute name="href">#</xsl:attribute>
								<xsl:attribute name="onclick">
                                                                    showDiv('<xsl:value-of select="$formOID"/>');return
                                                                    false;
                                                                </xsl:attribute>
								<xsl:value-of select="@Name"/>
							</a>
						</span>
					</xsl:for-each>
					<div class="tablebox_center" align="center" style="display:block;">
						<xsl:attribute name="id"><xsl:value-of select="@FormOID"/></xsl:attribute>
						<script language="javascript">
                                                            function showDiv(n) {
                                                            s = document.getElementById(n);
                                                            if (s.style.display == "block") {
                                                            s.style.display = "none";
                                                            } else {
                                                            s.style.display = "block";
                                                            }
                                                            }
                                                        </script>
						<table border="1" cellpadding="0" cellspacing="0">
							<tr valign="top">
								<td class="table_header_row">
									<xsl:text>Left Item Text</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Right Item Text</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Default Value</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Response Layout</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Response Type</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Response Label</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Response Options/Response Values</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Section Label</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Group Name</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Validation</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Validation Error Message</xsl:text>
								</td>
								<td class="table_header_row">
									<xsl:text>Required</xsl:text>
								</td>
							</tr>
							<tr valign="top">
								<td class="table_cell_left">
									<xsl:value-of select="./OpenClinica:LeftItemText"/>
								</td>
								<td class="table_cell">
									<xsl:value-of select="./OpenClinica:RightItemTextText"/>
								</td>
								<td class="table_cell">
									<xsl:value-of select="@DefaultValue"/>
								</td>
								<td class="table_cell">
									<xsl:value-of select="./OpenClinica:ItemResponse/@ResponseLayout"/>
								</td>
								<td class="table_cell">
									<xsl:value-of select="./OpenClinica:ItemResponse/@ResponseType"/>
								</td>
								<td class="table_cell">
									<xsl:value-of select="./OpenClinica:ItemResponse/@ResponseLabel"/>
								</td>
								<td class="table_cell">
									<xsl:for-each select="$codeList[@OID=$codeListOID]">
										<xsl:for-each select="./odm:CodeListItem">
											<xsl:value-of select="./odm:Decode/odm:TranslatedText"/>
                                                                            |
                                                                            <xsl:value-of select="@CodedValue"/>
											<br/>
										</xsl:for-each>
									</xsl:for-each>
								</td>
								<td class="table_cell">
									<xsl:value-of select="./OpenClinica:SectionLabel"/>
								</td>
								<xsl:for-each select="$groupDetails/odm:ItemRef[@ItemOID=$OID_Item]">
									<td class="table_cell">
										<xsl:value-of select="../@Name"/>
									</td>
								</xsl:for-each>
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>
								<td class="table_cell">
									<xsl:text disable-output-escaping="yes">&amp;nbsp;</xsl:text>
								</td>
								<xsl:for-each select="$groupDetails/odm:ItemRef[@ItemOID=$OID_Item]">
									<td class="table_cell">
										<xsl:value-of select="@Mandatory"/>
									</td>
								</xsl:for-each>
							</tr>
						</table>
					</div>
				</xsl:for-each>
				<style type="text/css">
                                                    .expandFormLink {
                                                    font-family: Tahoma, Arial, Helvetica, Sans-Serif;
                                                    font-size: 11px;
                                                    line-height: 15px;
                                                    color: #4D4D4D;font-weight: bold;
                                                    }
                                                    H1 { font-family: Tahoma, Arial, Helvetica, sans-serif;
                                                    font-size: 18px;
                                                    line-height: 24px;
                                                    font-weight: bold;
                                                    color: #789EC5;
                                                    }

                                                    td { font-family: Tahoma, Arial, Helvetica, Sans-Serif;
                                                    font-size: 11px;
                                                    line-height: 15px;
                                                    color: #4D4D4D;
                                                    }

                                                    a:link, a:visited { color: #789EC5;
                                                    text-decoration: none;
                                                    }


                                                    a:hover, a:active { color: #789EC5;
                                                    text-decoration: underline;
                                                    }


                                                    .tablebox_center { padding-left: 1px;
                                                    padding-right: 6px;
                                                    padding-top: 1px;
                                                    padding-bottom: 7px;
                                                    }

                                                    .table_title_Manage { font-family: Tahoma, Arial, Helvetica,
                                                    sans-serif;
                                                    font-size: 14px;
                                                    line-height: 18px;
                                                    font-weight: bold;
                                                    color: #D4A718;
                                                    padding-bottom: 6px;
                                                    }

                                                    .table_header_row { padding-top: 3px;
                                                    padding-left: 2px;
                                                    padding-right: 1px;
                                                    padding-bottom: 3px;
                                                    border-style: solid;
                                                    border-top-width: 0px;
                                                    border-left-width: 1px;
                                                    border-right-width: 0px;
                                                    border-bottom-width: 1px;
                                                    border-left-color: #CCCCCC;
                                                    border-bottom-color: #CCCCCC;
                                                    font-weight: bold;
                                                    color: #666666;
                                                    vertical-align: top;
                                                    }
                                                    .table_header_column { padding-top: 3px;
                                                    padding-left: 6px;
                                                    padding-right: 6px;
                                                    padding-bottom: 3px;
                                                    border-style: solid;
                                                    border-top-width: 1px;
                                                    border-left-width: 0px;
                                                    border-right-width: 1px;
                                                    border-bottom-width: 0px;
                                                    border-right-color: #CCCCCC;
                                                    border-top-color: #CCCCCC;
                                                    font-weight: bold;
                                                    color: #666666;
                                                    vertical-align: top;
                                                    }

                                                    .table_header_column_top { padding-top: 3px;
                                                    padding-left: 6px;
                                                    padding-right: 6px;
                                                    padding-bottom: 3px;
                                                    border-style: solid;
                                                    border-top-width: 0px;
                                                    border-left-width: 0px;
                                                    border-right-width: 1px;
                                                    border-bottom-width: 0px;
                                                    border-right-color: #CCCCCC;
                                                    font-weight: bold;
                                                    color: #666666;
                                                    vertical-align: top;
                                                    }

                                                    .table_cell {
                                                    padding: 0.2em;
                                                    border-style: solid;
                                                    border-top-width: 1px;
                                                    border-left-width: 1px;
                                                    border-right-width: 0;
                                                    border-bottom-width: 0;
                                                    border-left-color: #CCCCCC;
                                                    border-top-color: #E6E6E6;
                                                    vertical-align: top;
                                                    }


                                                    .table_cell_left {
                                                    padding: 0.2em;
                                                    border-style: solid;
                                                    border-top-width: 1px;
                                                    border-left-width: 0px;
                                                    border-right-width: 0px;
                                                    border-bottom-width: 0px;
                                                    border-top-color: #E6E6E6;
                                                    vertical-align: top;
                                                    }

                                                    .table_cell_top {
                                                    padding: 0.2em;
                                                    border-style: solid;
                                                    border-top-width: 0px;
                                                    border-left-width: 1px;
                                                    border-right-width: 0px;
                                                    border-bottom-width: 0px;
                                                    border-left-color: #CCCCCC;
                                                    vertical-align: top;
                                                    }
                                                </style>
			</div>
		</xsl:for-each>
	</xsl:template>
	<xsl:template name="FormatDate">
		<xsl:param name="DateTime"/>
		<xsl:variable name="month">
			<xsl:value-of select="substring($DateTime, 6, 2)"/>
		</xsl:variable>
		<xsl:variable name="days">
			<xsl:value-of select="substring($DateTime, 9, 2)"/>
		</xsl:variable>
		<xsl:variable name="year_of_date">
			<xsl:value-of select="substring($DateTime, 1, 4)"/>
		</xsl:variable>
		<xsl:value-of select="$year_of_date"/>
		<xsl:value-of select="'-'"/>
		<xsl:choose>
			<xsl:when test="$month = '01'">
				Jan
			</xsl:when>
			<xsl:when test="$month = '02'">
				Feb
			</xsl:when>
			<xsl:when test="$month = '03'">
				Mar
			</xsl:when>
			<xsl:when test="$month = '04'">
				Apr
			</xsl:when>
			<xsl:when test="$month = '05'">
				May
			</xsl:when>
			<xsl:when test="$month = '06'">
				Jun
			</xsl:when>
			<xsl:when test="$month = '07'">
				Jul
			</xsl:when>
			<xsl:when test="$month = '08'">
				Aug
			</xsl:when>
			<xsl:when test="$month = '09'">
				Sep
			</xsl:when>
			<xsl:when test="$month = '10'">
				Oct
			</xsl:when>
			<xsl:when test="$month = '11'">
				Nov
			</xsl:when>
			<xsl:when test="$month = '12'">
				Dec
			</xsl:when>
		</xsl:choose>
		<xsl:value-of select="'-'"/>
		<xsl:if test="(string-length($days) &lt; 2)">
			<xsl:value-of select="0"/>
		</xsl:if>
		<xsl:value-of select="$days"/>
	</xsl:template>
	<xsl:template match="//odm:ODM/odm:Study/odm:MetaDataVersion" mode="metadataDisplay">
		<xsl:apply-templates select="odm:StudyEventDef" mode="studyEventDefinition">
			<!--<xsl:for-each select="./odm:FormRef">-->
			<!--<xsl:apply-templates select="./odm:FormRef" mode="formRefToDefMapTemplate">
			<xsl:with-param name="crfRefPosition" select="position()"/>
		</xsl:apply-templates>-->
			<!--</xsl:for-each>-->
		</xsl:apply-templates>
	</xsl:template>
	<!--

	<xsl:template
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData"
		mode="studyEventData1">
		<xsl:variable name="eventPosition" select="position()" />
		<xsl:variable name="eventOID" select="@StudyEventOID" />
		<xsl:apply-templates
			select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID=$eventOID]"
			mode="studyeventDef1">
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="eventOID" select="$eventOID" />
		</xsl:apply-templates>
		<xsl:apply-templates
			select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID)[1])]"
			mode="formData1">
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="eventOID" select="$eventOID" />
		</xsl:apply-templates>
	</xsl:template>
	-->
	<xsl:template match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef" mode="studyEventDefinition">
		<!--<xsl:variable name="eventPosition" select="position()" />-->
		<xsl:variable name="OID" select="@OID"/>
		<xsl:variable name="studyName" select="@Name"/>
		<xsl:variable name="oid" select="$OID"/>
		<xsl:variable name="isRepeating" select="@Repeating"/>
		<xsl:variable name="eventDefPosition" select="position()"/>
		<xsl:apply-templates mode="studyEventDefList" select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID)[1])]">
			<xsl:with-param name="oid" select="$oid"/>
			<xsl:with-param name="studyName" select="$studyName"/>
			<xsl:with-param name="studyEventDefPosition" select="$eventDefPosition"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="./odm:FormRef" mode="formRefToDefMapTemplate">
			<xsl:with-param name="eventOID" select="$OID"/>
			<xsl:with-param name="eventDefPosition" select="$eventDefPosition"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template mode="studyEventDefList" match="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:param name="oid"/>
		<xsl:param name="studyName"/>
		<xsl:param name="studyEventDefPosition"/>
		
		<xsl:variable name="studyEventOID" select="@StudyEventOID"/>
		<xsl:if test="$oid=$studyEventOID">
			<tr>
				<td class="table_header_column">
					<xsl:text>Study Event Definition </xsl:text>
					<xsl:value-of select="$studyEventDefPosition"/>
					<xsl:variable name="isRepeating" select="@Repeating"/>
					<xsl:if test="$isRepeating='Yes'">
						<xsl:text>(Repeating)</xsl:text>
					</xsl:if>
				</td>
				<td class="table_cell">
					<xsl:value-of select="$studyName"/>
				</td>
				<td class="table_cell">
					<xsl:value-of select="$E"/>
					<!-- <xsl:value-of select="$eventPosition" />-->
					<xsl:value-of select="$studyEventDefPosition"/>
					<!--				<xsl:value-of select="$eol"></xsl:value-of>-->
				</td>
			</tr>
			<!--<xsl:apply-templates select="./odm:FormRef" mode="formRefToDefMapTemplate">
			<xsl:with-param name="crfRefPosition" select="position()"/>
		</xsl:apply-templates>-->
		</xsl:if>
		<!-- moved the template call to generate list of CRFs within an event here to cover the scenarion correctly where same CRF is included in more than one events -->
		<!--<xsl:variable name="studyEventDef" select="//odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef[@OID = $oid]"/>
		<xsl:apply-templates select="$studyEventDef/odm:FormRef" mode="formRefToDefMapTemplate">
			<xsl:with-param name="eventOID" select="$oid"/>
			<xsl:with-param name="eventDefPosition" select="$studyEventDefPosition"/>
		</xsl:apply-templates>-->
	</xsl:template>
	<xsl:template priority="1" mode="formRefToDefMapTemplate" match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef/odm:FormRef">
		<xsl:param name="eventOID"/>
		<xsl:param name="eventDefPosition"/>
		<xsl:variable name="FormOID" select="@FormOID"/>
		<!-- calculate form reference position -->
		<xsl:variable name="formRefNodeId" select="generate-id()"/>
		<xsl:variable name="currentFormRefPositon">
			<xsl:for-each select="$allFormRefElements">
				<xsl:if test="@FormOID = $FormOID">
					<xsl:if test="$formRefNodeId = generate-id()">
						<xsl:copy-of select="position()"/>
					</xsl:if>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:apply-templates mode="formDataTemplate" select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID = $FormOID]">
			<xsl:with-param name="crfRefPosition" select="$currentFormRefPositon"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template priority="1" mode="formDataTemplate" match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef">
		<xsl:param name="crfRefPosition"/>
		<xsl:param name="eventOID"/>
		
		<xsl:variable name="FormOID" select="@OID"/>
		<xsl:variable name="formName" select="@Name"/>
		<!--<xsl:variable name="crfPosition" select="count(preceding-sibling::*) + 1"/>
		<xsl:variable name="crfPositionCalc" select="number($crfPosition)-number($eventDefCount)-1"/>	-->
		<xsl:apply-templates select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[generate-id() = generate-id(key('eventCRFs',$FormOID)[1]) and ../@StudyEventOID = $eventOID]" mode="CrfInfo">
			<xsl:with-param name="oid" select="$FormOID"/>
			<xsl:with-param name="formName" select="$formName"/>
			<xsl:with-param name="crfPosition" select="$crfRefPosition"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template mode="CrfInfo" match="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData">
		<xsl:param name="oid"/>
		<xsl:param name="formName"/>
		<xsl:param name="crfPosition"/>
		<xsl:variable name="formOid" select="@FormOID"/>
		<xsl:if test="$oid=@FormOID">
			<tr>
				<td class="table_header_column">
					<xsl:text>CRF</xsl:text>
					<!--<xsl:value-of select="count(preceding-sibling::*) + 1" />-->
					<xsl:value-of select="$crfPosition"/>
					<!--<xsl:value-of select="count(//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef/odm:FormRef)" />-->
				</td>
				<td class="table_cell">
					<xsl:value-of select="$formName"/>
				</td>
				<td class="table_cell">
					<xsl:value-of select="$C"/>
					<!--<xsl:value-of select="count(preceding-sibling::*) + 1" />-->
					<xsl:value-of select="$crfPosition"/>
					<!--<xsl:value-of select="count(//odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef/odm:FormRef)" />-->
				</td>
			</tr>
		</xsl:if>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[@StudyEventOID]" mode="studyEventHeader">
		<xsl:variable name="eventOID" select="@StudyEventOID"/>
		<xsl:variable name="eventPosition">
			<xsl:for-each select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 	= generate-id(key('studyEvents',@StudyEventOID)[1])]">
				<xsl:copy-of select="position()"/>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="eventPosition2">
			<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $eventOID">
					<xsl:copy-of select="position()"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation"/>
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate"/>
		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status"/>
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent"/>
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate"/>
		<xsl:variable name="studyEventRepeatKey" select="@StudyEventRepeatKey"/>
		<!--<xsl:apply-templates
			select="odm:FormData"
			mode="formDataHeader">
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
		</xsl:apply-templates>-->
		<xsl:apply-templates select="odm:FormData" mode="formDataHeader2">
			<xsl:with-param name="eventPosition" select="$eventPosition2"/>
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>
			<xsl:with-param name="eventNodeId" select="generate-id()"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]" mode="studyFormAndDataItemsHeaders">
		<xsl:param name="generateIntHeadersList"/>
		<!--<xsl:variable name="formRefOID" select="@FormOID"/>-->
		<xsl:variable name="eventOID" select="@OID"/>
		<xsl:variable name="isEventRepeating" select="@Repeating"/>
		<xsl:variable name="isRepeatingEvent" select="@Repeating"/>
		<!-- calculate event position -->
		<xsl:variable name="eventPosition">
			<xsl:for-each select="/odm:ODM/odm:Study[1]/odm:MetaDataVersion/odm:StudyEventDef">
				<xsl:if test="@OID = $eventOID">
					<xsl:copy-of select="position()"/>
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
		<xsl:apply-templates select="." mode="studyFormColumnHeaders">
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="isRepeatingEvent" select="$isRepeatingEvent"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>
			<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
			<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
		</xsl:apply-templates>
		<!-- apply template for item data columns -->
		<xsl:apply-templates select="." mode="studyItemDataColumnHeaders">
			<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>
			<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
			<xsl:with-param name="MaxEventRepeatKey" select="$MaxEventRepeatKey"/>
		</xsl:apply-templates>
	</xsl:template>
	<xsl:template mode="formDataHeader" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[@FormOID]">
		<xsl:param name="eventPosition"/>
		<xsl:param name="studyEventRepeatKey"/>
		<xsl:variable name="crfPosition" select="position()"/>
		<xsl:variable name="parentEvent" select=".."/>
		<xsl:variable name="currentFormOID" select="@FormOID"/>
		<xsl:apply-templates mode="studyEventData" select="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData[generate-id() 	= generate-id(key('studyEvents',@StudyEventOID)[1])]">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
			<xsl:with-param name="parentEvent" select="$parentEvent"/>
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
		</xsl:apply-templates>
		<xsl:apply-templates select="odm:ItemGroupData/odm:ItemData" mode="itemDataColumnHeaders">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
			<xsl:with-param name="currentFormOID" select="$currentFormOID"/>
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
		</xsl:apply-templates>
		<!-- </xsl:for-each> -->
		<!-- event crf Header Data -->
	</xsl:template>
	<!--
	<xsl:template mode="formDataHeader2"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[@FormOID]">		
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

		
	</xsl:template>-->
	<!--
	<xsl:template mode="formDataHeader2"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData[@FormOID]">		
		<xsl:param name="eventPosition"/>
		<xsl:param name="studyEventRepeatKey"/>
		<xsl:param name="eventOID"/>	
		<xsl:param name="eventNodeId" />	
		
		<xsl:variable name="currentFormOID" select="@FormOID" />
		<xsl:variable name="crfPositionCalc">
			<xsl:for-each select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef">
				<xsl:if test="@OID = $currentFormOID">					
					 <xsl:copy-of select="position()" />
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>	
		
		<xsl:variable name="crfPosition" select="$crfPositionCalc" />
		<xsl:variable name="parentEvent" select=".." />
		
		
				<xsl:variable name="currentEventNodePositon">
					<xsl:for-each select="$allStudyEventDataElements">
					<xsl:choose>
						<xsl:when test="$studyEventRepeatKey">
							<xsl:if test="@StudyEventOID = $eventOID and @StudyEventRepeatKey = $studyEventRepeatKey">
							<xsl:if test="$eventNodeId = generate-id()">
								<xsl:copy-of select="position()" />
							</xsl:if>
						</xsl:if>
						</xsl:when>
						<xsl:otherwise>
							<xsl:if test="@StudyEventOID = $eventOID ">
								<xsl:if test="$eventNodeId = generate-id()">
									<xsl:copy-of select="position()" />
								</xsl:if>
						</xsl:if>
						</xsl:otherwise>
					</xsl:choose>
						
					</xsl:for-each>
				</xsl:variable>	
				
		<xsl:variable name="priorStudyElementDataElements" select="$allStudyEventDataElements[position() &lt; $currentEventNodePositon]"	/>				
		
		<xsl:choose>
			<xsl:when test="$studyEventRepeatKey">	
						
						<xsl:choose>						
							<xsl:when test="count($priorStudyElementDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $studyEventRepeatKey and (odm:FormData/@FormOID = $currentFormOID) ]) &gt; 0">							
							</xsl:when>
							<xsl:otherwise>
									<xsl:call-template name="studyEventData2">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="parentEvent" select="$parentEvent" />
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey" />			
		</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>	
			</xsl:when>
			<xsl:otherwise>
			
				<xsl:choose>						
							<xsl:when test="count($priorStudyElementDataElements[@StudyEventOID = $eventOID and (odm:FormData/@FormOID = $currentFormOID) ]) &gt; 0">do nothing
							</xsl:when>
							<xsl:otherwise>
									<xsl:call-template name="studyEventData2">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="parentEvent" select="$parentEvent" />
			<xsl:with-param name="eventPosition" select="$eventPosition" />
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey" />			
		</xsl:call-template>
							</xsl:otherwise>
						</xsl:choose>
			</xsl:otherwise>			
		</xsl:choose>	
		
		
	
		
		<xsl:apply-templates select="odm:ItemGroupData/odm:ItemData"
			mode="itemDataColumnHeaders2">
			<xsl:with-param name="crfPosition" select="$crfPosition" />
			<xsl:with-param name="currentFormOID" select="$currentFormOID" />
			<xsl:with-param name="eventPosition" select="$eventPosition"/>
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
			<xsl:with-param name="eventOID" select="$eventOID"/>
		</xsl:apply-templates>

		
	</xsl:template>-->
	<xsl:template mode="studyEventData" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData">
		<xsl:param name="crfPosition"/>
		<xsl:param name="parentEvent"/>
		<xsl:param name="eventPosition"/>
		<xsl:variable name="eventOID" select="@StudyEventOID"/>
		<!-- <xsl:variable name="eventPosition" select="position()" /> -->
		<xsl:variable name="crfVersionExist" select="odm:FormData/@OpenClinica:Version"/>
		<xsl:variable name="interviewerNameExist" select="odm:FormData/@OpenClinica:InterviewerName"/>
		<xsl:variable name="interviewDateExist" select="odm:FormData/@OpenClinica:InterviewDate"/>
		<xsl:variable name="crfStatusExist" select="odm:FormData/@OpenClinica:Status"/>
		<xsl:variable name="eventLocationExist" select="@OpenClinica:StudyEventLocation"/>
		<xsl:variable name="eventStartDateExist" select="@OpenClinica:StartDate"/>
		<xsl:variable name="eventStatusExist" select="@OpenClinica:Status"/>
		<xsl:variable name="ageExist" select="@OpenClinica:SubjectAgeAtEvent"/>
		<xsl:variable name="eventEndDateExist" select="@OpenClinica:EndDate"/>
		<xsl:if test="@StudyEventOID = $parentEvent/@StudyEventOID">
			<xsl:choose>
				<xsl:when test="@StudyEventRepeatKey">
					<xsl:variable name="allStudyEvents">
						<xsl:for-each select="//odm:StudyEventData">
							<xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
							<xsl:copy-of select="."/>
						</xsl:for-each>
					</xsl:variable>
					
				</xsl:when>
				<xsl:otherwise>
					<xsl:if test="$interviewerNameExist">
						<td class="table_header_row">
							<xsl:text>Interviewer_</xsl:text>
							<xsl:value-of select="$E"/>
							<xsl:value-of select="$eventPosition"/>
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C"/>
							<xsl:value-of select="$crfPosition"/>
						</td>
					</xsl:if>
					<xsl:if test="$interviewDateExist">
						<td class="table_header_row">
							<xsl:text>Interview Date_</xsl:text>
							<xsl:value-of select="$E"/>
							<xsl:value-of select="$eventPosition"/>
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C"/>
							<xsl:value-of select="$crfPosition"/>
						</td>
					</xsl:if>
					<xsl:if test="$crfStatusExist">
						<td class="table_header_row">
							<xsl:text>CRF Version Status_</xsl:text>
							<xsl:value-of select="$E"/>
							<xsl:value-of select="$eventPosition"/>
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C"/>
							<xsl:value-of select="$crfPosition"/>
						</td>
					</xsl:if>
					<xsl:if test="$crfVersionExist">
						<td class="table_header_row">
							<xsl:text>Version Name_</xsl:text>
							<xsl:value-of select="$E"/>
							<xsl:value-of select="$eventPosition"/>
							<xsl:text>_</xsl:text>
							<xsl:value-of select="$C"/>
							<xsl:value-of select="$crfPosition"/>
						</td>
					</xsl:if>
				</xsl:otherwise>
			</xsl:choose>
		</xsl:if>
	</xsl:template>
	<xsl:template mode="studyFormColumnHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
		<!--<xsl:template name="studyEventData2">-->
		<!--<xsl:param name="crfPosition"/>-->
		<xsl:param name="eventOID"/>
		<xsl:param name="eventPosition"/>
		<xsl:param name="isRepeatingEvent"/>
		<xsl:param name="generateIntHeadersList"/>	
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
									<xsl:copy-of select="position()"/>
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
					@OpenClinica:Status]) &gt; 0"/>
						
					<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and odm:FormData/@FormOID = 
						$formRefOID]) &gt; 0">
						<xsl:if test="$interviewerNameExist">
							<td class="table_header_row">
								<xsl:value-of select="' '"/>
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
								
								<xsl:value-of select="$eventPosition"/>
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C"/>
								<xsl:value-of select="$crfPosition"/>
							</td>
						</xsl:if>
						<xsl:if test="$interviewDateExist">
							<td class="table_header_row">
								<xsl:value-of select="' '"/>
								<xsl:text>Interview Date</xsl:text>
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
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C"/>
								<xsl:value-of select="$crfPosition"/>
							</td>
						</xsl:if>
						<xsl:if test="$crfStatusExist">
							<td class="table_header_row">
								<xsl:value-of select="' '"/>
								<xsl:text>CRF Version Status</xsl:text>
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
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C"/>
								<xsl:value-of select="$crfPosition"/>
							</td>
						</xsl:if>
						<xsl:if test="$crfVersionExist">
							<td class="table_header_row">
								<xsl:value-of select="' '"/>
								<xsl:text>Version Name</xsl:text>
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
								<xsl:text>_</xsl:text>
								<xsl:value-of select="$C"/>
								<xsl:value-of select="$crfPosition"/>
							</td>
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
		<xsl:param name="generateIntHeadersList"/>
		<xsl:param name="eventOID"/>
		<xsl:param name="eventPosition"/>
		<xsl:param name="eventRepeatCnt"/>
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
							<xsl:copy-of select="position()"/>
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
				<td class="table_header_row">
					<xsl:value-of select="' '"/>
					<!-- added for tokenization when displaying crf data -->
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
					<xsl:value-of select="$eventPosition"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$eventRepeatCnt"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$C"/>
					<xsl:value-of select="$crfPosition"/>
				</td>
			</xsl:if>
			<xsl:if test="$interviewDateExist">
				<td class="table_header_row">
					<xsl:value-of select="' '"/>
					<xsl:text>Interview Date</xsl:text>
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
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$eventRepeatCnt"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$C"/>
					<xsl:value-of select="$crfPosition"/>
				</td>
			</xsl:if>
			<xsl:if test="$crfStatusExist">
				<td class="table_header_row">
					<xsl:value-of select="' '"/>
					<xsl:text>CRF Version Status</xsl:text>
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
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$eventRepeatCnt"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$C"/>
					<xsl:value-of select="$crfPosition"/>
				</td>
			</xsl:if>
			<xsl:if test="$crfVersionExist">
				<td class="table_header_row">
					<xsl:value-of select="' '"/>
					<xsl:text>Version Name</xsl:text>
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
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$eventRepeatCnt"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$C"/>
					<xsl:value-of select="$crfPosition"/>
				</td>
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
	<xsl:template mode="studyItemDataColumnHeaders" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]">
		<xsl:param name="eventOID"/>
		<xsl:param name="generateIntHeadersList"/>
		<xsl:param name="MaxEventRepeatKey"/>
		
		<xsl:param name="isEventRepeating"/>
		<!--studyItemDataColumnHeaders-->
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
					<xsl:copy-of select="position()"/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>
		<!-- maximum value of StudyEventRepeatKey for an event -->
		<!--<xsl:variable name="MaxEventRepeatKey">
			<xsl:for-each select="//odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/@StudyEventRepeatKey">
				<xsl:sort data-type="number"/>
				<xsl:if test="position() = last()">
					<xsl:value-of select="."/>
				</xsl:if>
			</xsl:for-each>
		</xsl:variable>-->
		<!--<xsl:variable name="StudyEventRepeatKey" select="1"/>-->
		<!--temp hardcoded-->
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
					<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
					<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
					<!-- this is just need to pass on to further template which is common to repeating and non-repeating events -->
				</xsl:apply-templates>
			</xsl:when>
			<xsl:otherwise>
				<xsl:for-each select="odm:FormRef">
					<xsl:variable name="formRefOID" select="@FormOID"/>
					<!--formRefOID:<xsl:value-of select="$formRefOID"/>-->
					<xsl:variable name="formRefNodeId" select="generate-id()"/>
					<xsl:variable name="crfPosition">
						<xsl:for-each select="$allFormRefElements">
							<xsl:if test="@FormOID = $formRefOID">
								<xsl:if test="$formRefNodeId = generate-id()">
									<xsl:copy-of select="position()"/>
								</xsl:if>
							</xsl:if>
						</xsl:for-each>
					</xsl:variable>
					<xsl:apply-templates mode="formRefToDefTemplateForHeaders" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef[@OID = $formRefOID]">
						<xsl:with-param name="crfPosition" select="$crfPosition"/>
						<xsl:with-param name="eventPosition" select="$eventPosition"/>
						<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
						<xsl:with-param name="eventOID" select="$eventOID"/>
						<xsl:with-param name="StudyEventRepeatKey" select="$MaxEventRepeatKey"/>
						<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
						<!-- this param is of no use for non-repeating column further when creating the columns -->
					</xsl:apply-templates>
				</xsl:for-each>
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template name="createItemDataColForRepeatingEvent" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:StudyEventDef[@OID]" mode="createItemDataColForRepeatingEvent">
		<xsl:param name="eventOID"/>
		<xsl:param name="eventPosition"/>
		<xsl:param name="eventRepeatCnt"/>
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
								<xsl:copy-of select="position()"/>
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
		<xsl:param name="eventPosition"/>
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="eventOID"/>
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="generateIntHeadersList"/>
		
		<!--formRefToDefTemplateForHeaders-->
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
	<xsl:template mode="ItemGrpRefs" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:FormDef/odm:ItemGroupRef[@ItemGroupOID]">
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition"/>
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="formOID"/>
		<xsl:param name="eventOID"/>
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="generateIntHeadersList"/>
		
		<!--ItemGrpRefs:formOID:<xsl:value-of select="$formOID"/>-->
		<xsl:variable name="grpOID" select="@ItemGroupOID"/>
		<xsl:apply-templates mode="ItemGrpRefToDefTemplateForHeaders" select="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef[@OID = $grpOID]">
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
		<xsl:param name="eventPosition"/>
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="formOID"/>
		<xsl:param name="grpOID"/>
		<xsl:param name="eventOID"/>
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="generateIntHeadersList"/>
		
		<!--ItemGrpRefToDefTemplateForHeaders:formOID:<xsl:value-of select="$formOID"/>-->
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
					<xsl:with-param name="isLastItem" select="position()=last()"/>
					<xsl:with-param name="generateIntHeadersList" select="$generateIntHeadersList"/>
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
		<xsl:param name="eventPosition"/>
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="formOID"/>
		<xsl:param name="grpOID"/>
		<xsl:param name="eventOID"/>
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="itemGrpRepeatKey"/>
		<xsl:param name="isGrpRepeating"/>
		<xsl:param name="generateIntHeadersList"/>
		
		<!--createItemDataColForRepeatingGrps:formOID:<xsl:value-of select="$formOID"/>-->
		<!--cnt: <xsl:value-of select="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $StudyEventRepeatKey 
			and odm:FormData/@FormOID = $formOID and odm:FormData/odm:ItemGroupData/@ItemGroupOID = $grpOID 
			and odm:FormData/odm:ItemGroupData/@ItemGroupRepeatKey = $itemGrpRepeatKey])"/>
		cntModified : <xsl:value-of select="count($allStudyEventDataElements[@StudyEventOID = $eventOID 
			and odm:FormData/@FormOID = $formOID and odm:FormData/odm:ItemGroupData/@ItemGroupOID = $grpOID 
			and odm:FormData/odm:ItemGroupData/@ItemGroupRepeatKey = $itemGrpRepeatKey])"/>	-->
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
			
			StudyEventRepeatKey:<xsl:value-of select="$StudyEventRepeatKey"/>
			itemGrpRepeatKey:<xsl:value-of select="$itemGrpRepeatKey"/>-->
			
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
				<!--maxGrpRepeatKey:<xsl:value-of select="$maxGrpRepeatKey"/>	-->
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
						<xsl:with-param name="isLastItem" select="position()=last()"/>
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
				<!--maxGrpRepeatKey:<xsl:value-of select="$maxGrpRepeatKey"/>	
				cnt: <xsl:value-of select="count($allItemGrpDataDataElements[../../@StudyEventOID = $eventOID 
						and ../@FormOID = $formOID and @ItemGroupOID = $grpOID 
						and @ItemGroupRepeatKey = $itemGrpRepeatKey])"/>-->
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
						<xsl:with-param name="isLastItem" select="position()=last()"/>
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
	<xsl:template mode="GrpItemRefs" match="/odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemGroupDef/odm:ItemRef[@ItemOID]">
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition"/>
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="formOID"/>
		<xsl:param name="grpOID"/>
		<xsl:param name="isGrpRepeating"/>
		<xsl:param name="eventOID"/>
		<xsl:param name="itemGrpRepeatKey"/>
		<xsl:param name="isLastItem"/>
		<xsl:param name="generateIntHeadersList"/>
		<!--GrpItemRefs-->
		<xsl:variable name="itemOID" select="@ItemOID"/>
		<!--eventOID: *<xsl:value-of select="$eventOID"/>*
		StudyEventRepeatKey: *<xsl:value-of select="$StudyEventRepeatKey"/>*
		grpOID: *<xsl:value-of select="$grpOID"/>*
		itemGrpRepeatKey: *<xsl:value-of select="$itemGrpRepeatKey"/>*
		formOID: *<xsl:value-of select="$formOID"/>*
		itemOID: *<xsl:value-of select="$itemOID"/>*-->
		
		
		<xsl:choose>
			<xsl:when test="$isEventRepeating = 'Yes'">
				<!--<xsl:if test="count($allStudyEventDataElements[@StudyEventOID = $eventOID and @StudyEventRepeatKey = $StudyEventRepeatKey and odm:FormData/@FormOID = 
						$formOID and odm:FormData/odm:ItemGroupData/@ItemGroupOID = $grpOID and odm:FormData/odm:ItemGroupData/odm:ItemData/@ItemOID = $itemOID]) &gt; 0">
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
				</xsl:if>-->
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
			<xsl:otherwise><!--GrpItemRefs: non-repeating event
			eventOID: <xsl:value-of select="$eventOID"/>
			formOID: <xsl:value-of select="$formOID"/>
			grpOID: <xsl:value-of select="$grpOID"/>
			isGrpRepeating: *<xsl:value-of select="$isGrpRepeating"/>*
			itemGrpRepeatKey: <xsl:value-of select="$itemGrpRepeatKey"/>
			itemOID: <xsl:value-of select="$itemOID"/>
			cnt: <xsl:value-of select="count($allStudyEventDataElements[@StudyEventOID = $eventOID and odm:FormData/@FormOID = 
						$formOID and odm:FormData/odm:ItemGroupData/@ItemGroupOID = $grpOID and odm:FormData/odm:ItemGroupData/odm:ItemData/@ItemOID = $itemOID])"/>
						
			cnt2: <xsl:value-of select="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../@ItemGroupRepeatKey =$itemGrpRepeatKey and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID])"/>-->
			<xsl:choose>
				<xsl:when test="$isGrpRepeating = 'Yes'"><!--repeating grp-->
					<xsl:if test="count($allItemDataElements[@ItemOID = $itemOID and ../@ItemGroupOID = $grpOID and ../@ItemGroupRepeatKey =$itemGrpRepeatKey and ../../@FormOID = 
						$formOID and ../../../@StudyEventOID = $eventOID]) &gt; 0"><!--create col-->
					<xsl:apply-templates select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]" mode="ItemDefColHeaders2">
						<xsl:with-param name="crfPosition" select="$crfPosition"/>
						<xsl:with-param name="eventPosition" select="$eventPosition"/>
						<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
						<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
						<!--<xsl:with-param name="currentFormOID" select="$currentFormOID" />-->
						<!--<xsl:with-param name="itemData" select="$itemData" />-->
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
						$formOID and ../../../@StudyEventOID = $eventOID]) &gt; 0">
					<xsl:apply-templates select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]" mode="ItemDefColHeaders2">
						<xsl:with-param name="crfPosition" select="$crfPosition"/>
						<xsl:with-param name="eventPosition" select="$eventPosition"/>
						<xsl:with-param name="isEventRepeating" select="$isEventRepeating"/>
						<xsl:with-param name="isGrpRepeating" select="$isGrpRepeating"/>
						<!--<xsl:with-param name="currentFormOID" select="$currentFormOID" />-->
						<!--<xsl:with-param name="itemData" select="$itemData" />-->
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
			
						
				
			</xsl:otherwise>
		</xsl:choose>
	</xsl:template>
	<xsl:template mode="itemDataColumnHeaders" match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">
		<xsl:param name="crfPosition"/>
		<xsl:param name="currentFormOID"/>
		<xsl:param name="studyEventRepeatKey"/>
		<xsl:param name="eventPosition"/>
		<xsl:variable name="itemData" select="."/>
		<xsl:variable name="itemOID" select="@ItemOID"/>
		<xsl:apply-templates select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]" mode="ItemDefColHeaders">
			<xsl:with-param name="crfPosition" select="$crfPosition"/>
			<xsl:with-param name="currentFormOID" select="$currentFormOID"/>
			<xsl:with-param name="itemData" select="$itemData"/>
			<xsl:with-param name="itemOID" select="$itemOID"/>
			<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
			<xsl:with-param name="ePosition" select="$eventPosition"/>
		</xsl:apply-templates>
	</xsl:template>
	<!--
<xsl:template mode="itemDataColumnHeaders2"
		match="/odm:ODM/odm:ClinicalData/odm:SubjectData/odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData">

		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
		<xsl:param name="studyEventRepeatKey" />
		<xsl:param name="eventPosition" />
		<xsl:param name="eventOID" />
		
		<xsl:variable name="itemData" select="." />
		<xsl:variable name="itemOID" select="@ItemOID" />
		<xsl:variable name="itemGroupOID" select="../ItemGroupData/@ItemGroupOID" />
		
		
			<xsl:choose>
				<xsl:when test="$studyEventRepeatKey">
					 <xsl:variable name="priorSameItemDataOcrns" select="count($allStudyEventDataElements[(@StudyEventOID = $eventOID) and (@StudyEventRepeatKey = $studyEventRepeatKey) and (odm:FormData/@FormOID = $currentFormOID) and (odm:FormData/odm:ItemGroupData/@ItemGroupOID = $itemGroupOID) and (odm:FormData/odm:ItemGroupData/odm:ItemData/@ItemOID = $itemOID)])"/>
					 <xsl:choose>
			<xsl:when test="$priorSameItemDataOcrns &gt; 0">
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates
					select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]"
					mode="ItemDefColHeaders2">
					<xsl:with-param name="crfPosition" select="$crfPosition" />
					<xsl:with-param name="currentFormOID" select="$currentFormOID" />
					<xsl:with-param name="itemData" select="$itemData" />
					<xsl:with-param name="itemOID" select="$itemOID" />
					<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
					<xsl:with-param name="ePosition" select="$eventPosition"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>
				</xsl:when>
				<xsl:otherwise>
					 <xsl:variable name="priorSameItemDataOcrns" select="count($allStudyEventDataElements[(@StudyEventOID = $eventOID) and (odm:FormData/@FormOID = $currentFormOID) and (odm:FormData/odm:ItemGroupData/@ItemGroupOID = $itemGroupOID) and (odm:FormData/odm:ItemGroupData/odm:ItemData/@ItemOID = $itemOID)])"/>	
					 <xsl:choose>
			<xsl:when test="$priorSameItemDataOcrns &gt; 0">
			</xsl:when>
			<xsl:otherwise>
				<xsl:apply-templates
					select="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID=$itemOID]"
					mode="ItemDefColHeaders2">
					<xsl:with-param name="crfPosition" select="$crfPosition" />
					<xsl:with-param name="currentFormOID" select="$currentFormOID" />
					<xsl:with-param name="itemData" select="$itemData" />
					<xsl:with-param name="itemOID" select="$itemOID" />
					<xsl:with-param name="studyEventRepeatKey" select="$studyEventRepeatKey"/>
					<xsl:with-param name="ePosition" select="$eventPosition"/>
				</xsl:apply-templates>
			</xsl:otherwise>
		</xsl:choose>				
				</xsl:otherwise>
			</xsl:choose>
		
	</xsl:template>
-->
	<xsl:template mode="ItemDefColHeaders2" match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID]">
		<xsl:param name="crfPosition"/>
		<xsl:param name="eventPosition"/>
		<xsl:param name="isEventRepeating"/>
		<xsl:param name="isGrpRepeating"/>
		<xsl:param name="itemOID"/>
		<xsl:param name="eventOID"/>
		<xsl:param name="StudyEventRepeatKey"/>
		<xsl:param name="grpRepeatKey"/>
		<xsl:param name="itemGrpRepeatKey"/>
		<xsl:param name="isLastItem"/>
		<xsl:param name="generateIntHeadersList"/>
		
		<xsl:variable name="itemName" select="@Name"/>
		<td class="table_header_row">
			<a href="javascript: void(0)">
				<xsl:attribute name="onclick">
                openWin('<xsl:value-of select="$itemOID"/>'); return  false;
            </xsl:attribute>
				<xsl:value-of select="' '"/>
				<xsl:value-of select="@Name"/>
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
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$StudyEventRepeatKey"/>
				</xsl:if>
				<xsl:text>_</xsl:text>
				<xsl:value-of select="$C"/>
				<xsl:value-of select="$crfPosition"/>
				<xsl:if test="$isGrpRepeating ='Yes'">
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$itemGrpRepeatKey"/>
				</xsl:if>
				<xsl:if test="$isLastItem">
					<xsl:value-of select="' '"/>
				</xsl:if>
				<!--<xsl:text>_</xsl:text>
			<xsl:value-of select="$E"/>	<xsl:value-of select="$eventPosition"/>
			<xsl:if test="$isEventRepeating = 'Yes'">
				<xsl:text>_</xsl:text><xsl:value-of select="$StudyEventRepeatKey"/>
			</xsl:if>-->
			</a>
		</td>
	</xsl:template>
	<xsl:template mode="ItemDefColHeaders" match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID]">
		<xsl:param name="crfPosition"/>
		<xsl:param name="currentFormOID"/>
		<xsl:param name="itemData"/>
		<xsl:param name="itemOID"/>
		<xsl:param name="studyEventRepeatKey"/>
		<xsl:param name="ePosition"/>
		<xsl:variable name="formOID" select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID"/>
		<td class="table_header_row">
			<xsl:if test="$currentFormOID = $formOID">
				<!-- Changed from$currentFormOID = $formOID -->
				<a href="javascript: void(0)">
					<xsl:attribute name="onclick">
                                            openWin('<xsl:value-of select="$itemOID"/>'); return  false;
                                            </xsl:attribute>
					<xsl:value-of select="@Name"/>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$C"/>
					<xsl:value-of select="$crfPosition"/>
					<xsl:variable name="group" select="$itemData/parent::node()"/>
					<xsl:variable name="groupOID" select="$group/@ItemGroupOID"/>
					<!-- JN: Commenting out the logic for now, not sure if this is right as per Paul's suggestion -->
					<!--<xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
				<xsl:if test="@Name !='Ungrouped'">
					<xsl:value-of select="@Name" />
				</xsl:if>
			</xsl:for-each>
			-->
					<xsl:if test="$group/@ItemGroupRepeatKey">
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$group/@ItemGroupRepeatKey"/>
					</xsl:if>
					<xsl:text>_</xsl:text>
					<xsl:value-of select="$E"/>
					<xsl:value-of select="$ePosition"/>
					<xsl:if test="$studyEventRepeatKey">
						<xsl:text>_</xsl:text>
						<xsl:value-of select="$studyEventRepeatKey"/>
					</xsl:if>
				</a>
			</xsl:if>
		</td>
	</xsl:template>
	<!--
	<xsl:template mode="ItemDefColHeaders2"
		match="//odm:ODM/odm:Study/odm:MetaDataVersion/odm:ItemDef[@OID]">
		<xsl:param name="crfPosition" />
		<xsl:param name="currentFormOID" />
		<xsl:param name="itemData" />
		<xsl:param name="itemOID" />
		<xsl:param name="studyEventRepeatKey"/>
		<xsl:param name="ePosition"/>
		<xsl:variable name="formOID"
			select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID" />
	<td class="table_header_row">

		<xsl:if test="$currentFormOID = $formOID">
		 <a href="javascript: void(0)">
					<xsl:attribute name="onclick">
                                            openWin('<xsl:value-of	select="$itemOID" />'); return  false;
                                            </xsl:attribute>
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
		<xsl:text>_</xsl:text>	<xsl:value-of select="$E"/>	<xsl:value-of select="$ePosition"/>
			<xsl:if test="$studyEventRepeatKey">
			<xsl:text>_</xsl:text>
			<xsl:value-of select="$studyEventRepeatKey"/>
			</xsl:if>
			</a>
		</xsl:if>
		</td>
	</xsl:template>
	-->
	<!--



	

	-->
	<!--<xsl:template name="repeatKey"> -->
	<!--<xsl:param name="testOID"/> -->
	<!--<xsl:param name="studyEventData"/> -->
	<!--<xsl:value-of select="$studyEventData[@StudyEventOID=$testOID and generate-id() 
		= -->
	<!--generate-id(key('studyEvents', @StudyEventRepeatKey)[1])]"/> -->
	<!--<xsl:value-of select="@StudyEventRepeatKey"/> -->
	<!--</xsl:template> -->
</xsl:stylesheet>
