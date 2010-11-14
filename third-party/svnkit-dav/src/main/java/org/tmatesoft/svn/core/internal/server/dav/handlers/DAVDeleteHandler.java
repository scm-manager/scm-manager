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

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVDeleteHandler extends ServletDAVHandler {
    
    private DAVDeleteRequest myDAVRequest;
    
    public DAVDeleteHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response) {
        super(repositoryManager, request, response);
    }

    public void execute() throws SVNException {
        readInput(false);

        DAVResource resource = getRequestedDAVResource(false, false);
        if (!resource.exists()) {
            sendError(HttpServletResponse.SC_NOT_FOUND, null);
            return;
        }
        
        DAVDepth depth = getRequestDepth(DAVDepth.DEPTH_INFINITY);
        if (resource.isCollection() && depth != DAVDepth.DEPTH_INFINITY) {
            SVNDebugLog.getDefaultLog().logError(SVNLogType.NETWORK, "Depth must be \"infinity\" for DELETE of a collection.");
            sendError(HttpServletResponse.SC_BAD_REQUEST, null);
            return;
        }
        
        if (!resource.isCollection() && depth == DAVDepth.DEPTH_ONE) {
            SVNDebugLog.getDefaultLog().logError(SVNLogType.NETWORK, "Depth of \"1\" is not allowed for DELETE.");
            sendError(HttpServletResponse.SC_BAD_REQUEST, null);
            return;
        }
        
        try {
            validateRequest(resource, depth, DAV_VALIDATE_PARENT | DAV_VALIDATE_USE_424, null, null, null);
        } catch (DAVException dave) {
            throw new DAVException("Could not DELETE {0} due to a failed precondition (e.g. locks).", 
                    new Object[] { SVNEncodingUtil.xmlEncodeCDATA(getURI()) }, dave.getResponseCode(), null, SVNLogType.NETWORK, Level.FINE, 
                    dave, null, null, 0, dave.getResponse());
        }
        
        int respCode = unlock(resource, null);
        if (respCode != HttpServletResponse.SC_OK) {
            sendError(respCode, null);
            return;
        }
        
        DAVAutoVersionInfo avInfo = autoCheckOut(resource, true);
        try {
            removeResource(resource);
        } catch (DAVException dave) {
            autoCheckIn(null, true, false, avInfo);
            throw new DAVException("Could not DELETE {0}.", new Object[] { SVNEncodingUtil.xmlEncodeCDATA(getURI()) }, dave.getResponseCode(), 
                    dave, 0); 
        }
        
        try {
            autoCheckIn(null, false, false, avInfo);
        } catch (DAVException dave) {
            //TODO: add better logging here later
            SVNDebugLog.getDefaultLog().logFine(SVNLogType.NETWORK, "The DELETE was successful, but there was a problem automatically checking in the parent collection.");
        }
        setResponseStatus(HttpServletResponse.SC_NO_CONTENT);
    }

    protected DAVRequest getDAVRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVDeleteRequest();
        }
        return myDAVRequest;
    }

}
