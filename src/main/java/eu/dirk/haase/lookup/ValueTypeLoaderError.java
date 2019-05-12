package eu.dirk.haase.lookup;

import java.net.URL;

class ValueTypeLoaderError extends Error {

    private static final long serialVersionUID = 0L;

    /**
     * Constructs a new instance with the specified message.
     *
     * @param msg The message, or <tt>null</tt> if there is no message
     */
    ValueTypeLoaderError(String msg) {
        super(msg);
    }

    /**
     * Constructs a new instance with the specified message and cause.
     *
     * @param msg   The message, or <tt>null</tt> if there is no message
     * @param cause The cause, or <tt>null</tt> if the cause is nonexistent
     *              or unknown
     */
    ValueTypeLoaderError(String msg, Throwable cause) {
        super(msg, cause);
    }

    static ValueTypeLoaderError fail(Class<?> valueFactory, String msg, Throwable cause)
            throws ValueTypeLoaderError {
        return new ValueTypeLoaderError(valueFactory.getName() + ": " + msg,
                cause);
    }

    static ValueTypeLoaderError fail(Class<?> valueFactory, String msg)
            throws ValueTypeLoaderError {
        return new ValueTypeLoaderError(valueFactory.getName() + ": " + msg);
    }

    static ValueTypeLoaderError fail(Class<?> valueFactory, URL u, int line, String msg)
            throws ValueTypeLoaderError {
        return fail(valueFactory, u + ":" + line + ": " + msg);
    }


}
