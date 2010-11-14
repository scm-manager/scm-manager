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

import org.tmatesoft.svn.core.io.SVNRepository;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVPrivateResourceHelper extends DAVResourceHelper {

    protected void prepare(DAVResource resource) throws DAVException {
    }

    protected DAVResource getParentResource(DAVResource resource) throws DAVException {
        DAVResourceHelper.throwIllegalGetParentResourceError(resource);
        return null;
    }

    public static DAVResource createPrivateResource(DAVResource resource, DAVResourceKind resourceKind) {
        DAVResource privateResource = new DAVResource();
        resource.copyTo(privateResource);
        
        DAVResourceURI resourceURI = privateResource.getResourceURI();
        resourceURI.setKind(resourceKind);
        resourceURI.setType(DAVResourceType.PRIVATE);

        String path = "/" + DAVResourceURI.SPECIAL_URI + "/" + resourceKind.toString();
        resourceURI.setURI(path);
        resourceURI.setPath(null);
        
        privateResource.setCollection(true);
        privateResource.setExists(true);
        privateResource.setRevision(SVNRepository.INVALID_REVISION);
        return privateResource; 
    }

}
