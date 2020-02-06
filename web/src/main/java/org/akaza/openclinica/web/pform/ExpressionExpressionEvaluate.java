/*
 * LibreClinica is distributed under the
 * GNU Lesser General Public License (GNU LGPL).

 * For details see: https://libreclinica.org/license
 * LibreClinica, copyright (C) 2020
 */
package org.akaza.openclinica.web.pform;

public class ExpressionExpressionEvaluate {
	private String expression;
	private boolean expressionEvaluate;
	
	
	public ExpressionExpressionEvaluate() {
	}
	
	public String getExpression() {
		return expression;
	}
	public void setExpression(String expression) {
		this.expression = expression;
	}
	public boolean isExpressionEvaluate() {
		return expressionEvaluate;
	}
	public void setExpressionEvaluate(boolean expressionEvaluate) {
		this.expressionEvaluate = expressionEvaluate;
	}
	
	

}
