/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.logic.score;

/**
 * The <code>Parser</code> is used to parse expression String into ScoreToken
 * ArrayList and parse item variables in expression.
 * 
 * @author ywang
 * @version 1.0, 01-16-2008
 * 
 */
import org.akaza.openclinica.bean.submit.ItemBean;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.TreeSet;

public class Parser {
    private Logger logger = LoggerFactory.getLogger(getClass().getName());
    private HashMap<String, ItemBean> map;
    private HashMap<String, String> itemdata;
    private StringBuffer errors;

    public Parser(HashMap<String, ItemBean> map, HashMap<String, String> itemdata) {
        this.map = map;
        this.itemdata = itemdata;
        this.errors = new StringBuffer();
    }

    /**
     * Convert expression string to ScoreToken arrayList;
     * <p>
     * Supported expression could start with a key word 'func' and include
     * <ul>
     * <li>operators +, -, *, /
     * <li>parenthesises (, )
     * <li>formula as the pattern that starts with formula name, formula
     * arguments are grouped in parenthesises with delimiter as ',' For example:
     * sum(item_x,item_y,item_z)
     * </ul>
     * 
     * @param expression
     * @return
     */
    public ArrayList<ScoreToken> parseScoreTokens(String expression) {
        ArrayList<ScoreToken> list = new ArrayList<ScoreToken>();
        if (expression == null || expression.length() < 1) {
            return list;
        }
        // process the prefix
        String exp = expression;
        if (exp.startsWith("func:")) {
            exp = exp.substring(5).trim();
        }
        exp = exp.replaceAll("##", ",");
        exp = exp.replace(" ", "");

        char contents[] = exp.toCharArray();
        String token = "";
        int pos = 0;
        int listpos = 0;
        ScoreToken scoretoken = new ScoreToken();
        while (pos < contents.length) {
            char c = contents[pos];
            // ignore spaces
            if (c == ' ') {
                // do nothing
            } else if (ScoreUtil.isOperator(c)) {
                if (token.length() > 0) {
                    scoretoken = new ScoreToken(ScoreSymbol.TERM_SYMBOL, token);
                    list.add(scoretoken);
                    ++listpos;
                    token = "";
                }
                scoretoken = new ScoreToken(ScoreSymbol.ARITHMETIC_OPERATOR_SYMBOL, c + "");
                list.add(scoretoken);
                ++listpos;
            } else if (c == '(') {
                String funcname = ScoreUtil.getFunctionName(token);
                if (funcname != null) {
                    scoretoken = new ScoreToken(ScoreSymbol.FUNCTION_SYMBOL, token);
                    list.add(scoretoken);
                    ++listpos;
                    token = "";
                } else if (token.length() > 0) {
                    scoretoken = new ScoreToken(ScoreSymbol.TERM_SYMBOL, token);
                    list.add(scoretoken);
                    ++listpos;
                    token = "";
                }
                scoretoken = new ScoreToken(ScoreSymbol.OPEN_PARENTH_SYMBOL, c + "");
                list.add(scoretoken);
                ++listpos;
            } else if (c == ')') {
                if (token.length() > 0) {
                    scoretoken = new ScoreToken(ScoreSymbol.TERM_SYMBOL, token);
                    list.add(scoretoken);
                    ++listpos;
                    token = "";
                }
                scoretoken = new ScoreToken(ScoreSymbol.CLOSE_PARENTH_SYMBOL, c + "");
                list.add(scoretoken);
                ++listpos;
            } else if (c == ',') {
                if (token.length() > 0) {
                    scoretoken = new ScoreToken(ScoreSymbol.TERM_SYMBOL, token);
                    list.add(scoretoken);
                    ++listpos;
                    token = "";
                }
                scoretoken = new ScoreToken(ScoreSymbol.COMMA_SYMBOL, c + "");
                list.add(scoretoken);
                ++listpos;
            } else {
                token += c;
            }
            pos++;
        }
        if (token.length() > 0) {
            scoretoken = new ScoreToken(ScoreSymbol.TERM_SYMBOL, token);
            list.add(scoretoken);
        }
        return list;
    }

