/*
 * OpenClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: http://www.openclinica.org/license
 * copyright 2003-2005 Akaza Research
 */

package org.akaza.openclinica.logic.score;

/**
 *
 * @author ywang (Mar. 2008)
 */
public class ScoreToken {
    char symbol;
    String name;

    public ScoreToken() {
        this.symbol = ' ';
        this.name = "";
    }

    public ScoreToken(char symbol, String name) {
        this.symbol = symbol;
        this.name = name;
    }

    public void setSymbol(char scoreSymbol) {
        this.symbol = scoreSymbol;
    }

    public char getSymbol() {
        return symbol;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}