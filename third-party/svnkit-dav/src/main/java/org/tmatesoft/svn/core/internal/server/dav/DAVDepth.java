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
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVDepth {

    public static final DAVDepth DEPTH_ZERO = new DAVDepth(0, "0");
    public static final DAVDepth DEPTH_ONE = new DAVDepth(1, "1");
    public static final DAVDepth DEPTH_INFINITY = new DAVDepth(Integer.MAX_VALUE, "infinity");

    private int myID;
    private String myName;

    private DAVDepth(int id, String name) {
        myID = id;
        myName = name;
    }

    public int getID() {
        return myID;
    }

    public String toString() {
        return myName;
    }

    public static DAVDepth parseDepth(String depth) {
        if (DAVDepth.DEPTH_INFINITY.toString().equalsIgnoreCase(depth)) {
            return DAVDepth.DEPTH_INFINITY;
        } else if (DAVDepth.DEPTH_ZERO.toString().equalsIgnoreCase(depth)) {
            return DAVDepth.DEPTH_ZERO;
        } else if (DAVDepth.DEPTH_ONE.toString().equalsIgnoreCase(depth)) {
            return DAVDepth.DEPTH_ONE;
        }
        return null;
    }

    public static DAVDepth decreaseDepth(DAVDepth currentDepth) throws SVNException {
        if (currentDepth == null) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_MALFORMED_DATA, "Depth is not specified."), SVNLogType.NETWORK);
        }
        if (currentDepth == DEPTH_ZERO || currentDepth == DEPTH_INFINITY) {
            return currentDepth;
        }
        return DAVDepth.DEPTH_ZERO;
    }
}
