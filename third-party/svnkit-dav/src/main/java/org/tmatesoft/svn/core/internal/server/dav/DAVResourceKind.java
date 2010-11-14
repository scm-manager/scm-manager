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

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVResourceKind {
    public static final DAVResourceKind ACT_COLLECTION = new DAVResourceKind("act");
    public static final DAVResourceKind BASELINE = new DAVResourceKind("bln");
    public static final DAVResourceKind BASELINE_COLL = new DAVResourceKind("bc");
    public static final DAVResourceKind HISTORY = new DAVResourceKind("his");
    public static final DAVResourceKind WORKING = new DAVResourceKind("wrk");
    public static final DAVResourceKind PUBLIC = new DAVResourceKind("");
    public static final DAVResourceKind VERSION = new DAVResourceKind("ver");
    public static final DAVResourceKind VCC = new DAVResourceKind("vcc");
    public static final DAVResourceKind WRK_BASELINE = new DAVResourceKind("wbl");
    public static final DAVResourceKind ROOT_COLLECTION = new DAVResourceKind("rc");
    public static final DAVResourceKind UNKNOWN = new DAVResourceKind(null);

    private String myKind;

    private DAVResourceKind(String kind) {
        myKind = kind;
    }

    public String toString() {
        return myKind;
    }

    public static DAVResourceKind parseKind(String kind) {
        if ("act".equals(kind)) {
            return ACT_COLLECTION;
        } else if ("bln".equals(kind)) {
            return BASELINE;
        } else if ("bc".equals(kind)) {
            return BASELINE_COLL;
        } else if ("".equals(kind)) {
            return PUBLIC;
        } else if ("ver".equals(kind)) {
            return VERSION;
        } else if ("his".equals(kind)) {
            return HISTORY;
        } else if ("wrk".equals(kind)) {
            return WORKING;
        } else if ("wbl".equals(kind)) {
            return WRK_BASELINE;
        } else if ("vcc".equals(kind)) {
            return VCC;
        }
        return UNKNOWN;
    }
}
