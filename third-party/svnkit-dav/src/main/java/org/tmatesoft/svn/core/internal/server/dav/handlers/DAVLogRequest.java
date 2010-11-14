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
import java.util.LinkedList;
import java.util.List;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVLogRequest extends DAVRequest {

    private static final DAVElement DISCOVER_CHANGED_PATHS = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "discover-changed-paths");
    private static final DAVElement INCLUDE_MERGED_REVISIONS = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "include-merged-revisions");
    private static final DAVElement STRICT_NODE_HISTORY = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "strict-node-history");
    private static final DAVElement OMIT_LOG_TEXT = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "omit-log-text");
    private static final DAVElement LIMIT = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "limit");
    private static final DAVElement ALL_REVPROPS = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "all-revprops");
    private static final DAVElement REVPROP = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "revprop");

    private static final String[] DEFAULT_REVISION_PROPERTIES = new String[]{ SVNRevisionProperty.AUTHOR, SVNRevisionProperty.DATE, SVNRevisionProperty.LOG };

    private boolean myDiscoverChangedPaths = false;
    private boolean myStrictNodeHistory = false;
    private boolean myIncludeMergedRevisions = false;
    private boolean myOmitLogText = false;
    private long myStartRevision = DAVResource.INVALID_REVISION;
    private long myEndRevision = DAVResource.INVALID_REVISION;
    private long myLimit = 0;
    private Collection myTargetPathsCollection;
    private String[] myTargetPathsArray;
    private Collection myRevisionPropertiesCollection;
    private String[] myRevisionPropertiesArray;
    private boolean myCustomPropertyRequested = false;

    public boolean isDiscoverChangedPaths() {
        return myDiscoverChangedPaths;
    }

    private void setDiscoverChangedPaths(boolean discoverChangedPaths) {
        myDiscoverChangedPaths = discoverChangedPaths;
    }

    public boolean isStrictNodeHistory() {
        return myStrictNodeHistory;
    }

    private void setStrictNodeHistory(boolean strictNodeHistory) {
        myStrictNodeHistory = strictNodeHistory;
    }

    public boolean isIncludeMergedRevisions() {
        return myIncludeMergedRevisions;
    }

    private void setIncludeMergedRevisions(boolean includeMergedRevisions) {
        myIncludeMergedRevisions = includeMergedRevisions;
    }

    public boolean isOmitLogText() {
        return myOmitLogText;
    }

    private void setOmitLogText(boolean omitLogText) {
        myOmitLogText = omitLogText;
    }

    public long getStartRevision() {
        return myStartRevision;
    }

    private void setStartRevision(long startRevision) {
        myStartRevision = startRevision;
    }

    public long getEndRevision() {
        return myEndRevision;
    }

    private void setEndRevision(long endRevision) {
        myEndRevision = endRevision;
    }

    public long getLimit() {
        return myLimit;
    }

    private void setLimit(long limit) {
        myLimit = limit;
    }

    public String[] getTargetPaths() {
        if (myTargetPathsArray == null && myTargetPathsCollection != null) {
            myTargetPathsArray = (String[]) myTargetPathsCollection.toArray(new String[myTargetPathsCollection.size()]);
        }
        return myTargetPathsArray;
    }

    private void addTargetPaths(Collection targetPaths) {
        if (myTargetPathsCollection == null) {
            myTargetPathsCollection = new LinkedList();
        }
        myTargetPathsCollection.addAll(targetPaths);
    }

    public String[] getRevisionProperties() {
        if (myRevisionPropertiesArray == null && myRevisionPropertiesCollection != null) {
            myRevisionPropertiesArray = (String[]) myRevisionPropertiesCollection.toArray(new String[myRevisionPropertiesCollection.size()]);
        }
        return myRevisionPropertiesArray;
    }

    private void addRevisionProperties(Collection revisionProperties) {
        if (myRevisionPropertiesCollection == null) {
            myRevisionPropertiesCollection = new LinkedList();
        }
        myRevisionPropertiesCollection.addAll(revisionProperties);
    }

    private void setRevisionProperties(String[] revisionProperties) {
        myRevisionPropertiesArray = revisionProperties;
    }

    public boolean isCustomPropertyRequested() {
        return myCustomPropertyRequested;
    }

    private void setCustomPropertyRequested(boolean customPropertyRequested) {
        myCustomPropertyRequested = customPropertyRequested;
    }

    protected void init() throws SVNException {
        boolean revisionPropertyRequested = false;
        List children = getRootElement().getChildren();
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            DAVElementProperty property = (DAVElementProperty) iterator.next();
            DAVElement element = property.getName();
            if (element == DISCOVER_CHANGED_PATHS) {
                setDiscoverChangedPaths(true);
            } else if (element == STRICT_NODE_HISTORY) {
                setStrictNodeHistory(true);
            } else if (element == INCLUDE_MERGED_REVISIONS) {
                setIncludeMergedRevisions(true);
            } else if (element == OMIT_LOG_TEXT) {
                setOmitLogText(true);
            } else if (element == DAVElement.START_REVISION) {
                String revisionString = property.getFirstValue(true);
                try {
                    setStartRevision(Long.parseLong(revisionString));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (element == DAVElement.END_REVISION) {
                String revisionString = property.getFirstValue(true);
                try {
                    setEndRevision(Long.parseLong(revisionString));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (element == LIMIT) {
                String limitString = property.getFirstValue(true);
                setLimit(Integer.parseInt(limitString));
            } else if (element == DAVElement.PATH) {
                Collection paths = property.getValues();
                if (paths != null) {
                    addTargetPaths(paths);
                }
            } else if (element == ALL_REVPROPS) {
                setCustomPropertyRequested(true);
                revisionPropertyRequested = true;
            } else if (element == REVPROP) {
                Collection properties = property.getValues();
                addRevisionProperties(properties);
                setCustomPropertyRequested(containsCustomProperty(properties));
                revisionPropertyRequested = true;
            }
        }
        if (!revisionPropertyRequested) {
            setRevisionProperties(DEFAULT_REVISION_PROPERTIES);
        }
    }

    private boolean containsCustomProperty(Collection requestedProperties) {
        for (Iterator iterator = requestedProperties.iterator(); iterator.hasNext();) {
            String property = (String) iterator.next();
            if (!SVNRevisionProperty.AUTHOR.equals(property) && !SVNRevisionProperty.DATE.equals(property) &&
                    !SVNRevisionProperty.LOG.equals(property)) {
                return true;
            }
        }
        return false;
    }
}
