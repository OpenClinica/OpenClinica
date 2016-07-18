/* 
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).
 * For details see: http://www.openclinica.org/license
 *
 * Copyright 2003-2008 Akaza Research 
 */
package org.akaza.openclinica.bean.oid;

import java.io.Serializable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * OID Generator solves the problems described below. We have Domain Objects
 * that need to be assigned a specific OID. - The OID is generated differently
 * for every Domain Object - The OID keys depend on the Domain object ,So some
 * domain objects need two keys to make up an OID some need three ... - The
 * number of Domain object needing an OID is small with respect to the total
 * amount of domain objects.
 * 
 * 
 * @author Krikor Krumlian
 * @see Strategy Pattern, Template Pattern
 */

public abstract class OidGenerator implements Serializable {

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((logger == null) ? 0 : logger.hashCode());
        result = prime * result + oidLength;
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        OidGenerator other = (OidGenerator) obj;
        if (logger == null) {
            if (other.logger != null)
                return false;
        } else if (!logger.equals(other.logger))
            return false;
        if (oidLength != other.oidLength)
            return false;
        return true;
    }

    private final int oidLength = 40;
    protected final Logger logger = LoggerFactory.getLogger(getClass().getName());

    public final String generateOid(String... keys) throws Exception {
        verifyArgumentLength(keys);
        String oid = createOid(keys);
        validate(oid);
        return oid;
    }

    public String randomizeOid(String input) {
        if (input == null || input.length() == 0)
            input = "";
        if (!input.endsWith("_"))
            input = input + "_";
        input = input + new Double((Math.random() * 10000)).intValue();
        return input;
    }

    abstract void verifyArgumentLength(String... keys) throws Exception;

    abstract String createOid(String... keys);

    String stripNonAlphaNumeric(String input) {
        // Add capitalization too
        return input.trim().replaceAll("\\s+|\\W+", "");
    }

    String capitalize(String input) {
        return input.toUpperCase();
    }

    String truncateToXChars(String input, int x) {
        return input.length() > x ? input.substring(0, x) : input;
    }

    String truncateTo4Chars(String input) {
        return truncateToXChars(input, 4);
    }

    String truncateTo8Chars(String input) {
        return truncateToXChars(input, 8);
    }

    public boolean validate(String oid) throws Exception {
        Pattern pattern = Pattern.compile("^[A-Z_0-9]+$");
        Matcher matcher = pattern.matcher(oid);
        boolean isValid = matcher.matches();
        if (!isValid || oid.length() > oidLength || oid.length() <= 0) {
            throw new Exception();
        }
        return isValid;
    }

}
