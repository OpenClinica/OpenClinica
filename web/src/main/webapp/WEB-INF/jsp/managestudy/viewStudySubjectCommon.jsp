<style>
    .subsection {
        margin-top: 25px;
        margin-bottom: 75px;
        font-size: .85rem;
    }
    table.datatable {
        border-bottom: none !important;
        border-collapse: collapse !important;
        margin-top: 2px !important;
    }
    .dataTables_info {
        padding-top: 0.5em !important;
    }
    .dataTables_length {
        padding-top: 0.5em;
        padding-left: 1.5em;
    }
    .datatable td {
        border: 1px solid #ccc;
        border-bottom-color: #ccc !important;
    }
    .datatable thead td {
        border-color: white !important;
        border-top-color: #ccc !important;
        background-color: #ccc !important;
    }
    .datatable thead td:first-child {
        border-left-color: #ccc !important;
    }
    .datatable thead td:last-child {
        border-right-color: #ccc !important;
    }
    td.actions {
        padding: 3.4px !important;
        vertical-align: middle;
    }
    td.actions td {
        padding: 3.4px !important;
        border: none;
    }
    tr.submission:hover, td.highlight {
        background-color: whitesmoke !important;
    }
    .form-name {
        display: inline;
        margin-right: 10px;
    }
    input[type=button][disabled] {
        display: none;
    }
    .add-new {
        height: 22px;
        margin-top: 3px !important;
        padding: 3px 9px !important;
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
}
</style>

<div id="commonEvents"></div>

