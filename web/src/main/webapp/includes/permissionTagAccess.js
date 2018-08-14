$(document).ready(function() {
    $('.accessCheck').unbind().click(function (event) {
        var href = $(this).attr('href');
        var ecId = getParameterByName("eventCrfId", href);
        var formLayoutId = getParameterByName("formLayoutId", href);
        var studyEventId = getParameterByName("studyEventId", href);
        console.log("ecId:" + ecId);
        event.preventDefault();
        validateResourceAccess(ecId, formLayoutId, studyEventId).done(function(data){
            if (data.status == true) {
                console.log("In promise:" + data.status);
                location.href = href;
            } else {
                alert("You don't have permission to perform this action. Please contact your administrator if you think you have received this message in error.");
            }
        });
    });
});
function getParameterByName(name, url) {
    if (!url) url = window.location.href;
    name = name.replace(/[\[\]]/g, '\\$&');
    var regex = new RegExp('[?&]' + name + '(=([^&#]*)|&|#|$)'),
        results = regex.exec(url);
    if (!results) return null;
    if (!results[2]) return '';
    return decodeURIComponent(results[2].replace(/\+/g, ' '));
}

function validateResourceAccess(eventCrfId, formLayoutId, studyEventId){
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
