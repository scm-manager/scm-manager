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

import java.util.ArrayList;
import java.util.Collection;

import org.tmatesoft.svn.core.SVNException;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVOptionsRequest extends DAVRequest {

    private boolean myIsActivitySetRequest = false;

    private Collection myRequestedMethods;
    private Collection myRequestedReports;
    private Collection myRequestedLiveProperties;

    public DAVOptionsRequest() {
        super();
    }

    protected void init() throws SVNException {
    }

    public boolean isEmpty() {
        return !(isActivitySetRequest() || isSupportedLivePropertiesRequest() || isSupportedMethodsRequest() || isSupportedMethodsRequest());
    }

    public boolean isActivitySetRequest() {
        return myIsActivitySetRequest;
    }

    public void setActivitySetRequest(boolean isActivitySetRequest) {
        myIsActivitySetRequest = isActivitySetRequest;
    }

    public boolean isSupportedMethodsRequest() {
        return myRequestedMethods != null;
    }

    public boolean isSupportedLivePropertiesRequest() {
        return myRequestedLiveProperties != null;
    }

    public boolean isSupportedReportsRequest() {
        return myRequestedReports != null;
    }

    public Collection getRequestedMethods() {
        if (myRequestedMethods == null) {
            myRequestedMethods = new ArrayList();
        }
        return myRequestedMethods;
    }

    public Collection getRequestedLiveProperties() {
        if (myRequestedLiveProperties == null) {
            myRequestedLiveProperties = new ArrayList();
        }
        return myRequestedLiveProperties;
    }

    public Collection getRequestedReports() {
        if (myRequestedReports == null) {
            myRequestedReports = new ArrayList();
        }
        return myRequestedReports;
    }
}

