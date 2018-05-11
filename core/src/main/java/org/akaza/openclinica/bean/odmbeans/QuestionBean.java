/*
 * OpenClinica is distributed under the GNU Lesser General Public License (GNU
 * LGPL).
 *
 * For details see: http://www.openclinica.org/license copyright 
 *
 */

package org.akaza.openclinica.bean.odmbeans;



/**
 *
 * @author ywang (Aug, 2010)
 *
 */

public class QuestionBean {
    private TranslatedTextBean question;
    //openclinica extension
    private String questionNumber;
    
    public QuestionBean() {
        question = new TranslatedTextBean();
    }
    public TranslatedTextBean getQuestion() {
        return question;
    }
    public void setQuestion(TranslatedTextBean question) {
        this.question = question;
    }
    public String getQuestionNumber() {
        return questionNumber;
    }
    public void setQuestionNumber(String questionNumber) {
        this.questionNumber = questionNumber;
    }
}