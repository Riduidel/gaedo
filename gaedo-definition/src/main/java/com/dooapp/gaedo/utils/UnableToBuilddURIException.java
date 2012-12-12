package com.dooapp.gaedo.utils;

import com.dooapp.gaedo.CrudServiceException;

import java.net.URISyntaxException;

/**
 * Created with IntelliJ IDEA.
 * User: ndx
 * Date: 12/12/12
 * Time: 12:08
 * To change this template use File | Settings | File Templates.
 */
public class UnableToBuilddURIException extends CrudServiceException {
    public UnableToBuilddURIException() {
    }

    public UnableToBuilddURIException(String message) {
        super(message);
    }

    public UnableToBuilddURIException(Throwable cause) {
        super(cause);
    }

    public UnableToBuilddURIException(String message, Throwable cause) {
        super(message, cause);
    }
}
