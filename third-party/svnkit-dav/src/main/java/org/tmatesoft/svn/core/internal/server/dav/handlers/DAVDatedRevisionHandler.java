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

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVDatedRevisionHandler extends DAVReportHandler {

    private DAVDatedRevisionRequest myDAVRequest;

    public DAVDatedRevisionHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response) {
        super(repositoryManager, request, response);
    }

    protected DAVRequest getDAVRequest() {
        return getDatedRevisionRequest();
    }

    private DAVDatedRevisionRequest getDatedRevisionRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVDatedRevisionRequest();
        }
        return myDAVRequest;
    }

    public void execute() throws SVNException {
        setDAVResource(getRequestedDAVResource(false, false));

        String responseBody = generateResponseBody();

        try {
            int contentLength = responseBody.getBytes(UTF8_ENCODING).length;
            setResponseContentLength(contentLength);
        } catch (UnsupportedEncodingException e) {
        }

        write(responseBody);
    }

    private String generateResponseBody() throws SVNException {
        StringBuffer xmlBuffer = new StringBuffer();
        long revision = getDatedRevision();
        addXMLHeader(xmlBuffer, null);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.VERSION_NAME.getName(), String.valueOf(revision), xmlBuffer);
        addXMLFooter(xmlBuffer, null);
        return xmlBuffer.toString();
    }

    private long getDatedRevision() throws SVNException {
        return getDAVResource().getRepository().getDatedRevision(getDatedRevisionRequest().getDate());
    }
}
