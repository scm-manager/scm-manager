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
public class DAVPropPatchRequest extends DAVRequest {

    private static final DAVElement PROPERTY_UPDATE = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "propertyupdate");
    public static final DAVElement REMOVE = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "remove");
    public static final DAVElement SET = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "set");

    protected void init() throws SVNException {
        if (getRoot().getName() != PROPERTY_UPDATE) {
            invalidXMLRoot();
        }
    }

    protected void invalidXMLRoot() throws SVNException {
        throw new DAVException("The request body does not contain a \"propertyupdate\" element.", null, HttpServletResponse.SC_BAD_REQUEST, 
                null, SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
    }

    protected DAVElementProperty getRoot() throws SVNException {
        if (getRootElement() == null) {
            invalidXMLRoot();
        }
        return getRootElement();
    }
}
