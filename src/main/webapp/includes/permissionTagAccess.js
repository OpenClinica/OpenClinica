$(document).ready(function() {
    $('.accessCheck').unbind().click(function (event) {
        var btn = $(event.target).closest('.accessCheck');
        var ecId = getParameterByName("eventCrfId", btn);
        var formLayoutId = getParameterByName("formLayoutId", btn);
        var studyEventId = getParameterByName("studyEventId", btn);
        event.preventDefault();
        validateResourceAccess(ecId, formLayoutId, studyEventId).done(function(data){
            if (data.status == true) {
                location.href = btn.attr('href');
                var onclick = btn.data('onclick');
                if (onclick) {
                    onclick.call(this, event || window.event);
                }
            } else {
                alert("You don't have permission to perform this action. Please contact your administrator if you think you have received this message in error.");
            }
        });
    }).each(function() {
        if (this.onclick) {
            $(this).data('onclick', this.onclick);
            this.onclick = null;
        }
    });
});
function getParameterByName(name, btn) {
    var data = btn.data(name.toLowerCase());
    if (data) return data;
    var url = btn.attr('href') || window.location.href;
    name = name.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

function validateResourceAccess(eventCrfId, formLayoutId, studyEventId) {
    if (formLayoutId == null || formLayoutId == '')
        formLayoutId = 0;
    if (studyEventId == null || studyEventId == '')
        studyEventId = 0;
    return $.ajax({
        url: myContextPath + '/pages/checkAccess?eventCrfId=' + eventCrfId + "&formLayoutId=" + formLayoutId + "&studyEventId=" + studyEventId,
        type: 'GET',
        cache: false,
        success: function(data) {
            console.log('Success:' + data.status);
        },
        error: function(data) {
            console.log('Error:' + data.status);
        }
    });
}
