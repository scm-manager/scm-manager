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

import java.util.Date;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.util.SVNDate;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVDatedRevisionRequest extends DAVRequest {

    Date myDate = null;

    public Date getDate() {
        return myDate;
    }

    private void setDate(Date date) {
        myDate = date;
    }

    protected void init() throws SVNException {
        DAVElementProperty creationDateElement = getRootElement().getChild(DAVElement.CREATION_DATE);
        String dateString = creationDateElement.getFirstValue(true);
        setDate(SVNDate.parseDate(dateString));
    }
}
