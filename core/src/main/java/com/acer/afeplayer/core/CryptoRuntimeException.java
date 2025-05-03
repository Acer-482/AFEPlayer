package com.acer.afeplayer.core;

import java.security.GeneralSecurityException;

/**
 * 加密解密时异常
 */
public class CryptoRuntimeException extends RuntimeException {
    public CryptoRuntimeException(Exception e) {
        super(e);
    }
}
