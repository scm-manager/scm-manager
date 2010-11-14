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
public class DAVIFState {
    
    public static final int IF_CONDITION_NORMAL = 0;
    public static final int IF_CONDITION_NOT = 1;
    
    private DAVIFStateType myType;
    private String myETag;
    private String myLockToken;
    private int myCondition;
    
    public DAVIFState(int condition, String tag, String lockToken, DAVIFStateType type) {
        myCondition = condition;
        myETag = tag;
        myType = type;
        myLockToken = lockToken;
    }
    
    public DAVIFStateType getType() {
        return myType;
    }
    
    public String getLockToken() {
        return myLockToken;
    }

    public String getETag() {
        return myETag;
    }

    public int getCondition() {
        return myCondition;
    }
    
}
