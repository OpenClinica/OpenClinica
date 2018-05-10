/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.bean.oid;

/**
 *
 * @author thickerson
 *
 */
public class StudySubjectOidGenerator extends OidGenerator {

    private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
    	String oid = "SS_";
        String studySubjectID = keys[0];
        studySubjectID = truncateTo8Chars(capitalize(stripNonAlphaNumeric(studySubjectID)));

        if (studySubjectID.length() == 0) {
            studySubjectID = randomizeOid("");
        }
        return oid + studySubjectID;
    }

}
