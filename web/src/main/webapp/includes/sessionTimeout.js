
function createReturnLogoutCookie(currentURL) {
// expired
    var bridgeTimeoutReturnExp = "; expires=" + moment().add(14, 'days').toDate();
    // create a return cookie
    if (currentURL.indexOf("/pages/logout") == -1) {
        document.cookie = "bridgeTimeoutReturn-" + userName + "=" + currentURL + bridgeTimeoutReturnExp + "; path=/";
    }
}

function isSessionTimedOut(currentURL, setStorageFlag) {
    processLoggedOutKey(true, false);
    var storage = new CrossStorageClient(crossStorageURL);
    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    storage.onConnect()
        .then(function() {
            console.log("After connecting crossStorage");
            return storage.get(ocAppTimeoutKey);
        }).then(function(res) {
        console.log(res);
        if (res == null) {
            storage.set(ocAppTimeoutKey, newExpiration);
            console.log("no value for " + ocAppTimeoutKey + " found");
        } else {
            var existingTimeout = res;
            if (currentTime > existingTimeout) {
                storage.del(ocAppTimeoutKey);

                createReturnLogoutCookie(currentURL);
                console.log("currentTime: " + currentTime + " > existingTimeout: " + existingTimeout + " returning to Login screen");
                console.log("navBar:" + myContextPath + '/pages/logout');
                window.location.replace (myContextPath + '/pages/logout');
            } else {
                console.log("currentTime: " + currentTime + " <= existingTimeout: " + existingTimeout);
                if (setStorageFlag)
                    storage.set(ocAppTimeoutKey, newExpiration);
            }
        }
    })['catch'](function(err) {
        console.log(err);
    });
}
function deleteOCAppTimeout() {
    var storage = new CrossStorageClient(crossStorageURL);
    storage.onConnect()
        .then(function () {
            console.log("Deleting crossStorage key");
            storage.del(ocAppTimeoutKey);
        })['catch'](function (err) {
        console.log(err);
    });

}
function processLoggedOutKey(processLogout, setLoggedOutFlag) {
    var storage = new CrossStorageClient(crossStorageURL);
    console.log("processLoggedOutKey called:" + currentURL);
    console.log("firstLoginCheck*******" + firstLoginCheck);
    storage.onConnect()
        .then(function () {
            if (setLoggedOutFlag) {
                storage.set("loggedOut", "true");
                console.log("setting loggedOut to true(((((");
            }
            console.log("********************" + storage.get("loggedOut"));
            return storage.get("loggedOut");
        }).then(function(res) {
            console.log(res);
            if (res == null) {
                console.log("no value for loggedOut found");
            } else if (res == "true") {
                if (firstLoginCheck == true) {
                    console.log("Setting loggedOut to false");
                    storage.set("loggedOut", "false");
                } else {
                    console.log("Setting the cookie");
                    createReturnLogoutCookie(currentURL);
                    if (processLogout) {
                        console.log("navBar:" + myContextPath + '/pages/logout');
                        window.location.replace (myContextPath + '/pages/logout');
                    }
                }
            }
        })['catch'](function(err) {
            console.log(err);
        });
}