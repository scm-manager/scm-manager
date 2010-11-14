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
public class DAVHistoryResourceHelper extends DAVResourceHelper {

    protected void prepare(DAVResource resource) throws DAVException {
    }

    public DAVResource getParentResource(DAVResource resource) throws DAVException {
        DAVResourceHelper.throwIllegalGetParentResourceError(resource);
        return null;
    }

}
