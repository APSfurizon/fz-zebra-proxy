package net.furizon.zebra_proxy.infrastructure.web;

public enum ApiCommonErrorCode {
    UNKNOWN,
    UNAUTHENTICATED,
    SESSION_NOT_FOUND,
    INVALID_INPUT,
    ;

    @Override
    public String toString() {
        return name();
    }
}
