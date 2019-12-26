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
    <div class="section expanded hide" id="common-event-{{sectionNumber}}" data-section-number="{{sectionNumber}}" data-section-oid="{{studyEvent.[@OID]}}">
        <div class="section-header" title='<fmt:message key="collapse_section" bundle="${resword}"/>'>
            {{studyEvent.[@Name]}}
        </div>
        <div class="section-body">
            Loading...<br><br>
        </div>
    </div>
</script>
<script id="section-body-tmpl" type="text/x-handlebars-template">
    {{#if sectionErrors}}
        <div>ERROR</div>
        <ul>
            {{#each sectionErrors as |err|}}
                <li>{{err}}</li>
            {{/each}}
        </ul>
    {{/if}}
    {{#each studyEvent.forms as |form|}}
    {{#if form.showMe}}
        <div class="subsection" id="common.{{../studyEvent.[@OID]}}.{{form.[@OID]}}">
            <div class="subsection-header">
                <h3 class="form-name">
                    {{form.[@Name]}}
                </h3>
                {{#if form.addNew}}
                    <input class="add-new" type="button" value='<fmt:message key="add_new" bundle="${resword}"/>' data-url="{{form.addNew}}">
                {{/if}}
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
</script>

<script>
$(function() {

    function collection() {
        var obj = arguments[0];
        for (var i=1; i<arguments.length; i++) {
            if (obj)
                obj = obj[arguments[i]];
        }
        if (obj)
            return obj.length ? obj : [obj];
        return [];
    }

    function foreach(items, action, errors) {
        collection(items).forEach(function(item) {
            try {
                action(item);
            }
            catch (e) {
                logError('Unable to process' + item, e);
            }
        });
    }

    function findone(items, condition, errors) {
        for (var i=0, items=collection(items); i<items.length; i++) {
            var item = items[i];
            try {
                if (condition(item))
                    return item;                
            }
            catch (e) {
                logError('Unable to process' + item, e);
            }
        }
    }

    var linksOrder = ['edit', 'view', 'remove', 'restore', 'reassign', 'sign', 'lock'];
    function collectLinks(studyEventData, formData) {
        var links = [];
        $.merge(links, collection(studyEventData, 'OpenClinica:Links', 'OpenClinica:Link'));
        $.merge(links, collection(formData, 'OpenClinica:Links', 'OpenClinica:Link'));
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
    var errors = [];

    function logError(message, objs) {
        console.log(arguments);
        return errors.push(message);
    }

    $.when(
        $.get('rest/clinicaldata/json/view/${study.oc_oid}/${studySub.oid}/*/*?showArchived=y&clinicaldata=n&links=y', function(data) {
            odm = data;
            var study = findone(odm.Study, function(study) {
                return study['@OID'] === '${study.oc_oid}';
            }, errors);

            if (study && study.MetaDataVersion) {
                metadata = study.MetaDataVersion;
            }
            else {
                return logError('Unable to fetch metadata for study: ${study.oc_oid}', study);
            }

            foreach(metadata.CodeList, function(codelist) {
                var code = {};
                foreach(codelist.CodeListItem, function(item) {
                    code[item['@CodedValue']] = item.Decode.TranslatedText;
                }, errors);
                codes[codelist['@OID']] = code;
            }, errors);
            foreach(metadata['OpenClinica:MultiSelectList'], function(multiselect) {
                var code = {};
                foreach(multiselect['OpenClinica:MultiSelectListItem'], function(item) {
                    code[item['@CodedOptionValue']] = item.Decode.TranslatedText;
                }, errors);
                codes[multiselect['@ID']] = code;
            }, errors);
            foreach(metadata.ItemDef, function(item) {
                items[item['@OID']] = item;
                if (item.CodeListRef)
                    item.codes = codes[item.CodeListRef['@CodeListOID']]
                if (item['OpenClinica:MultiSelectListRef'])
                    item.codes = codes[item['OpenClinica:MultiSelectListRef']['@MultiSelectListID']]
            }, errors);
            foreach(metadata.ItemGroupDef, function(itemGroup) {
                itemGroup.items = collection(itemGroup.ItemRef).map(function(ref) {
                    return items[ref['@ItemOID']];
                });
                itemGroups[itemGroup['@OID']] = itemGroup;
            }, errors);
            foreach(metadata.FormDef, function(form) {
                form.itemGroups = collection(form.ItemGroupRef).map(function(ref) {
                    return itemGroups[ref['@ItemGroupOID']];
                });
                forms[form['@OID']] = form;
            }, errors);
        })
        .error(function(e) {
            logError('Unable to load any Common Events.', e);
        }),

        $.get('pages/api/studies/${study.oc_oid}/pages/view%20subject', function(pageJson) {
            foreach(pageJson.components, function(component) {
                columns[component.name] = component.columns;
            }, errors);
        })
        .error(function(e) {
            logError('Unable to load Components data.', e);
        })

    ).then(function() {
        if (!metadata)
            return;

        var numVisitBased = 0;
        foreach(metadata.StudyEventDef, function(studyEvent) {
            studyEvents[studyEvent['@OID']] = studyEvent;
            
            var eventType = studyEvent['@OpenClinica:EventType'];
            if (eventType === 'Common') {
                if (studyEvent['@OpenClinica:Status'] !== 'DELETED')
                    studyEvent.showMe = true;
                else {
                    studyEventOid = studyEvent['@OID'];
                    $.ajax({
                        type: "GET",
                        url: 'rest/clinicaldata/json/stats/${study.oc_oid}/${studySub.oid}/' + studyEventOid,
                        async: false,
                        success: function(statData) {
                            var stats = statData;
                            if (stats.body.matchingForms > 0) {
                                studyEvent.showMe = true;
                            }
                        }
                    });
                }
            }
            else if (eventType === 'Unscheduled')
                numVisitBased++;

            studyEvent.forms = {};
            foreach(studyEvent.FormRef, function(ref) {
                var studyEventOid = studyEvent['@OID'];
                var formOid = ref['@FormOID'];
                var form = forms[formOid];
                var formStatus = ref['@OpenClinica:Status'];
                var formNotArchived = formStatus !== 'DELETED' && formStatus !== 'AUTO_DELETED';
                var columnTitles = [];
                var submissionFields = {};
                var componentOid = studyEventOid + '.' + formOid;
                var components = columns[componentOid];
                foreach(components, function(col) {
                    var item = items[col];
                    if (item) {
                        if (item['@BriefDescription']) {
                            columnTitles.push(item['@BriefDescription']);
                        } else if (item.Question) {
                            columnTitles.push(item.Question.TranslatedText);
                        }
                        else {
                            columnTitles.push(item['@Name']);
                        }
                    }
                    else {
                        columnTitles.push('!?' + col);
                        logError('Unable to reference Common Event Form Item: ' + col, items);
                    }
                    submissionFields[col] = [];
                }, errors);

                studyEvent.forms[formOid] = $.extend({
                    columnTitles: columnTitles,
                    submissionFields: submissionFields,
                    submissions: [],
                    addNew: false,
                    showMe: false
                }, form);
            }, errors);
        }, errors);

        if (numVisitBased && $('#subjectEvents').hasClass('hide')) {
            showSection(1, '#subjectEvents');            
        }

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

        var hideStatus = $('#oc-status-hide').val();
        var sectionTmpl = Handlebars.compile($('#section-tmpl').html());
        var sectionBodyTmpl = Handlebars.compile($('#section-body-tmpl').html());
        var sectionIndex = 2; // Section 0 = General Information, 1 = Visits
        for (var studyEventId in studyEvents) {
            var studyEvent = studyEvents[studyEventId];
            if (!studyEvent)
                continue;

            if (studyEvent['@OpenClinica:EventType'] === 'Common') {
                if (studyEvent.showMe) {
                    $('#commonEvents').append(sectionTmpl({
                        sectionNumber: sectionIndex,
                        studyEvent: studyEvent
                    }));
                    showSection(sectionIndex, '#common-event-' + sectionIndex);
                    sectionIndex++;
                }
            }
        }

        $.fn.dataTable.moment('DD-MMM-YYYY');
        function datatablefy($tables) {
            $tables.each(function() {
                var table = $(this);
                var subsection = table.closest('.subsection');
                var id = subsection.attr('id');
                var datatable = table.DataTable({
                    stateSave: true,
                    stateSaveCallback: function(settings, state) {
                        store(function(data) {
                            data.datatables[id] = state;
                        });
                    },
                    stateLoadCallback: function(settings, callback) {
                        var data = store.data.datatables[id];
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
        }).on('uncollapse', '.section', function() {
            var sectionDiv = $(this);
            var studyEventOid = sectionDiv.data('section-oid');
            var studyEvent = studyEvents[studyEventOid];
            var sectionErrors = [];
            $.get('rest/clinicaldata/json/view/${study.oc_oid}/${studySub.oid}/' + studyEventOid + '/*?showArchived=y&includeMetadata=n&links=y', function(data) {
                var odm = data;
                for (var formOid in studyEvent.forms) {
                    var form = studyEvent.forms[formOid];
                    form.submissions = [];
                }

                foreach(collection(odm.ClinicalData.SubjectData, 'OpenClinica:Links', 'OpenClinica:Link'), function(link) {
                    if (link['@rel'] !== 'common-add-new')
                        return;

                    var refs = link['@tag'].split('.');
                    var studyEventRef = refs[0];
                    var formRef = refs[1];

                    if (studyEventRef !== studyEventOid)
                        return;

                    var form = studyEvent.forms[formRef];
                    if (form) {
                        if (odm.ClinicalData.SubjectData['@OpenClinica:Status'] === 'Available')
                            form.addNew = link['@href'];
                        form.showMe = studyEvent.showMe = true;                        
                    }
                    else {
                        logError('Unable to reference Common Event Form: ' + formRef, formRef, studyEvent);
                    }
                }, sectionErrors);

                foreach(odm.ClinicalData.SubjectData.StudyEventData, function(studyEventData) {
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
                    foreach(formData.ItemGroupData, function(igd) {
                        foreach(igd.ItemData, function(itemData) {
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
                        }, sectionErrors);
                    }, sectionErrors);

                    form.submissions.push(submission);
                    form.showMe = true;
                    studyEvent.showMe = true;
                }, sectionErrors);

                var sectionBody = $(sectionBodyTmpl({
                    studyEvent: studyEvent,
                    sectionErrors: sectionErrors
                }));
                sectionDiv.children('.section-body').empty().append(sectionBody);
                setTimeout(function() {
                    datatablefy(sectionBody.find('table.datatable'));
                }, 1);
            });
        }).children('.expanded').trigger('uncollapse');

        $('div.section.collapsed').children('.section-body').hide();
    })
    .always(function() {
        if (errors.length) {
            $('#loading').html('<h1>ERROR</h1><ul>' + errors.map(function(err) {
                return '<li>' + err + '</li>';
            }).join('') + '</ul>').show();
        }
        else {
            $('#loading').hide();
        }

    });
});
</script>
