/**
 * Builds an absolute casebook URL based on the user's selection
 *
 * @returns {string}
 */
function buildCasebookUrl() {
    var casebookType = $("[name='casebookType']:checked").val();

    var casebookParams = [];
    $("[name='casebookParam']:checked").each(function() {
        casebookParams.push($(this).val());
    });

    var baseUrl = window.location.href;
    baseUrl = baseUrl.substr(0, baseUrl.lastIndexOf("/")) + "/rest/clinicaldata/"
        + casebookType + "/" + studySubjectResource();
    baseUrl += "/" + "*" + "/" + "*" ; // Separating * and / so they don't start or finish a block comment

    if (casebookParams.length > 0) {
        baseUrl += "?" + (casebookParams.join("&"));
    }

    return baseUrl;
}
/**
 * Updates casebook link text field, reflecting the user's selection
 */
function updateCasebookLinkBox() {
    $("#casebookLinkText").val(buildCasebookUrl());
}

/**
 * Triggers casebook link update when user changes any option
 */
$("[name='casebookType'],[name='casebookParam']").change(function() {
    updateCasebookLinkBox();
});

/**
 * Show/hides casebook link text field
 */
$("#casebookLinkBtn").click(function() {
    updateCasebookLinkBox();
    $("#casebookLinkDisplay").toggle();
});

/**
 * Open casebook on a new window
 */
$("#casebookOpenBtn").click(function() {
    openPrintCRFWindow(buildCasebookUrl());
});