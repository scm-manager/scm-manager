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
import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceType;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;
import org.tmatesoft.svn.util.Version;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVGetHandler extends ServletDAVHandler {
   
    public DAVGetHandler(DAVRepositoryManager connector, HttpServletRequest request, HttpServletResponse response) {
        super(connector, request, response);
    }

    public void execute() throws SVNException {
        DAVResource resource = getRequestedDAVResource(true, false);

        if (!resource.exists()) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_PATH_NOT_FOUND, "Path ''{0}'' you requested not found", resource.getResourceURI().getPath()), SVNLogType.NETWORK);
        }

        if (resource.getResourceURI().getType() != DAVResourceType.REGULAR && resource.getResourceURI().getType() != DAVResourceType.VERSION
                && resource.getResourceURI().getType() != DAVResourceType.WORKING) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_MALFORMED_DATA, "Cannot GET this type of resource."), SVNLogType.NETWORK);
        }

        setDefaultResponseHeaders();
        setResponseHeaders(resource);

        try {
            checkPreconditions(resource.getETag(), resource.getLastModified());
        } catch (SVNException e) {
            //Nothing to do, there are no enough conditions
        }

        if (resource.isCollection()) {
            StringBuffer body = new StringBuffer();
            generateResponseBody(resource, body);
            String responseBody = body.toString();

            try {
                setResponseContentLength(responseBody.getBytes(UTF8_ENCODING).length);
            } catch (UnsupportedEncodingException e) {
                setResponseContentLength(responseBody.getBytes().length);
            }

            try {
                getResponseWriter().write(responseBody);
            } catch (IOException e) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, e), e, SVNLogType.NETWORK);
            }
        } else if (resource.getDeltaBase() != null) {
            //Here we should send SVN delta (for old clients)
        } else {
            resource.writeTo(getResponseOutputStream());
        }
    }

    protected DAVRequest getDAVRequest() {
        return null;
    }

    protected int checkPreconditions(String eTag, Date lastModified) {
        super.checkPreconditions(eTag, lastModified);
        lastModified = lastModified == null ? new Date() : lastModified;
        boolean isNotModified = false;
        Enumeration ifNoneMatch = getRequestHeaders(IF_NONE_MATCH_HEADER);
        if (ifNoneMatch != null && ifNoneMatch.hasMoreElements()) {
            if (eTag != null) {
                if (getRequestHeaders(RANGE_HEADER) != null && getRequestHeaders(RANGE_HEADER).hasMoreElements()) {
                    isNotModified = !eTag.startsWith("W") && containsValue(ifNoneMatch, eTag, "*");
                } else {
                    isNotModified = containsValue(ifNoneMatch, eTag, "*");
                }
            }
        }
        long ifModified = getRequestDateHeader(IF_MODIFIED_SINCE_HEADER);
        long date = getRequestDateHeader(DATE_HEADER);
        if ((isNotModified || ifNoneMatch == null || !ifNoneMatch.hasMoreElements()) && ifModified != -1 && date != -1) {
            isNotModified = ifModified >= lastModified.getTime() && ifModified <= date;
        }
        if (isNotModified) {
            // TODO status HTTP_NOT_MODIFIED
        } else {
            //TODO status OK
        }
        
        return 0;//TODO
    }

    private void setResponseHeaders(DAVResource resource) {
        try {
            setResponseContentType(resource.getContentType());
        } catch (SVNException e) {
            //nothing to do we just skip this header
        }
        setResponseHeader(ACCEPT_RANGES_HEADER, ACCEPT_RANGES_DEFAULT_VALUE);
        try {
            Date lastModifiedTime = resource.getLastModified();
            if (lastModifiedTime != null) {
                setResponseHeader(LAST_MODIFIED_HEADER, SVNDate.formatRFC1123Date(lastModifiedTime));
            }
        } catch (SVNException e) {
            //nothing to do we just skip this header
        }

        String eTag = resource.getETag();
        if (eTag != null) {
            setResponseHeader(ETAG_HEADER, eTag);
        }
    }

    private void generateResponseBody(DAVResource resource, StringBuffer buffer) throws SVNException {
        startBody(SVNPathUtil.tail(resource.getResourceURI().getContext()), resource.getResourceURI().getPath(), resource.getRevision(), buffer);
        addUpperDirectoryLink(resource.getResourceURI().getContext(), resource.getResourceURI().getPath(), buffer);
        addDirectoryEntries(resource, buffer);
        finishBody(buffer);
    }

    private void startBody(String contextComponent, String path, long revision, StringBuffer buffer) {
        buffer.append("<html><head><title>");
        buffer.append(contextComponent);
        buffer.append(" - Revision ");
        buffer.append(String.valueOf(revision));
        buffer.append(": ");
        buffer.append(path);
        buffer.append("</title></head>\n");
        buffer.append("<body>\n<h2>");
        buffer.append(contextComponent);
        buffer.append(" - Revision ");
        buffer.append(String.valueOf(revision));
        buffer.append(": ");
        buffer.append(path);
        buffer.append("</h2>\n <ul>\n");
    }

    private void addUpperDirectoryLink(String context, String path, StringBuffer buffer) {
        if (!"/".equals(path)) {
            buffer.append("<li><a href=\"");
            buffer.append(context);
            String parent = DAVPathUtil.removeTail(path, true);
            buffer.append("/".equals(parent) ? "" : parent);
            buffer.append("/");
            buffer.append("\">..</a></li>\n");
        }
    }

    private void addDirectoryEntries(DAVResource resource, StringBuffer buffer) throws SVNException {
        for (Iterator iterator = resource.getEntries().iterator(); iterator.hasNext();) {
            SVNDirEntry entry = (SVNDirEntry) iterator.next();
            boolean isDir = entry.getKind() == SVNNodeKind.DIR;
            buffer.append("<li><a href=\"");
            buffer.append(resource.getResourceURI().getContext());
            buffer.append("/".equals(resource.getResourceURI().getPath()) ? "" : resource.getResourceURI().getPath());
            buffer.append(DAVPathUtil.standardize(entry.getName()));
            buffer.append(isDir ? "/" : "");
            buffer.append("\">");
            buffer.append(entry.getName());
            buffer.append(isDir ? "/" : "");
            buffer.append("</a></li>\n");
        }
    }

    private void finishBody(StringBuffer buffer) {
        buffer.append("</ul><hr noshade><em>");
        buffer.append("Powered by ");
        buffer.append(Version.getVersionString());
        buffer.append("</em>\n</body></html>");
    }
}
