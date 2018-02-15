package cz.cuni.mff.fruiton.exception;

import cz.cuni.mff.fruiton.dto.CommonProtos.ErrorMessage.ErrorId;

public class FruitonServerException extends RuntimeException {

    private ErrorId errorId = ErrorId.GENERAL;

    public FruitonServerException(final String message) {
        super(message);
    }

    public FruitonServerException(final Throwable cause) {
        super(cause);
    }

    public FruitonServerException(final Throwable cause, final ErrorId errorId) {
        super(cause);
        this.errorId = errorId;
    }

    public FruitonServerException(final String message, final Throwable cause) {
        super(message, cause);
    }

    public FruitonServerException(final String message, final ErrorId errorId) {
        super(message);
        this.errorId = errorId;
    }

    public FruitonServerException(final String message, final Throwable cause, final ErrorId errorId) {
        super(message, cause);
        this.errorId = errorId;
    }

    public final ErrorId getErrorId() {
        return errorId;
    }
}
