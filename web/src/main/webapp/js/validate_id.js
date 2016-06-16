    function checkingInput(text) {
        if (text === undefined) 
            return true;

        var map = [':','=','(',')','{','}','&','<','>','"',"'"];
        for (var i = 0; i < map.length; i++){
            if (text.toLowerCase().indexOf(map[i]) > -1) {
                return false;
            }           
        }
        return true;
    }

    function validateForm( file ) {
        var validStudySubjectId;
        var validSecondaryId;
        
        if (file === "addNewSubjectExpressNewWeb") {
            validStudySubjectId = checkingInput(jQuery("input[name='label']").val());

            if (validStudySubjectId) {
                jQuery('#labelMessage').html("");
            } else {
                jQuery('#labelMessage').html("<div ID='spanAlert-label'' class='alert'>The input you provided contain unacceptable character.</div><br /><br />");
            }

            return validStudySubjectId;
        } else if (file === "addNewSubjectExpressNewWs") {
            return checkingInput(jQuery("input[name='label']").val());
        } else {
            validStudySubjectId = checkingInput($("input[name='label']").val());
            validSecondaryId = checkingInput($("input[name='secondaryLabel']").val());
            
            if (validStudySubjectId) {
                $('#labelMessage').html("");
            } else {
                $('#labelMessage').html("<div ID='spanAlert-label'' class='alert'>The input you provided contain unacceptable character.</div><br /><br />");
            }

            if (validSecondaryId) {
                $('#secondaryLabelMessage').html("");
            } else {
                $('#secondaryLabelMessage').html("<div ID='spanAlert-label'' class='alert'>The input you provided contain unacceptable character.</div><br /><br />");
            }

            return validStudySubjectId && validSecondaryId;
        }

    }