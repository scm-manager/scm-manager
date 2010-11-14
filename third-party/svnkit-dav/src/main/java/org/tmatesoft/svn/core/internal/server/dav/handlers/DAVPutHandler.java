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

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.delta.SVNDeltaReader;
import org.tmatesoft.svn.core.internal.io.fs.FSCommitter;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSRoot;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionInfo;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceState;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceType;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNDeltaConsumer;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVPutHandler extends ServletDAVHandler {

    public DAVPutHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response) {
        super(repositoryManager, request, response);
    }

    public void execute() throws SVNException {
        DAVResource resource = getRequestedDAVResource(false, false);
        
        if (resource.getType() != DAVResourceType.REGULAR && resource.getType() != DAVResourceType.WORKING) {
            String body = "Cannot create resource " + SVNEncodingUtil.xmlEncodeCDATA(getURI()) + " with PUT.";
            response(body, DAVServlet.getStatusLine(HttpServletResponse.SC_CONFLICT), HttpServletResponse.SC_CONFLICT);
            return;
        }
        
        if (resource.isCollection()) {
            response("Cannot PUT to a collection.", DAVServlet.getStatusLine(HttpServletResponse.SC_CONFLICT), HttpServletResponse.SC_CONFLICT);
            return;
        }
        
        DAVResourceState resourceState = getResourceState(resource);
        validateRequest(resource, DAVDepth.DEPTH_ZERO, resourceState == DAVResourceState.NULL ? DAV_VALIDATE_PARENT : DAV_VALIDATE_RESOURCE, 
                null, null, null);
        
        DAVAutoVersionInfo avInfo = autoCheckOut(resource, false);
        int mode = DAV_MODE_WRITE_TRUNC;
        long[] range = parseRange();
        if (range != null) {
            mode = DAV_MODE_WRITE_SEEKABLE;
        }
        
        SVNDeltaReader deltaReader = null;
        DAVException error = null;
        try {
            deltaReader = openStream(resource, mode);
        } catch (DAVException dave) {
            error = new DAVException("Unable to PUT new contents for {0}.", new Object[] { SVNEncodingUtil.xmlEncodeCDATA(getURI()) }, 
                    HttpServletResponse.SC_FORBIDDEN, dave, 0); 
        }

        if (error == null && range != null) {
            error = new DAVException("Resource body read/write cannot use ranges (at this time)", HttpServletResponse.SC_NOT_IMPLEMENTED, 0);
        }

        DAVException error2 = null;
        if (error == null) {
            String path = resource.getResourceURI().getPath();
            FSRoot root = resource.getRoot();
            FSFS fsfs = resource.getFSFS();
            FSTransactionInfo txn = resource.getTxnInfo();
            Collection lockTokens = resource.getLockTokens();
            String userName = resource.getUserName();
            FSCommitter committer = getCommitter(fsfs, root, txn, lockTokens, userName);
            ISVNDeltaConsumer deltaConsumer = getDeltaConsumer(root, committer, fsfs, userName, lockTokens);
            SVNDeltaGenerator deltaGenerator = new SVNDeltaGenerator();
            InputStream inputStream = null;
            try {
                inputStream = getRequestInputStream();
                byte[] buffer = new byte[2048];
                int readCount = -1;
                while ((readCount = inputStream.read(buffer)) != -1) {
                    if (readCount == 0) {
                        continue;
                    }
                    if (deltaReader != null) {
                        deltaReader.nextWindow(buffer, 0, readCount, path, deltaConsumer);
                    } else {
                        deltaGenerator.sendDelta(path, buffer, readCount, deltaConsumer);
                    }
                }
            } catch (IOException ioe) {
                error = new DAVException("An error occurred while reading the request body.", HttpServletResponse.SC_BAD_REQUEST, 0);
            } catch (SVNException svne) {
                error = DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "could not write the file contents", null);
            } finally {
                SVNFileUtil.closeFile(inputStream);
                if (deltaReader != null) {
                    try {
                        deltaReader.reset(path, deltaConsumer);
                    } catch (SVNException svne) {
                        error2 = DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                                "error finalizing applying windows", null);
                    }
                    
                    if (error2 != null && error == null) {
                        error = error2;
                    }
                    deltaConsumer.textDeltaEnd(path);
                }
            }
        }
        
        if (error == null) {
            resource.setExists(true);
        }

        try {
            autoCheckIn(resource, error != null, false, avInfo);
        } catch (DAVException dave) {
            error2 = dave;
        }
        
        if (error != null) {
            throw error;
        }
        
        if (error2 != null) {
            error2 = new DAVException("The PUT was successful, but there was a problem automatically checking in the resource or its parent collection.", 
                    null, error2.getResponseCode(), error2, 0);
            //TODO: add here better logging
        }

        DAVLockInfoProvider lockProvider = null;
        try {
            lockProvider = DAVLockInfoProvider.createLockInfoProvider(this, false);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "The file was PUT successfully, but there was a problem opening the lock database which prevents inheriting locks from the parent resources.", 
                    null);
        }

        notifyCreated(resource, lockProvider, resourceState, DAVDepth.DEPTH_ZERO);

        handleDAVCreated(null, "Resource", resourceState == DAVResourceState.EXISTS);
    }

    protected DAVRequest getDAVRequest() {
        return null;
    }
    
}
