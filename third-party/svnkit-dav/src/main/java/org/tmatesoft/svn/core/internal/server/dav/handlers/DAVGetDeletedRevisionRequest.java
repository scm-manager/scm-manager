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

import java.util.Iterator;
import java.util.List;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class DAVGetDeletedRevisionRequest extends DAVRequest {
    private long myPegRevision;
    private long myEndRevision;
    private String myPath;
    
    public long getPegRevision() {
        return myPegRevision;
    }

    public long getEndRevision() {
        return myEndRevision;
    }
    
    public String getPath() {
        return myPath;
    }
    
    protected void init() throws SVNException {
        DAVElementProperty rootElement = getRootElement();
        List rootChildren = rootElement.getChildren();
        if (rootChildren != null) {
            for (Iterator childrenIter = rootChildren.iterator(); childrenIter.hasNext();) {
                DAVElementProperty childElement = (DAVElementProperty) childrenIter.next();
                DAVElement childElementName = childElement.getName();
                if (!DAVElement.SVN_NAMESPACE.equals(childElementName.getNamespace())) {
                    continue;
                }
                
                if (childElementName == DAVElement.PEG_REVISION) {
                    try {
                        myPegRevision = Long.parseLong(childElement.getFirstValue(true));
                    } catch (NumberFormatException nfe) {
                        SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                    }
                } else if (childElementName == DAVElement.END_REVISION) {
                    try {
                        myEndRevision = Long.parseLong(childElement.getFirstValue(true));
                    } catch (NumberFormatException nfe) {
                        SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                    }
                } else if (childElementName == DAVElement.PATH) {
                    String path = childElement.getFirstValue(false);
                    DAVPathUtil.testCanonical(path);
                    myPath = path;
                }
            }
        }
    }
}
