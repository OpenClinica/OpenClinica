<?xml version="1.0" encoding="ISO-8859-1"?>
<!-- Copyright (C) 2010, Akaza Research, LLC.  -->
<xsl:stylesheet version="1.0" xmlns:odm="http://www.cdisc.org/ns/odm/v1.3"
                xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsi="http://www.w3c.org/2001/XMLSchema-instance"
                xmlns:def="http://www.cdisc.org/ns/def/v1.0" xmlns:xlink="http://www.w3c.org/1999/xlink"
                xmlns:exsl="http://exslt.org/common" extension-element-prefixes="exsl"
                xmlns:OpenClinica="http://www.openclinica.org/ns/odm_ext_v130/v3.1"
                xmlns:fn="http://www.w3.org/2005/02/xpath-functions"
                xsi:schemaLocation="http://www.cdisc.org/ns/odm/v1.3 ">
    <xsl:output method="text" version="4.0" encoding="utf-8" indent="no"/>
    <xsl:variable name="delimiter" select="'&#x09;'"/>
    <!--E to represent Events-->
    <xsl:variable name="E" select="'E'"/>
    <!--C to represent CRFS-->
    <xsl:variable name="C" select="'C'"/>
    <xsl:variable name="A" select="A"/>
    <xsl:variable name="ADATE" select="ADATE"/>
    <xsl:key name="studyEvents" match="odm:StudyEventData" use="@StudyEventOID"></xsl:key>
    <xsl:key name="eventCRFs" match="odm:FormData" use="@FormOID"></xsl:key>

    <xsl:variable name="sexExist" select="//odm:SubjectData/@OpenClinica:Sex"/>
    <xsl:variable name="uniqueIdExist" select="//odm:SubjectData/@OpenClinica:UniqueIdentifier"/>
    <xsl:variable name="dobExist" select="//odm:SubjectData/@OpenClinica:DateOfBirth"/>
    <xsl:variable name="subjectStatusExist" select="//odm:SubjectData/@OpenClinica:Status"/>

    <xsl:variable name="eventLocationExist" select="//odm:StudyEventData/@OpenClinica:StudyEventLocation"/>
    <xsl:variable name="eventStartDateExist" select="//odm:StudyEventData/@OpenClinica:StartDate"/>
    <xsl:variable name="eventEndDateExist" select="//odm:StudyEventData/@OpenClinica:EndDate"/>
    <xsl:variable name="eventStatusExist" select="//odm:StudyEventData/@OpenClinica:Status"/>
    <xsl:variable name="ageExist" select="//odm:StudyEventData/@OpenClinica:SubjectAgeAtEvent"/>

    <xsl:variable name="crfVersionExist" select="//odm:FormData/@OpenClinica:Version"/>
    <xsl:variable name="interviewerNameExist" select="//odm:FormData/@OpenClinica:InterviewerName"/>
    <xsl:variable name="interviewDateExist" select="//odm:FormData/@OpenClinica:InterviewDate"/>
    <xsl:variable name="crfStatusExist" select="//odm:FormData/@OpenClinica:Status"/>

    <xsl:template match="/">
        <xsl:text>GET DATA  /TYPE = TXT/FILE = 'All_Items_SPSS_data_spss.dat' /DELCASE = LINE /DELIMITERS = "\t" /ARRANGEMENT = DELIMITED /FIRSTCASE = 2 /IMPORTCASE = ALL /VARIABLES =</xsl:text>
        <xsl:text>&#xa;</xsl:text>
        <!--****************************************************************************************************** -->
        <!--                        Starting Columns and its data types                                            -->
        <!--****************************************************************************************************** -->
        <xsl:for-each select="/odm:ODM/odm:ClinicalData/odm:SubjectData[1]">
            <xsl:variable name="subIdLen" select="string-length(@OpenClinica:StudySubjectId)"/>
            <xsl:variable name="uniqIdLen" select="string-length(@OpenClinica:UniqueIdentifier)"/>
            <xsl:variable name="subStatusLen" select="string-length(@OpenClinica:Status)"/>
            <xsl:variable name="sexLen" select="string-length(@OpenClinica:Sex)"/>
            <xsl:variable name="dobLen" select="string-length(@OpenClinica:DateOfBirth)"/>
            <xsl:text>Subject ID A</xsl:text>
            <xsl:choose>
                <xsl:when test="$subIdLen &gt; 8">
                    <xsl:text>8</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$subIdLen"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>&#xa;</xsl:text>
            <xsl:text>ProtocolID A</xsl:text>
            <xsl:variable name="protocolLen" select="string-length(//odm:ProtocolName[1])"/>
            <xsl:choose>
                <xsl:when test="$protocolLen &gt; 8">
                    <xsl:text>8</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$protocolLen"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>&#xa;</xsl:text>

            <xsl:if test="$uniqueIdExist">
            <xsl:text>Unique ID A</xsl:text>
            <xsl:choose>
                <xsl:when test="$uniqIdLen &gt; 8">
                    <xsl:text>8</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$uniqIdLen"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>&#xa;</xsl:text>
            </xsl:if>


            <xsl:if test="$subjectStatusExist">
            <xsl:text>Subject Status A</xsl:text>
            <xsl:choose>
                <xsl:when test="$subStatusLen &gt; 8">
                    <xsl:text>8</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$subStatusLen"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>&#xa;</xsl:text>
            </xsl:if>

            <xsl:if test="$sexExist">
            <xsl:text>Sex A</xsl:text>
            <xsl:choose>
                <xsl:when test="$sexLen &gt; 8">
                    <xsl:text>8</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$sexLen"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>&#xa;</xsl:text>
            </xsl:if>

            <xsl:if test="$dobExist">
            <xsl:text>Date of Birth A</xsl:text>
            <xsl:choose>
                <xsl:when test="$dobLen &gt; 8">
                    <xsl:text>8</xsl:text>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:value-of select="$dobLen"/>
                </xsl:otherwise>
            </xsl:choose>
            <xsl:text>&#xa;</xsl:text>
            </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="//odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID))]">
            <xsl:variable name="eventOID" select="@StudyEventOID"/>
            <xsl:variable name="eventPosition" select="position()"/>
            <xsl:variable name="locationLen" select="string-length(@OpenClinica:StudyEventLocation)"/>
            <xsl:variable name="eventStatusLen" select="string-length(@OpenClinica:Status)"/>
            <xsl:variable name="ageLen" select="string-length(@OpenClinica:SubjectAgeAtEvent)"/>
            <xsl:choose>
                <xsl:when test="@StudyEventRepeatKey">
                    <xsl:variable name="allStudyEvents">
                        <xsl:for-each select="//odm:StudyEventData[@StudyEventOID=$eventOID]">
                            <xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
                            <xsl:copy-of select="."/>
                        </xsl:for-each>
                    </xsl:variable>

                    <xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
                        <xsl:choose>
                            <xsl:when test="position()=1">
                                <xsl:if test="$eventLocationExist">
                                <xsl:text>Location_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> A</xsl:text>
                                <xsl:choose>
                                    <xsl:when test="$locationLen &gt; 8">
                                        <xsl:text>8</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$locationLen"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>

                                <xsl:if test="$eventStartDateExist">
                                <xsl:text>StartDate_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> ADATE10</xsl:text>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>

                                <xsl:if test="$eventEndDateExist">
                                <xsl:text>EndDate_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> ADATE10</xsl:text>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>

                                <xsl:if test="$eventStatusExist">
                                <xsl:text>Event Status_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> A</xsl:text>
                                <xsl:choose>
                                    <xsl:when test="$eventStatusLen &gt; 8">
                                        <xsl:text>8</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$eventStatusLen"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>

                                <xsl:if test="$ageExist">
                                <xsl:text>Age_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> A</xsl:text>
                                <xsl:choose>
                                    <xsl:when test="$ageLen &gt; 8">
                                        <xsl:text>8</xsl:text>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$ageLen"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:if test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">                                    <xsl:text>Location_</xsl:text>
                                    <xsl:if test="$eventLocationExist">
                                    <xsl:text>Location_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> A</xsl:text>
                                    <xsl:choose>
                                        <xsl:when test="$locationLen &gt; 8">
                                            <xsl:text>8</xsl:text>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$locationLen"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:text>&#xa;</xsl:text>
                                    </xsl:if>

                                    <xsl:if test="$eventStartDateExist">
                                    <xsl:text>StartDate_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> ADATE10</xsl:text>
                                    <xsl:text>&#xa;</xsl:text>
                                    </xsl:if>

                                    <xsl:if test="$eventEndDateExist">
                                    <xsl:text>EndDate_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> ADATE10</xsl:text>
                                    <xsl:text>&#xa;</xsl:text>
                                    </xsl:if>

                                    <xsl:if test="$eventStatusExist">
                                    <xsl:text>Event Status_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> A</xsl:text>
                                    <xsl:choose>
                                        <xsl:when test="$eventStatusLen &gt; 8">
                                            <xsl:text>8</xsl:text>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$eventStatusLen"/>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                    <xsl:text>&#xa;</xsl:text>
                                    </xsl:if>

                                    <xsl:if test="$ageExist">
                                    <xsl:text>Age_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> A</xsl:text>
                                    <xsl:choose>
                                        <xsl:when test="$ageLen &gt; 8">
                                            <xsl:text>8</xsl:text>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$ageLen"/>
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
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> A</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$locationLen &gt; 8">
                            <xsl:text>8</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$locationLen"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                    <xsl:if test="$eventStartDateExist">
                    <xsl:text>StartDate_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> ADATE10</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    <xsl:text>StartDate_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> ADATE10</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                    <xsl:if test="$eventEndDateExist">
                    <xsl:text>EndDate_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> ADATE10</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    <xsl:text>EndDate_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> ADATE10</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                    <xsl:if test="$eventStatusExist">
                    <xsl:text>Event Status_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> A</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$eventStatusLen &gt; 8">
                            <xsl:text>8</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$eventStatusLen"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                    <xsl:if test="$ageExist">
                    <xsl:text>Age_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> A</xsl:text>
                    <xsl:choose>
                        <xsl:when test="$ageLen &gt; 8">
                            <xsl:text>8</xsl:text>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:value-of select="$ageLen"/>
                        </xsl:otherwise>
                    </xsl:choose>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>

        <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]">
            <xsl:variable name="crfPosition" select="position()"/>
            <xsl:variable name="parentEvent" select="//odm:StudyEventData[odm:FormData[@FormOID=@FormOID]]"/>
            <xsl:variable name="interLen" select="string-length(@OpenClinica:InterviewerName)"/>
            <xsl:variable name="interStatusLen" select="string-length(@OpenClinica:Status)"/>
            <xsl:variable name="versionLen" select="string-length(@OpenClinica:Version)"/>

            <xsl:for-each select="//odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID))]">
                <xsl:variable name="eventPosition" select="position()"/>
                <xsl:if test="@StudyEventOID = $parentEvent/@StudyEventOID">
                    <xsl:choose>
                        <xsl:when test="@StudyEventRepeatKey">
                            <xsl:variable name="allStudyEvents">
                                <xsl:for-each select="//odm:StudyEventData">
                                    <xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
                                    <xsl:copy-of select="."/>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
                                <xsl:choose>
                                    <xsl:when test="position()=1">
                                        <xsl:if test="$interviewerNameExist">
                                        <xsl:text>Interviewer_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> A</xsl:text>
                                        <xsl:choose>
                                            <xsl:when test="$interLen &gt; 8">
                                                <xsl:text>8</xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$interLen"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$interviewDateExist">
                                        <xsl:text>Interviewer date</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> ADATE10</xsl:text>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$crfStatusExist">
                                        <xsl:text>CRF Version Status_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> A</xsl:text>
                                        <xsl:choose>
                                            <xsl:when test="$interStatusLen &gt; 8">
                                                <xsl:text>8</xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$interStatusLen"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$crfVersionExist">
                                        <xsl:text>Version Name_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> A</xsl:text>
                                        <xsl:choose>
                                            <xsl:when test="$versionLen &gt; 8">
                                                <xsl:text>8</xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$versionLen"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:if test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">                                            <xsl:text>Interviewer_</xsl:text>
                                        <xsl:if test="$interviewerNameExist">
                                        <xsl:text>Interviewer_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> A</xsl:text>
                                        <xsl:choose>
                                            <xsl:when test="$interLen &gt; 8">
                                                <xsl:text>8</xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$interLen"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$interviewDateExist">
                                        <xsl:text>Interviewer date</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> ADATE10</xsl:text>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$crfStatusExist">
                                        <xsl:text>CRF Version Status_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> A</xsl:text>
                                        <xsl:choose>
                                            <xsl:when test="$interStatusLen &gt; 8">
                                                <xsl:text>8</xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$interStatusLen"/>
                                            </xsl:otherwise>
                                        </xsl:choose>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$crfVersionExist">
                                        <xsl:text>Version Name_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> A</xsl:text>
                                        <xsl:choose>
                                            <xsl:when test="$versionLen &gt; 8">
                                                <xsl:text>8</xsl:text>
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$versionLen"/>
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
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:text> A</xsl:text>
                            <xsl:choose>
                                <xsl:when test="$interLen &gt; 8">
                                    <xsl:text>8</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$interLen"/>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:text>&#xa;</xsl:text>
                            </xsl:if>
                            <xsl:if test="$interviewDateExist">
                            <xsl:text>Interviewer date_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:text> ADATE10</xsl:text>
                            <xsl:text>&#xa;</xsl:text>
                            </xsl:if>
                            <xsl:if test="$crfStatusExist">
                            <xsl:text>CRF Version Status_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:text> A</xsl:text>
                            <xsl:choose>
                                <xsl:when test="$interStatusLen &gt; 8">
                                    <xsl:text>8</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$interStatusLen"/>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:text>&#xa;</xsl:text>
                            </xsl:if>

                            <xsl:if test="$crfVersionExist">
                            <xsl:text>Version Name_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:text> A</xsl:text>
                            <xsl:choose>
                                <xsl:when test="$versionLen &gt; 8">
                                    <xsl:text>8</xsl:text>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$versionLen"/>
                                </xsl:otherwise>
                            </xsl:choose>
                            <xsl:text>&#xa;</xsl:text>
                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>

        <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]">
                <xsl:variable name="crfPosition" select="position()"/>
                <xsl:variable name="currentFormOID" select="@FormOID"/>
                <xsl:for-each select="//odm:ItemData">
                    <xsl:variable name="itemData" select="."/>
                    <xsl:variable name="itemOID" select="@ItemOID"/>
                    <xsl:variable name="valueLength" select="string-length(@Value)"/>
                    <xsl:for-each select="//odm:ItemDef[@OID=$itemOID]">
                        <xsl:variable name="formOID" select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID"/>
                        <xsl:if test="$currentFormOID = $formOID">
                        <xsl:value-of select="@Name"/>
                        <xsl:text>_</xsl:text>
                        <xsl:value-of select="$C"/>
                        <xsl:value-of select="$crfPosition"/>
                        <xsl:text>_</xsl:text>
                        <xsl:variable name="group" select="$itemData/parent::node()"/>
                        <xsl:variable name="groupOID" select="$group/@ItemGroupOID"/>
                        <xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
                            <xsl:if test="@Name !='Ungrouped'">
                                <xsl:value-of select="@Name"/>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:if test="$group/@ItemGroupRepeatKey">
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$group/@ItemGroupRepeatKey"/>
                        </xsl:if>
                        <xsl:variable name="dataType" select="@DataType"/>
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
                                        <xsl:value-of select="$valueLength"/>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                        <xsl:text>&#xa;</xsl:text>
                        
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:text>.</xsl:text>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text>VARIABLE LABELS</xsl:text>
        <xsl:text>&#xa;</xsl:text>

        <!--****************************************************************************************************** -->
        <!--                        Starting Columns and its labels                                                -->
        <!--****************************************************************************************************** -->
        <xsl:for-each select="/odm:ODM/odm:ClinicalData/odm:SubjectData[1]">
            <xsl:text>Subject ID</xsl:text>
            <xsl:text> "Study Subject ID"</xsl:text>
            <xsl:text>&#xa;</xsl:text>
            <xsl:text>ProtocolID</xsl:text>
            <xsl:text> "Protocol ID_Site ID"</xsl:text>
            <xsl:text>&#xa;</xsl:text>
            <xsl:if test="$uniqueIdExist">
            <xsl:text>Unique ID</xsl:text>
            <xsl:text> "Unique ID"</xsl:text>
            <xsl:text>&#xa;</xsl:text>
            </xsl:if>

            <xsl:if test="$subjectStatusExist">
            <xsl:text>Subject Status</xsl:text>
            <xsl:text> "Subject Status"</xsl:text>
            <xsl:text>&#xa;</xsl:text>
            </xsl:if>

            <xsl:if test="$sexExist">
            <xsl:text>Sex</xsl:text>
            <xsl:text> "Sex"</xsl:text>
            <xsl:text>&#xa;</xsl:text>
            </xsl:if>

            <xsl:if test="$dobExist">
            <xsl:text>Date of Birth</xsl:text>
            <xsl:text> "Date of Birth"</xsl:text>
            <xsl:text>&#xa;</xsl:text>
            </xsl:if>
        </xsl:for-each>

        <xsl:for-each select="//odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID))]">
            <xsl:variable name="eventOID" select="@StudyEventOID"/>
            <xsl:variable name="eventPosition" select="position()"/>
            <xsl:variable name="eventName" select="//odm:StudyEventDef[@OID=$eventOID]/@Name"/>
            <xsl:choose>
                <xsl:when test="@StudyEventRepeatKey">
                    <xsl:variable name="allStudyEvents">
                        <xsl:for-each select="//odm:StudyEventData[@StudyEventOID=$eventOID]">
                            <xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
                            <xsl:copy-of select="."/>
                        </xsl:for-each>
                    </xsl:variable>
                    <xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
                        <xsl:choose>
                            <xsl:when test="position()=1">

                                <xsl:if test="$eventLocationExist">
                                <xsl:text>Location_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> "Location For </xsl:text>
                                <xsl:value-of select="$eventName"/>
                                <xsl:text>(</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text>)"</xsl:text>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>

                                <xsl:if test="$eventStartDateExist">
                                <xsl:text>StartDate_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> "Start Date For </xsl:text>
                                <xsl:value-of select="$eventName"/>
                                <xsl:text>(</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text>)"</xsl:text>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>

                                <xsl:if test="$eventEndDateExist">
                                <xsl:text>EndDate_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> "End Date For </xsl:text>
                                <xsl:value-of select="$eventName"/>
                                <xsl:text>(</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text>)"</xsl:text>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>

                                <xsl:if test="$eventStatusExist">
                                <xsl:text>Event Status_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> "Event Status For </xsl:text>
                                <xsl:value-of select="$eventName"/>
                                <xsl:text>(</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text>)"</xsl:text>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>

                                <xsl:if test="$ageExist">
                                <xsl:text>Age_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text> "Age For </xsl:text>
                                <xsl:value-of select="$eventName"/>
                                <xsl:text>(</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:text>)"</xsl:text>
                                <xsl:text>&#xa;</xsl:text>
                                </xsl:if>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:if test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">                                    <xsl:text>Location_</xsl:text>
                                    <xsl:if test="$eventLocationExist">
                                    <xsl:text>Location_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> "Location For </xsl:text>
                                    <xsl:value-of select="$eventName"/>
                                    <xsl:text>(</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text>)"</xsl:text>
                                    <xsl:text>&#xa;</xsl:text>
                                    </xsl:if>

                                    <xsl:if test="$eventStartDateExist">
                                    <xsl:text>StartDate_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> "Start Date For </xsl:text>
                                    <xsl:value-of select="$eventName"/>
                                    <xsl:text>(</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text>)"</xsl:text>
                                    <xsl:text>&#xa;</xsl:text>
                                    </xsl:if>

                                    <xsl:if test="$eventEndDateExist">
                                    <xsl:text>EndDate_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> "End Date For </xsl:text>
                                    <xsl:value-of select="$eventName"/>
                                    <xsl:text>(</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text>)"</xsl:text>
                                    <xsl:text>&#xa;</xsl:text>
                                    </xsl:if>

                                    <xsl:if test="$eventStatusExist">
                                    <xsl:text>Event Status_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> "Event Status For </xsl:text>
                                    <xsl:value-of select="$eventName"/>
                                    <xsl:text>(</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text>)"</xsl:text>
                                    <xsl:text>&#xa;</xsl:text>
                                    </xsl:if>

                                    <xsl:if test="$ageExist">
                                    <xsl:text>Age_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:text> "Age For </xsl:text>
                                    <xsl:value-of select="$eventName"/>
                                    <xsl:text>(</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
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
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> "Location For </xsl:text>
                    <xsl:value-of select="$eventName"/>
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text>)"</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                    <xsl:if test="$eventStartDateExist">
                    <xsl:text>StartDate_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> "Start Date For </xsl:text>
                    <xsl:value-of select="$eventName"/>
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text>)"</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                    <xsl:if test="$eventEndDateExist">
                    <xsl:text>EndDate_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> "End Date For </xsl:text>
                    <xsl:value-of select="$eventName"/>
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text>)"</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                    <xsl:if test="$eventStatusExist">
                    <xsl:text>Event Status_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> "Event Status For </xsl:text>
                    <xsl:value-of select="$eventName"/>
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text>)"</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>

                    <xsl:if test="$ageExist">
                    <xsl:text>Age_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text> "Age For </xsl:text>
                    <xsl:value-of select="$eventName"/>
                    <xsl:text>(</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:text>)"</xsl:text>
                    <xsl:text>&#xa;</xsl:text>
                    </xsl:if>
                </xsl:otherwise>
            </xsl:choose>
        </xsl:for-each>

        <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]">
            <xsl:variable name="crfPosition" select="position()"/>
            <xsl:variable name="parentEvent" select=".."/>
            <xsl:for-each select="//odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID))]">
                <xsl:variable name="eventOID" select="@StudyEventOID"/>
                <xsl:variable name="eventPosition" select="position()"/>
                <xsl:variable name="eventName" select="//odm:StudyEventDef[@OID=$eventOID]/@Name"/>

                <xsl:if test="@StudyEventOID = $parentEvent/@StudyEventOID">
                    <xsl:choose>
                        <xsl:when test="@StudyEventRepeatKey">
                            <xsl:variable name="allStudyEvents">
                                <xsl:for-each select="//odm:StudyEventData">
                                    <xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
                                    <xsl:copy-of select="."/>
                                </xsl:for-each>
                            </xsl:variable>
                            <xsl:for-each select="exsl:node-set($allStudyEvents)/odm:StudyEventData">
                                <xsl:choose>
                                    <xsl:when test="position()=1">
                                        <xsl:if test="$interviewerNameExist">
                                        <xsl:text>Interviewer_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> "Interviewer Name For </xsl:text>
                                        <xsl:value-of select="$eventName"/>
                                        <xsl:text>(</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:text>_repeat</xsl:text>
                                        <xsl:value-of select="@StudyEventRepeatKey"/>
                                        <xsl:text>)"</xsl:text>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$interviewDateExist">
                                        <xsl:text>Interviewer date</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> "Interviewer Date For </xsl:text>
                                        <xsl:value-of select="$eventName"/>
                                        <xsl:text>(</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:text>_repeat</xsl:text>
                                        <xsl:value-of select="@StudyEventRepeatKey"/>
                                        <xsl:text>)"</xsl:text>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$crfStatusExist">
                                        <xsl:text>CRF Version Status_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> "CRF Version Status For </xsl:text>
                                        <xsl:value-of select="$eventName"/>
                                        <xsl:text>(</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:text>_repeat</xsl:text>
                                        <xsl:value-of select="@StudyEventRepeatKey"/>
                                        <xsl:text>)"</xsl:text>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>

                                        <xsl:if test="$crfVersionExist">
                                        <xsl:text>Version Name_</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:if test="@StudyEventRepeatKey">
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                        </xsl:if>
                                        <xsl:text>_</xsl:text>
                                        <xsl:value-of select="$C"/>
                                        <xsl:value-of select="$crfPosition"/>
                                        <xsl:text> "Version Name For </xsl:text>
                                        <xsl:value-of select="$eventName"/>
                                        <xsl:text>(</xsl:text>
                                        <xsl:value-of select="$E"/>
                                        <xsl:value-of select="$eventPosition"/>
                                        <xsl:text>_repeat</xsl:text>
                                        <xsl:value-of select="@StudyEventRepeatKey"/>
                                        <xsl:text>)"</xsl:text>
                                        <xsl:text>&#xa;</xsl:text>
                                        </xsl:if>
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:if test="preceding-sibling::odm:StudyEventData[1]/@StudyEventRepeatKey != @StudyEventRepeatKey">                                            <xsl:text>Interviewer_</xsl:text>
                                            <xsl:if test="$interviewerNameExist">
                                            <xsl:text>Interviewer_</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:if test="@StudyEventRepeatKey">
                                                <xsl:text>_repeat</xsl:text>
                                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                            </xsl:if>
                                            <xsl:text>_</xsl:text>
                                            <xsl:value-of select="$C"/>
                                            <xsl:value-of select="$crfPosition"/>
                                            <xsl:text> "Interviewer Name For </xsl:text>
                                            <xsl:value-of select="$eventName"/>
                                            <xsl:text>(</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                            <xsl:text>)"</xsl:text>
                                            <xsl:text>&#xa;</xsl:text>
                                            </xsl:if>

                                            <xsl:if test="$interviewDateExist">
                                            <xsl:text>Interviewer date</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:if test="@StudyEventRepeatKey">
                                                <xsl:text>_repeat</xsl:text>
                                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                            </xsl:if>
                                            <xsl:text>_</xsl:text>
                                            <xsl:value-of select="$C"/>
                                            <xsl:value-of select="$crfPosition"/>
                                            <xsl:text> "Interviewer Date For </xsl:text>
                                            <xsl:value-of select="$eventName"/>
                                            <xsl:text>(</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                            <xsl:text>)"</xsl:text>
                                            <xsl:text>&#xa;</xsl:text>
                                            </xsl:if>

                                            <xsl:if test="$crfStatusExist">
                                            <xsl:text>CRF Version Status_</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:if test="@StudyEventRepeatKey">
                                                <xsl:text>_repeat</xsl:text>
                                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                            </xsl:if>
                                            <xsl:text>_</xsl:text>
                                            <xsl:value-of select="$C"/>
                                            <xsl:value-of select="$crfPosition"/>
                                            <xsl:text> "CRF Version Status For </xsl:text>
                                            <xsl:value-of select="$eventName"/>
                                            <xsl:text>(</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
                                            <xsl:text>)"</xsl:text>
                                            <xsl:text>&#xa;</xsl:text>
                                            </xsl:if>

                                            <xsl:if test="$crfVersionExist">
                                            <xsl:text>Version Name_</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:if test="@StudyEventRepeatKey">
                                                <xsl:text>_repeat</xsl:text>
                                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                            </xsl:if>
                                            <xsl:text>_</xsl:text>
                                            <xsl:value-of select="$C"/>
                                            <xsl:value-of select="$crfPosition"/>
                                            <xsl:text> "Version Name For </xsl:text>
                                            <xsl:value-of select="$eventName"/>
                                            <xsl:text>(</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:text>_repeat</xsl:text>
                                            <xsl:value-of select="@StudyEventRepeatKey"/>
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
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:text> "Interviewer Name for </xsl:text>
                            <xsl:value-of select="$eventName"/>
                            <xsl:text>&#xa;</xsl:text>
                            </xsl:if>

                            <xsl:if test="$interviewDateExist">
                            <xsl:text>Interviewer date_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:text> "Interviewer Date For </xsl:text>
                            <xsl:value-of select="$eventName"/>
                            <xsl:text>&#xa;</xsl:text>
                            </xsl:if>

                            <xsl:if test="$crfStatusExist">
                            <xsl:text>CRF Version Status_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:text> "CRF Version Status For </xsl:text>
                            <xsl:value-of select="$eventName"/>
                            <xsl:text>&#xa;</xsl:text>
                            </xsl:if>

                            <xsl:if test="$crfVersionExist">
                            <xsl:text>Version Name_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:text> "Version Name For </xsl:text>
                            <xsl:value-of select="$eventName"/>
                            <xsl:text>&#xa;</xsl:text>
                            </xsl:if>
                        </xsl:otherwise>
                    </xsl:choose>
                </xsl:if>
            </xsl:for-each>
        </xsl:for-each>

        <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]">
            <xsl:variable name="crfPosition" select="position()"/>
            <xsl:variable name="currentFormOID" select="@FormOID"/>
            <xsl:for-each select="//odm:ItemData">
                <xsl:variable name="itemData" select="."/>
                <xsl:variable name="itemOID" select="@ItemOID"/>
                <xsl:for-each select="//odm:ItemDef[@OID=$itemOID]">
                   <xsl:variable name="formOID" select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID"/>
                    <xsl:if test="$currentFormOID = $formOID">
                        <xsl:value-of select="@Name"/>
                        <xsl:text>_</xsl:text>
                        <xsl:value-of select="$C"/>
                        <xsl:value-of select="$crfPosition"/>
                        <xsl:text>_</xsl:text>
                        <xsl:variable name="group" select="$itemData/parent::node()"/>
                        <xsl:variable name="groupOID" select="$group/@ItemGroupOID"/>
                        <xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
                            <xsl:if test="@Name !='Ungrouped'">
                                <xsl:value-of select="@Name"/>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:if test="$group/@ItemGroupRepeatKey">
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$group/@ItemGroupRepeatKey"/>
                        </xsl:if>
                        <xsl:text> "</xsl:text>
                        <xsl:value-of select="@Comment"/>
                        <xsl:text>"</xsl:text>
                        <xsl:text>&#xa;</xsl:text>

                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:for-each>

        <!--****************************************************************************************************** -->
        <!--                        Starting Item values                                                           -->
        <!--****************************************************************************************************** -->
        <xsl:text>.</xsl:text>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text>VALUE LABELS</xsl:text>
        <xsl:text>&#xa;</xsl:text>
        
        <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]">
            <xsl:variable name="crfPosition" select="position()"/>
            <xsl:variable name="currentFormOID" select="@FormOID"/>
            <xsl:for-each select="//odm:ItemData">
                <xsl:variable name="itemData" select="."/>
                <xsl:variable name="itemOID" select="@ItemOID"/>
                <xsl:for-each select="//odm:ItemDef[@OID=$itemOID]">
                    <xsl:variable name="formOID" select="OpenClinica:ItemDetails/OpenClinica:ItemPresentInForm[@FormOID = $itemData/../../@FormOID]/@FormOID"/>
                    <xsl:if test="$currentFormOID = $formOID">
                        <xsl:value-of select="@Name"/>
                        <xsl:text>_</xsl:text>
                        <xsl:value-of select="$C"/>
                        <xsl:value-of select="$crfPosition"/>
                        <xsl:text>_</xsl:text>
                        <xsl:variable name="group" select="$itemData/parent::node()"/>
                        <xsl:variable name="groupOID" select="$group/@ItemGroupOID"/>
                        <xsl:for-each select="//odm:ItemGroupDef[@OID=$groupOID]">
                            <xsl:if test="@Name !='Ungrouped'">
                                <xsl:value-of select="@Name"/>
                            </xsl:if>
                        </xsl:for-each>
                        <xsl:if test="$group/@ItemGroupRepeatKey">
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$group/@ItemGroupRepeatKey"/>
                        </xsl:if>
                        <xsl:text>&#xa;</xsl:text>
                        <xsl:variable name="codeListOID" select="./odm:CodeListRef/@CodeListOID"/>
                        <xsl:for-each select="//odm:CodeList[@OID=$codeListOID]">
                            <xsl:for-each select="./odm:CodeListItem">
                                <xsl:value-of select="@CodedValue"/>
                                <xsl:text> "</xsl:text>
                                    <xsl:value-of select="./odm:Decode/odm:TranslatedText"/>
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
                </xsl:for-each>
            </xsl:for-each>
        </xsl:for-each>
        <xsl:text>.</xsl:text>
        <xsl:text>&#xa;</xsl:text>
        <xsl:text>EXECUTE.</xsl:text>

        
    </xsl:template>

</xsl:stylesheet>