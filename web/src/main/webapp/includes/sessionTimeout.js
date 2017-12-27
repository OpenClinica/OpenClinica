
function createReturnLogoutCookie(currentURL) {
// expired
    var bridgeTimeoutReturnExp = "; expires=" + moment().add(14, 'days').toDate();
    // create a return cookie
    if (currentURL.indexOf("/pages/logout") == -1) {
        document.cookie = "bridgeTimeoutReturn-" + userName + "=" + currentURL + bridgeTimeoutReturnExp + "; path=/";
    }
}

function isSessionTimedOut(currentURL, setStorageFlag) {
    processLoggedOutKey(true);
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

function setLoggedOutFlag(flagValue) {
    var storage = new CrossStorageClient(crossStorageURL);
    storage.onConnect()
        .then(function () {
            console.log("setting loggedOut to " + flagValue + "(((((");
            storage.set("loggedOut", flagValue);
        }).then(function(res) {
    })['catch'](function(err) {
        console.log(err);
    });


}

function processLoggedOutKey(processLogout) {
    var storage = new CrossStorageClient(crossStorageURL);
    console.log("processLoggedOutKey called:" + currentURL);
    console.log("firstLoginCheck*******" + firstLoginCheck);
    console.log("processLogout:" + processLogout);
    storage.onConnect()
        .then(function () {
            var isLoggedOut = storage.get("loggedOut");
            console.log("isLoggedOut:" + isLoggedOut);
            return isLoggedOut;
        }).then(function(res) {
            console.log("loggedOut:" + res);
            if (res === null) {
                console.log("no value for loggedOut found");
            } else if (res === "true") {
                if (firstLoginCheck == "true") {
                    console.log("Setting loggedOut to false");
                    setLoggedOutFlag("false");
                    firstLoginCheck = "false";
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

function invalidateToken() {

    jQuery.get(myContextPath + '/pages/invalidateAuth0Token')
        .error(function(jqXHR, textStatus, errorThrown) {
            "Error calling :" + myContextPath + '/pages/invalidateAuth0Token' + " " + textStatus + " " + errorThrown
        });

    return null;
}