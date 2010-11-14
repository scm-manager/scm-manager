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
package org.tmatesoft.svn.core.internal.server.dav.handlers;


/**
 * @version 1.1.2
 * @author  TMate Software Ltd.
 */
public class DAVResponse {
    private DAVResponse myNextResponse;
    private DAVPropsResult myPropResult;
    private int myStatusCode;
    private String myDescription;
    private String myHref;
    
    public DAVResponse(String description, String href, DAVResponse nextResponse, DAVPropsResult propResult, int statusCode) {
        myDescription = description;
        myHref = href;
        myNextResponse = nextResponse;
        myStatusCode = statusCode;
        myPropResult = propResult;
    }

    public DAVResponse getNextResponse() {
        return myNextResponse;
    }

    public int getStatusCode() {
        return myStatusCode;
    }
    
    public String getDescription() {
        return myDescription;
    }
    
    public String getHref() {
        return myHref;
    }
    
    public DAVPropsResult getPropResult() {
        return myPropResult;
    }
    
}
