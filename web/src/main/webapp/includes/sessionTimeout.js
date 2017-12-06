function isSessionTimedOut(currentURL, setStorageFlag) {
    var storage = new CrossStorageClient(crossStorageURL);


    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    var key = "OCAppTimeout-" + userName;
    storage.onConnect()
        .then(function() {
            console.log("After connecting crossStorage");
            return storage.get(key);
        }).then(function(res) {
            console.log(res);
            if (res == null) {
                if (setStorageFlag)
                    storage.set(key, newExpiration);
                console.log("no value for " + key + " found");
            } else {
                var existingTimeout = res;
                if (currentTime > existingTimeout) {
                    storage.del(key);

                    // expired
                    var bridgeTimeoutReturnExp = "; expires=" + moment().add(7, 'days').toDate();
                    // create a return cookie
                    document.cookie = "bridgeTimeoutReturn-" + userName + "=" + currentURL + bridgeTimeoutReturnExp + "; path=/";
                    console.log("currentTime: " + currentTime + " > existingTimeout: " + existingTimeout + " returning to Login screen");
                    console.log("navBar:" + myContextPath + '/pages/logout');
                    window.location.replace (myContextPath + '/pages/logout');
                } else {
                    console.log("currentTime: " + currentTime + " <= existingTimeout: " + existingTimeout);
                    if (setStorageFlag)
                        storage.set(key, newExpiration);
                }
            }
        })['catch'](function(err) {
        console.log(err);
    });
}