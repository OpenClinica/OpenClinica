function isSessionTimedOut(currentURL) {
    var storage = new CrossStorageClient(crossStorageURL);


    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    var key = "OCAppTimeout-" + userName;
    storage.onConnect()
        .then(function() {
            console.log("in first function****");
            return storage.get(key);
        }).then(function(res) {
            console.log(res);
            if (res == null) {
                storage.set(key, newExpiration);
                console.log("no value for " + key + "found");
                return false;
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
                    console.log("currentTime > existingTimeout: returning to Login screen");
                    console.log("navBar:" + myContextPath + '/pages/logout');
                    window.location.replace (myContextPath + '/pages/logout');
                    return true;
                } else {
                    storage.set(key, newExpiration);
                    console.log("currentTime <= existingTimeout");
                    return false;
                }
            }
        })['catch'](function(err) {
        console.log(err);
    });
}