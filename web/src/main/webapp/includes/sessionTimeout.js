function isSessionTimedOut() {
    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    var existingTimeout = localStorage.getItem("bridgeTimeout");
    if (existingTimeout == null) {
        localStorage.setItem("bridgeTimeout", newExpiration);
        return false;
    } else {
        if (currentTime > existingTimeout) {
            // expired
            localStorage.removeItem("bridgeTimeout");
            return true;
        } else {
            localStorage.setItem("bridgeTimeout", newExpiration);
            return false;
        }
    }

}