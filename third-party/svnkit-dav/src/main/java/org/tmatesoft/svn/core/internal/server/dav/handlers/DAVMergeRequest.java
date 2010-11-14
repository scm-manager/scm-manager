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


import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVMergeRequest extends DAVRequest {
    private static final DAVElement MERGE = DAVElement.getElement(DAVElement.DAV_NAMESPACE, "merge");
    
    protected void init() throws SVNException {
        if (getRoot().getName() != MERGE) {
            invalidXMLRoot();
        }
    }

    protected void invalidXMLRoot() throws SVNException {
        throw new DAVException("The request body must be present and must be a DAV:merge element.", HttpServletResponse.SC_BAD_REQUEST, 0);
    }

    protected DAVElementProperty getRoot() throws SVNException {
        if (getRootElement() == null) {
            invalidXMLRoot();
        }
        return getRootElement();
    }

}
