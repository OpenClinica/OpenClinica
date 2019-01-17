<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<fmt:setBundle basename="org.akaza.openclinica.i18n.words" var="resword"/>
<fmt:setBundle basename="org.akaza.openclinica.i18n.notes" var="restext"/>

<style>
    .subsection {
        margin-bottom: 25px;
        font-size: .85rem;
    }
    .subsection-title {
        width: 100%;
    }
    .subsection-title td {
        vertical-align: middle;
    }
    .subsection-title td > * {
        vertical-align: middle;
    }
    .datatable {
        border-bottom: none !important;
        border-collapse: collapse !important;
        margin-top: 2px !important;
        width: 0 !important;
        min-width: 600px !important;
        float: left;
    }
    .datatable td {
        border: 1px solid #ccc;
        border-bottom-color: #ccc !important;
    }
    .datatable thead td {
        border-color: white !important;
        border-top-color: #ccc !important;
        background-color: #ccc !important;
        text-align: center;
        width: 0 !important;
    }
    .datatable thead td:first-child {
        border-left-color: #ccc !important;
    }
    .datatable thead td:last-child {
        border-right-color: #ccc !important;
    }
    .dataTables_info {
        padding-top: 0.5em !important;
    }
    .dataTables_length {
        padding-top: 0.5em;
        padding-left: 1.5em;
    }
    .dataTables_length > label {
        margin-left: 10px;
    }
    .dataTables_filter {
        display: inline-block;
        float: right;
        margin-top: 5px;
    }
    .dataTables_filter input {
        margin-left: 5px;
    }
    .info-filtered {
        color: #cc6600;
        font-weight: bold;
    }
    .table_tools, .table_actions {
        vertical-align: middle !important;
    }
    .table_tools > a > input {
        margin-top: 3px !important;
    }
    td.actions {
        padding: 3.4px !important;
        vertical-align: middle;
        white-space: nowrap;
    }
    td.highlight, .submission:hover {
        background-color: whitesmoke !important;
    }
    .submission.oc-status-removed {
        color: red;
    }
    .form-name {
        display: inline;
        margin-right: 10px;
    }
    .add-new {
        margin: 3px !important;
    }
    .button_search {
        margin-top: 2px !important;
    }
    input[type=button][disabled] {
        background: #618ebb !important;
        color: #a2bbd4;
    }
    .actions .icon:before {
        content: "\f1234";
    }
    .actions .icon.icon-remove:before {
        content: "\e816";
    }
    .actions .icon.icon-edit:before {
        content: "\f14c";
    }
    .actions .icon.icon-view:before {
        content: "\e813";
    }
    .actions .icon.icon-reassign:before {
        content: "\e92f";
    }
    .actions .icon.icon-restore:before {
        content: "\e817";
    }
    .actions .icon.icon-sign:before {
        content: "\e91a";
    }
    .actions .icon.icon-lock:before {
        content: "\e811";
    }
}
</style>

<div id="commonEvents"></div>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.16/css/jquery.dataTables.min.css"/>
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/handlebars.js/4.0.11/handlebars.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdnjs.cloudflare.com/ajax/libs/moment.js/2.8.4/moment.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/plug-ins/1.10.16/sorting/datetime-moment.js"></script>
<script type="text/JavaScript" language="JavaScript" src="//cdn.datatables.net/plug-ins/1.10.16/api/fnSortNeutral.js"></script>
<script>
    Handlebars.registerHelper('truncate', function(s, length) {
        if (!s)
            return '';
        if (s.join)
            s = s.join(', ');
        s = s.trim();
        return s.length < length ? s : s.substring(0, length) + '...';
    });
