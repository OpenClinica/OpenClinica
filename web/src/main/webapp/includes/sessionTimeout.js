function isSessionTimedOut(currentURL) {
    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    var key = "bridgeTimeout-" + userName;
    var existingTimeoutStr = localStorage.getItem(key);
    if (existingTimeoutStr == null) {
        localStorage.setItem(key, newExpiration + ";" + currentURL);
        return false;
    } else {
        var values = existingTimeoutStr.split(';');
        var existingTimeout = values[0];
        if (currentTime > existingTimeout) {
            // expired
            var bridgeTimeoutReturnExp = "; expires=" + moment().add(7, 'days').toDate();
            localStorage.removeItem(key);
            // create a return cookie
            document.cookie = "bridgeTimeoutReturn-" + userName + "=" + values[1] + bridgeTimeoutReturnExp + "; path=/";
            return true;
        } else {
            localStorage.setItem(key, newExpiration + ";" + currentURL);
            return false;
        }
    }

}