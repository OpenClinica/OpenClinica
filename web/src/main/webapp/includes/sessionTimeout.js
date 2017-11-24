function isSessionTimedOut(currentURL) {
    var storage = new CrossStorageClient(crossStorageURL);


    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    var key = "OCAppTimeout-" + userName;
    storage.onConnect()
        .then(function() {
            return storage.get(key);
        }).then(function(res) {
            console.log(res);
            if (res == null) {
                storage.set(key, newExpiration + ";" + currentURL);
                return false;
            } else {
                var values = res.split(';');
                var existingTimeout = values[0];
                if (currentTime > existingTimeout) {
                    // expired
                    var bridgeTimeoutReturnExp = "; expires=" + moment().add(7, 'days').toDate();
                    storage.del(key);
                    // create a return cookie
                    document.cookie = "bridgeTimeoutReturn-" + userName + "=" + values[1] + bridgeTimeoutReturnExp + "; path=/";
                    return true;
                } else {
                    storage.set(key, newExpiration + ";" + currentURL);
                    return false;
                }
            }
        })['catch'](function(err) {
        console.log(err);
    });
}