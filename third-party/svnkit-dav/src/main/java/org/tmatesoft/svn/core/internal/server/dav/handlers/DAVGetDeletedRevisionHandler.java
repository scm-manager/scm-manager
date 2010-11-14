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

import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceURI;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class DAVGetDeletedRevisionHandler extends DAVReportHandler {

    private DAVGetDeletedRevisionRequest myDAVRequest;
    private DAVReportHandler myCommonReportHandler;
    
    protected DAVGetDeletedRevisionHandler(DAVRepositoryManager connector, HttpServletRequest request, HttpServletResponse response, 
            DAVReportHandler commonReportHandler) {
        super(connector, request, response);
        myCommonReportHandler = commonReportHandler;
    }

    public void execute() throws SVNException {
        myCommonReportHandler.checkSVNNamespace(null);
        setDAVResource(getRequestedDAVResource(false, false));
        
        DAVGetDeletedRevisionRequest request = getDeletedRevisionRequest();
        String relPath = request.getPath();
        long pegRev = request.getPegRevision();
        long endRev = request.getEndRevision();
        if (relPath == null || !SVNRevision.isValidRevisionNumber(pegRev) || !SVNRevision.isValidRevisionNumber(endRev)) {
            throw new DAVException("Not all parameters passed.", null, HttpServletResponse.SC_BAD_REQUEST, null, SVNLogType.NETWORK, Level.FINE, 
                    null, DAVXMLUtil.SVN_DAV_ERROR_TAG, DAVElement.SVN_DAV_ERROR_NAMESPACE, 0, null);
        }
        
        DAVResourceURI resourceURI = getDAVResource().getResourceURI();
        String path = relPath;
        if (!path.startsWith("/")) {
            path = SVNPathUtil.append(resourceURI.getPath(), relPath);    
        }
        
        SVNRepository repository = getDAVResource().getRepository(); 
        long deletedRev = SVNRepository.INVALID_REVISION;
        try {
            deletedRev = repository.getDeletedRevision(path, pegRev, endRev);
        } catch (SVNException svne) {
            throw new DAVException("Could not find revision path was deleted.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 0);
        }
        
        writeXMLHeader(null);
        StringBuffer xmlBuffer = SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.VERSION_NAME.getName(), String.valueOf(deletedRev), null);
        write(xmlBuffer);
        writeXMLFooter(null);
    }
    
    protected DAVRequest getDAVRequest() {
        return getDeletedRevisionRequest();
    }
    
    private DAVGetDeletedRevisionRequest getDeletedRevisionRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVGetDeletedRevisionRequest();
        }
        return myDAVRequest;
    }
}
