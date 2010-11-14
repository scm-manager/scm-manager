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
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVErrorCode {

    public static final int IF_PARSE = 100;
    public static final int IF_MULTIPLE_NOT = 101;
    public static final int IF_UNK_CHAR = 102;
    public static final int IF_ABSENT = 103;
    public static final int IF_TAGGED = 104;
    public static final int IF_UNCLOSED_PAREN = 105;
    
    public static final int PROP_BAD_MAJOR = 200;
    public static final int PROP_READONLY = 201;
    public static final int PROP_NO_DATABASE = 202;
    public static final int PROP_NOT_FOUND = 203;
    public static final int PROP_BAD_LOCKDB = 204;
    public static final int PROP_OPENING = 205;
    public static final int PROP_EXEC = 206;

    public static final int LOCK_OPENDB = 400;
    public static final int LOCK_NODB = 401;
    public static final int CORRUPT_DB = 402;
    public static final int UNK_STATE_TOKEN = 403;
    public static final int PARSE_TOKEN = 404;
    public static final int LOCK_SAVE_LOCK = 405;
    
}
