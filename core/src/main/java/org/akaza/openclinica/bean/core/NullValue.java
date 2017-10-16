/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.bean.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Jun Xu
 * 
 * NI NoInformation No information whatsoever can be inferred from this
 * exceptional value. This is the most general exceptional value. It is also the
 * default exceptional value.
 * 
 * NA not applicable No proper value is applicable in this context (e.g., last
 * menstrual period for a male).
 * 
 * UNK unknown A proper value is applicable, but not known.
 * 
 * NASK not asked This information has not been sought (e.g., patient was not
 * asked)
 * 
 * ASKU asked but unknown Information was sought but not found (e.g., patient
 * was asked but didn't know)
 * 
 * NAV temporarily unavailable Information is not available at this time but it
 * is expected that it will be available later.
 * 
 * OTH other The actual value is not an element in the value domain of a
 * variable. (e.g., concept not provided by required code system).
 * 
 * PINF positive infinity Positive infinity of numbers.
 * 
 * NINF negative infinity Negative infinity of numbers.
 * 
 * MSK masked There is information on this item available but it has not been
 * provided by the sender due to security, privacy or other reasons. There may
 * be an alternate mechanism for gaining access to this information. Note: using
 * this null flavor does provide information that may be a breach of
 * confidentiality. Its primary purpose is for those circumstances where it is
 * necessary to inform the receiver that the information does exist.
 * 
 * NP not present Value is not present in a message. This is only defined in
 * messages, never in application data! All values not present in the message
 * must be replaced by the applicable default, or no-information (NI) as the
 * default of all defaults.
 * 
 */

// Internationalized description in Term.getDescription()
public class NullValue extends Term {
    public static final NullValue INVALID = new NullValue(0, "invalid", "invalid");
    public static final NullValue NI = new NullValue(1, "NI", "no_information");
    public static final NullValue NA = new NullValue(2, "NA", "not_applicable");
    public static final NullValue UNK = new NullValue(3, "UNK", "unknown");
    public static final NullValue NASK = new NullValue(4, "NASK", "not_asked");
    public static final NullValue ASKU = new NullValue(5, "ASKU", "asked_but_unknown");
    public static final NullValue NAV = new NullValue(6, "NAV", "not_available");
    public static final NullValue OTH = new NullValue(7, "OTH", "other");
    public static final NullValue PINF = new NullValue(8, "PINF", "positive_infinity");
    public static final NullValue NINF = new NullValue(9, "NINF", "negative_infinity");
    public static final NullValue MSK = new NullValue(10, "MSK", "masked");
    public static final NullValue NP = new NullValue(11, "NP", "not_present");
    public static final NullValue NPE = new NullValue(12, "NPE", "not_performed");

    private static final NullValue[] members = { NI, NA, UNK, NASK, ASKU, NAV, OTH, PINF, NINF, MSK, NP, NPE };

    public static final List list = Arrays.asList(members);

    private NullValue(int id, String name, String description) {
        super(id, name, description);
    }

    private NullValue() {
    }

    public static boolean contains(int id) {
        return Term.contains(id, list);
    }

    public static NullValue get(int id) {
        Term t = Term.get(id, list);
        if (!t.isActive()) {
            return INVALID;
        } else {
            return (NullValue) t;
        }
    }

    public static NullValue getByName(String name) {
        for (int i = 0; i < list.size(); i++) {
            NullValue temp = (NullValue) list.get(i);
            if (temp.getName().equals(name)) {
                return temp;
            }
        }
        return INVALID;
    }

    public static ArrayList toArrayList() {
        return new ArrayList(list);
    }

    @Override
    public String getName() {
        return name;
    }
}
