<%@ page contentType="text/html; charset=UTF-8" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>

<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.format" var="resformat"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.page_messages" var="resmessages"/>


<jsp:include page="include/managestudy_top_pages.jsp"/>

<!-- move the alert message to the sidebar-->
<jsp:include page="include/sideAlert.jsp"/>
<!-- then instructions-->
<tr id="sidebar_Instructions_open">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-down gray border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${restext}"/>

        <div class="sidebar_tab_content">

            <fmt:message key="design_implement_sdv_study_subject" bundle="${restext}"/>

        </div>

    </td>

</tr>
<tr id="sidebar_Instructions_closed" style="display: none">
    <td class="sidebar_tab">

        <a href="javascript:leftnavExpand('sidebar_Instructions_open'); leftnavExpand('sidebar_Instructions_closed');"><span class="icon icon-caret-right gray" border="0" align="right" hspace="10"></span></a>

        <fmt:message key="instructions" bundle="${restext}"/>

    </td>
</tr>
<jsp:include page="include/sideInfo.jsp"/>
<link rel="stylesheet" href="../includes/jmesa/jmesa.css" type="text/css">
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jmesa.js"></script>
<script type="text/JavaScript" language="JavaScript" src="../includes/jmesa/jquery.jmesa.js"></script>
<script type="text/javascript" language="JavaScript" src="../includes/jmesa/jquery-migrate-1.1.1.js"></script>
<script type="text/javascript">
    function onInvokeAction(id,action) {
        setExportToLimit(id, '');
        createHiddenInputFieldsForLimitAndSubmit(id);
    }
    function onInvokeExportAction(id) {
        var parameterString = createParameterStringForLimit(id);
        //location.href = '${pageContext.request.contextPath}/ViewCRF?module=manage&crfId=' + '${crf.id}&' + parameterString;
    }
</script>

<style>
    .section {
        background-color: #f1f1f1;
        background-image: none;
        border-top: 1px solid #999;
        font-size: .8em;
        padding: 1em;
    }
    .section-body.collapsed {
        display: none;
    }
    .collapsed .icon-caret-down {
        display: none;
    }
    .expanded .icon-caret-right {
        display: none;
    }
    .datatable {
        padding-top: 5px;
    }
    thead .table_cell {
        background-color: #ccc !important;
    }
    td.actions {
        padding: 3.4px !important;
        vertical-align: middle;
    }
    td.actions td {
        padding: 3.4px !important;
    }
    .form-name {
        display: inline;
        margin-right: 10px;
    }
    .dataTables_info {
        padding-top: 0.5em !important;
    }
    .dataTables_length {
        padding-top: 0.5em;
        padding-left: 1.5em;
    }
}
</style>

<table cellspacing="0" width="100%">
<tbody id="sections">
    <tr id="loading">
        <td>Loading...</td>
    </tr>
