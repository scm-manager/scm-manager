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
package org.tmatesoft.svn.core.internal.server.dav;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVResourceURI {

    public static final String SPECIAL_URI = "!svn";
    public static final String DEDAULT_VCC_NAME = "default";

    private String myURI;
    private String myContext;
    private DAVResourceType myType;
    private DAVResourceKind myKind;
    private long myRevision;
    private String myPath;
    private String myActivityID;
    private boolean myIsExists = false;
    private boolean myIsVersioned = false;
    private boolean myIsBaseLined = false;
    private boolean myIsWorking = false;

    public DAVResourceURI(String context, String uri, String label, boolean useCheckedIn) throws SVNException {
        StringBuffer logBuffer = new StringBuffer();
        logBuffer.append('\n');
        logBuffer.append("uri: " + uri);
        logBuffer.append('\n');
        logBuffer.append("label: " + label);
        logBuffer.append('\n');
        logBuffer.append("context: " + context);
        
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, logBuffer.toString());
        
        myURI = uri == null ? "" : uri;
        myContext = context;
        myRevision = DAVResource.INVALID_REVISION;
        parseURI(label, useCheckedIn);

        logBuffer.delete(0, logBuffer.length());
        logBuffer.append('\n');
        logBuffer.append("DAVResourceURI.getRequestURI(): " + getRequestURI());
        logBuffer.append('\n');
        logBuffer.append("DAVResourceURI.getURI(): " + getURI());
        logBuffer.append('\n');
        logBuffer.append("DAVResourceURI.getPath(): " + getPath());
        logBuffer.append('\n');
        logBuffer.append("DAVResourceURI.getContext(): " + getContext());
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, logBuffer.toString());
    }

    public DAVResourceURI(String context, String uri, String path, long revision, DAVResourceKind kind, DAVResourceType type, String activityID, 
            boolean exists, boolean isVersioned, boolean isBaseLined, boolean isWorking) {
        myContext = context;
        myURI = uri;
        myPath = path;
        myActivityID = activityID;
        myRevision = revision;
        myType = type;
        myKind = kind;
        myIsExists = exists;
        myIsVersioned = isVersioned;
        myIsBaseLined = isBaseLined;
        myIsWorking = isWorking;
    }
    
    public DAVResourceURI() {
    }
    
    public DAVResourceURI dup() {
        return new DAVResourceURI(myContext, myURI, myPath, myRevision, myKind, myType, myActivityID, myIsExists, myIsVersioned, myIsBaseLined, 
                myIsWorking);
    }
    
    public String getRequestURI() {
        return SVNPathUtil.append(getContext(), getURI());
    }

    public String getContext() {
        return myContext;
    }

    public String getURI() {
        return myURI;
    }

    public void setURI(String uri) {
        myURI = uri;
    }

    public DAVResourceType getType() {
        return myType;
    }

    public DAVResourceKind getKind() {
        return myKind;
    }

    public long getRevision() {
        return myRevision;
    }

    public String getPath() {
        return myPath;
    }

    public String getActivityID() {
        return myActivityID;
    }

    public boolean exists() {
        return myIsExists;
    }

    public boolean isVersioned() {
        return myIsVersioned;
    }

    public boolean isBaseLined() {
        return myIsBaseLined;
    }

    public boolean isWorking() {
        return myIsWorking;
    }

    public void setExists(boolean isExist) {
        myIsExists = isExist;
    }

    public void setPath(String path) {
        myPath = DAVPathUtil.standardize(path);
    }

    public void setVersioned(boolean isVersioned) {
        myIsVersioned = isVersioned;
    }

    public void setKind(DAVResourceKind kind) {
        myKind = kind;
    }

    public void setType(DAVResourceType type) {
        myType = type;
    }

    public void setRevision(long revisionNumber) {
        myRevision = revisionNumber;
    }

    public void setWorking(boolean isWorking) {
        myIsWorking = isWorking;
    }

    public void setActivityID(String activityID) {
        myActivityID = activityID;
    }

    public void setBaseLined(boolean isBaseLined) {
        myIsBaseLined = isBaseLined;
    }

    private void parseURI(String label, boolean useCheckedIn) throws SVNException {
        if (!SPECIAL_URI.equals(DAVPathUtil.head(getURI()))) {
            setKind(DAVResourceKind.PUBLIC);
            setType(DAVResourceType.REGULAR);
            setPath(getURI());
            setVersioned(true);
        } else {
            String specialPart = DAVPathUtil.removeHead(getURI(), false);
            if (specialPart.length() == 0) {
                // root/!svn
                setType(DAVResourceType.PRIVATE);
                setKind(DAVResourceKind.ROOT_COLLECTION);
            } else {
                specialPart = DAVPathUtil.dropLeadingSlash(specialPart);
                if (!specialPart.endsWith("/") && SVNPathUtil.getSegmentsCount(specialPart) == 1) {
                    // root/!svn/XXX
                    setType(DAVResourceType.PRIVATE);
                } else {
                    DAVResourceKind kind = DAVResourceKind.parseKind(DAVPathUtil.head(specialPart));
                    if (kind != DAVResourceKind.UNKNOWN) {
                        setKind(kind);
                        String parameter = DAVPathUtil.removeHead(specialPart, false);
                        parameter = DAVPathUtil.dropLeadingSlash(parameter);
                        if (kind == DAVResourceKind.VCC) {
                            parseVCC(parameter, label, useCheckedIn);
                        } else if (kind == DAVResourceKind.VERSION) {
                            parseVersion(parameter);
                        } else if (kind == DAVResourceKind.BASELINE) {
                            parseBaseline(parameter);
                        } else if (kind == DAVResourceKind.BASELINE_COLL) {
                            parseBaselineCollection(parameter);
                        } else if (kind == DAVResourceKind.ACT_COLLECTION) {
                            parseActivity(parameter);
                        } else if (kind == DAVResourceKind.HISTORY) {
                            parseHistory(parameter);
                        } else if (kind == DAVResourceKind.WRK_BASELINE) {
                            parseWorkingBaseline(parameter);
                        } else if (kind == DAVResourceKind.WORKING) {
                            parseWorking(parameter);
                        }
                    }
                }
            }
        }
    }

    private void parseWorking(String parameter) {
        setType(DAVResourceType.WORKING);
        setVersioned(true);
        setWorking(true);
        if (SVNPathUtil.getSegmentsCount(parameter) == 1) {
            setActivityID(parameter);
            setPath("/");
        } else {
            setActivityID(DAVPathUtil.head(parameter));
            setPath(DAVPathUtil.removeHead(parameter, false));
        }
    }

    private void parseWorkingBaseline(String parameter) throws SVNException {
        setType(DAVResourceType.WORKING);
        setWorking(true);
        setVersioned(true);
        setBaseLined(true);
        if (SVNPathUtil.getSegmentsCount(parameter) == 1) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_ILLEGAL_URL, "Invalid URI ''{0}''", getRequestURI()), SVNLogType.NETWORK);
        }
        setActivityID(DAVPathUtil.head(parameter));
        try {
            String revisionParameter = DAVPathUtil.removeHead(parameter, false);
            long revision = Long.parseLong(DAVPathUtil.dropLeadingSlash(revisionParameter));
            setRevision(revision);
        } catch (NumberFormatException e) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_ILLEGAL_URL, e), e, SVNLogType.NETWORK);
        }
    }

    private void parseHistory(String parameter) {
        setType(DAVResourceType.HISTORY);
        setPath(parameter);
    }

    private void parseActivity(String parameter) {
        setType(DAVResourceType.ACTIVITY);
        setActivityID(parameter);
    }

    private void parseBaselineCollection(String parameter) throws SVNException {
        long revision = DAVResource.INVALID_REVISION;
        String parameterPath;
        if (SVNPathUtil.getSegmentsCount(parameter) == 1) {
            parameterPath = "/";
            try {
                revision = Long.parseLong(parameter);
            } catch (NumberFormatException e) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_ILLEGAL_URL, e.getMessage()), e, SVNLogType.NETWORK);
            }
        } else {
            try {
                revision = Long.parseLong(DAVPathUtil.head(parameter));
            } catch (NumberFormatException e) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_ILLEGAL_URL, e.getMessage()), e, SVNLogType.NETWORK);
            }
            parameterPath = DAVPathUtil.removeHead(parameter, false);
        }
        setType(DAVResourceType.REGULAR);
        setVersioned(true);
        setRevision(revision);
        setPath(parameterPath);
    }

    private void parseBaseline(String parameter) throws SVNException {
        try {
            setRevision(Long.parseLong(parameter));
        } catch (NumberFormatException e) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_ILLEGAL_URL, e.getMessage()), e, SVNLogType.NETWORK);
        }
        setVersioned(true);
        setBaseLined(true);
        setType(DAVResourceType.VERSION);
    }

    private void parseVersion(String parameter) throws SVNException {
        setVersioned(true);
        setType(DAVResourceType.VERSION);
        if (SVNPathUtil.getSegmentsCount(parameter) == 1) {
            try {
                setRevision(Long.parseLong(parameter));
            } catch (NumberFormatException e) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_ILLEGAL_URL, "Invalid URI ''{0}''", e.getMessage()), e, SVNLogType.NETWORK);
            }
            setPath("/");
        } else {
            try {
                setRevision(Long.parseLong(DAVPathUtil.head(parameter)));
            } catch (NumberFormatException e) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_ILLEGAL_URL, e.getMessage()), e, SVNLogType.NETWORK);
            }
            setPath(DAVPathUtil.removeHead(parameter, false));
        }
    }

    private void parseVCC(String parameter, String label, boolean useCheckedIn) throws SVNException {
        if (!DEDAULT_VCC_NAME.equals(parameter)) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "Invalid VCC name ''{0}''", parameter), SVNLogType.NETWORK);
        }
        if (label == null && !useCheckedIn) {
            setType(DAVResourceType.PRIVATE);
            setExists(true);
            setVersioned(true);
            setBaseLined(true);
        } else {
            long revision = DAVResource.INVALID_REVISION;
            if (label != null) {
                try {
                    revision = Long.parseLong(label);
                } catch (NumberFormatException e) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_ILLEGAL_URL, "Invalid label header ''{0}''", label), SVNLogType.NETWORK);
                }
            }
            setType(DAVResourceType.VERSION);
            setRevision(revision);
            setVersioned(true);
            setBaseLined(true);
            setPath(null);
        }
    }
}