<link rel="stylesheet" type="text/css" href="https://cdn.datatables.net/1.10.16/css/jquery.dataTables.min.css"/>
<script type="text/JavaScript" language="JavaScript" src="https://cdn.datatables.net/1.10.16/js/jquery.dataTables.min.js"></script>
<script type="text/JavaScript" language="JavaScript" src="https://cdnjs.cloudflare.com/ajax/libs/handlebars.js/4.0.11/handlebars.js"></script>
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
    <div class="section expanded" data-study-event-oid="{{studyEventOid}}">
        <div class="section-header">
            {{sectionName}}
        </div>
        <div class="section-body">
            {{#each forms as |form|}}
                <div class="subsection">
                    <input type="button" class="add-new" value="Add New" 
                        data-form-oid="{{form.[@OID]}}" 
                        {{#if form.disableAddNew}}disabled="disabled"{{/if}}>
                    <h3 class="form-name">{{form.[@Name]}}</h3>
                    <table class="datatable">
                    <thead>
                        <tr>
                            {{#each form.itemGroups as |itemGroup|}}
                                {{#each itemGroup.items as |item|}}
                                    <td>{{truncate item.Question.TranslatedText 30}}</td>
                                {{/each}}
                            {{/each}}
                            <td>
                                <center>Status</center>
                            </td>
                            <td>
                                <center>Last Update</center>
                            </td>
                            <td>
                                <center>Updated By</center>
                            </td>
                            <td>
                                <center>Actions</center>
                            </td>
                            <td></td>
                        </tr>
                    </thead>
                    <tbody>
                        {{#each form.submissions as |submission|}}
                            <tr class="submission">
                                {{#each submission.data as |data|}}
                                    <td data-search="{{data}}">{{truncate data 200}}</td>
                                {{/each}}
                                <td>{{submission.studyStatus}}</td>
                                <td>{{submission.updatedDate}}</td>
                                <td>{{submission.updatedBy}}</td>
                                <td class="actions">
                                    <table>
                                        <tbody>
                                            <tr>
                                                {{#each submission.links as |link|}}
                                                <td>
                                                    <a href="${pageContext.request.contextPath}{{link.[@href]}}">
                                                        <span class="icon icon-{{link.[@rel]}}" alt="{{link.[@rel]}}" title="{{link.[@rel]}}"></span>
                                                    </a>
                                                </td>
                                                {{/each}}
                                            </tr>
                                        </tbody>
                                    </table>
                                </td>
                                <td>{{submission.hideStatus}}</td>
                            </tr>
                        {{/each}}
                    </tbody>
                    </table>
                </div>
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
    $.get('rest/clinicaldata/json/view/${study.oid}/${studySub.oid}/*/*?showArchived=y', function(data) {
        var numVisitBaseds = 0;
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
                form.disableAddNew = false;
                return form;
            });
            studyEvents[studyEvent['@OID']] = studyEvent;
        });

        collection(data.ClinicalData.SubjectData.StudyEventData).forEach(function(studyEventData) {
            var formData = studyEventData.FormData;
            if (!formData)
                return;

            var form = forms[formData['@FormOID']];
            if (!form)
                return;

            var studyEvent = studyEvents[studyEventData['@StudyEventOID']];
            if (studyEvent['@OpenClinica:EventType'] !== 'Common')
                return;

            if (studyEvent['@Repeating'] === 'No')
                form.disableAddNew = true;

            var links = [];
            $.merge(links, collection(studyEventData['OpenClinica:links']['OpenClinica:link']));
            $.merge(links, collection(formData['OpenClinica:links']['OpenClinica:link']));
            var order = ['edit', 'view', 'remove', 'restore', 'reassign', 'sign'];
            links.sort(function(a, b) {
                return order.indexOf(a['@rel']) - order.indexOf(b['@rel']);
            });


            var submission = {
                studyStatus: studyEventData['@OpenClinica:Status'],
                formStatus: formData['@OpenClinica:Status'],
                hideStatus: formData['@OpenClinica:Status'] === 'invalid' ? 'oc-status-removed' : 'oc-status-active',
                updatedDate: formData['@OpenClinica:UpdatedDate'].split(' ')[0],
                updatedBy: formData['@OpenClinica:UpdatedBy'],
                data: $.extend(true, {}, form.submissionObj),
                links: links
            };
            collection(formData.ItemGroupData).forEach(function(igd) {
                collection(igd.ItemData).forEach(function(item) {
                    submission.data[item['@ItemOID']].push(item['@Value']);
                });
            });
            form.submissions.push(submission);
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
        });

        var hideStatus = $('#oc-status-hide').val();
        var sectionTable = $('#commonEvents');
        var sectionTmpl = Handlebars.compile($('#section-tmpl').html());
        for (var studyEventId in studyEvents) {
            var studyEvent = studyEvents[studyEventId];
            if (studyEvent['@OpenClinica:EventType'] === 'Common') {
                var status = studyEvent['@OpenClinica:Status'] === 'AVAILABLE' ? 'oc-status-active' : 'oc-status-removed';
                var display = status === hideStatus ? 'display:none;' : '';
                sectionTable.append(sectionTmpl({
                    sectionName: studyEvent['@Name'],
                    sectionStatus: status,
                    sectionDisplay: display,
                    studyEventOid: studyEventId,
                    forms: studyEvent.forms
                }));
            }
            else {
                numVisitBaseds++;
            }
        }
        sectionTable.on('click', '.section-header', function() {
            $(this).next().addBack().toggleClass('collapsed expanded');
        });
        sectionTable.on('click', '.add-new', function() {
            var btn = $(this);
            var formOid = btn.data('form-oid');
            var studyEventOid = btn.closest('.section').data('study-event-oid');
            console.log(studyOid, studyEventOid, studySubjectOid, formOid);
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

        if (numVisitBaseds) {
            $('#subjectEvents').removeClass('hide');
        }

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
                    visible: false
                }]
            });
            $(this).children('tbody').on('mouseenter', 'td', function () {
                var colIdx = table.cell(this).index().column;
                $(table.cells().nodes()).removeClass('highlight');
                $(table.column(colIdx).nodes()).addClass('highlight');
            });
        });
        datatables.each(function() {
            var table = $(this);
            var header = table.parent();
            var paging = table.next();
            var pagesize = paging.next().children().contents();
            header.prevUntil().prependTo(header);
            paging.text(paging.text().replace('Showing', 'Results').replace(' to ', '-').replace('entries', ''));
            pagesize[2].replaceWith(' per page');
            table.css('width', '');
        });
        datatables.parent().css({
            'max-width': $(window).width() - 200 + 'px',
            'overflow': 'scroll'
        });        

        $('#loading').remove();
    });
});
</script>
