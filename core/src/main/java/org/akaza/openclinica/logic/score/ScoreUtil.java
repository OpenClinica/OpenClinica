/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.logic.score;

import org.akaza.openclinica.exception.ScoreException;
import org.akaza.openclinica.logic.score.function.Function;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Stack;

/**
 * Util class for scoring feature. It contains expression evaluation methods.
 * Expression should pass ScoreValidator before evaluation.
 * 
 * @author ywang (Jan. 2008)
 * 
 */
public class ScoreUtil {

    protected static final Logger logger = LoggerFactory.getLogger(ScoreUtil.class.getName());
    private static final String FUNCTION_PACKAGE = "org.akaza.openclinica.logic.score.function";

    /**
     * Evaluation math expression which might contain functions.
     * <p>
     * Some pre-conditions:
     * <ul>
     * <li>Supported operators include only '+', '-', '*', '/'
     * <li>Math expression should pass ScoreValidator before evaluation.
     * </ul>
     * 
     * @param expression
     * @return String
     */
    public static String eval(ArrayList<ScoreToken> expression) throws ScoreException {
        if (expression == null || expression.size() < 1) {
            return "";
        }
        ScoreToken token = new ScoreToken();
        ArrayList<ScoreToken> finalexp = new ArrayList<ScoreToken>();
        String value = "";
        Info info = new Info();
        info.pos = 0;
        info.level = 0;
        boolean couldBeSign = true;
        while (info.pos < expression.size()) {
            ScoreToken t = new ScoreToken();
            t = expression.get(info.pos);
            char c = t.getSymbol();
            // ignore spaces
            if (c == ' ') {
                // do nothing
            } else if (c == ScoreSymbol.ARITHMETIC_OPERATOR_SYMBOL) {
                if (couldBeSign & isSign(t.getName())) {
                    if (token.getName().length() > 0) {
                        logger.info("Wrong at operator " + t.getName() + " at position " + info.pos);
                        throw new ScoreException(t.getName() + " at position " + info.pos + " is invalid.", "1");
                    } else {
                        token.setName(t.getName());
                        token.setSymbol(t.getSymbol());
                    }
                    couldBeSign = false;
                } else {
                    if (token.getName().length() > 0) {
                        finalexp.add(token);
                        token = new ScoreToken();
                    }
                    finalexp.add(t);
                    couldBeSign = true;
                }
            } else if (c == '(') {
                couldBeSign = true;
                String sign = "";
                String tokenname = token.getName();
                if (tokenname.length() > 0 && isSign(tokenname.charAt(0))) {
                    sign = tokenname.charAt(0) + "";
                    tokenname = tokenname.substring(1);
                }
                String funcname = getFunctionName(tokenname);
                if (funcname != null && !funcname.equalsIgnoreCase("getexternalvalue") && !funcname.equalsIgnoreCase(FUNCTION_PACKAGE + "getexternalvalue")) {
                    try {
                        token.setName(sign + evalFunc(expression, info, (Function) Class.forName(funcname).newInstance()));
                        token.setSymbol(ScoreSymbol.TERM_SYMBOL);
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                    finalexp.add(token);
                    token = new ScoreToken();
                    couldBeSign = false;
                } else {
                    info.level++;
                    if (token.getName().length() > 0) {
                        finalexp.add(token);
                    }
                    token = new ScoreToken();
                    finalexp.add(t);
                }
            } else if (c == ')') {
                couldBeSign = false;
                if (token.getName().length() > 0) {
                    finalexp.add(token);
                }
                token = new ScoreToken();
                finalexp.add(t);
                info.level--;
            }
            // This should not happen since it is automatically handled by
            // getFunctionValue()
            // So, if this happens, either the expression or unlikely code has a
            // problem.
            else if (c == ',') {
                 token = new ScoreToken();
                throw new ScoreException("Found unexpected character , when doing evaluation", "2");
            } else {
                couldBeSign = false;
                if (isSign(token.getName())) {
                    token.setName(token.getName() + t.getName());
                    token.setSymbol(ScoreSymbol.TERM_SYMBOL);
                } else {
                    token.setName(t.getName());
                    token.setSymbol(t.getSymbol());
                }
            }
            info.pos++;
        }

        if (info.level != 0) {
            logger.info("expression invalid, unpaired parentheses."); // !!!!!
            throw new ScoreException("Found unpaired parentheses when doing evaluation", "3");
        }
        // There might be a last token which should be added to the final
        // expression.
        // For example, to expression 2+4, we must do so.
        if (token.getName().length() > 0) {
            finalexp.add(token);
        }
        if (finalexp != null && finalexp.size() > 0) {
            if (finalexp.size() == 1) {
                value = finalexp.get(0).getName();
            } else {
                value = evalSimple(createPostfix(finalexp));
            }
        }

        return "" + value;
    }

    /**
     * Evaluate a function which might contain arithmetic expressions, and
     * return result as a String.
     * <p>
     * If an item can not be found in the eventCRF, it will be treated as empty.
     * If empty items exist in a function, the result will be empty.
     * 
     * @param contents
     * @param info
     * @param function
     * @return
     */
    public static String evalFunc(ArrayList<ScoreToken> contents, Info info, Function function) throws ScoreException {
        int originalLevel = info.level;
        info.pos++;
        info.level++;
        ScoreToken token = new ScoreToken();
        // currArg is in fact representing the current argument.
        ArrayList<ScoreToken> currArg = new ArrayList<ScoreToken>();
        boolean couldBeSign = true;
        while (info.pos < contents.size()) {
            ScoreToken scoretoken = contents.get(info.pos);
            char c = scoretoken.getSymbol();
            if (c == ')') {
                couldBeSign = false;
                info.level--;
                // end of the function, marked by the equal level
                if (info.level == originalLevel) {
                    if (token.getName().length() > 0) {
                        currArg.add(token);
                    }
                    String t = evalArgument(currArg);
                    if (t != null && t.length() > 0) {
                        function.addArgument(t);
                    } else {
                        // error message has been handled in evalArgument()
                        return "";
                    }
                    token = new ScoreToken();
                    break;
                } else {
                    // end of an expression, just store them in the current
                    // argument
                    if (token.getName().length() > 0) {
                        currArg.add(token);
                    }
                    currArg.add(scoretoken);
                }
                token = new ScoreToken();
            } else if (c == '(') {
                couldBeSign = true;
                String sign = "";
                String tokenname = token.getName();
                if (tokenname.length() > 0 && isSign(tokenname.charAt(0))) {
                    sign = tokenname.charAt(0) + "";
                    tokenname = tokenname.substring(1);
                }
                // it is either the start of a function or an expression
                String funcname = getFunctionName(tokenname);
                if (funcname != null) {
                    // store in the current argument
                    try {
                        token.setName(sign + evalFunc(contents, info, (Function) Class.forName(funcname).newInstance()));
                        token.setSymbol(ScoreSymbol.TERM_SYMBOL);
                        currArg.add(token);
                        couldBeSign = false;
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                        return "";
                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                        return "";
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                        return "";
                    }
                }// if it is the start of an expression
                else {
                    info.level++;
                    if (token.getName().length() > 0) {
                        currArg.add(token);
                    }
                    currArg.add(scoretoken);
                }
                token = new ScoreToken();
            }// end of an argument
            else if (c == ',') {
                couldBeSign = true;
                if (token.getName().length() > 0) {
                    currArg.add(token);
                }
                // compute the argument
                String t = evalArgument(currArg);
                if (t != null && t.length() > 0) {
                    function.addArgument(t);
                } else {
                    return "";
                }
                token = new ScoreToken();
                // reset the argument for next one
                currArg = new ArrayList<ScoreToken>();
            }
            // else if(isOperator(c)){
            else if (c == ScoreSymbol.ARITHMETIC_OPERATOR_SYMBOL) {
                if (couldBeSign && isSign(scoretoken.getName())) {
                    if (token.getName().length() > 0) {
                        throw new ScoreException(scoretoken.getName() + " at position " + info.pos + " is invalid.", "1");
                    } else {
                        // token = scoretoken;
                        token.setName(scoretoken.getName());
                        token.setSymbol(scoretoken.getSymbol());
                    }
                    couldBeSign = false;
                } else {
                    if (token.getName().length() > 0) {
                        currArg.add(token);
                    }
                    token = new ScoreToken();
                    currArg.add(scoretoken);
                    couldBeSign = true;
                }
            } else {
                couldBeSign = false;
                if (isSign(token.getName())) {
                    token.setName(token.getName() + scoretoken.getName());
                    token.setSymbol(ScoreSymbol.TERM_SYMBOL);
                } else {
                    if (c != ' ') {
                        // token = scoretoken;
                        token.setName(scoretoken.getName());
                        token.setSymbol(scoretoken.getSymbol());
                    }
                }
            }
            info.pos++;
        }
        function.execute();
        if (function.getErrors().size() > 0) {
            String errors = new String();
            HashMap<Integer, String> es = function.getErrors();
            for (int i = 0; i < es.size(); ++i) {
                errors += es.get(Integer.valueOf(i));
            }
            throw new ScoreException(errors, "4");
        }

        return function.getValue();
    }

    /**
     * Evaluate argument of a function. Argument could be an expression.
     * 
     * @param arg
     * @return
     */
    public static String evalArgument(ArrayList<ScoreToken> arg) {
        String v = "";
        if (arg != null && arg.size() > 0) {
            try {
                if (arg.size() == 1) {
                    v = arg.get(0).getName();
                } else {
                    v = evalSimple(createPostfix(arg));
                }
            } catch (Exception e) {
                try {
                    v = eval(arg);
                } catch (ScoreException sc) {
                    sc.printStackTrace();
                }
            }
        }
        return v;
    }

    /**
     * Create postfix(ArrayList<ScoreToken>) for an expression(ArrayList<ScoreToken>).
     * This method only handles (, ), sign(ie, + -), arithmethic operators (ie, + * - /)
     * and numbers.
     * 
     * @param exp
     * @return
     */
    public static ArrayList<ScoreToken> createPostfix(ArrayList<ScoreToken> exp) {
        if (exp.size() < 3) {
            return exp;
        }
        Stack<ScoreToken> temp = new Stack<ScoreToken>();
        ArrayList<ScoreToken> post = new ArrayList<ScoreToken>();
        for (int i = 0; i < exp.size(); ++i) {
            if (exp.get(i).getName().equals("(")) {
                temp.push(exp.get(i));
            } else if (exp.get(i).getName().equals(")")) {
                while (!temp.isEmpty()) {
                    ScoreToken s = temp.pop();
                    if (!s.getName().equals("(")) {
                        post.add(s);
                    } else {
                        break;
                    }
                }
            } else if (isOperator(exp.get(i).getName())) {
                boolean finished = false;
                while (!temp.isEmpty() && !finished) {
                    ScoreToken s = temp.pop();
                    if (isOperator(s.getName())) {
                        if (getPriority(s.getName()) >= getPriority(exp.get(i).getName())) {
                            post.add(s);
                        } else {
                            temp.push(s);
                            finished = true;
                        }
                    } else {
                        temp.push(s);
                        finished = true;
                    }
                }
                temp.push(exp.get(i));
            } else {
                post.add(exp.get(i));
            }
        }
        while (!temp.isEmpty()) {
            post.add(temp.pop());
        }
        return post;
    }

    /**
     * Evaluates + * - / using postfix algorithm and return a String. If the
     * parameter exp size is 1, the first and the only element of exp will be
     * returned. If exp size > 1 and contains non-number elements, empty string
     * will be returned.
     * 
     * @param exp
     *            ArrayList<ScoreToken> should be postfix of an expression.
     * @param errors
     * @return
     */
    // public static String evalSimple(ArrayList<ScoreToken> exp, StringBuffer
    // errors) {
    public static String evalSimple(ArrayList<ScoreToken> exp) {

        String stringValue = "";
        double value = Double.NaN;
        Stack<Double> st = new Stack<Double>();
        int size = exp.size();
        if (size == 1) {
            try {
                value = Double.valueOf(exp.get(0).getName());
            } catch (Exception e) {
                // for function like decode whose argument might be a String
                stringValue = exp.get(0).getName();
            }
        } else if (size > 2) {
            for (int i = 0; i < size; ++i) {
                String s = exp.get(i).getName();
                if (isOperator(s)) {
                    double second = st.pop();
                    double first = st.pop();
                    try {
                        if (s.equals("+")) {
                            value = first + second;
                        } else if (s.equals("-")) {
                            value = first - second;
                        } else if (s.equals("*")) {
                            value = first * second;
                        } else if (s.equals("/")) {
                            value = first / second;
                        }
                    } catch (Exception ee) {
                        ee.printStackTrace();
                        value = Double.NaN;
                    }
                    st.push(value);
                } else {
                    double d = Double.NaN;
                    try {
                        d = Double.valueOf(exp.get(i).getName());
                    } catch (Exception e) {
                        e.printStackTrace();
                        return exp.get(i).getName();
                    }
                    st.push(d);
                }
            }
        }
        stringValue = stringValue.equalsIgnoreCase("") ? ((value + "").equalsIgnoreCase("NaN") ? "" : value + "") : stringValue;

        return stringValue;
    }

    /**
     * Return true if one character matches one of those characters '+', '-',
     * '*', '/'
     * 
     * @param ch
     * @return
     */
    public static boolean isOperator(char c) {
        return c == '+' || c == '-' || c == '*' || c == '/';
    }

    private static boolean isOperator(String s) {
        return s.equals("+") || s.equals("-") || s.equals("*") || s.equals("/");
    }

    private static boolean isSign(String s) {
        return s.equals("+") || s.equals("-");
    }

    private static boolean isSign(char c) {
        return c == '+' || c == '-';
    }

    public static String getFunctionName(String token) {
        return Parser.convertToClassName(FUNCTION_PACKAGE, token);
    }

    /*
     * Only handled + * - /
     */
    protected static byte getPriority(String operator) {
        byte p = 0;
        if (operator.equals("+") || operator.equals("-")) {
            p = 0;
        } else if (operator.equals("*") || operator.equals("/")) {
            p = 1;
        } else {
            p = -1;
        }
        return p;
    }

    static class Info {
        int level = 0;
        int pos = 0;
    }
}