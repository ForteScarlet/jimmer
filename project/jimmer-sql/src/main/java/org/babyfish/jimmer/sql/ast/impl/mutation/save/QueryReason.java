package org.babyfish.jimmer.sql.ast.impl.mutation.save;

public enum QueryReason {
    NONE,
    TRIGGER,
    FILTER,
    INTERCEPTOR,
    CHECKING,
    RECURSIVE,
    TOO_DEEP,
}