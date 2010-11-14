/*
 * ====================================================================
 * Copyright (c) 2004-2009 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.server.dav.handlers;

import java.util.LinkedList;


import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVLock;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockScope;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class DAVLockWalker implements IDAVResourceWalkHandler {
    private DAVResource myResource;
    private DAVLock myLock;
    
    public DAVLockWalker(DAVResource resource, DAVLock lock) {
        myResource = resource;
        myLock = lock;
    }
    
    public DAVResponse handleResource(DAVResponse response, DAVResource resource, DAVLockInfoProvider lockInfoProvider, LinkedList ifHeaders, int flags, 
            DAVLockScope lockScope, CallType callType)
            throws DAVException {
        if (myResource.equals(resource)) {
            return null;
        }
        
        try {
            lockInfoProvider.appendLock(resource, myLock);
        } catch (DAVException dave) {
            if (DAVServlet.isHTTPServerError(dave.getResponseCode())) {
                throw dave;
            }
            DAVResponse resp = new DAVResponse(null, resource.getResourceURI().getRequestURI(), response, null, dave.getResponseCode());        
            return resp;
        }
        
        return null;
    }

}
