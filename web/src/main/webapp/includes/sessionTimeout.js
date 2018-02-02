var dupeFirstUserCheck;
function setCurrentUser(thisUser) {
    storage.onConnect()
        .then(function() {
            return storage.set(currentUser, thisUser);
        })['catch'](function(err) {
        console.log(err);
    });

}
function getPromise(value) {
    // Promise
    var returnPromise = new Promise(
        function (resolve, reject) {
            if (value != null) {
                resolve(value); // fulfilled
            } else {
                var reason = new Error('test');
                reject(reason); // reject
            }

        }
    );
    return returnPromise;
}

function processCurrentUser(currentTime, newExpiration) {
    storage.onConnect()
    .then(function() {
        return storage.get(currentUser);
    }).then(function(res) {
        dupeFirstUserCheck = firstLoginCheck;
        if (firstLoginCheck === "true") {
            var prevUser = res;
            console.log("prevUser in firstLoginCheck:" + prevUser);
            if (prevUser !== "") {
                console.log("prevUser is not blank");
                storage.get(ocAppTimeoutKey).then(function(res) {
                    // the user closed the browser session before the session timeout and reopened it after the timeout
                    // exceeded
                    var prevTimeout = res;
                    console.log("prevTimeout:processCurrentUser:" + prevTimeout);
                    if (prevTimeout < currentTime) {
                        console.log("prevTimeout is less than currentTime");
                        storage.set(currentUser, "").then(function() {
                            processUserData(getPromise(""));
                        });
                    } else {
                            console.log("prevTimeout is greater than currentTime");
                            storage.set(currentUser, userName).then(function() {
                            storage.set(ocAppTimeoutKey, newExpiration).then(function() {
                                processUserData(getPromise("-1"));
                            });
                        }) ['catch'](function (err) {
                            console.log(err);
                        });
                    }
                });
            } else {
                storage.set(currentUser, userName).then(function() {
                    console.log("current user:" + userName
                        + " setting new expiration:" + newExpiration);
                    storage.set(ocAppTimeoutKey, newExpiration).then(function() {
                        processUserData(getPromise("-1"));
                    });
                }) ['catch'](function (err) {
                    console.log(err);
                });
            }
        } else {
            storage.get(currentUser).then(function(res1) {
                processUserData(getPromise(res1));
            });
        }
    })['catch'](function(err) {
        console.log(err);
    });
}

function processUserData(inputPromise) {
    inputPromise.then(function(res) {
        console.log("Result:" + res);
        if (res === null) {
            console.log("result is null");
            return storage.set(currentUser, userName);
        } else if (res === "") {
            if (firstLoginCheck === "true") {
                firstLoginCheck = false;
                jQuery.get(myContextPath + '/pages/resetFirstLogin')
                    .error(function(jqXHR, textStatus, errorThrown) {
                        "Error calling :" + myContextPath + '/pages/resetFirstLogin' + " " + textStatus + " " + errorThrown
                    });
            } else {
                console.log(" returning to Login screen");
                window.location.replace(myContextPath + '/pages/logout');
            }
        } else if (res === "-1") {
            firstLoginCheck = false;
            storage.set(currentUser, userName).then(function() {
                jQuery.get(myContextPath + '/pages/resetFirstLogin')
                    .error(function (jqXHR, textStatus, errorThrown) {
                        "Error calling :" + myContextPath + '/pages/resetFirstLogin' + " " + textStatus + " " + errorThrown
                    });
            });
        } else {
            var thisUser = res;
            console.log("In processcurrentUser:" + thisUser);
            if (thisUser !== userName) {
                console.log("another user:" + thisUser + "New user is:" + userName);
                window.location.replace (myContextPath + '/pages/invalidateAuth0Token');

            }
        }
    }).then(function(res) {
        console.log("set all storage processCurrentUser");
    })
}
function updateOCAppTimeout() {
    processTimedOuts(false, true);
}
function processTimedOuts(checkCurrentUser, storageFlag) {
    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    var currentTime = moment().valueOf();
    if (checkCurrentUser) {
        processCurrentUser(currentTime, newExpiration);
    }


    storage.onConnect()
        .then(function() {
            return storage.get(ocAppTimeoutKey);
        }).then(function(res) {
            if (res == null) {
                //console.log("*****setting new expiration1:" + newExpiration);
                return storage.set(ocAppTimeoutKey, newExpiration);
            } else if (res){
                var existingTimeout = res;
                console.log("processTimedOuts: currentTime: " + currentTime + " existingTimeout: " + existingTimeout);
                if (currentTime > existingTimeout) {
                    storage.set(currentUser, "").then(function(res1) {
                        if (dupeFirstUserCheck !== "true" || !checkCurrentUser) {
                            console.log("currentTime: " + currentTime + " > existingTimeout: " + existingTimeout + " returning to Login screen");
                            window.location.replace(myContextPath + '/pages/logout');
                        }

                    }) ['catch'](function(err) {
                        console.log(err);
                    });
                } else {
                    if (storageFlag) {
                        console.log("setting newExpiration:" + newExpiration);
                        return storage.set(ocAppTimeoutKey, newExpiration);
                    }
                }
            }
    }).then(function(res) {
        console.log("set all storage");
    })['catch'](function(err) {
            console.log(err);
    });
}
