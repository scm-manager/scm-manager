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

import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVUpdateRequest extends DAVRequest {

    private static final DAVElement TARGET_REVISION = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "target-revision");
    private static final DAVElement SRC_PATH = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "src-path");
    private static final DAVElement DST_PATH = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "dst-path");
    private static final DAVElement UPDATE_TARGET = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "update-target");
    private static final DAVElement DEPTH = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "depth");
    private static final DAVElement RECURSIVE = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "recursive");
    private static final DAVElement SEND_COPYFROM_ARGS = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "send-copyfrom-args");
    private static final DAVElement IGNORE_ANCESTRY = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "ignore-ancestry");
    private static final DAVElement TEXT_DELTAS = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "text-deltas");
    private static final DAVElement RESOURCE_WALK = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "resource-walk");

    private boolean mySendAll = false;
    private long myRevision = DAVResource.INVALID_REVISION;
    private SVNURL mySrcURL = null;
    private SVNURL myDstURL = null;
    private String myTarget = "";
    private boolean myTextDeltas = true;
    private SVNDepth myDepth = SVNDepth.UNKNOWN;
    private boolean mySendCopyFromArgs = false;
    private boolean myDepthRequested = false;
    private boolean myRecursiveRequested = false;
    private boolean myIgnoreAncestry = false;
    private boolean myResourceWalk = false;

    private boolean myIsInitialized;


    public boolean isSendAll() {
        return mySendAll;
    }

    private void setSendAll(boolean sendAll) {
        mySendAll = sendAll;
    }

    public long getRevision() {
        return myRevision;
    }

    private void setRevision(long revision) {
        myRevision = revision;
    }

    public SVNURL getSrcURL() {
        return mySrcURL;
    }

    private void setSrcURL(SVNURL srcURL) {
        mySrcURL = srcURL;
    }

    public SVNURL getDstURL() {
        return myDstURL;
    }

    private void setDstURL(SVNURL dstURL) {
        myDstURL = dstURL;
    }

    public String getTarget() {
        return myTarget;
    }

    private void setTarget(String target) {
        myTarget = target;
    }

    public boolean isTextDeltas() {
        return myTextDeltas;
    }

    private void setTextDeltas(boolean textDeltas) {
        myTextDeltas = textDeltas;
    }

    public SVNDepth getDepth() {
        return myDepth;
    }

    private void setDepth(SVNDepth depth) {
        myDepth = depth;
    }

    public boolean isSendCopyFromArgs() {
        return mySendCopyFromArgs;
    }

    private void setSendCopyFromArgs(boolean sendCopyFromArgs) {
        mySendCopyFromArgs = sendCopyFromArgs;
    }

    public boolean isDepthRequested() {
        return myDepthRequested;
    }

    private void setDepthRequested(boolean depthRequested) {
        myDepthRequested = depthRequested;
    }

    public boolean isRecursiveRequested() {
        return myRecursiveRequested;
    }

    private void setRecursiveRequested(boolean recursiveRequested) {
        myRecursiveRequested = recursiveRequested;
    }

    public boolean isIgnoreAncestry() {
        return myIgnoreAncestry;
    }

    private void setIgnoreAncestry(boolean ignoreAncestry) {
        myIgnoreAncestry = ignoreAncestry;
    }

    public boolean isResourceWalk() {
        return myResourceWalk;
    }

    private void setResourceWalk(boolean resourceWalk) {
        myResourceWalk = resourceWalk;
    }

    private boolean isInitialized() {
        return myIsInitialized;
    }

    private void setInitialized(boolean isInitialized) {
        myIsInitialized = isInitialized;
    }

    protected void init() throws SVNException {
        if (!isInitialized()) {
            String sendAll = getRootElementAttributeValue("send-all");
            setSendAll(sendAll != null && Boolean.valueOf(sendAll).booleanValue());
            DAVElementProperty rootElement = getRootElement();
            for (Iterator iterator = rootElement.getChildren().iterator(); iterator.hasNext();) {
                DAVElementProperty property = (DAVElementProperty) iterator.next();
                DAVElement element = property.getName();
                if (element == TARGET_REVISION) {
                    assertNullCData(element, property);
                    String value = property.getFirstValue(true);
                    try {
                        setRevision(Long.parseLong(value));
                    } catch (NumberFormatException nfe) {
                        SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                    }
                } else if (element == SRC_PATH) {
                    assertNullCData(element, property);
                    String value = property.getFirstValue(false);
                    DAVPathUtil.testCanonical(value);
                    setSrcURL(SVNURL.parseURIEncoded(value));
                } else if (element == DST_PATH) {
                    assertNullCData(element, property);
                    String value = property.getFirstValue(false);
                    DAVPathUtil.testCanonical(value);
                    setDstURL(SVNURL.parseURIEncoded(value));
                } else if (element == UPDATE_TARGET) {
                    String value = property.getFirstValue(false);
                    DAVPathUtil.testCanonical(value);
                    setTarget(value);
                } else if (element == DEPTH) {
                    assertNullCData(element, property);
                    String value = property.getFirstValue(true);
                    setDepth(SVNDepth.fromString(value));
                    setDepthRequested(true);
                } else if (element == SEND_COPYFROM_ARGS) {
                    assertNullCData(element, property);
                    setSendCopyFromArgs("no".equals(property.getFirstValue(true)));
                } else if (element == RECURSIVE && !isDepthRequested()) {
                    assertNullCData(element, property);
                    String value = property.getFirstValue(true);
                    setDepth(SVNDepth.fromRecurse(!"no".equals(value)));
                    setRecursiveRequested(true);
                } else if (element == IGNORE_ANCESTRY) {
                    assertNullCData(element, property);
                    String value = property.getFirstValue(true);
                    setIgnoreAncestry(!"no".equals(value));
                } else if (element == TEXT_DELTAS) {
                    assertNullCData(element, property);
                    String value = property.getFirstValue(true);
                    setTextDeltas(!"no".equals(value));
                } else if (element == RESOURCE_WALK) {
                    assertNullCData(element, property);
                    String value = property.getFirstValue(true);
                    setResourceWalk(!"no".equals(value));
                }
            }
            if (!isDepthRequested() && !isRecursiveRequested() && (getDepth() == SVNDepth.UNKNOWN)) {
                setDepth(SVNDepth.INFINITY);
            }
            if (getSrcURL() == null) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, 
                        "The request did not contain the '<src-path>' element.\nThis may indicate that your client is too old."), SVNLogType.NETWORK);
            }
            if (!isSendAll()) {
                setTextDeltas(false);
            }
            setInitialized(true);
        }
    }
}
