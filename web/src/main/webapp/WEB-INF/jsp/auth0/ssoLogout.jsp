<script src="//cdn.auth0.com/js/auth0/8.8/auth0.min.js"></script>
<script type="text/javascript">
    $(window).on('mouseover', (function () {
        window.onbeforeunload = null;
    }));
    $(window).on('mouseout', (function () {
        window.onbeforeunload = ConfirmLeave;
    }));
    function ConfirmLeave() {
        localStorage.removeItem('userToken');
        jQuery.get('${urlPrefix}pages/invalidateSession', function (data) {
        });
        return null;
    }
    var prevKey = "";
    $(document).keydown(function (e) {
        if (e.key == "F5") {
            window.onbeforeunload = ConfirmLeave;
        }
        else if (e.key.toUpperCase() == "W" && prevKey == "CONTROL") {
            window.onbeforeunload = ConfirmLeave;
        }
        else if (e.key.toUpperCase() == "R" && prevKey == "CONTROL") {
            window.onbeforeunload = ConfirmLeave;
        }
        else if (e.key.toUpperCase() == "F4" && (prevKey == "ALT" || prevKey == "CONTROL")) {
            window.onbeforeunload = ConfirmLeave;
        }
        prevKey = e.key.toUpperCase();
    });

    var auth = new auth0.Authentication({
        domain: localStorage.getItem("domain"),
        clientID: localStorage.getItem("clientID")
    });
    function getAuth0SSOData() {
        auth.getSSOData(function (err, data) {
            //alert("data.sso:" + data.sso);
            // if there is still a session, do nothing
            if (err || (data && data.sso)) {
                return;
            }
            // alert("Removing token");

            // if we get here, it means there is no session on Auth0,
            // then remove the token and redirect to #login
            localStorage.removeItem('userToken');
            window.location = '${urlPrefix}pages/logout?externalReturnUrl=' + window.location;

        });
    }
    getAuth0SSOData();
    setInterval(function () {
        //alert("comes in navBar:" + localStorage.getItem('userToken'));
        // if the token is not in local storage, there is nothing to check (i.e. the user is already logged out)
        if (!localStorage.getItem('userToken')) return;
        getAuth0SSOData();
    }, 10000);
</script>