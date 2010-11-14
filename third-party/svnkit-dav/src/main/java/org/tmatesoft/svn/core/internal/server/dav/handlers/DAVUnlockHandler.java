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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.http.HTTPHeader;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceState;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class DAVUnlockHandler extends ServletDAVHandler {

    protected DAVUnlockHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response) {
        super(repositoryManager, request, response);
    }

    public void execute() throws SVNException {
        String lockToken = getRequestHeader(HTTPHeader.LOCK_TOKEN_HEADER);
        if (lockToken == null) {
            sendError(HttpServletResponse.SC_BAD_REQUEST, "Unlock failed: No Lock-Token specified in header");
            return;
        }
        
        if (lockToken.indexOf('<') == -1) {
            sendError(HttpServletResponse.SC_BAD_REQUEST, "Unlock failed: Malformed Lock-Token header");
            return;
        }
        
        lockToken = lockToken.substring(1);
        if (lockToken.charAt(lockToken.length() - 1) != '>') {
            sendError(HttpServletResponse.SC_BAD_REQUEST, "Unlock failed: Malformed Lock-Token header");
            return;
        }
        
        lockToken = lockToken.substring(0, lockToken.length() - 1);
        DAVResource resource = getRequestedDAVResource(false, false);
        DAVResourceState resourceState = getResourceState(resource);
        validateRequest(resource, DAVDepth.DEPTH_ZERO, resourceState == DAVResourceState.LOCK_NULL ? 
                ServletDAVHandler.DAV_VALIDATE_PARENT : ServletDAVHandler.DAV_VALIDATE_RESOURCE, null, lockToken, null);
        
        unlock(resource, lockToken);
        setResponseStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    protected DAVRequest getDAVRequest() {
        return null;
    }

}
