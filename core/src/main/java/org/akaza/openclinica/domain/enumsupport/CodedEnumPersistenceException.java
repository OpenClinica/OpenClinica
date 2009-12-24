package org.akaza.openclinica.domain.enumsupport;

@SuppressWarnings("serial")
public class CodedEnumPersistenceException extends RuntimeException {

    public CodedEnumPersistenceException(String message, Object... messageFormats) {
        super(String.format(message, messageFormats));
    }

    public CodedEnumPersistenceException(String message, Throwable cause, Object... messsageFormats) {
        super(String.format(message, messsageFormats), cause);
    }

    public CodedEnumPersistenceException(Throwable cause) {
        super(cause);
    }

}
