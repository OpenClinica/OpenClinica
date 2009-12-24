/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.logic.score.function;

import java.util.HashMap;

public final class Decode extends AbstractFunction {
    public Decode() {
        super();
    }

    /**
     * @see Function#execute(HashMap)
     */
    public void execute() {
        logger.info("Execute the function Decode... ");

        String condition = getArgument(0).toString();

        if (condition == null || condition.length() == 0) {
            value = "";
            return;
        }
        boolean found = false;
        for (int i = 1; i < argumentCount() - 1; i += 2) {
            if (condition.equals(getArgument(i).toString())) {
                value = getArgument(i + 1).toString();
                found = true;
                break;
            }
        }

        if (!found) {
            if (argumentCount() % 2 == 0) {
                value = getArgument(argumentCount() - 1).toString();
            } else {
                value = "";
            }
        }
    }
}
