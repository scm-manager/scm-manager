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
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSRoot;
import org.tmatesoft.svn.core.wc.SVNRevision;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVVersionResourceHelper extends DAVResourceHelper {
    
    protected void prepare(DAVResource resource) throws DAVException {
        if (!SVNRevision.isValidRevisionNumber(resource.getRevision())) {
            try {
                resource.setRevision(resource.getLatestRevision());
            } catch (SVNException e) {
                throw DAVException.convertError(e.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not fetch 'youngest' revision to enable accessing the latest baseline resource.", null);
            }
        }
        
        FSRoot root = resource.getRoot();
        FSFS fsfs = resource.getFSFS();
        if (root == null) {
            try {
                root = fsfs.createRevisionRoot(resource.getRevision());
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not open a revision root.", null);
            }
            resource.setRoot(root);
        }
        
        resource.getResourceURI().setURI(DAVPathUtil.buildURI(null, DAVResourceKind.BASELINE, resource.getRevision(), null, false));
        resource.setExists(true);
    }

    protected DAVResource getParentResource(DAVResource resource) throws DAVException {
        DAVResourceHelper.throwIllegalGetParentResourceError(resource);
        return null;
    }

    public static DAVResource createVersionResource(DAVResource resource, String uri) throws DAVException {
        DAVResourceURI regularResourceURI = null;
        
        try {
            regularResourceURI = new DAVResourceURI(resource.getResourceURI().getContext(), uri, null, false);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Could not parse version resource uri.", null);
        }
        
        return new DAVResource(resource.getRepository(), resource.getRepositoryManager(), regularResourceURI, resource.isSVNClient(), 
                resource.getDeltaBase(), resource.getVersion(), resource.getClientOptions(), resource.getBaseChecksum(), 
                resource.getResultChecksum(), resource.getUserName(), resource.getActivitiesDB(), resource.getLockTokens(), 
                resource.getClientCapabilities());
    }
}
