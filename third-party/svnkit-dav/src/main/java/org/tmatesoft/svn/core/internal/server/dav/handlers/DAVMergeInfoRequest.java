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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNMergeInfoInheritance;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVMergeInfoRequest extends DAVRequest {

    private static final DAVElement INHERIT = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "inherit");

    long myRevision = DAVResource.INVALID_REVISION;
    SVNMergeInfoInheritance myInherit = SVNMergeInfoInheritance.EXPLICIT;
    String[] myTargetPaths = null;

    public long getRevision() {
        return myRevision;
    }

    private void setRevision(long revision) {
        myRevision = revision;
    }

    public SVNMergeInfoInheritance getInherit() {
        return myInherit;
    }

    private void setInherit(SVNMergeInfoInheritance inherit) {
        myInherit = inherit;
    }

    public String[] getTargetPaths() {
        return myTargetPaths;
    }

    private void setTargetPaths(String[] targetPaths) {
        myTargetPaths = targetPaths;
    }

    protected void init() throws SVNException {
        List children = getRootElement().getChildren();
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            DAVElementProperty property = (DAVElementProperty) iterator.next();
            DAVElement element = property.getName();
            if (element == DAVElement.REVISION) {
                try {
                    setRevision(Long.parseLong(property.getFirstValue(true)));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (element == INHERIT) {
                setInherit(parseInheritance(property.getFirstValue(true)));
                if (getInherit() == null) {
                    invalidXML();
                }
            } else if (element == DAVElement.PATH) {
                Collection paths = property.getValues();
                String[] targetPaths = new String[paths.size()];
                targetPaths = (String[]) paths.toArray(targetPaths);
                setTargetPaths(targetPaths);
            }
        }
    }

    private SVNMergeInfoInheritance parseInheritance(String inheritance) {
        if (SVNMergeInfoInheritance.EXPLICIT.toString().equals(inheritance)) {
            return SVNMergeInfoInheritance.EXPLICIT;
        } else if (SVNMergeInfoInheritance.INHERITED.toString().equals(inheritance)) {
            return SVNMergeInfoInheritance.INHERITED;
        } else if (SVNMergeInfoInheritance.NEAREST_ANCESTOR.toString().equals(inheritance)) {
            return SVNMergeInfoInheritance.NEAREST_ANCESTOR;
        }
        return null;
    }
}
