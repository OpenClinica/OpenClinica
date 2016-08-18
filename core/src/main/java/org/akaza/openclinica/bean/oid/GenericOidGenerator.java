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
public class GenericOidGenerator extends OidGenerator {

    private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid;
        String key = keys[0];

        oid = truncateTo4Chars(capitalize(stripNonAlphaNumeric(key)));

        // If oid is made up of all special characters then
        if (oid.equals("_")) {
            oid = randomizeOid("");
        }
        return oid;
    }
}
