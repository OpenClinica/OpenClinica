
function isSessionTimedOut(currentURL, setStorageFlag, invalidateFlag) {
    processLoggedOutKey(invalidateFlag);
    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    storage.onConnect()
        .then(function() {
            return storage.get(ocAppTimeoutKey);
        }).then(function(res) {
        if (res == null) {
            storage.set(ocAppTimeoutKey, newExpiration);
        } else {
            var existingTimeout = res;
            if (currentTime > existingTimeout) {
                storage.del(ocAppTimeoutKey);
                setLoggedOutFlag();
                console.log("currentTime: " + currentTime + " > existingTimeout: " + existingTimeout + " returning to Login screen");
                window.location.replace (myContextPath + '/pages/logout');
            } else {
                if (setStorageFlag)
                    storage.set(ocAppTimeoutKey, newExpiration);
            }
        }
    })['catch'](function(err) {
        console.log(err);
    });
}
function deleteOCAppTimeout() {
    storage.onConnect()
        .then(function () {
            console.log("Deleting crossStorage key");
            storage.del(ocAppTimeoutKey);
        })['catch'](function (err) {
        console.log(err);
    });

}

function setLoggedOutFlag() {
    storage.onConnect()
        .then(function () {
            console.log("setting loggedOut to " + appName + "(((((");
            storage.set(logoutByKey, appName);
        }).then(function(res) {
    })['catch'](function(err) {
        console.log(err);
    });


}

function processLoggedOutKey(invalidateFlag) {
    console.log("In processLoggedOutKey");
    storage.onConnect()
        .then(function () {
            var isLoggedOut = storage.get(logoutByKey);
            console.log("isLoggedOut:" + logoutByKey + " is:" + isLoggedOut);
            return isLoggedOut;
        }).then(function(res) {
            console.log("loggedOut:" + res);
            if (res === null) {
                console.log("no value for loggedOut found");
            } else {
                if (firstLoginCheck == "true") {
                    console.log("Firstlogincheck is true");
                    storage.del(logoutByKey);
                    firstLoginCheck = "false";
                } else {
                    console.log("Firstlogincheck is false");
                    console.log("Current URL&&&&&&&&&" + currentURL + "invalidateFlag " + invalidateFlag);
                    if (invalidateFlag) {
                        console.log("************userName:" + userName);
                        if (logoutByKey.startsWith(userName + "-")) {
                            console.log("backend invalidateAuth0Token");
                            window.location.replace (myContextPath + '/pages/invalidateAuth0Token');
                        } else {
                            jQuery.get(myContextPath + '/pages/invalidateAuth0Token')
                                .error(function(jqXHR, textStatus, errorThrown) {
                                    "Error calling :" + myContextPath + '/pages/invalidateAuth0Token' + " " + textStatus + " " + errorThrown
                                });
                            return null;
                        }

                    }
                }
            }
        })['catch'](function(err) {
            console.log(err);
        });
}