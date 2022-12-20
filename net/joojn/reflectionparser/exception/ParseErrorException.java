package net.joojn.reflectionparser.exception;

public class ParseErrorException extends RuntimeException {

    public ParseErrorException() {
    }

    public ParseErrorException(String message) {
        super(message);
    }

    public ParseErrorException(String message, Object... args)
    {
        super(String.format(message, args));
    }
    public ParseErrorException(String message, Throwable cause) {
        super(message, cause);
    }

    public ParseErrorException(Throwable cause) {
        super(cause);
    }

    public ParseErrorException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }

    public static void assertThat(Class<?> clazz, boolean assertion){
        if(!assertion) {
            throw new ParseErrorException(
                    String.format(
                            "Parser threw error when trying to parse line, class: %s", clazz.getName()
                    )
            );
        }
    }
}
