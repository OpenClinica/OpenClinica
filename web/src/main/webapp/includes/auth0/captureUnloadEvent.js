/**
 * Created by yogi on 10/13/17.
 */
$(window).on('mouseout', (function () {
    window.onbeforeunload = handleUnloadEvent;
}));
function handleUnloadEvent() {
   isSessionTimedOut(currentURL, true);
}
var prevKey = "";
$(document).keydown(function (e) {
    if (e.key == "F5") {
        window.onbeforeunload = handleUnloadEvent;
    }
    else if (e.key.toUpperCase() == "W" && prevKey == "CONTROL") {
        window.onbeforeunload = handleUnloadEvent;
    }
    else if (e.key.toUpperCase() == "R" && prevKey == "CONTROL") {
        window.onbeforeunload = handleUnloadEvent;
    }
    else if (e.key.toUpperCase() == "F4" && (prevKey == "ALT" || prevKey == "CONTROL")) {
        window.onbeforeunload = handleUnloadEvent;
    }
    prevKey = e.key.toUpperCase();
});