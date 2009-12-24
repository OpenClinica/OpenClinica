package org.akaza.openclinica.logic.score.function;

import org.apache.commons.math.stat.StatUtils;

public class Sum extends AbstractFunction {
    public Sum() {
        super();
    }

    /**
     * If one argument is "", then the value of this function will be "" too.
     *
     * All arguments should has been parsed to numbers before this execute()
     * method is called.
     *
     * @see Function#execute
     */
    public void execute() {
        logger.info("Execute the function Sum execute() ... ");

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
                // errors.put(new Integer(errorCount++), e.getMessage() + " when
                // evaluate " + "Sum(); ");
                errors.put(new Integer(errorCount++), "Unparseable number:" + " " + arg + " " + "in evaluation of" + " Sum(); ");
            }
        }
        if (errors.size() > 0) {
            logger.error("The following errors happended when Sum() evaluation was performed: " + errors);
            value = "";
            return;
        }

        if (values != null && values.length > 0) {
            double v = StatUtils.sum(values);
            value = Double.toString(v);
        } else {
            value = "";
        }
    }
}
