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


import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.util.SVNBase64;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVGetLocksHandler extends DAVReportHandler {

    private DAVGetLocksRequest myDAVRequest;

    protected DAVGetLocksHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response) {
        super(repositoryManager, request, response);
    }


    protected DAVRequest getDAVRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVGetLocksRequest();
        }
        return myDAVRequest;
    }

    public void execute() throws SVNException {
        setDAVResource(getRequestedDAVResource(false, false));

        String responseBody = generateResponseBody();

        try {
            setResponseContentLength(responseBody.getBytes(UTF8_ENCODING).length);
        } catch (UnsupportedEncodingException e) {
        }

        write(responseBody);
    }

    private String generateResponseBody() throws SVNException {
        if (getDAVResource().getResourceURI().getPath() == null) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "get-locks-report run on resource which doesn't represent a path within a repository."), SVNLogType.NETWORK);
        }

        SVNLock[] locks = getDAVResource().getLocks();

        StringBuffer xmlBuffer = new StringBuffer();
        addXMLHeader(xmlBuffer, null);
        for (int i = 0; i < locks.length; i++) {
            addLock(locks[i], xmlBuffer);
        }
        addXMLFooter(xmlBuffer, null);
        return xmlBuffer.toString();
    }

    private void addLock(SVNLock lock, StringBuffer xmlBuffer) {
        SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "lock", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "path", lock.getPath(), xmlBuffer);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "token", lock.getID(), xmlBuffer);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "creationdate", SVNDate.formatDate(lock.getCreationDate()), xmlBuffer);
        if (lock.getExpirationDate() != null) {
            SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "expirationdate", SVNDate.formatDate(lock.getExpirationDate()), xmlBuffer);
        }
        if (lock.getOwner() != null) {
            if (SVNEncodingUtil.isXMLSafe(lock.getOwner())) {
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "owner", lock.getOwner(), xmlBuffer);
            } else {
                String ownerEncoded = null;
                try {
                    ownerEncoded = SVNBase64.byteArrayToBase64(lock.getOwner().getBytes(UTF8_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    ownerEncoded = SVNBase64.byteArrayToBase64(lock.getOwner().getBytes());
                }
                
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "owner", ownerEncoded, ENCODING_ATTR, BASE64_ENCODING, 
                        false, false, xmlBuffer);
            }
        }
        if (lock.getComment() != null) {
            if (SVNEncodingUtil.isXMLSafe(lock.getComment())) {
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "comment", lock.getComment(), xmlBuffer);
            } else {
                String commentEncoded = null;
                try {
                    commentEncoded = SVNBase64.byteArrayToBase64(lock.getComment().getBytes(UTF8_ENCODING));
                } catch (UnsupportedEncodingException e) {
                    commentEncoded = SVNBase64.byteArrayToBase64(lock.getComment().getBytes());
                }
                
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "comment", commentEncoded, ENCODING_ATTR, BASE64_ENCODING, 
                        false, false, xmlBuffer);
            }
        }
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "lock", xmlBuffer);
    }
}
