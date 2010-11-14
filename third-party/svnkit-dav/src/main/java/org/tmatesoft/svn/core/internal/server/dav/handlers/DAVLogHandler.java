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

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.ISVNLogEntryHandler;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLogEntry;
import org.tmatesoft.svn.core.SVNLogEntryPath;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNHashMap;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.wc.SVNRevision;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVLogHandler extends DAVReportHandler implements ISVNLogEntryHandler {

    private DAVLogRequest myDAVRequest;
    private int myDepth = 0;
    private DAVReportHandler myCommonReportHandler;

    public DAVLogHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response, 
            DAVReportHandler commonReportHandler) {
        super(repositoryManager, request, response);
        myCommonReportHandler = commonReportHandler;
    }

    protected DAVRequest getDAVRequest() {
        return getLogRequest();
    }

    private DAVLogRequest getLogRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVLogRequest();
        }
        return myDAVRequest;
    }

    private int getDepth() {
        return myDepth;
    }

    private void increaseDepth() {
        myDepth++;
    }

    private void decreaseDepth() {
        myDepth--;
    }

    public void execute() throws SVNException {
        myCommonReportHandler.checkSVNNamespace(null);
        setDAVResource(getRequestedDAVResource(false, false));

        writeXMLHeader(null);

        for (int i = 0; i < getLogRequest().getTargetPaths().length; i++) {
            String currentPath = getLogRequest().getTargetPaths()[i];
            DAVPathUtil.testCanonical(currentPath);
            getLogRequest().getTargetPaths()[i] = SVNPathUtil.append(getDAVResource().getResourceURI().getPath(), currentPath);
        }

        DAVLogRequest logRequest = getLogRequest();
        getDAVResource().getRepository().log(logRequest.getTargetPaths(), logRequest.getStartRevision(),
                logRequest.getEndRevision(), logRequest.isDiscoverChangedPaths(), logRequest.isStrictNodeHistory(),
                logRequest.getLimit(), logRequest.isIncludeMergedRevisions(), logRequest.getRevisionProperties(),
                this);

        writeXMLFooter(null);
    }

    public void handleLogEntry(SVNLogEntry logEntry) throws SVNException {
        if (logEntry.getRevision() == DAVResource.INVALID_REVISION) {
            if (getDepth() == 0) {
                return;
            }
            decreaseDepth();
        }

        StringBuffer xmlBuffer = new StringBuffer();
        SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "log-item", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.VERSION_NAME.getName(), String.valueOf(logEntry.getRevision()), xmlBuffer);

        boolean noCustomProperties = getLogRequest().isCustomPropertyRequested();
        SVNProperties revProps = logEntry.getRevisionProperties(); 
        for (Iterator iterator = revProps.nameSet().iterator(); iterator.hasNext();) {
            String propName = (String) iterator.next();
            String propValue = revProps.getStringValue(propName);
            
            if (SVNRevisionProperty.AUTHOR.equals(propName)) {
                SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.CREATOR_DISPLAY_NAME.getName(), propValue, null, 
                        false, true, xmlBuffer);
            } else if (SVNRevisionProperty.DATE.equals(propName)) {
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "date", propValue, null, false, true, xmlBuffer);
            } else if (SVNRevisionProperty.LOG.equals(propName)) {
                String comment = SVNEncodingUtil.fuzzyEscape(propValue);
                SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.COMMENT.getName(), comment, null, false, true, xmlBuffer);
            } else {
                noCustomProperties = false;
                String encodedPropName = SVNEncodingUtil.xmlEncodeCDATA(propName, false);
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "revprop", propValue, NAME_ATTR, encodedPropName, false, 
                        true, xmlBuffer);
            }
        }

        if (noCustomProperties) {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "no-custom-revprops", SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, xmlBuffer);
        }

        if (logEntry.hasChildren()) {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "has-children", SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, xmlBuffer);
            increaseDepth();
        }

        write(xmlBuffer);

        if (logEntry.getChangedPaths() != null) {
            for (Iterator iterator = logEntry.getChangedPaths().entrySet().iterator(); iterator.hasNext();) {
                Map.Entry pathEntry = (Map.Entry) iterator.next();
                String path = (String) pathEntry.getKey();
                SVNLogEntryPath logEntryPath = (SVNLogEntryPath) pathEntry.getValue();
                addChangedPathTag(path, logEntryPath);
            }
        }

        xmlBuffer = SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "log-item", null);
        write(xmlBuffer);
    }

    private void addChangedPathTag(String path, SVNLogEntryPath logEntryPath) throws SVNException {
        StringBuffer xmlBuffer = new StringBuffer();
        switch (logEntryPath.getType()) {
            case SVNLogEntryPath.TYPE_ADDED:
                if (logEntryPath.getCopyPath() != null && SVNRevision.isValidRevisionNumber(logEntryPath.getCopyRevision())) {
                    Map attrs = new SVNHashMap();
                    attrs.put(COPYFROM_PATH_ATTR, logEntryPath.getCopyPath());
                    attrs.put(COPYFROM_REVISION_ATTR, String.valueOf(logEntryPath.getCopyRevision()));
                    SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "added-path", path, attrs, xmlBuffer);
                } else {
                    SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "added-path", path, xmlBuffer);
                }
                break;
            case SVNLogEntryPath.TYPE_REPLACED:
                Map attrs = null;
                if (logEntryPath.getCopyPath() != null && SVNRevision.isValidRevisionNumber(logEntryPath.getCopyRevision())) {
                    attrs = new SVNHashMap();
                    attrs.put(COPYFROM_PATH_ATTR, logEntryPath.getCopyPath());
                    attrs.put(COPYFROM_REVISION_ATTR, String.valueOf(logEntryPath.getCopyRevision()));
                }
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "replaced-path", path, attrs, xmlBuffer);

                break;
            case SVNLogEntryPath.TYPE_MODIFIED:
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "modified-path", path, xmlBuffer);
                break;
            case SVNLogEntryPath.TYPE_DELETED:
                SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "deleted-path", path, xmlBuffer);
                break;
            default:
                break;
        }
        write(xmlBuffer);
    }
}

