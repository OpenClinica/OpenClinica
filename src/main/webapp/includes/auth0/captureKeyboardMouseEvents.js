/**
 * Created by yogi on 10/13/17.
 */

function checkSessionTimeout() {
    updateOCAppTimeout();
}
window.addEventListener("keydown", checkSessionTimeout);
window.addEventListener("mousedown", checkSessionTimeout);