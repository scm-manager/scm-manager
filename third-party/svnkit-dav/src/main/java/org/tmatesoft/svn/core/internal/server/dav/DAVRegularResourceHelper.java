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


import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSRoot;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVRegularResourceHelper extends DAVResourceHelper {

    protected void prepare(DAVResource resource) throws DAVException {
        if (!SVNRevision.isValidRevisionNumber(resource.getRevision())) {
            try {
                resource.setRevision(resource.getLatestRevision());
            } catch (SVNException e) {
                throw DAVException.convertError(e.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not determine the proper revision to access", null);
            }
        }
        
        FSRoot root = resource.getRoot();
        FSFS fsfs = resource.getFSFS();
        if (root == null) {
            try {
                root = fsfs.createRevisionRoot(resource.getRevision());
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not open the root of the repository", null);
            }
            
            resource.setRoot(root);
        }
        
        SVNNodeKind kind = DAVServletUtil.checkPath(root, resource.getResourceURI().getPath());
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, "resource path is " + resource.getResourceURI().getPath());
        SVNDebugLog.getDefaultLog().logFine(SVNLogType.DEFAULT, "resource kind is " + kind);
        resource.setExists(kind != SVNNodeKind.NONE);
        resource.setCollection(kind == SVNNodeKind.DIR);
    }

    protected DAVResource getParentResource(DAVResource resource) throws DAVException {
        DAVResource parentResource = new DAVResource();
        resource.copyTo(parentResource);

        DAVResourceURI parentResourceURI = parentResource.getResourceURI();
        String uri = parentResourceURI.getURI();
        String path = parentResourceURI.getPath();
        
        parentResourceURI.setURI(SVNPathUtil.removeTail(uri));
        parentResourceURI.setPath(SVNPathUtil.removeTail(path));
        
        parentResource.setExists(true);
        parentResource.setCollection(true);
        parentResource.setVersioned(true);
        return parentResource;
    }

}
