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
public class StudyEventDefinitionOidGenerator extends OidGenerator {

    private final int argumentLength = 1;

    @Override
    void verifyArgumentLength(String... keys) throws Exception {
        if (keys.length != argumentLength) {
            throw new Exception();
        }
    }

    @Override
    String createOid(String... keys) {
        String oid = "SE_";
        String key = keys[0];
        oid = oid + truncateToXChars(capitalize(stripNonAlphaNumeric(key)), 28);
        if (oid.length() == 3) {
            oid = randomizeOid(oid);

        }
        return oid;
    }

}
