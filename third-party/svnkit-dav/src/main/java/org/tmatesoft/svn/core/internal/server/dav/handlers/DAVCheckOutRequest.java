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

import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVCheckOutRequest extends DAVRequest {
    public static final DAVElement NEW = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "new");
    public  static final DAVElement CHECKOUT = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "checkout");
    public static final DAVElement APPLY_TO_VERSION = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "apply-to-version");
    public static final DAVElement UNRESERVED = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "unreserved");
    public static final DAVElement FORK_OK = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "fork-ok");
    public static final DAVElement ACTIVITY_SET = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "activity-set");

    public boolean isUnreserved() throws SVNException {
        DAVElementProperty root = getRoot();
        return root.hasChild(UNRESERVED);
    }
    
    public boolean isForkOk() throws SVNException {
        DAVElementProperty root = getRoot();
        return root.hasChild(FORK_OK);
    }

    public boolean isApplyToVersion() throws SVNException {
        DAVElementProperty root = getRoot(); 
        return root.hasChild(APPLY_TO_VERSION);
    }

    public DAVElementProperty getRoot() throws SVNException {
        if (getRootElement() == null) {
            invalidXMLRoot();
        }
        return getRootElement();
    }

    protected void init() throws SVNException {
        DAVElementProperty rootElement = getRootElement();
        if (rootElement == null || rootElement.getName() != CHECKOUT) {
            invalidXMLRoot();
        }
    }

    protected void invalidXMLRoot() throws SVNException {
        throw new DAVException("The request body, if present, must be a DAV:checkout element.", null, HttpServletResponse.SC_BAD_REQUEST, 
                null, SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
    }

}