</tbody>
</table>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.16/css/jquery.dataTables.min.css"/>
<script type="text/JavaScript" language="JavaScript" src="https://cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="https://cdnjs.cloudflare.com/ajax/libs/handlebars.js/4.0.11/handlebars.js"></script>
<script id="section-tmpl" type="text/x-handlebars-template">
    <tr class="section-header collapsed">
        <td class="section">
            <span class="icon icon-caret-down gray"></span>
            <span class="icon icon-caret-right gray"></span>
            <span>{{sectionName}}</span>
        </td>
    </tr>
    <tr class="section-body collapsed" data-study-event-oid="{{studyEventOid}}">
    <td>
    <div class="box_T">
    <div class="box_L">
    <div class="box_R">
    <div class="box_B">
    <div class="box_TL">
    <div class="box_TR">
    <div class="box_BL">
    <div class="box_BR">
    <div class="tablebox_center">
        <table border="0" cellpadding="0" cellspacing="0">
        <tbody>
        {{#each forms as |form|}}
        <tr>
            <td colspan="3" valign="top">
                <input type="button" class="add-new" value="Add New" data-form-oid="{{form.[@OID]}}">
                <h3 class="form-name">{{form.[@Name]}}</h3>
                <table border="0" cellpadding="0" cellspacing="0" class="datatable">
                <thead>
                <tr valign="top">
                    {{#each form.itemGroups as |itemGroup|}}
                        {{#each itemGroup.items as |item|}}
                            <td class="table_cell">{{item.Question.TranslatedText}}</td>
                        {{/each}}
                    {{/each}}
                    <td class="table_cell">
                        <center>Status</center>
                    </td>
                    <td class="table_cell">
                        <center>Updated</center>
                    </td>
                    <td class="table_cell">
                        <center>Actions</center>
                    </td>
                </tr>
                </thead>
                <tbody>
                    {{#each form.submissions as |submission|}}
                        <tr>
                            {{#each submission as |item|}}
                                <td class="table_cell">{{item}}</td>
                            {{/each}}
                            <td align="center" class="table_cell">{{submission.status}}</td>
                            <td align="center" class="table_cell"></td>
                            <td align="center" class="table_cell actions">
                                <table border="0" cellpadding="0" cellspacing="0">
                                    <tbody>
                                        <tr valign="top">
                                            <td>
                                                <a href="EnketoFormServlet?formLayoutId=2&amp;studyEventId=3&amp;eventCrfId=3&amp;originatingPage=ViewStudySubject%3Fid%3D1&amp;mode=edit" onmousedown="javascript:setImage('bt_EnterData1','images/bt_EnterData_d.gif');" onmouseup="javascript:setImage('bt_EnterData1','images/bt_EnterData.gif');">
                                                <span name="bt_EnterData1" class="icon icon-pencil-squared" border="0" alt="Administrative Editing" title="Administrative Editing" align="left" hspace="6">
                                                </span></a>
                                            </td>
                                            <td>
                                                <a href="EnketoFormServlet?formLayoutId=2&amp;studyEventId=3&amp;eventCrfId=3&amp;originatingPage=ViewStudySubject%3Fid%3D1&amp;mode=view" onmousedown="javascript:setImage('bt_View1','images/bt_View_d.gif');" onmouseup="javascript:setImage('bt_View1','images/bt_View.gif');"><span name="bt_View1" class="icon icon-search" border="0" alt="View" title="View" align="left" hspace="6"></span></a>
                                            </td>
                                            <td><a href="RemoveEventCRF?action=confirm&amp;id=3&amp;studySubId=1" onmousedown="javascript:setImage('bt_Remove1','images/bt_Remove_d.gif');" onmouseup="javascript:setImage('bt_Remove1','images/bt_Remove.gif');"><span name="bt_Remove1" class="icon icon-cancel" border="0" alt="Remove" title="Remove" align="left" hspace="6"></span></a>
                                            </td>
                                            <td>
                                                <a href="DeleteEventCRF?action=confirm&amp;ssId=1&amp;ecId=3" onmousedown="javascript:setImage('bt_Delete1','images/bt_Delete_d.gif');" onmouseup="javascript:setImage('bt_Delete1','images/bt_Delete.gif');"><span name="bt_Delete1" class="icon icon-trash red" border="0" alt="Delete" title="Delete" align="left" hspace="6"></span></a>
                                            </td>
                                            <td>
                                                <a href="pages/managestudy/chooseCRFVersion?crfId=2&amp;crfName=Medications&amp;formLayoutId=2&amp;formLayoutName=1&amp;studySubjectLabel=GOGO&amp;studySubjectId=1&amp;eventCRFId=3&amp;eventDefinitionCRFId=2" onmousedown="javascript:setImage('bt_Reassign','images/bt_Reassign_d.gif');" onmouseup="javascript:setImage('bt_Reassign','images/bt_Reassign.gif');"><span name="Reassign" class="icon icon-icon-reassign3" border="0" alt="Reassign CRF to a New Version" title="Reassign CRF to a New Version" align="left" hspace="6"></span></a>
                                            </td>
                                        </tr>
                                    </tbody>
                                </table>
                            </td>
                        </tr>
                    {{/each}}
                </tbody>
                </table>
            {{/each}}
            </td>
        </tr>
        </tbody>
        </table>
        <br>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </div>
    </td>
    </tr>
</script>

<script>
$(function() {
    $.get("..${jsonPath}", function(data) {
        var studyEvents = {};
        var forms = {};
        var itemGroups = {};
        var items = {};
        data.Study.MetaDataVersion.ItemDef.forEach(function(item) {
            items[item['@OID']] = item;
        });
        data.Study.MetaDataVersion.ItemGroupDef.forEach(function(itemGroup) {
            itemGroup.items = itemGroup.ItemRef.map(function(ref) {
                return items[ref['@ItemOID']];
            });
            itemGroups[itemGroup['@OID']] = itemGroup;
        });
        data.Study.MetaDataVersion.FormDef.forEach(function(form) {
            form.itemGroups = {};
            form.submissionObj = {};
            var groupRef = form.ItemGroupRef;
            if (!groupRef.length)
                groupRef = [groupRef];
            groupRef.forEach(function(ref) {
                var id = ref['@ItemGroupOID'];
                var itemGroup = itemGroups[id]
                form.itemGroups[id] = itemGroup;
                itemGroup.items.forEach(function(item) {
                    form.submissionObj[item['@OID']] = [];
                });
            });
            form.submissions = [];
            forms[form['@OID']] = form;
        });
        data.Study.MetaDataVersion.StudyEventDef.forEach(function(studyEvent) {
            var formRef = studyEvent.FormRef;
            if (!formRef.length)
                formRef = [formRef];
            studyEvent.forms = formRef.map(function(ref) {
                return forms[ref['@FormOID']];
            });
            studyEvents[studyEvent['@OID']] = studyEvent;
        });
        data.ClinicalData.SubjectData.StudyEventData.forEach(function(data) {
            var formData = data.FormData;
            if (!formData)
                return;

            var form = forms[formData['@FormOID']];
            var submission = $.extend(true, {}, form.submissionObj);
            var status = formData['@OpenClinica:Status'];
            var itemGroupData = formData.ItemGroupData;
            if (!itemGroupData.length)
                itemGroupData = [itemGroupData];
            itemGroupData.forEach(function(igd) {
                igd.ItemData.forEach(function(item) {
                    submission[item['@ItemOID']].push(item['@Value']);
                });
            });
            form.submissions.push(submission);
        });

        var studyOid = data.ClinicalData['@StudyOID'];
        var studySubjectOid = data.ClinicalData.SubjectData['@SubjectKey'];

        var sectionTable = $('#sections');
        var sectionTmpl = Handlebars.compile($('#section-tmpl').html());
        for (var studyEventId in studyEvents) {
            var studyEvent = studyEvents[studyEventId];
            sectionTable.append(sectionTmpl({
                sectionName: studyEvent['@Name'],
                studyEventOid: studyEventId,
                forms: studyEvent.forms
            }));
        };
        sectionTable.on('click', '.section-header', function() {
            $(this).next().addBack().toggleClass('collapsed expanded');
        });
        sectionTable.on('click', '.add-new', function() {
            var btn = $(this);
            var formOid = btn.data('form-oid');
            var studyEventOid = btn.closest('.section-body').data('study-event-oid');
            $.ajax({
                type: 'post',
                url: '${pageContext.request.contextPath}/pages/api/addAnotherForm',
                cache: false,
                data: {
                    studyoid: studyOid,
                    studyeventdefinitionoid: studyEventOid,
                    studysubjectoid: studySubjectOid,
                    crfoid: formOid
                },
                success: function(obj) {
                    window.location.href = '${pageContext.request.contextPath}' + obj.url;
                },
                error: function(e) {
                    alert('Error. See console log.');
                    console.log(e);
                }
            });
        });
        var datatables = $('table.datatable');
        datatables.DataTable({
            dom: "frtilp",
            language: {
                paginate: {
                    first: '<<',
                    previous: '<',
                    next: '>',
                    last: '>>'
                }
            }
        });
        datatables.each(function() {
            var theTable = $(this);
            var header = theTable.parent();
            var paging = theTable.next();
            var pagesize = paging.next().children().contents();
            header.prevUntil().prependTo(header);
            paging.text(paging.text().replace(' to ', '-').replace('entries', 'rows'));
            pagesize[2].replaceWith(' rows per page');
            pagesize[0].remove();
        });
        datatables.parent().css({
            'max-width': $(window).width() - 200 + 'px',
            'overflow': 'scroll'
        });        
        $('#loading').remove();
    });
});
</script>
<jsp:include page="include/footer.jsp"/>
</body>
</html>