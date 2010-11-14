/*
 * ====================================================================
 * Copyright (c) 2004-2008 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.server.dav;


import java.text.MessageFormat;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.handlers.DAVResponse;
import org.tmatesoft.svn.core.internal.server.dav.handlers.ServletDAVHandler;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.1.2
 * @author  TMate Software Ltd.
 */
public class DAVException extends SVNException {
    private static final long serialVersionUID = 4845L;

    private String myMessage;
    private int myResponseCode;
    private int myErrorID;
    private DAVException myPreviousException;
    private String myTagName;
    private String myNameSpace;
    private DAVResponse myResponse;
    
    public DAVException(String message, Object[] objects, int responseCode, SVNErrorMessage error, SVNLogType logType, Level level, 
            DAVException previousException, String tagName, String nameSpace, int errorID, DAVResponse response) {
        super(error != null ? error : SVNErrorMessage.create(SVNErrorCode.UNKNOWN));
        myMessage = objects == null ? message : MessageFormat.format(message, objects); 
        myResponseCode = responseCode;
        myPreviousException = previousException;
        myTagName = tagName;
        myNameSpace = nameSpace; 
        myErrorID = errorID;
        myResponse = response;
        SVNDebugLog.getDefaultLog().log(logType, message, level);
    }

    public DAVException(String message, int respondCode, DAVException previousException, int errorID) {
        this(message, null, respondCode, null, SVNLogType.NETWORK, Level.FINE, previousException, null, null, errorID, null);
    }

    public DAVException(String message, Object[] objects, int respondCode, DAVException previousException, int errorID) {
        this(message, objects, respondCode, null, SVNLogType.NETWORK, Level.FINE, previousException, null, null, errorID, null);
    }
    
    public DAVException(String message, int responseCode, SVNLogType logType) {
        this(message, null, responseCode, null, logType, Level.FINE, null, null, null, 0, null);
    }

    public DAVException(String message, int responseCode, SVNLogType logType, DAVResponse response) {
        this(message, null, responseCode, null, logType, Level.FINE, null, null, null, 0, response);
    }

    public DAVException(String message, int responseCode, SVNLogType logType, String tagName, String nameSpace) {
        this(message, null, responseCode, null, logType, Level.FINE, null, tagName, nameSpace, 0, null);
    }

    public DAVException(String message, Object[] objects, int responseCode, int errorID) {
        this(message, objects, responseCode, null, SVNLogType.NETWORK, Level.FINE, null, null, null, errorID, null);
    }

    public DAVException(String message, Object[] objects, int responseCode, int errorID, DAVResponse response) {
        this(message, objects, responseCode, null, SVNLogType.NETWORK, Level.FINE, null, null, null, errorID, response);
    }

    public DAVException(String message, int responseCode, int errorID) {
        this(message, null, responseCode, errorID);
    }

    public DAVException(String message, int responseCode, int errorID, DAVResponse response) {
        this(message, null, responseCode, errorID, response);
    }

    public int getErrorID() {
        return myErrorID;
    }

    public String getTagName() {
        return myTagName;
    }
    
    public int getResponseCode() {
        return myResponseCode;
    }
    
    public String getMessage() {
        return myMessage;
    }

    public DAVException getPreviousException() {
        return myPreviousException;
    }

    public String getNameSpace() {
        return myNameSpace;
    }

    public DAVResponse getResponse() {
        return myResponse;
    }
    
    public void setResponse(DAVResponse response) {
        myResponse = response;
    }

    public void setPreviousException(DAVException previousException) {
        myPreviousException = previousException;
    }
    
    public static DAVException convertError(SVNErrorMessage err, int statusCode, String message, Object[] objects) {
        if (err.getErrorCode() == SVNErrorCode.FS_NOT_FOUND) {
            statusCode = HttpServletResponse.SC_NOT_FOUND;
        } else if (err.getErrorCode() == SVNErrorCode.UNSUPPORTED_FEATURE) {
            statusCode = HttpServletResponse.SC_NOT_IMPLEMENTED;
        } else if (err.getErrorCode() == SVNErrorCode.FS_PATH_ALREADY_LOCKED) {
            statusCode = ServletDAVHandler.SC_HTTP_LOCKED;
        }

        DAVException error = buildErrorChain(err, statusCode);
        if (message != null && err.getErrorCode() != SVNErrorCode.REPOS_HOOK_FAILURE) {
            if (objects != null) {
                message = MessageFormat.format(message, objects);
            }
            error = new DAVException(message, null, statusCode, null, SVNLogType.NETWORK, Level.FINE, error, null, null, 
                    err.getErrorCode().getCode(), null);
        }
        return error;
    }
    
    private static DAVException buildErrorChain(SVNErrorMessage err, int statusCode) {
        DAVException error = new DAVException(err.getMessage(), null, statusCode, err, SVNLogType.NETWORK, Level.FINE, null, 
                DAVXMLUtil.SVN_DAV_ERROR_TAG, DAVElement.SVN_DAV_ERROR_NAMESPACE, err.getErrorCode().getCode(), null);
        if (err.getChildErrorMessage() != null) {
            error.setPreviousException(buildErrorChain(err.getChildErrorMessage(), statusCode));
        }
       
        return error;
    }

}
