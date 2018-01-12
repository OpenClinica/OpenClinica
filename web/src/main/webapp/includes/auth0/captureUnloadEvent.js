/**
 * Created by yogi on 10/13/17.
 */

$(window).on('mouseout', (function () {
    window.onbeforeunload = checkSessionTimeout;
}));
function checkSessionTimeout() {
    //isSessionTimedOut(currentURL, false, false);
}
var prevKey = "";
$(document).keydown(function (e) {
    if (e.key == "F5") {
        window.onbeforeunload = checkSessionTimeout;
    }
    else if (e.key.toUpperCase() == "W" && prevKey == "CONTROL") {
        window.onbeforeunload = checkSessionTimeout;
    }
    else if (e.key.toUpperCase() == "R" && prevKey == "CONTROL") {
        window.onbeforeunload = checkSessionTimeout;
    }
    else if (e.key.toUpperCase() == "F4" && (prevKey == "ALT" || prevKey == "CONTROL")) {
        window.onbeforeunload = checkSessionTimeout;
    }
    prevKey = e.key.toUpperCase();
});