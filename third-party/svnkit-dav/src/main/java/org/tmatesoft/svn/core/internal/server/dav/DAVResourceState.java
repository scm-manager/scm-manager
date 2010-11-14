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
public class DAVResourceState {

    public final static DAVResourceState NULL = new DAVResourceState(11);
    public final static DAVResourceState LOCK_NULL = new DAVResourceState(10);
    public final static DAVResourceState EXISTS = new DAVResourceState(12);
    public final static DAVResourceState ERROR = new DAVResourceState(13);
    
    private int myID;

    private DAVResourceState(int id) {
        myID = id;
    }
    
    public int getID() {
        return myID;
    }
}
