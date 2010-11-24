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
    <!--E to represent Events-->
    <xsl:variable name="E" select="'E'"/>
    <!--C to represent CRFS-->
    <xsl:variable name="C" select="'C'"/>
    <xsl:key name="studyEvents" match="odm:StudyEventData" use="@StudyEventOID"></xsl:key>
    <xsl:key name="eventCRFs" match="odm:FormData" use="@FormOID"></xsl:key>
    <xsl:template match="/">
        <!-- Getting the Dataset Name -->
        <xsl:variable name="study" select="/odm:ODM/odm:Study[1]"/>

        <xsl:text>Subject ID</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Unique ID</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Subject Status</xsl:text>
        <xsl:value-of select="$delimiter" />
        <xsl:text>Sex</xsl:text>
        <xsl:value-of select="$delimiter" />

        <!-- Selecting Study Event column headers-->
        <xsl:for-each select="//odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID))]">
            <xsl:variable name="eventOID" select="@StudyEventOID"/>
            <xsl:variable name="eventPosition" select="position()"/>
            <xsl:variable name="eventRepeatKey" select="@StudyEventRepeatKey"/>
            <xsl:choose>
                <xsl:when test="@StudyEventRepeatKey">
                    <xsl:variable name="repeatedEvents" select="//odm:StudyEventData[@StudyEventOID=$eventOID]"/>
                    <xsl:for-each select="//odm:StudyEventData[@StudyEventOID=$eventOID]">
                        <xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
                        <xsl:choose>
                            <xsl:when test="position()=1">
                                <xsl:text>Location_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:value-of select="$delimiter" />
                                <xsl:text>StartDate_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:value-of select="$delimiter" />
                                <xsl:text>Event Status_</xsl:text>
                                <xsl:value-of select="$E"/>
                                <xsl:value-of select="$eventPosition"/>
                                <xsl:text>_repeat</xsl:text>
                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                <xsl:value-of select="$delimiter" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:variable name="prevIndex" select="position()-1"/>
                                <xsl:if test="$repeatedEvents[$prevIndex]/@StudyEventRepeatKey != @StudyEventRepeatKey">
                                    <xsl:text>Location_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:value-of select="$delimiter" />
                                    <xsl:text>StartDate_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:value-of select="$delimiter" />
                                    <xsl:text>Event Status_</xsl:text>
                                    <xsl:value-of select="$E"/>
                                    <xsl:value-of select="$eventPosition"/>
                                    <xsl:text>_repeat</xsl:text>
                                    <xsl:value-of select="@StudyEventRepeatKey"/>
                                    <xsl:value-of select="$delimiter" />
                                </xsl:if>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:for-each>
                </xsl:when>
                <xsl:otherwise>
                    <xsl:text>Location_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:value-of select="$delimiter" />
                    <xsl:text>StartDate_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:value-of select="$delimiter" />
                    <xsl:text>Event Status_</xsl:text>
                    <xsl:value-of select="$E"/>
                    <xsl:value-of select="$eventPosition"/>
                    <xsl:value-of select="$delimiter" />
                </xsl:otherwise>
            </xsl:choose>

        </xsl:for-each>
        <!--Selecting Event CRF column headers -->
        <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]">
            <xsl:variable name="crfPosition" select="position()"/>
            <xsl:variable name="parentEvent" select="//odm:StudyEventData[odm:FormData[@FormOID=@FormOID]]"/>
            <xsl:for-each select="//odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID))]">
                <xsl:variable name="eventOID" select="@StudyEventOID"/>
                <xsl:variable name="eventPosition" select="position()"/>
                <xsl:if test="@StudyEventOID = $parentEvent/@StudyEventOID">
                    <xsl:choose>
                        <xsl:when test="@StudyEventRepeatKey">
                            <xsl:variable name="repeatedEvents" select="//odm:StudyEventData[@StudyEventOID=$eventOID]"/>
                            <xsl:for-each select="//odm:StudyEventData[@StudyEventOID=$eventOID]">
                                <xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
                                <xsl:choose>
                                    <xsl:when test="position()=1">
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
                                        <xsl:value-of select="$delimiter" />
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
                                        <xsl:value-of select="$delimiter" />
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
                                        <xsl:value-of select="$delimiter" />
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
                                        <xsl:value-of select="$delimiter" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:variable name="prevIndex" select="position()-1"/>
                                        <xsl:if test="$repeatedEvents[$prevIndex]/@StudyEventRepeatKey != @StudyEventRepeatKey">
                                            <xsl:text>Interviewer_</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:if test="@StudyEventRepeatKey">
                                                <xsl:text>_repeat_</xsl:text>
                                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                            </xsl:if>
                                            <xsl:text>_</xsl:text>
                                            <xsl:value-of select="$C"/>
                                            <xsl:value-of select="$crfPosition"/>
                                            <xsl:value-of select="$delimiter" />
                                            <xsl:text>Interviewer date_</xsl:text>
                                            <xsl:value-of select="$E"/>
                                            <xsl:value-of select="$eventPosition"/>
                                            <xsl:if test="@StudyEventRepeatKey">
                                                <xsl:text>_repeat</xsl:text>
                                                <xsl:value-of select="@StudyEventRepeatKey"/>
                                            </xsl:if>
                                            <xsl:text>_</xsl:text>
                                            <xsl:value-of select="$C"/>
                                            <xsl:value-of select="$crfPosition"/>
                                            <xsl:value-of select="$delimiter" />
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
                                            <xsl:value-of select="$delimiter" />
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
                                            <xsl:value-of select="$delimiter" />
                                        </xsl:if>
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:for-each>
                        </xsl:when>
                        <xsl:otherwise>
                            <xsl:text>Interviewer_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:value-of select="$delimiter" />
                            <xsl:text>Interviewer date_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:value-of select="$delimiter" />
                            <xsl:text>CRF Version Status_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:value-of select="$delimiter" />
                            <xsl:text>Version Name_</xsl:text>
                            <xsl:value-of select="$E"/>
                            <xsl:value-of select="$eventPosition"/>
                            <xsl:text>_</xsl:text>
                            <xsl:value-of select="$C"/>
                            <xsl:value-of select="$crfPosition"/>
                            <xsl:value-of select="$delimiter" />
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
                    <xsl:variable name="formOID" select="@OpenClinica:FormOIDs"/>
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
                        <xsl:value-of select="$delimiter"/>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>
        </xsl:for-each>

        <xsl:text>&#xa;</xsl:text>

        <!--Pulling out column values -->
        <xsl:for-each select="/odm:ODM/odm:ClinicalData/odm:SubjectData">
            <xsl:value-of select="@OpenClinica:StudySubjectId"></xsl:value-of>
            <xsl:value-of select="$delimiter" />
            <xsl:value-of select="@OpenClinica:UniqueIdentifier"></xsl:value-of>
            <xsl:value-of select="$delimiter" />
            <xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
            <xsl:value-of select="$delimiter" />
            <xsl:value-of select="@OpenClinica:Sex"></xsl:value-of>
            <xsl:value-of select="$delimiter" />
            <xsl:variable name="subjectEvents" select="./odm:StudyEventData"/>
            <xsl:for-each select="//odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID))]">
                <xsl:variable name="eventOID" select="@StudyEventOID"/>
                <xsl:variable name="eventPosition" select="position()"/>
                <xsl:choose>
                    <xsl:when test="@StudyEventRepeatKey">
                        <xsl:variable name="repeatedEvents" select="//odm:StudyEventData[@StudyEventOID=$eventOID]"/>
                        <xsl:for-each select="//odm:StudyEventData[@StudyEventOID=$eventOID]">
                            <xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
                            <xsl:variable name="repeatKey" select="@StudyEventRepeatKey"/>
                            <xsl:variable name="subjectEvent" select="$subjectEvents[@StudyEventOID=$eventOID and @StudyEventRepeatKey=$repeatKey]"/>
                            <xsl:choose>
                                <xsl:when test="position()=1">
                                    <xsl:choose>
                                        <xsl:when test="$subjectEvent/node()">
                                            <xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
                                            <xsl:value-of select="$delimiter" />
                                            <xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
                                            <xsl:value-of select="$delimiter" />
                                            <xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
                                            <xsl:value-of select="$delimiter" />
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:value-of select="$delimiter" />
                                            <xsl:value-of select="$delimiter" />
                                            <xsl:value-of select="$delimiter" />
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:variable name="prevIndex" select="position()-1"/>
                                    <xsl:if test="$repeatedEvents[$prevIndex]/@StudyEventRepeatKey != @StudyEventRepeatKey">
                                        <xsl:choose>
                                            <xsl:when test="$subjectEvent/node()">
                                                <xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
                                                <xsl:value-of select="$delimiter" />
                                                <xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
                                                <xsl:value-of select="$delimiter" />
                                                <xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
                                                <xsl:value-of select="$delimiter" />
                                            </xsl:when>
                                            <xsl:otherwise>
                                                <xsl:value-of select="$delimiter" />
                                                <xsl:value-of select="$delimiter" />
                                                <xsl:value-of select="$delimiter" />
                                            </xsl:otherwise>
                                        </xsl:choose>
                                    </xsl:if>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:for-each>
                    </xsl:when>
                    <xsl:otherwise>
                        <xsl:variable name="subjectEvent" select="$subjectEvents[@StudyEventOID=$eventOID]"/>
                        <xsl:choose>
                            <xsl:when test="$subjectEvent/node()">
                                <xsl:value-of select="@OpenClinica:StudyEventLocation"></xsl:value-of>
                                <xsl:value-of select="$delimiter" />
                                <xsl:value-of select="@OpenClinica:StartDate"></xsl:value-of>
                                <xsl:value-of select="$delimiter" />
                                <xsl:value-of select="@OpenClinica:Status"></xsl:value-of>
                                <xsl:value-of select="$delimiter" />
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:value-of select="$delimiter" />
                                <xsl:value-of select="$delimiter" />
                                <xsl:value-of select="$delimiter" />
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:otherwise>
                </xsl:choose>

            </xsl:for-each>

            <xsl:variable name="subjectForms" select="./odm:StudyEventData/odm:FormData"/>
            <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]">
                <xsl:variable name="currentForm" select="current()"/>
                <xsl:variable name="subjectFormData" select="$subjectForms[@FormOID=$currentForm/@FormOID]"/>
                <xsl:variable name="parentEvent" select="//odm:StudyEventData[odm:FormData[@FormOID=@FormOID]]"/>
                <xsl:for-each select="//odm:StudyEventData[generate-id() = generate-id(key('studyEvents',@StudyEventOID))]">
                    <xsl:variable name="eventOID" select="@StudyEventOID"/>
                    <xsl:if test="@StudyEventOID = $parentEvent/@StudyEventOID">
                        <xsl:choose>
                            <xsl:when test="@StudyEventRepeatKey">
                                <xsl:variable name="repeatedEvents" select="//odm:StudyEventData[@StudyEventOID=$eventOID]"/>
                                <xsl:for-each select="//odm:StudyEventData[@StudyEventOID=$eventOID]">
                                    <xsl:sort select="@StudyEventRepeatKey" data-type="number"/>
                                    <xsl:choose>
                                        <xsl:when test="position()=1">
                                            <xsl:choose>
                                                <xsl:when test="$subjectFormData/node()">
                                                    <xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"></xsl:value-of>
                                                    <xsl:value-of select="$delimiter" />
                                                    <xsl:value-of select="$currentForm/@OpenClinica:StartDate"></xsl:value-of>
                                                    <xsl:value-of select="$delimiter" />
                                                    <xsl:value-of select="$currentForm/@OpenClinica:Status"></xsl:value-of>
                                                    <xsl:value-of select="$delimiter" />
                                                    <xsl:value-of select="$currentForm/@OpenClinica:Version"></xsl:value-of>
                                                    <xsl:value-of select="$delimiter" />
                                                </xsl:when>
                                                <xsl:otherwise>
                                                    <xsl:value-of select="$delimiter" />
                                                    <xsl:value-of select="$delimiter" />
                                                    <xsl:value-of select="$delimiter" />
                                                    <xsl:value-of select="$delimiter" />
                                                </xsl:otherwise>
                                            </xsl:choose>
                                        </xsl:when>
                                        <xsl:otherwise>
                                            <xsl:variable name="prevIndex" select="position()-1"/>
                                            <xsl:if test="$repeatedEvents[$prevIndex]/@StudyEventRepeatKey != @StudyEventRepeatKey">
                                                <xsl:choose>
                                                    <xsl:when test="$subjectFormData/node()">
                                                        <xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"></xsl:value-of>
                                                        <xsl:value-of select="$delimiter" />
                                                        <xsl:value-of select="$currentForm/@OpenClinica:StartDate"></xsl:value-of>
                                                        <xsl:value-of select="$delimiter" />
                                                        <xsl:value-of select="$currentForm/@OpenClinica:Status"></xsl:value-of>
                                                        <xsl:value-of select="$delimiter" />
                                                        <xsl:value-of select="$currentForm/@OpenClinica:Version"></xsl:value-of>
                                                        <xsl:value-of select="$delimiter" />
                                                    </xsl:when>
                                                    <xsl:otherwise>
                                                        <xsl:value-of select="$delimiter" />
                                                        <xsl:value-of select="$delimiter" />
                                                        <xsl:value-of select="$delimiter" />
                                                        <xsl:value-of select="$delimiter" />
                                                    </xsl:otherwise>
                                                </xsl:choose>
                                            </xsl:if>
                                        </xsl:otherwise>
                                    </xsl:choose>
                                </xsl:for-each>
                            </xsl:when>
                            <xsl:otherwise>
                                <xsl:choose>
                                    <xsl:when test="$subjectFormData/node()">
                                        <xsl:value-of select="$currentForm/@OpenClinica:InterviewerName"></xsl:value-of>
                                        <xsl:value-of select="$delimiter" />
                                        <xsl:value-of select="$currentForm/@OpenClinica:StartDate"></xsl:value-of>
                                        <xsl:value-of select="$delimiter" />
                                        <xsl:value-of select="$currentForm/@OpenClinica:Status"></xsl:value-of>
                                        <xsl:value-of select="$delimiter" />
                                        <xsl:value-of select="$currentForm/@OpenClinica:Version"></xsl:value-of>
                                        <xsl:value-of select="$delimiter" />
                                    </xsl:when>
                                    <xsl:otherwise>
                                        <xsl:value-of select="$delimiter" />
                                        <xsl:value-of select="$delimiter" />
                                        <xsl:value-of select="$delimiter" />
                                        <xsl:value-of select="$delimiter" />
                                    </xsl:otherwise>
                                </xsl:choose>
                            </xsl:otherwise>
                        </xsl:choose>
                    </xsl:if>
                </xsl:for-each>
            </xsl:for-each>

            <xsl:variable name="subjectItems" select="./odm:StudyEventData/odm:FormData/odm:ItemGroupData/odm:ItemData"/>
            <xsl:for-each select="//odm:FormData[generate-id() = generate-id(key('eventCRFs',@FormOID))]">
                <xsl:variable name="crfPosition" select="position()"/>
                <xsl:variable name="currentFormOID" select="@FormOID"/>
                <xsl:for-each select="//odm:ItemData">
                    <xsl:variable name="itemData" select="current()"/>
                    <xsl:variable name="itemOID" select="@ItemOID"/>
                    <xsl:variable name="subjectItem" select="$subjectItems[@ItemOID = $itemData/@ItemOID]"/>
                    <xsl:for-each select="//odm:ItemDef[@OID=$itemOID]">
                        <xsl:variable name="formOID" select="@OpenClinica:FormOIDs"/>
                        <xsl:if test="$currentFormOID = $formOID">
                            <xsl:choose>
                                <xsl:when test="count($subjectItem) &gt; 0">
                                    <xsl:value-of select="$itemData/@Value"/>
                                    <xsl:value-of select="$delimiter"/>
                                </xsl:when>
                                <xsl:otherwise>
                                    <xsl:value-of select="$delimiter"/>
                                </xsl:otherwise>
                            </xsl:choose>
                        </xsl:if>
                    </xsl:for-each>
                </xsl:for-each>
            </xsl:for-each>

            <xsl:text>&#xa;</xsl:text>
        </xsl:for-each>
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

    <xsl:value-of select="$year_of_date"/>
    <xsl:value-of select="'-'"/>
    <xsl:choose>
    <xsl:when test="$month = '01'">Jan</xsl:when>
    <xsl:when test="$month = '02'">Feb</xsl:when>
    <xsl:when test="$month = '03'">Mar</xsl:when>
    <xsl:when test="$month = '04'">Apr</xsl:when>
    <xsl:when test="$month = '05'">May</xsl:when>
    <xsl:when test="$month = '06'">Jun</xsl:when>
    <xsl:when test="$month = '07'">Jul</xsl:when>
    <xsl:when test="$month = '08'">Aug</xsl:when>
    <xsl:when test="$month = '09'">Sep</xsl:when>
    <xsl:when test="$month = '10'">Oct</xsl:when>
    <xsl:when test="$month = '11'">Nov</xsl:when>
    <xsl:when test="$month = '12'">Dec</xsl:when>
    </xsl:choose>

    <xsl:value-of select="'-'"/>
    <xsl:if test="(string-length($days) &lt; 2)">
    <xsl:value-of select="0"/>
    </xsl:if>

    <xsl:value-of select="$days"/>
    </xsl:template>


</xsl:stylesheet>