/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.logic.score.function;

public class Median extends AbstractFunction {
    public Median() {
        super();
    }

    /**
     * If one argument is "", then the value of this function will be "" too.
     *
     * @see Function#execute
     */

    public void execute() {
        logger.info("Execute the function Median... ");

        double[] values = new double[argumentCount()];
        for (int i = 0; i < argumentCount(); i++) {
            String arg = getArgument(i).toString();
            if (arg == null || arg.length() == 0) {
                value = "";
                return;
            }
            try {
                values[i] = Double.parseDouble(arg);
            } catch (Exception e) {
                errors.put(new Integer(errorCount++), "Unparseable number:" + " " + arg + " " + "in evaluation of" + " Median(); ");
            }
        }
        if (errors.size() > 0) {
            logger.error("The following errors happended when Median() evaluation was performed: " + errors);
            value = "";
            return;
        }

        if (values != null && values.length > 0) {
            double v = (new org.apache.commons.math.stat.descriptive.rank.Median()).evaluate(values);
            value = Double.toString(v);
        } else {
            value = "";
        }
    }

}
