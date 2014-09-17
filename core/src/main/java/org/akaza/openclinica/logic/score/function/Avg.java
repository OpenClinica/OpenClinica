/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */
package org.akaza.openclinica.logic.score.function;

import org.apache.commons.math.stat.StatUtils;

public class Avg extends AbstractFunction {

    public Avg() {
        super();
    }

    // public void execute(HashMap<String,String> map) {
    public void execute() {
        logger.info("Execute the function Avg... ");

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
                errors.put(new Integer(errorCount++), "Unparseable number:" + " " + arg + " " + "in evaluation of" + " Avg(); ");
            }
        }
        if (errors.size() > 0) {
            logger.error("The following errors happended when Avg() evaluation was performed: " + errors);
            value = "";
            return;
        }

        if (values != null && values.length > 0) {
            double v = StatUtils.mean(values);
            value = Double.toString(v);
        } else {
            value = "";
        }
    }
}