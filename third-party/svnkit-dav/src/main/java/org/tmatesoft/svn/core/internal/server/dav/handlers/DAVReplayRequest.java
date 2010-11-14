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
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVReplayRequest extends DAVRequest {

    private static final DAVElement EDITOR_REPORT = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "editor-report");

    private static final DAVElement LOW_WATER_MARK = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "low-water-mark");
    private static final DAVElement SEND_DELTAS = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "send-deltas");

    boolean myIsSendDeltas = true;
    long myLowRevision = DAVResource.INVALID_REVISION;
    long myRevision = DAVResource.INVALID_REVISION;

    public boolean isSendDeltas() {
        return myIsSendDeltas;
    }

    private void setSendDeltas(boolean isSendDelta) {
        myIsSendDeltas = isSendDelta;
    }

    public long getLowRevision() {
        return myLowRevision;
    }

    private void setLowRevision(long lowRevision) {
        myLowRevision = lowRevision;
    }

    public long getRevision() {
        return myRevision;
    }

    private void setRevision(long revision) {
        myRevision = revision;
    }

    protected void init() throws SVNException {
        DAVElementProperty rootElement = getRootElement();
        rootElement.setElementName(EDITOR_REPORT);
        List children = rootElement.getChildren();
        for (Iterator iterator = children.iterator(); iterator.hasNext();) {
            DAVElementProperty property = (DAVElementProperty) iterator.next();
            DAVElement element = property.getName();
            if (element == DAVElement.REVISION) {
                assertNullCData(element, property);
                try {
                    setRevision(Long.parseLong(property.getFirstValue(true)));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (element == LOW_WATER_MARK) {
                assertNullCData(element, property);
                try {
                    setLowRevision(Long.parseLong(property.getFirstValue(true)));
                } catch (NumberFormatException nfe) {
                    SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
                }
            } else if (element == SEND_DELTAS) {
                assertNullCData(element, property);
                int sendDeltas = Integer.parseInt(property.getFirstValue(true));
                setSendDeltas(sendDeltas != 0);
            }
        }
        if (!SVNRevision.isValidRevisionNumber(getRevision())) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "Request was missing the revision argument."), SVNLogType.NETWORK);
        }
        if (!SVNRevision.isValidRevisionNumber(getLowRevision())) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "Request was missing the low-water-mark argument."), SVNLogType.NETWORK);
        }
    }
}
