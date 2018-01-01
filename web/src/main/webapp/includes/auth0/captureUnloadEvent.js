/**
 * Created by yogi on 10/13/17.
 */
$(window).on('mouseout', (function () {
    window.onbeforeunload = ConfirmLeave;
}));
function ConfirmLeave() {


    jQuery.get(myContextPath + '/pages/invalidateAuth0Token')
        .error(function(jqXHR, textStatus, errorThrown) {
            "Error calling :" + myContextPath + '/pages/invalidateAuth0Token' + " " + textStatus + " " + errorThrown
        });

    return null;
}
var prevKey = "";
$(document).keydown(function (e) {
    if (e.key == "F5") {
        window.onbeforeunload = ConfirmLeave;
    }
    else if (e.key.toUpperCase() == "W" && prevKey == "CONTROL") {
        window.onbeforeunload = ConfirmLeave;
    }
    else if (e.key.toUpperCase() == "R" && prevKey == "CONTROL") {
        window.onbeforeunload = ConfirmLeave;
    }
    else if (e.key.toUpperCase() == "F4" && (prevKey == "ALT" || prevKey == "CONTROL")) {
        window.onbeforeunload = ConfirmLeave;
    }
    prevKey = e.key.toUpperCase();
});