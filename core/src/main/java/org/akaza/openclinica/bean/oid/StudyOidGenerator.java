/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.bean.oid;

import java.io.Serializable;

/**
 * Assumes we are getting the unique protocol id from a study, and truncating to
 * eight chars.
 *
 * @author thickerson
 *
 */
public class StudyOidGenerator extends OidGenerator implements Serializable {

    public int getArgumentLength() {
		return argumentLength;
	}

	private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
    	String oid = "S_";
        String uniqueProtocolID = keys[0];
        uniqueProtocolID = truncateTo8Chars(capitalize(stripNonAlphaNumeric(uniqueProtocolID)));
        System.out.println("*****unique id:" + uniqueProtocolID);
        if (uniqueProtocolID.length() == 0) {
            uniqueProtocolID = randomizeOid("");
        }
        oid = oid + uniqueProtocolID;
        System.out.println("****oid=" + oid);
        return oid;
    }

}
