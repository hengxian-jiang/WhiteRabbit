package org.ohdsi.whiteRabbit;

import static java.lang.String.format;

public class TooManyTablesException extends Exception {
    public static final int MAX_TABLES_COUNT = 100;

    public TooManyTablesException() {
        super(format("Too many tables. Max count is %d.", MAX_TABLES_COUNT));
    }
}