    /**
     * Given a ScoreToken ArrayList, assign variables with their values
     * 
     * @param expression
     * @param ordinal
     * @return
     */
    public ArrayList<ScoreToken> assignVariables(ArrayList<ScoreToken> expression, int ordinal) {
        if (expression.isEmpty()) {
            errors.append("Expression is empty" + "; ");
            return expression;
        }
        boolean isEmpty = false;
        for (int i = 0; i < expression.size(); ++i) {
            ScoreToken t = expression.get(i);
            if (t.getSymbol() == ScoreSymbol.TERM_SYMBOL) {
                char next = i < expression.size() - 1 ? expression.get(i + 1).getSymbol() : ' ';
                if (map.containsKey(t.getName().trim()) && next != '(') {
                    // String key = ((ItemBean)map.get(var.trim())).getId() +
                    // "_" + ordinal;
                    String key = map.get(t.getName().trim()).getId() + "_" + ordinal;
                    if (itemdata.containsKey(key)) {
                        String idvalue = itemdata.get(key);
                        if (idvalue.length() > 0) {
                            t.setName(idvalue);
                        } else {
                            // if no item value found, score result will be
                            // empty
                            errors.append(" " + t.getName().trim() + " " + "is empty" + "; ");
                            isEmpty = true;
                        }
                    } else {
                        // if no item value found, score result will be empty
                        errors.append(" " + t.getName().trim() + " " + "is empty" + "; ");
                        isEmpty = true;
                    }
                }
            }
        }
        if (isEmpty) {
            return new ArrayList<ScoreToken>();
        } else {
            return expression;
        }
    }

    /**
     * Given a ScoreToken ArrayList, assign variables with their values
     * 
     * @param expression
     * @param itemOrdinals
     * @return
     */
    public ArrayList<ScoreToken> assignVariables(ArrayList<ScoreToken> expression, HashMap<Integer, TreeSet<Integer>> itemOrdinals) {
        if (expression.isEmpty()) {
            errors.append("Expression is empty" + "; ");
            return expression;
        }
        boolean isEmpty = false;
        ArrayList<ScoreToken> list = new ArrayList<ScoreToken>();
        for (int i = 0; i < expression.size(); ++i) {
            ScoreToken t = expression.get(i);
            if (t.getSymbol() != ScoreSymbol.TERM_SYMBOL) {
                list.add(t);
            } else {
                char next = i < expression.size() - 1 ? expression.get(i + 1).getSymbol() : ' ';
                if (map.containsKey(t.getName().trim()) && next != '(') {
                    int itemId = map.get(t.getName().trim()).getId();
                    if (itemOrdinals.containsKey(itemId)) {
                        TreeSet<Integer> ordinals = itemOrdinals.get(itemId);
                        int groupsize = ordinals.size();
                        int count = 0;
                        Iterator it = ordinals.iterator();
                        while (it.hasNext()) {
                            String key = itemId + "_" + it.next();
                            String idvalue = "";
                            if (itemdata.containsKey(key)) {
                                idvalue = itemdata.get(key);
                                if (idvalue.length() > 0) {
                                    ScoreToken temp0 = new ScoreToken();
                                    temp0.setSymbol(ScoreSymbol.TERM_SYMBOL);
                                    temp0.setName(idvalue);
                                    list.add(temp0);
                                    if (count < groupsize - 1) {
                                        ScoreToken temp = new ScoreToken(ScoreSymbol.COMMA_SYMBOL, ",");
                                        list.add(temp);
                                    }
                                } else {
                                    // if no item value found, score result will
                                    // be empty
                                    errors.append(" " + t.getName().trim() + " " + "is empty" + "; ");
                                    isEmpty = true;
                                }
                            } else {
                                // if no item value found, score result will be
                                // empty
                                errors.append(" " + t.getName().trim() + " " + "is empty" + "; ");
                                isEmpty = true;
                            }
                            ++count;
                        }
                    } else {
                        errors.append(" " + t.getName().trim() + " " + "is not available" + "; ");
                        isEmpty = true;
                    }
                }
            }
        }
        if (isEmpty) {
            return new ArrayList<ScoreToken>();
        } else {
            return list;
        }
    }

    public boolean isChanged(TreeSet<String> changedItems, ArrayList<ScoreToken> expression) {
        for (ScoreToken t : expression) {
            if (t.getSymbol() == ScoreSymbol.TERM_SYMBOL) {
                if (changedItems.contains(t.getName().trim()))
                    return true;
            }
        }

        return false;
    }

    /**
     * A helper function to create a function class name.
     * 
     * @param functionName
     *            a function name.
     * @return the function class name.
     * 
     * @author Hailong Wang, 08/25/2006
     */
    public static String convertToClassName(String packageName, String functionName) {
        if (functionName == null || functionName.length() == 0) {
            return null;
        }
        return packageName + "." + functionName.substring(0, 1).toUpperCase() + functionName.substring(1, functionName.length());
    }

    public HashMap<String, ItemBean> getMap() {
        return this.map;
    }

    public HashMap<String, String> getItemData() {
        return this.itemdata;
    }

    public StringBuffer getErrors() {
        return this.errors;
    }

    public void setErrors(StringBuffer e) {
        this.errors = e;
    }
}
