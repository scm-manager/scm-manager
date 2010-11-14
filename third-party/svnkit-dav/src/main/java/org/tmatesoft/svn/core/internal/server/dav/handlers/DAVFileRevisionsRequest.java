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
import java.util.List;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVFileRevisionsRequest extends DAVRequest {

    private String myPath;
    private long myStartRevision = DAVResource.INVALID_REVISION;
    private long myEndRevision = DAVResource.INVALID_REVISION;
    private boolean myIsIncludeMergedRevisions;
    
    public String getPath() {
        return myPath;
    }

    public long getStartRevision() {
        return myStartRevision;
    }

    public long getEndRevision() {
        return myEndRevision;
    }
    
    public boolean isIncludeMergedRevisions() {
        return myIsIncludeMergedRevisions;
    }

    protected void init() throws SVNException {
        DAVElementProperty rootElement = getRootElement();
        List children = rootElement.getChildren();
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            DAVElementProperty childElement = (DAVElementProperty) iterator.next();
            DAVElement childElementName = childElement.getName();
            if (!DAVElement.SVN_NAMESPACE.equals(childElementName.getNamespace())) {
                continue;
            }
            if (childElementName == DAVElement.PATH) {
                String path = childElement.getFirstValue(false);
                DAVPathUtil.testCanonical(path);
                myPath = path;
            } else if (childElementName == DAVElement.START_REVISION) {
                try {
                    myStartRevision = Long.parseLong(childElement.getFirstValue(true));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (childElementName == DAVElement.END_REVISION) {
                try {
                    myEndRevision = Long.parseLong(childElement.getFirstValue(true));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (childElementName == DAVElement.INCLUDE_MERGED_REVISIONS) {
                myIsIncludeMergedRevisions = true;
            }
        }
    }
}
