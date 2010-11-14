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

import java.io.IOException;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.dav.http.HTTPHeader;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVLock;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceState;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class DAVLockHandler extends ServletDAVHandler {
    private DAVLockRequest myLockRequest;
    
    protected DAVLockHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response) {
        super(repositoryManager, request, response);
    }

    public void execute() throws SVNException {
        long readLength = readInput(false);
        
        DAVDepth depth = null;
        try {
            depth = getRequestDepth(DAVDepth.DEPTH_INFINITY);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_BAD_REQUEST, null, null);
        }

        if (depth != DAVDepth.DEPTH_ZERO && depth != DAVDepth.DEPTH_INFINITY) {
            sendError(HttpServletResponse.SC_BAD_REQUEST, "Depth must be 0 or \"infinity\" for LOCK.");
            return;
        }
        
        DAVLockInfoProvider lockProvider = null;


        DAVResource resource = getRequestedDAVResource(false, false);
        
        try {
            lockProvider = DAVLockInfoProvider.createLockInfoProvider(this, false);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, null);
        }
            
        boolean isNewLockRequest = false;
        DAVLock lock = null;

        if (readLength > 0) {
            lock = getLockRequest().parseLockInfo(this, resource, getNamespaces());
            isNewLockRequest = true;
        }
            
        DAVResourceState resourceState = getResourceState(resource);
        try {
            int flags = resourceState == DAVResourceState.NULL ? DAV_VALIDATE_PARENT : DAV_VALIDATE_RESOURCE;
            flags |= DAV_VALIDATE_ADD_LD;
            validateRequest(resource, depth, flags, isNewLockRequest ? lock.getScope() : null, null, lockProvider);
        } catch (DAVException dave) {
            DAVException next = new DAVException("Could not LOCK {0} due to a failed precondition (e.g. other locks).", 
                    new Object[] { SVNEncodingUtil.xmlEncodeCDATA(resource.getResourceURI().getRequestURI(), true) }, dave.getResponseCode(), dave, 0);
            throw next;
        }
            
        if (!isNewLockRequest) {
            List lockTokens = null;
            try {
                lockTokens = getLockTokensList();
            } catch (DAVException dave) {
                DAVException next = new DAVException("The lock refresh for {0} failed because no lock tokens were specified in an \"If:\" header.", 
                        new Object[] { SVNEncodingUtil.xmlEncodeCDATA(resource.getResourceURI().getRequestURI(), true) }, dave.getResponseCode(), dave, 0);
                throw next;
            }
                
            String lockToken = (String) lockTokens.get(0);
            lock = lockProvider.refreshLock(resource, lockToken, getTimeout());
        } else {
            if (lock.getTimeOutDate() != null) {
                //TODO: add expiration date renewal
                //Date timeoutDate = lock.getTimeOutDate();
            }
            
            lockProvider.addLock(lock, resource);
            setResponseHeader(HTTPHeader.LOCK_TOKEN_HEADER, "<" + lock.getLockToken() + ">");
        }
        
        HttpServletResponse servletResponse = getHttpServletResponse(); 
        servletResponse.setContentType(DEFAULT_XML_CONTENT_TYPE);
        servletResponse.setStatus(HttpServletResponse.SC_OK);
        
        try {
            StringBuffer xmlBuffer = SVNXMLUtil.addXMLHeader(null);
            DAVXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), null, null, xmlBuffer, true, false);
            if (lock == null) {
                SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_DISCOVERY.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, xmlBuffer);
            } else {
                SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_DISCOVERY.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, xmlBuffer);
                xmlBuffer.append(DAVLockInfoProvider.getActiveLockXML(lock));
                xmlBuffer.append('\n');
                SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_DISCOVERY.getName(), xmlBuffer);
            }
            SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), xmlBuffer);
            getResponseWriter().write(xmlBuffer.toString());
        } catch (IOException e) {
            throw new DAVException(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 0);
        }
    }

    protected DAVRequest getDAVRequest() {
        return getLockRequest();
    }
    
    private DAVLockRequest getLockRequest() {
        if (myLockRequest == null) {
            myLockRequest = new DAVLockRequest();
        }
        return myLockRequest;
    }

}
