package org.akaza.openclinica.exception;

/**
 * Description: this exception will be thrown in the class CdiscOdmXmlBean when
 * multiple values are found for one item. If this exception is thrown, it means
 * either the database integrity is not consistent or my understanding of the
 * tables has flaw.
 *
 * @author ywang
 */

public class MultipleItemValuesFoundInXMLParsingException extends Exception {
    public MultipleItemValuesFoundInXMLParsingException() {
        super();
    }

    public MultipleItemValuesFoundInXMLParsingException(String mesg) {
        super(mesg);
    }
}