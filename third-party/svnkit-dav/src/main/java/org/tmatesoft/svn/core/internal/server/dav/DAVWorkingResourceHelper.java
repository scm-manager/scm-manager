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

import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSRoot;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionInfo;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVWorkingResourceHelper extends DAVResourceHelper {

    protected void prepare(DAVResource resource) throws DAVException {
        String txnName = DAVServletUtil.getTxn(resource.getActivitiesDB(), resource.getResourceURI().getActivityID());
        if (txnName == null) {
            throw new DAVException("An unknown activity was specified in the URL. This is generally caused by a problem in the client software.", 
                    null, HttpServletResponse.SC_BAD_REQUEST, null, SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
        }

        resource.setTxnName(txnName);
        
        FSFS fsfs = resource.getFSFS(); 
        FSTransactionInfo txnInfo = null;
        try {
            txnInfo = fsfs.openTxn(txnName);
        } catch (SVNException svne) {
            if (svne.getErrorMessage().getErrorCode() == SVNErrorCode.FS_NO_SUCH_TRANSACTION) {
                throw new DAVException("An activity was specified and found, but the corresponding SVN FS transaction was not found.", 
                        null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null); 
            }
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "An activity was specified and found, but the corresponding SVN FS transaction was not found.", null);
        }
        
        resource.setTxnInfo(txnInfo);
        
        if (resource.isBaseLined()) {
            resource.setExists(true);
            return;
        }
        
        String userName = resource.getUserName();
        if (resource.getUserName() != null) {
            SVNProperties props = null;
            try {
                props = fsfs.getTransactionProperties(txnName);
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Failed to retrieve author of the SVN FS transaction corresponding to the specified activity.", null);
            }
            
            String currentAuthor = props.getStringValue(SVNRevisionProperty.AUTHOR);
            if (currentAuthor == null) {
                try {
                    fsfs.setTransactionProperty(txnName, SVNRevisionProperty.AUTHOR, SVNPropertyValue.create(userName));
                } catch (SVNException svne) {
                    throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                            "Failed to set the author of the SVN FS transaction corresponding to the specified activity.", null);
                }
            } else if (!currentAuthor.equals(userName)) {
                throw new DAVException("Multi-author commits not supported.", null, HttpServletResponse.SC_NOT_IMPLEMENTED, null, 
                        SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
            }
        }
        
        FSRoot root = null;
        try {
            root = fsfs.createTransactionRoot(txnInfo);
            resource.setRoot(root);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Could not open the (transaction) root of the repository", null);
        }
        
        SVNNodeKind kind = DAVServletUtil.checkPath(root, resource.getResourceURI().getPath());
        resource.setExists(kind != SVNNodeKind.NONE);
        resource.setCollection(kind == SVNNodeKind.DIR);
    }

    protected DAVResource getParentResource(DAVResource resource) throws DAVException {
        return DAVPrivateResourceHelper.createPrivateResource(resource, DAVResourceKind.WORKING);
    }

    public static DAVResource createWorkingResource(DAVResource baseResource, String activityID, String txnName, boolean inPlace) {
        StringBuffer pathBuffer = new StringBuffer();
        if (baseResource.isBaseLined()) {
            pathBuffer.append('/');
            pathBuffer.append(DAVResourceURI.SPECIAL_URI);
            pathBuffer.append("/wbl/");
            pathBuffer.append(activityID);
            pathBuffer.append('/');
            pathBuffer.append(baseResource.getRevision());
        } else {
            pathBuffer.append('/');
            pathBuffer.append(DAVResourceURI.SPECIAL_URI);
            pathBuffer.append("/wrk/");
            pathBuffer.append(activityID);
            pathBuffer.append(baseResource.getResourceURI().getPath());
        }
        
        String uriPath = SVNEncodingUtil.uriEncode(pathBuffer.toString());
        DAVResource resource = null;
        if (inPlace) {
            resource = baseResource;
        } else {
            resource = new DAVResource();
            baseResource.copyTo(resource);
        }
         
        
        resource.setTxnName(txnName);
        resource.setExists(true);
        resource.setVersioned(true);
        resource.setWorking(true);
        
        DAVResourceURI uri = resource.getResourceURI();
        uri.setType(DAVResourceType.WORKING);
        uri.setURI(uriPath);
        uri.setActivityID(activityID);
        return resource;
    }

 
}
