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
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSRepository;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionInfo;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVServletUtil;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVMakeActivityHandler extends ServletDAVHandler {

    private FSFS myFSFS;
    
    public DAVMakeActivityHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response) {
        super(repositoryManager, request, response);
    }
    
    public void execute() throws SVNException {
        DAVResource resource = getRequestedDAVResource(false, false);
        FSRepository repos = (FSRepository) resource.getRepository();
        myFSFS = repos.getFSFS();
 
        readInput(true);
        if (resource.exists()) {
            throw new DAVException("<DAV:resource-must-be-null/>", HttpServletResponse.SC_CONFLICT, SVNLogType.NETWORK);
        }

        if (!resource.canBeActivity()) {
            throw new DAVException("<DAV:activity-location-ok/>", HttpServletResponse.SC_FORBIDDEN, SVNLogType.NETWORK);
        }
        
        try {
            makeActivity(resource);
        } catch (DAVException dave) {
            throw new DAVException("Could not create activity {0}.", new Object[] { SVNEncodingUtil.xmlEncodeCDATA(resource.getResourceURI().getURI()) }, 
                    dave.getResponseCode(), null, SVNLogType.NETWORK, Level.FINE, dave, null, null, 0, null);
        }

        setResponseHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
        handleDAVCreated(resource.getResourceURI().getURI(), "Activity", false);
    }

    protected DAVRequest getDAVRequest() {
        return null;
    }

    private void makeActivity(DAVResource resource) throws DAVException {
        FSTransactionInfo txnInfo = DAVServletUtil.createActivity(resource, myFSFS);
        DAVServletUtil.storeActivity(resource, txnInfo.getTxnId());
        resource.setExists(true);
        resource.setTxnName(txnInfo.getTxnId());
    }
    
}
