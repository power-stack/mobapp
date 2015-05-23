package com.stackbase.mobapp.utils;

import java.io.IOException;

public class RemoteException extends IOException {
    private int statusCode;

    public RemoteException(int statusCode, String error) {
        super(error);
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }
}
