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
public class CrfOidGenerator extends OidGenerator {

    private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid = "F_";
        String crfName = keys[0];

        crfName = truncateToXChars(capitalize(stripNonAlphaNumeric(crfName)), 12);
        oid = oid + crfName;

        if (oid.equals("F_")) {
            oid = randomizeOid("F_");
        }
        return oid;
    }
}