</script>
<script id="section-tmpl" type="text/x-handlebars-template">
    <div class="section {{collapseState}}" data-section-number="{{sectionNumber}}">
        <div class="section-header" title='<fmt:message key="collapse_section" bundle="${resword}"/>'>
            {{studyEvent.[@Name]}}
        </div>
        <div class="section-body">
            {{#each studyEvent.forms as |form|}}
            {{#if form.showMe}}
                <div class="subsection" id="common.{{../studyEvent.[@OID]}}.{{form.[@OID]}}">
                    <div class="subsection-header">
                        <h3 class="form-name">{{form.[@Name]}}</h3>
                        <input class="add-new" type="button" value='<fmt:message key="add_new" bundle="${resword}"/>'
                            {{#if form.addNew}}
                                data-url="{{form.addNew}}"
                            {{else}}
                                disabled="disabled"
                            {{/if}}>
                    </div>
                    <table class="datatable" data-repeating="{{../studyEvent.[@Repeating]}}">
                    <thead>
                        <tr>
                            {{#each form.columnTitles as |coltitle|}}
                                <td>{{truncate coltitle 30}}</td>
                            {{/each}}
                            <td>Status</td>
                            <td>Last Updated</td>
                            <td>Updated By</td>
                            <td>Actions</td>
                            <td>oc-status-hide</td>
                        </tr>
                    </thead>
                    <tbody>
                        {{#each form.submissions as |submission|}}
                            <tr class="submission {{submission.hideStatus}}">
                                {{#each submission.fields as |data|}}
                                    <td data-search="{{data}}">{{truncate data 200}}</td>
                                {{/each}}
                                <td>{{submission.studyStatus}}</td>
                                <td>{{submission.updatedDate}}</td>
                                <td>{{submission.updatedBy}}</td>
                                <td class="actions">
                                    {{#each submission.links as |link|}}
                                        <a href="${pageContext.request.contextPath}{{link.[@href]}}">
                                            <span class="icon icon-{{link.[@rel]}}" alt="{{link.[@rel]}}" title="{{link.[@rel]}}"></span>
                                        </a>
                                    {{/each}}
                                </td>
                                <td>{{submission.hideStatus}}</td>
                            </tr>
                        {{/each}}
                    </tbody>
                    </table>
                </div>
            {{/if}}
            {{/each}}
        </div>
    </div>
</script>

<script>
$(function() {

    function collection(x) {
        if (x)
            return x.length ? x : [x];
        return [];
    }

    var linksOrder = ['edit', 'view', 'remove', 'restore', 'reassign', 'sign', 'lock'];
    function collectLinks(studyEventData, formData) {
        var links = [];
        $.merge(links, collection(studyEventData['OpenClinica:Links']['OpenClinica:Link']));
        $.merge(links, collection(formData['OpenClinica:Links']['OpenClinica:Link']));
        links.sort(function(a, b) {
            return linksOrder.indexOf(a['@rel']) - linksOrder.indexOf(b['@rel']);
        });
        return links;
    }

    function copyObject(obj) {
        var copy = {};
        $.extend(true, copy, obj);
        return copy;
    }

    var odm;
    var metadata;
    var studyEvents = {};
    var forms = {};
    var itemGroups = {};
    var items = {};
    var codes = {};
    var columns = {};
    
    $.when(
        $.get('rest/clinicaldata/json/view/${study.oid}/${studySub.oid}/*/*?showArchived=y', function(data){
            odm = data;
            for (var i=0, studies=collection(odm.Study); i<studies.length; i++) {
                if (studies[i]['@OID'] === '${study.oid}') {
                    metadata = studies[i].MetaDataVersion;
                    break;
                }
            }
            collection(metadata.CodeList).forEach(function(codelist) {
                var code = {};
                collection(codelist.CodeListItem).forEach(function(item) {
                    code[item['@CodedValue']] = item.Decode.TranslatedText;
                });
                codes[codelist['@OID']] = code;
            });
            collection(metadata['OpenClinica:MultiSelectList']).forEach(function(multiselect) {
                var code = {};
                collection(multiselect['OpenClinica:MultiSelectListItem']).forEach(function(item) {
                    code[item['@CodedOptionValue']] = item.Decode.TranslatedText;
                });
                codes[multiselect['@ID']] = code;
            });
            collection(metadata.ItemDef).forEach(function(item) {
                items[item['@OID']] = item;
                if (item.CodeListRef)
                    item.codes = codes[item.CodeListRef['@CodeListOID']]
                if (item['OpenClinica:MultiSelectListRef'])
                    item.codes = codes[item['OpenClinica:MultiSelectListRef']['@MultiSelectListID']]
            });
            collection(metadata.ItemGroupDef).forEach(function(itemGroup) {
                itemGroup.items = collection(itemGroup.ItemRef).map(function(ref) {
                    return items[ref['@ItemOID']];
                });
                itemGroups[itemGroup['@OID']] = itemGroup;
            });
            collection(metadata.FormDef).forEach(function(form) {
                form.itemGroups = collection(form.ItemGroupRef).map(function(ref) {
                    return itemGroups[ref['@ItemGroupOID']];
                });
                forms[form['@OID']] = form;
            });        
        }),

        $.get('pages/api/studies/${study.oid}/pages/view%20subject', function(pageJson){
            collection(pageJson.components).forEach(function(component) {
                columns[component.name] = component.columns;
            });
        })

    ).then(function() {
        collection(metadata.StudyEventDef).forEach(function(studyEvent) {
            studyEvent.forms = {};

            collection(studyEvent.FormRef).forEach(function(ref) {
                var studyEventOid = studyEvent['@OID'];
                var formOid = ref['@FormOID'];
                var form = forms[formOid];
                var formStatus = ref['@OpenClinica:Status'];
                var formNotArchived = formStatus !== 'DELETED' && formStatus !== 'AUTO_DELETED';
                var columnTitles = [];
                var submissionFields = {};
                var componentOid = studyEventOid + '.' + formOid;
                var components = columns[componentOid];
                collection(components).forEach(function(col) {
                    var item = items[col];
                    columnTitles.push((item && item.Question) ? item.Question.TranslatedText : col);
                    submissionFields[col] = [];
                });

                studyEvent.forms[formOid] = $.extend({
                    columnTitles: columnTitles,
                    submissionFields: submissionFields,
                    submissions: [],
                    addNew: false,
                    showMe: false
                }, form);
            });

            studyEvent.showMe = false;
            studyEvents[studyEvent['@OID']] = studyEvent;
        });

        collection(odm.ClinicalData.SubjectData['OpenClinica:Links']['OpenClinica:Link']).forEach(function(link) {
            if (link['@rel'] !== 'common-add-new')
                return;

            var oids = link['@tag'].split('.');
            var studyEvent = studyEvents[oids[0]];
            var form = studyEvent.forms[oids[1]];
            form.addNew = link['@href'];
            form.showMe = studyEvent.showMe = true;
        });

        collection(odm.ClinicalData.SubjectData.StudyEventData).forEach(function(studyEventData) {
            var studyEventOid = studyEventData['@StudyEventOID'];
            var studyEvent = studyEvents[studyEventOid];
            if (!studyEvent)
                return;

            if (studyEvent['@OpenClinica:EventType'] !== 'Common')
                return;

            var formData = studyEventData.FormData;
            if (!formData)
                return;

            var formOid = formData['@FormOID'];
            var form = studyEvent.forms[formOid];
            if (!form)
                return;

            var submission = {
                studyStatus: studyEventData['@OpenClinica:Status'],
                hideStatus: formData['@OpenClinica:Status'] === 'invalid' ? 'oc-status-removed' : 'oc-status-active',
                updatedDate: String(formData['@OpenClinica:UpdatedDate']).split(' ')[0],
                updatedBy: formData['@OpenClinica:UpdatedBy'],
                fields: copyObject(form.submissionFields),
                links: collectLinks(studyEventData, formData)
            };
            collection(formData.ItemGroupData).forEach(function(igd) {
                collection(igd.ItemData).forEach(function(itemData) {
                    var itemOid = itemData['@ItemOID'];
                    var data = submission.fields[itemOid];
                    if (data) {
                        var value = itemData['@Value'];
                        var item = items[itemOid];
                        if (item.codes) {
                            value = value.split(',').map(function(code) {
                                return item.codes[code];
                            }).join(', ');
                        }
                        data.push(value);
                    }
                });
            });

            form.submissions.push(submission);
            form.showMe = true;
            studyEvent.showMe = true;
        });

        var hideClass = 'oc-status-removed';
        $.fn.DataTable.ext.search.push(
           function(settings, data, dataIndex) {
              return data[data.length-1] !== hideClass;
           }
        );
        $('#oc-status-hide').on('change', function() {
            hideClass = $(this).val();
            var sections = $('tr.section-header');
            var hides = sections.filter('.' + hideClass);
            hides.hide();
            sections.not(hides).show();
            $('table.datatable').DataTable().draw();
            $('tr.section-header, tr.section-body').removeClass('expanded').addClass('collapsed');
            store(function(data) {
                data.ocStatusHide = hideClass;
            });
        }).change();

        var numVisitBaseds = 0;
        var hideStatus = $('#oc-status-hide').val();
        var sectionTmpl = Handlebars.compile($('#section-tmpl').html());
        var sectionIndex = 2; // Section 0 = General Information, 1 = Visits
        for (var studyEventId in studyEvents) {
            var studyEvent = studyEvents[studyEventId];
            if (!studyEvent)
                continue;

            if (studyEvent['@OpenClinica:EventType'] === 'Common') {
                if (studyEvent.showMe) {
                    $('#commonEvents').append(sectionTmpl({
                        sectionNumber: sectionIndex,
                        collapseState: store.data.collapseSections[sectionIndex] ? 'collapsed' : 'expanded',
                        studyEvent: studyEvent
                    }));
                    sectionIndex++;
                }
            }
            else { // event type != common event
                numVisitBaseds++;
            }
        }
        if (numVisitBaseds) {
            if (store.data.collapseSections[1])
                $('#subjectEvents').toggleClass('expanded collapsed').children('.section-body').hide();
            $('#subjectEvents').removeClass('hide');
        }

        $('#commonEvents').on('click', '.add-new', function() {
            $.ajax({
                type: 'post',
                url: '${pageContext.request.contextPath}' + $(this).data('url'),
                cache: false,
                success: function(obj) {
                    window.location.href = '${pageContext.request.contextPath}' + obj.url;
                },
                error: function(e) {
                    console.log(e);
                    alert('Error. See console log.');
                }
            });
        });

        $.fn.dataTable.moment('DD-MMM-YYYY');
        $('table.datatable')
            .each(function(i) {
                var table = $(this);
                var datatable = table.DataTable({
                    stateSave: true,
                    stateSaveCallback: function(settings, state) {
                        store(function(data) {
                            data.datatables[i] = state;
                        });
                    },
                    stateLoadCallback: function(settings, callback) {
                        var data = store.data.datatables[i];
                        callback(data);
                        if (!data)
                            this.fnSortNeutral();
                    },
                    dom: table.data('repeating') == 'Yes' ? 'frtilp' : 'frti',
                    language: {
                        paginate: {
                            first: '<<',
                            previous: '<',
                            next: '>',
                            last: '>>'
                        },
                        info: '<fmt:message key="results_m_n_of_total" bundle="${resword}"/>',
                        infoEmpty: '<fmt:message key="results_zero_of_zero" bundle="${resword}"/>',
                        infoFiltered: '<span class="info-filtered"><fmt:message key="results_filtered" bundle="${resword}"/></span>',
                        lengthMenu: '<fmt:message key="results_pagesize" bundle="${resword}"/>'
                    },
                    columnDefs: [{
                        targets: -1,
                        visible: false
                    }, {
                        targets: -2,
                        orderable: false
                    }]
                });
                table.children('tbody').on('mouseenter', 'td', function () {
                    var colIdx = datatable.cell(this).index();
                    if (colIdx) {
                        var col = colIdx.column;
                        $(datatable.cells().nodes()).removeClass('highlight');
                        $(datatable.column(col).nodes()).addClass('highlight');
                    }
                });
                var tableWidth = table.width();
                table.closest('.subsection').css('max-width', tableWidth < 500 ? 500 : tableWidth);
            })
            .prev('.dataTables_filter').each(function(i) {
                var searchbox = $(this);
                var subheader = searchbox.closest('.subsection').find('.subsection-header');
                searchbox.appendTo(subheader);
            })
            .end()
            .wrap($('<div>', {
                css: {
                    'max-width': $(window).width() - 200,
                    overflow: 'auto'
                }
            }));

        $('div.section.collapsed').children('.section-body').hide();
        $('#loading').remove();
    }, function() {
        $('#loading').text("Can't Load View Participant Page");
    });
});
</script>
