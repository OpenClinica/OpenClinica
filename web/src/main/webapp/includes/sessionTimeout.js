function setCurrentUser(thisUser) {
    storage.onConnect()
        .then(function() {
            storage.set(currentUser, thisUser);
        })['catch'](function(err) {
        console.log(err);
    });

}

function processCurrentUser(newExpiration) {
    storage.onConnect()
    .then(function() {
        if (firstLoginCheck === "true") {
            console.log("***********returning null");
            storage.set(currentUser, userName);
            storage.set(ocAppTimeoutKey, newExpiration);
            return "-1";
        } else {
            return storage.get(currentUser);
        }
    }).then(function(res) {
        console.log("Result:" + res);
        if (res === null) {
            console.log("result is null");
            storage.set(currentUser, userName);
        } else if (res === "-1") {
            firstLoginCheck = false;
            // set the backend
            window.location.replace (myContextPath + '/pages/resetFirstLogin?redirectURL='+ encodeURI(currentURL));
        } else if (res === "") {
            storage.del(ocAppTimeoutKey);
            console.log(" returning to Login screen");
            window.location.replace (myContextPath + '/pages/logout');
        } else {
            var thisUser = res;
            console.log("In processcurrentUser:" + thisUser);
            if (thisUser !== userName) {
                console.log("another user: " +
                    +" currently loggedIn: " + thisUser + "New user is:" + userName);
                window.location.replace (myContextPath + '/pages/invalidateAuth0Token');

            }
        }
    })['catch'](function(err) {
        console.log(err);
    });
}

function updateOCAppTimeout() {
    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    storage.onConnect()
        .then(function() {
            storage.set(ocAppTimeoutKey, newExpiration);
            var login = storage.get(currentUser);
            if (login === null) {
                storage.set(currentUser, userName);
            }
        })['catch'](function(err) {
        console.log(err);
    });
}
function isSessionTimedOut(setStorageFlag) {
    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    //console.log("setStorageFlag:" + setStorageFlag);
    processCurrentUser(newExpiration);
    var currentTime = moment().valueOf();
    storage.onConnect()
        .then(function() {
            return storage.get(ocAppTimeoutKey);
        }).then(function(res) {
        if (res == null) {
            //console.log("*****setting new expiration1:" + newExpiration);
            storage.set(ocAppTimeoutKey, newExpiration);
        } else {
            var existingTimeout = res;
            //console.log("currentTime: " + currentTime + " existingTimeout: " + existingTimeout);
            if (currentTime > existingTimeout) {
                storage.del(ocAppTimeoutKey);
                storage.set(currentUser, "");
                console.log("currentTime: " + currentTime + " > existingTimeout: " + existingTimeout + " returning to Login screen");
                window.location.replace (myContextPath + '/pages/logout');
            } else {
                if (setStorageFlag) {
                    console.log("setStorageFlag is true: setting newExpiration:" + newExpiration);
                    storage.set(ocAppTimeoutKey, newExpiration);
                }
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
