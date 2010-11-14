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


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.fs.FSCommitter;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSPathChange;
import org.tmatesoft.svn.core.internal.io.fs.FSPathChangeKind;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionRoot;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionInfo;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionRoot;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceKind;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceType;
import org.tmatesoft.svn.core.internal.server.dav.DAVServlet;
import org.tmatesoft.svn.core.internal.server.dav.DAVServletUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVMergeHandler extends ServletDAVHandler {
    private DAVMergeRequest myDAVRequest;

    protected DAVMergeHandler(DAVRepositoryManager connector, HttpServletRequest request, HttpServletResponse response) {
        super(connector, request, response);
    }

    public void execute() throws SVNException {
        long readLength = readInput(false);
        if (readLength <= 0) {
            getMergeRequest().invalidXMLRoot();
        }

        DAVMergeRequest requestXMLObject = getMergeRequest();  
        DAVElementProperty rootElement = requestXMLObject.getRoot();
        DAVElementProperty sourceElement = rootElement.getChild(DAVElement.SOURCE);
        if (sourceElement == null) {
            throw new DAVException("The DAV:merge element must contain a DAV:source element.", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        DAVElementProperty hrefElement = sourceElement.getChild(DAVElement.HREF);
        if (hrefElement == null) {
            throw new DAVException("The DAV:source element must contain a DAV:href element.", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        String source = hrefElement.getFirstValue(false);
        URI uri = null; 
        try {
            uri = DAVServletUtil.lookUpURI(source, getRequest(), false);
        } catch (DAVException dave) {
            if (dave.getResponseCode() == HttpServletResponse.SC_BAD_REQUEST) {
                throw dave;
            }
            response(dave.getMessage(), DAVServlet.getStatusLine(dave.getResponseCode()), dave.getResponseCode());
        }

        String path = uri.getPath();
        DAVRepositoryManager manager = getRepositoryManager();
        String resourceContext = manager.getResourceContext();
        
        if (!path.startsWith(resourceContext)) {
            throw new DAVException("Destination url starts with a wrong context", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        //TODO: cut away the servlet context part
        path = path.substring(resourceContext.length());
        DAVResource srcResource = getRequestedDAVResource(false, false, path);

        //NOTE: for now this all are no-ops, just commented them for a while 
        //boolean noAutoMerge = rootElement.hasChild(DAVElement.NO_AUTO_MERGE);
        //boolean noCheckOut = rootElement.hasChild(DAVElement.NO_CHECKOUT);
        //DAVElementProperty propElement = rootElement.getChild(DAVElement.PROP);
        
        DAVResource resource = getRequestedDAVResource(false, false);
        if (!resource.exists()) {
            sendError(HttpServletResponse.SC_NOT_FOUND, null);
            return;
        }
        
        setResponseHeader(CACHE_CONTROL_HEADER, CACHE_CONTROL_VALUE);
        String response = null;
        try {
            response = merge(resource, srcResource);
        } catch (DAVException dave) {
            throw new DAVException("Could not MERGE resource \"{0}\" into \"{1}\".", new Object[] { SVNEncodingUtil.xmlEncodeCDATA(source), 
                    SVNEncodingUtil.xmlEncodeCDATA(getURI()) }, dave.getResponseCode(), null, SVNLogType.NETWORK, Level.FINE, dave, null, 
                    null, 0, null);
        }

        try {
            setResponseContentLength(response.getBytes(UTF8_ENCODING).length);
        } catch (UnsupportedEncodingException e) {
        }

        try {
            getResponseWriter().write(response);
        } catch (IOException ioe) {
            throw new DAVException(ioe.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, SVNErrorCode.IO_ERROR.getCode()); 
        }
    }

    protected DAVRequest getDAVRequest() {
        return getMergeRequest();
    }

    private String merge(DAVResource targetResource, DAVResource sourceResource) throws DAVException {
        boolean disableMergeResponse = false;
        if (sourceResource.getType() != DAVResourceType.ACTIVITY) {
            throw new DAVException("MERGE can only be performed using an activity as the source [at this time].", null, 
                    HttpServletResponse.SC_METHOD_NOT_ALLOWED, null, SVNLogType.NETWORK, Level.FINE, null, DAVXMLUtil.SVN_DAV_ERROR_TAG, 
                    DAVElement.SVN_DAV_ERROR_NAMESPACE, SVNErrorCode.INCORRECT_PARAMS.getCode(), null);
        }
        
        Map locks = parseLocks(getMergeRequest().getRootElement(), targetResource.getResourceURI().getPath());
        if (!locks.isEmpty()) {
            sourceResource.setLockTokens(locks.values());
        }
        
        FSFS fsfs = sourceResource.getFSFS();
        String txnName = sourceResource.getTxnName();
        FSTransactionInfo txn = DAVServletUtil.openTxn(fsfs, txnName);
        FSTransactionRoot txnRoot = null;
        try {
            txnRoot = fsfs.createTransactionRoot(txn);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Could not open a (transaction) root in the repository", null);
        }
        
        FSCommitter committer = getCommitter(sourceResource.getFSFS(), txnRoot, txn, 
                sourceResource.getLockTokens(), sourceResource.getUserName());
        
        StringBuffer buffer = new StringBuffer();
        SVNErrorMessage[] postCommitHookErr = new SVNErrorMessage[1];
        String postCommitErrMessage = null;
        long newRev = -1;
        try {
            newRev = committer.commitTxn(true, true, postCommitHookErr, buffer);
        } catch (SVNException svne) {
            if (postCommitHookErr[0] == null) {
                try {
                    FSCommitter.abortTransaction(fsfs, txnName);
                } catch (SVNException svne1) {
                    //
                }
                
                if (svne.getErrorMessage().getErrorCode() == SVNErrorCode.FS_CONFLICT) {
                    throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_CONFLICT, 
                            "A conflict occurred during the MERGE processing. The problem occurred with the \"{0}\" resource.",  
                            new Object[] { buffer.toString() });
                } 

                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_CONFLICT, 
                        "An error occurred while committing the transaction.", null);
            }
        }
        
        if (postCommitHookErr[0] != null) {
            SVNErrorMessage childErr = postCommitHookErr[0].getChildErrorMessage(); 
            if (childErr != null && childErr.getMessage() != null) {
                postCommitErrMessage = childErr.getMessage();
            }
        }
        
        //TODO: maybe add logging here
        
        DAVServletUtil.storeActivity(sourceResource, "");
        String clientOptions = sourceResource.getClientOptions(); 
        if (clientOptions != null) {
            if (clientOptions.indexOf(DAVLockInfoProvider.RELEASE_LOCKS_OPTION) != -1 && !locks.isEmpty()) {
                for (Iterator locksIter = locks.keySet().iterator(); locksIter.hasNext();) {
                    String path = (String) locksIter.next();
                    String lockToken = (String) locks.get(path);
                    try {
                        fsfs.unlockPath(path, lockToken, sourceResource.getUserName(), false, true);
                    } catch (SVNException svne) {
                        // TODO: ignore exceptions. maybe add logging
                    }
                }
            }
            
            if (clientOptions.indexOf(DAVLockInfoProvider.NO_MERGE_RESPONSE) != -1) {
                disableMergeResponse = true;
            }
        }
        
        return response(fsfs, newRev, postCommitErrMessage, disableMergeResponse);
    }
    
    private String response(FSFS fsfs, long newRev, String postCommitErr, boolean disableMergeResponse) throws DAVException {
        FSRevisionRoot root = null;
        try {
            root = fsfs.createRevisionRoot(newRev);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Could not open the FS root for the revision just committed.", null);
        }
        
        String vcc = DAVPathUtil.buildURI(getRepositoryManager().getResourceContext(), DAVResourceKind.VCC, -1, null, false);
        
        StringBuffer buffer = new StringBuffer();
        Map prefixMap = new HashMap();
        Collection namespaces = new LinkedList();
        namespaces.add(DAVElement.DAV_NAMESPACE);
        prefixMap.put(DAVElement.DAV_NAMESPACE, SVNXMLUtil.DAV_NAMESPACE_PREFIX);
        String postCommitErrElement = null;
        if (postCommitErr != null) {
            namespaces.add(DAVElement.SVN_NAMESPACE);
            prefixMap.put(DAVElement.SVN_NAMESPACE, SVNXMLUtil.SVN_NAMESPACE_PREFIX);
            
            SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, DAVElement.POST_COMMIT_ERROR.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, 
                    null, buffer);
            buffer.append(postCommitErr);
            SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, DAVElement.POST_COMMIT_ERROR.getName(), buffer);
            
            postCommitErrElement = buffer.toString();
            buffer.delete(0, buffer.length());
        } else {
            postCommitErrElement = "";
        }
        
        String creationDate = null;
        String creatorDisplayName = null;
        
        try {
            SVNProperties revProps = fsfs.getRevisionProperties(newRev);
            creationDate = revProps.getStringValue(SVNRevisionProperty.DATE);
            creatorDisplayName = revProps.getStringValue(SVNRevisionProperty.AUTHOR);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Could not get author of newest revision", null);
        }

        SVNXMLUtil.addXMLHeader(buffer);
        SVNXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.MERGE_RESPONSE.getName(), namespaces, prefixMap, 
                null, buffer, true);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.UPDATE_SET.getName(), SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESPONSE.getName(), SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        
        SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.HREF.getName(), vcc, null, true, true, buffer);
        
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(), SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESOURCE_TYPE.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.BASELINE.getName(), 
                SVNXMLUtil.XML_STYLE_SELF_CLOSING | SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESOURCE_TYPE.getName(), buffer);
        buffer.append(postCommitErrElement);
        buffer.append('\n');
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.VERSION_NAME.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        buffer.append(newRev);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.VERSION_NAME.getName(), buffer);
        
        if (creationDate != null ) {
            SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.CREATION_DATE.getName(), creationDate, null, true, true, buffer);
        }
        
        if (creatorDisplayName != null ) {
            SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.CREATOR_DISPLAY_NAME.getName(), creatorDisplayName, null, true, 
                    true, buffer);
        }
        
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        buffer.append("HTTP/1.1 200 OK");
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(), buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(), buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESPONSE.getName(), buffer);
        if (!disableMergeResponse) {
            try {
                doResources(root, buffer);
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Error constructing resource list.", null);
            }
        }
        
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.UPDATE_SET.getName(), buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.MERGE_RESPONSE.getName(), buffer);
        return buffer.toString();
    }
    
    private void doResources(FSRevisionRoot root, StringBuffer buffer) throws SVNException {
        Map changedPaths = root.getChangedPaths();
        Map sentPaths = new HashMap();
        for (Iterator pathsIter = changedPaths.keySet().iterator(); pathsIter.hasNext();) {
            String path = (String) pathsIter.next();
            FSPathChange pathChange = (FSPathChange) changedPaths.get(path);
            boolean sendSelf = false;
            boolean sendParent = false;
            FSPathChangeKind changeKind = pathChange.getChangeKind();
            if (changeKind == FSPathChangeKind.FS_PATH_CHANGE_DELETE) {
                sendSelf = false;
                sendParent = true;
            } else if (changeKind == FSPathChangeKind.FS_PATH_CHANGE_ADD || changeKind == FSPathChangeKind.FS_PATH_CHANGE_REPLACE) {
                sendSelf = true;
                sendParent = true;
            } else {
                sendSelf = true;
                sendParent = false;
            }
            
            if (sendSelf) {
                if (!sentPaths.containsKey(path)) {
                    SVNNodeKind pathKind = root.checkNodeKind(path);
                    sendResponse(root, path, pathKind == SVNNodeKind.DIR, buffer);
                    sentPaths.put(path, path);
                }
            }
            
            if (sendParent) {
                String parentPath = SVNPathUtil.removeTail(path);
                if (!sentPaths.containsKey(parentPath)) {
                    sendResponse(root, parentPath, true, buffer);
                    sentPaths.put(parentPath, parentPath);
                }
            }
        }
    }
    
    private void sendResponse(FSRevisionRoot root, String path, boolean isDir, StringBuffer buffer) {
        
        String context = getRepositoryManager().getResourceContext();
        
        String href = DAVPathUtil.buildURI(context, DAVResourceKind.PUBLIC, -1, path, false);
        long revToUse = DAVServletUtil.getSafeCreatedRevision(root, path);
        String vsnURL = DAVPathUtil.buildURI(context, DAVResourceKind.VERSION, revToUse, path, false);
        
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESPONSE.getName(), 
                SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        
        SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.HREF.getName(), href, null, true, true, buffer);
        
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        
        if (isDir) {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESOURCE_TYPE.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, 
                    buffer);
            SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.COLLECTION.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING | 
                    SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
            SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESOURCE_TYPE.getName(), buffer);
        } else {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESOURCE_TYPE.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING | 
                    SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        }
        
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.CHECKED_IN.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        
        SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.HREF.getName(), vsnURL, null, true, true, buffer);
        
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.CHECKED_IN.getName(), buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROP.getName(), buffer);
        
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        buffer.append("HTTP/1.1 200 OK");
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.STATUS.getName(), buffer);
        
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.PROPSTAT.getName(), buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.RESPONSE.getName(), buffer);
    }
    
    private DAVMergeRequest getMergeRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVMergeRequest();
        }
        return myDAVRequest;
    }

}
