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
            jQuery.get(myContextPath + '/pages/resetFirstLogin')
                .error(function(jqXHR, textStatus, errorThrown) {
                    "Error calling :" + myContextPath + '/pages/resetFirstLogin' + " " + textStatus + " " + errorThrown
                });
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
    isSessionTimedOut(false, true);
}
function isSessionTimedOut(checkCurrentUser, storageFlag) {
    var newExpiration = moment().add(sessionTimeout, 's').valueOf();
    if (checkCurrentUser) {
        processCurrentUser(newExpiration);
    }

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
                if (storageFlag) {
                    console.log("setting newExpiration:" + newExpiration);
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
