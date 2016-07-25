/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research
 */
package org.akaza.openclinica.bean.oid;

/**
 * @author Krikor Krumlian
 * 
 */
public class CrfVersionOidGenerator extends OidGenerator {

    private final int argumentLength = 2;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid;
        String crfOid = keys[0];
        String crfVersion = keys[1];

        // crfOid = truncateTo4Chars(capitalize(stripNonAlphaNumeric(crfOid)));
        crfVersion = truncateToXChars(capitalize(stripNonAlphaNumeric(crfVersion)), 10);

        oid = crfOid + "_" + crfVersion;

        if (crfVersion.length() == 0) {
            oid = randomizeOid(oid);
        }
        return oid;
    }
}
