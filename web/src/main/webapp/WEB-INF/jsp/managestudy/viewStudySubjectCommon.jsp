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
    tr:hover, td.highlight {
        background-color: whitesmoke !important;
    }
    input[type=button][disabled], input[type=button][disabled]:hover {
        background-color: lightgray;
        background-image: none;
        color: gray;
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
                <input type="button" class="add-new" value="Add New" data-form-oid="{{form.[@OID]}}" {{{form.disabled}}}>
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
                    <tr valign="top">
                        {{#each form.itemGroups as |itemGroup|}}
                            {{#each itemGroup.items as |item|}}
                                <td class="table_cell"></td>
                            {{/each}}
                        {{/each}}
                        <td class="table_cell">
                        </td>
                        <td class="table_cell">
                        </td>
                        <td class="table_cell">
                        </td>
                    </tr>
                </thead>
                <tbody>
                    {{#each form.submissions as |submission|}}
                        <tr>
                            {{#each submission.data as |item|}}
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
    function collection(x) {
        if (x)
            return x.length ? x : [x];
        return [];
    }
    $.get('rest/clinicaldata/json/view/${study.oid}/${studySub.oid}/*/*', function(data) {
        var studyOid = data.ClinicalData['@StudyOID'];
        var studySubjectOid = data.ClinicalData.SubjectData['@SubjectKey'];

        var studyEvents = {};
        var forms = {};
        var itemGroups = {};
        var items = {};

        var metadata;
        for (var i=0, studies=collection(data.Study); i<studies.length; i++) {
            if (studies[i]['@OID'] === '${study.oid}') {
                metadata = studies[i].MetaDataVersion;
                break;
            }
        }        
        collection(metadata.ItemDef).forEach(function(item) {
            items[item['@OID']] = item;
        });
        collection(metadata.ItemGroupDef).forEach(function(itemGroup) {
            itemGroup.items = itemGroup.ItemRef.map(function(ref) {
                return items[ref['@ItemOID']];
            });
            itemGroups[itemGroup['@OID']] = itemGroup;
        });
        collection(metadata.FormDef).forEach(function(form) {
            form.itemGroups = {};
            form.submissionObj = {};
            collection(form.ItemGroupRef).forEach(function(ref) {
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
        collection(metadata.StudyEventDef).forEach(function(studyEvent) {
            studyEvent.forms = collection(studyEvent.FormRef).filter(function(ref) {
                return ref['OpenClinica:ConfigurationParameters']['@HideCRF'] === 'No';
            }).map(function(ref) {
                var form = forms[ref['@FormOID']];
                form.studyEvent = studyEvent;
                form.disabled = '';
                return form;
            });
            studyEvents[studyEvent['@OID']] = studyEvent;
        });

        collection(data.ClinicalData.SubjectData.StudyEventData).forEach(function(studyEvent) {
            var formData = studyEvent.FormData;
            if (!formData)
                return;

            var form = forms[formData['@FormOID']];
            if (!form)
                return;

            if (form.studyEvent['@Repeating'] === 'No')
                form.disabled = 'disabled="disabled"';

            var submission = {
                status: studyEvent['@OpenClinica:Status'],
                data: $.extend(true, {}, form.submissionObj)
            };
            collection(formData.ItemGroupData).forEach(function(igd) {
                collection(igd.ItemData).forEach(function(item) {
                    submission.data[item['@ItemOID']].push(item['@Value']);
                });
            });
            form.submissions.push(submission);
        });

        var sectionTable = $('#sections');
        var sectionTmpl = Handlebars.compile($('#section-tmpl').html());
        for (var studyEventId in studyEvents) {
            var studyEvent = studyEvents[studyEventId];
            if (studyEvent['@OpenClinica:EventType'] !== 'Common')
                continue;
            sectionTable.append(sectionTmpl({
                sectionName: studyEvent['@Name'],
                studyEventOid: studyEventId,
                forms: studyEvent.forms
            }));
        }
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
        datatables.each(function() {
            var table = $(this).DataTable({
                dom: "frtilp",
                language: {
                    paginate: {
                        first: '<<',
                        previous: '<',
                        next: '>',
                        last: '>>'
                    }
                },
                columnDefs: [{
                    targets: -1,
                    render: function(data, type, row) {
                        return data;
                    }
                }, {
                    targets: '_all',
                    render: function(data, type, row) {
                        return data.length > 200 ?
                            data.substr(0, 200) +'…' : data;
                    }
                }],
                initComplete: function () {
                    var columns = this.api().columns();
                    columns.every(function() {
                        var column = this;
                        if (column.index() === columns.indexes().length - 1)
                            return;
                        var select = $('<select><option value=""></option></select>')
                            .prependTo($(column.header()))
                            .on('change', function () {
                                var val = $.fn.dataTable.util.escapeRegex(
                                    $(this).val()
                                );
                                column
                                    .search( val ? '^' + val + '$' : '', true, false )
                                    .draw();
                            });
                        column.data().unique().sort().each(function(val, index, api) {
                            select.append('<option value="' + val + '">' + val + '</option>');
                        });
                    });
                }
            });
            $(this).children('tbody').on('mouseenter', 'td.table_cell', function () {
                var colIdx = table.cell(this).index().column; 
                $(table.cells().nodes()).removeClass('highlight');
                $(table.column(colIdx).nodes()).addClass('highlight');
            });
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
