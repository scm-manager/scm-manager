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

import java.net.URI;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.http.HTTPHeader;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceState;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceType;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;
import org.tmatesoft.svn.core.internal.server.dav.DAVServletUtil;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVCopyMoveHandler extends ServletDAVHandler {

    private boolean myIsMove;
    
    protected DAVCopyMoveHandler(DAVRepositoryManager connector, HttpServletRequest request, HttpServletResponse response, boolean isMove) {
        super(connector, request, response);
        myIsMove = isMove;
    }

    public void execute() throws SVNException {
        DAVResource resource = getRequestedDAVResource(!myIsMove, false);
        if (!resource.exists()) {
            sendError(HttpServletResponse.SC_NOT_FOUND, null);
            return;
        }
        
        if (resource.getType() != DAVResourceType.REGULAR) {
            String body = "Cannot COPY/MOVE resource " + SVNEncodingUtil.xmlEncodeCDATA(getURI()) + ".";
            response(body, DAVServlet.getStatusLine(HttpServletResponse.SC_METHOD_NOT_ALLOWED), HttpServletResponse.SC_METHOD_NOT_ALLOWED);
            return;
        }
        
        String destination = getRequestHeader(HTTPHeader.DESTINATION_HEADER);
        if (destination == null) {
            String netScapeHost = getRequestHeader(HTTPHeader.HOST_HEADER);
            String netScapeNewURI = getRequestHeader(HTTPHeader.NEW_URI_HEADER);
            if (netScapeHost != null && netScapeNewURI != null) {
                String path = SVNPathUtil.append(netScapeHost, netScapeNewURI);
                if (path.startsWith("/")) {
                    path = path.substring(1);
                }
                destination = "http://" + path;
            }
        }
        
        if (destination == null) {
            SVNDebugLog.getDefaultLog().logFine(SVNLogType.NETWORK, "The request is missing a Destination header.");
            sendError(HttpServletResponse.SC_BAD_REQUEST, null);
            return;
        }
        
        URI uri = null; 
        try {
            uri = DAVServletUtil.lookUpURI(destination, getRequest(), true);
        } catch (DAVException dave) {
            if (dave.getResponseCode() == HttpServletResponse.SC_BAD_REQUEST) {
                throw dave;
            }
            response(dave.getMessage(), DAVServlet.getStatusLine(dave.getResponseCode()), dave.getResponseCode());
        }
        
        String path = uri.getPath();
        DAVRepositoryManager manager = getRepositoryManager();
        String resourceContext = manager.getResourceContext();
        
        if (!path.startsWith(resourceContext)) {
            throw new DAVException("Destination url starts with a wrong context", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        path = path.substring(resourceContext.length());
        DAVResource newResource = getRequestedDAVResource(false, false, path);
        int overwrite = getOverwrite();
        if (overwrite < 0) {
            sendError(HttpServletResponse.SC_BAD_REQUEST, null);
            return;
        }
        
        if (newResource.exists() && overwrite == 0) {
            response("Destination is not empty and Overwrite is not \"T\"", DAVServlet.getStatusLine(HttpServletResponse.SC_PRECONDITION_FAILED), 
                    HttpServletResponse.SC_PRECONDITION_FAILED);
            return;
        }
        
        if (resource.equals(newResource)) {
            response("Source and Destination URIs are the same.", DAVServlet.getStatusLine(HttpServletResponse.SC_FORBIDDEN), 
                    HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        boolean isDir = resource.isCollection();
        DAVDepth depth = null;
        try {
            depth = getRequestDepth(DAVDepth.DEPTH_INFINITY);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_BAD_REQUEST, null, null);
        }
        
        if (depth == DAVDepth.DEPTH_ONE) {
            //add logging here later
            sendError(HttpServletResponse.SC_BAD_REQUEST, null);
            return;
        }
        
        if (myIsMove && isDir && depth != DAVDepth.DEPTH_INFINITY) {
            //add logging here later
            sendError(HttpServletResponse.SC_BAD_REQUEST, null);
            return;
        }
        
        if (myIsMove) {
            try {
                validateRequest(resource, depth, DAV_VALIDATE_PARENT | DAV_VALIDATE_USE_424, null, null, null);
            } catch (DAVException dave) {
                throw new DAVException("Could not MOVE {0} due to a failed precondition on the source (e.g. locks).", 
                        new Object[] { SVNEncodingUtil.xmlEncodeCDATA(getURI()) }, dave.getResponseCode(), null, SVNLogType.NETWORK, 
                        Level.FINE, dave, null, null, 0, dave.getResponse()); 
            }
        }
        
        try {
            validateRequest(newResource, DAVDepth.DEPTH_INFINITY, DAV_VALIDATE_PARENT | DAV_VALIDATE_USE_424, null, null, null);
        } catch (DAVException dave) {
            throw new DAVException("Could not MOVE/COPY {0} due to a failed precondition on the destination (e.g. locks).", 
                    new Object[] { SVNEncodingUtil.xmlEncodeCDATA(getURI()) }, dave.getResponseCode(), null, SVNLogType.NETWORK, Level.FINE, 
                    dave, null, null, 0, dave.getResponse());
        }
        
        if (isDir && depth == DAVDepth.DEPTH_INFINITY && resource.isParentResource(newResource)) {
            response("Source collection contains the Destination.", DAVServlet.getStatusLine(HttpServletResponse.SC_FORBIDDEN), HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        if (isDir && newResource.isParentResource(newResource)) {
            response("Destination collection contains the Source and Overwrite has been specified.", DAVServlet.getStatusLine(HttpServletResponse.SC_FORBIDDEN), 
                    HttpServletResponse.SC_FORBIDDEN);
            return;
        }
        
        readInput(true);
        DAVLockInfoProvider lockProvider = null;
        try {
            lockProvider = DAVLockInfoProvider.createLockInfoProvider(this, false);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, null);
        }
        
        if (myIsMove) {
            unlock(resource, null);
        }
        
        DAVAutoVersionInfo srcAVInfo = null;
        if (myIsMove) {
            srcAVInfo = autoCheckOut(resource, true);
            
        }
        
        boolean replaceDestination = false;
        DAVResourceState resNewState = getResourceState(newResource);
        if (newResource.exists()) {
            if (myIsMove || !newResource.isVersioned()) {
                replaceDestination = true;
            } else if (resource.getType() != newResource.getType()) {
                replaceDestination = true;
            } else if (resource.isCollection() != newResource.isCollection()) {
                replaceDestination = true;
            } 
        }
        
        DAVAutoVersionInfo dstAVInfo = null;
        if (!newResource.exists() || replaceDestination) {
            try {
                 dstAVInfo = autoCheckOut(newResource, true);
            } catch (DAVException dave) {
                if (myIsMove) {
                    autoCheckIn(null, true, false, srcAVInfo);
                }
                throw dave;
            }
        }
        
        if (srcAVInfo != null && srcAVInfo.getParentResource() != null && dstAVInfo != null && 
                dstAVInfo.getParentResource() != null) {
            DAVResource srcParentResource = srcAVInfo.getParentResource();
            DAVResource dstParentResource = dstAVInfo.getParentResource();
            if (srcParentResource.equals(dstParentResource)) {
                dstAVInfo.setParentResource(srcAVInfo.getParentResource());
            }
        }
        
        DAVException err1 = null;
        if (replaceDestination) {
            try {
                removeResource(newResource);
            } catch (DAVException dave) {
                err1 = dave;
            }
        }

        if (err1 == null) {
            if (myIsMove) {
                try {
                    moveResource(resource, newResource);
                } catch (DAVException dave) {
                    err1 = dave;
                }
            } else {
                try {
                    copyResource(resource, newResource);
                } catch (DAVException dave) {
                    err1 = dave;
                }
            }
        }

        DAVException err2 = null;
        try {
            autoCheckIn(null, err1 != null, false, dstAVInfo);
        } catch (DAVException dave) {
            err2 = dave;
        }
        
        DAVException err3 = null;
        if (myIsMove) {
            try {
                autoCheckIn(null, err1 != null, false, srcAVInfo);
            } catch (DAVException dave) {
                err3 = dave;
            }
        }
        
        if (err1 != null) {
            throw new DAVException("Could not MOVE/COPY {0}.", new Object[] { SVNEncodingUtil.xmlEncodeCDATA(getURI()) }, 
                    err1.getResponseCode(), err1, 0);
        }
        
        if (err2 != null) {
            //throw new DAVException("The MOVE/COPY was successful, but there was a problem automatically checking in the source parent collection.", null, err2.getResponseCode(), err2, 0);
            //TODO: add logging here later
        }
        if (err3 != null) {
            //TODO: add logging here later
        }
        
        try {
            notifyCreated(newResource, lockProvider, resNewState, depth);
        } catch (DAVException dave) {
            throw new DAVException("The MOVE/COPY was successful, but there was a problem updating the lock information.", null, 
                    dave.getResponseCode(), dave, 0);
        }
        
        handleDAVCreated(uri.toString(), "Destination", resNewState == DAVResourceState.EXISTS);
    }

    protected int getOverwrite() {
        String overwriteValue = getRequestHeader(HTTPHeader.OVERWRITE_HEADER);
        if (overwriteValue == null) {
            return 1;
        }
        
        overwriteValue = overwriteValue.toLowerCase();
        if (overwriteValue.length() == 1 && overwriteValue.charAt(0) == 'f') {
            return 0;
        }
        if (overwriteValue.length() == 1 && overwriteValue.charAt(0) == 't') {
            return 1;
        }
        //TODO: add logging here later
        return -1;
    }
    
    protected DAVRequest getDAVRequest() {
        return null;
    }

}
