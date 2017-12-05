function isSessionTimedOut(currentURL, setStorageFlag) {
    var storage = new CrossStorageClient(crossStorageURL);


    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    var key = "OCAppTimeout-" + userName;
    storage.onConnect()
        .then(function() {
            return storage.get(key);
        }).then(function(res) {
            if (res == null) {
                if (setStorageFlag)
                    storage.set(key, newExpiration);
            } else {

                var existingTimeout = res;
                if (currentTime > existingTimeout) {
                    storage.del(key);
/*
                    // expired
                    var bridgeTimeoutReturnExp = "; expires=" + moment().add(7, 'days').toDate();
                    // create a return cookie
                    document.cookie = "bridgeTimeoutReturn-" + userName + "=" + values[1] + bridgeTimeoutReturnExp + "; path=/";
  */
                    window.location.replace (myContextPath + '/pages/logout');
                } else {
                    if (setStorageFlag)
                        storage.set(key, newExpiration);
                }
            }
        })['catch'](function(err) {
    });
}