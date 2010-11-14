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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.dav.handlers.BasicDAVHandler;
import org.tmatesoft.svn.core.internal.io.dav.http.HTTPHeader;
import org.tmatesoft.svn.core.internal.io.fs.FSCommitter;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSLock;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionInfo;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionRoot;
import org.tmatesoft.svn.core.internal.server.dav.DAVConfig;
import org.tmatesoft.svn.core.internal.server.dav.DAVDepth;
import org.tmatesoft.svn.core.internal.server.dav.DAVErrorCode;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVLock;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockRecType;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockScope;
import org.tmatesoft.svn.core.internal.server.dav.DAVLockType;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceHelper;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceURI;
import org.tmatesoft.svn.core.internal.server.dav.DAVServletUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVLockInfoProvider {

    public static final String LOCK_BREAK_OPTION = "lock-break";
    public static final String LOCK_STEAL_OPTION = "lock-steal";
    public static final String RELEASE_LOCKS_OPTION = "release-locks";
    public static final String KEEP_LOCKS_OPTION = "keep-locks";
    public static final String NO_MERGE_RESPONSE = "no-merge-response";
    
    private boolean myIsReadOnly;
    private boolean myIsStealLock;
    private boolean myIsBreakLock;
    private boolean myIsKeepLocks;
    private long myWorkingRevision;
    private ServletDAVHandler myOwner;
    
    public static DAVLockInfoProvider createLockInfoProvider(ServletDAVHandler owner, boolean readOnly) throws SVNException {
        String clientOptions = owner.getRequestHeader(ServletDAVHandler.SVN_OPTIONS_HEADER);
        
        DAVLockInfoProvider provider = new DAVLockInfoProvider();
        provider.myOwner = owner;
        provider.myIsReadOnly = readOnly;
        
        if (clientOptions != null) {
            if (clientOptions.indexOf(LOCK_BREAK_OPTION) != -1) {
                provider.myIsBreakLock = true;
            } 
            if (clientOptions.indexOf(LOCK_STEAL_OPTION) != -1) {
                provider.myIsStealLock = true;
            }
            if (clientOptions.indexOf(KEEP_LOCKS_OPTION) != -1) {
                provider.myIsKeepLocks = true;
            }
        }
        
        String versionName = owner.getRequestHeader(ServletDAVHandler.SVN_VERSION_NAME_HEADER);
        provider.myWorkingRevision = SVNRepository.INVALID_REVISION;
        if (versionName != null) {
            try {
                provider.myWorkingRevision = Long.parseLong(versionName);
            } catch (NumberFormatException nfe) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
            }
        }
        
        return provider;
    }

    public void addLock(DAVLock lock, DAVResource resource) throws DAVException {
        DAVDepth depth = lock.getDepth();
        if (!resource.isCollection()) {
            depth = DAVDepth.DEPTH_ZERO;
        }
        
        appendLock(resource, lock);
        
        if (depth != DAVDepth.DEPTH_ZERO) {
            DAVResourceWalker walker = new DAVResourceWalker();
            DAVLockWalker lockHandler = new DAVLockWalker(resource, lock);
            DAVResponse response = walker.walk(this, resource, null, 0, null, DAVResourceWalker.DAV_WALKTYPE_NORMAL | DAVResourceWalker.DAV_WALKTYPE_AUTH, 
                    lockHandler, DAVDepth.DEPTH_INFINITY);

            if (response != null) {
                throw new DAVException("Error(s) occurred on resources during the addition of a depth lock.", ServletDAVHandler.SC_MULTISTATUS, 0, response);
            }
        }
    }
    
    public DAVLock refreshLock(DAVResource resource, String lockToken, Date newTime) throws DAVException {
        //TODO: add here authz check
        FSFS fsfs = resource.getFSFS();
        String path = resource.getResourceURI().getPath();

        FSLock svnLock = null;
        try {
            svnLock = (FSLock) fsfs.getLockHelper(path, false);
        } catch (SVNException e) {
            throw DAVException.convertError(e.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Token doesn't point to a lock.", null);
        }
        
        if (svnLock == null || !svnLock.getID().equals(lockToken)) {
            throw new DAVException("Lock refresh request doesn't match existing lock.", HttpServletResponse.SC_UNAUTHORIZED, DAVErrorCode.LOCK_SAVE_LOCK);
        }
        
        try {
            svnLock = (FSLock) fsfs.lockPath(svnLock.getPath(), svnLock.getID(), resource.getUserName(), svnLock.getComment(), newTime, 
                    SVNRepository.INVALID_REVISION, true, svnLock.isDAVComment());
        } catch (SVNException e) {
            SVNErrorMessage err = e.getErrorMessage();
            if (err.getErrorCode() == SVNErrorCode.FS_NO_USER) {
                throw new DAVException("Anonymous lock refreshing is not allowed.", HttpServletResponse.SC_UNAUTHORIZED, DAVErrorCode.LOCK_SAVE_LOCK);
            }
            throw DAVException.convertError(err, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to refresh existing lock.", null);
        }
        return convertSVNLockToDAVLock(svnLock, false, resource.exists());
    }
    
    public void inheritLocks(DAVResource resource, boolean useParent) throws DAVException {
        DAVResource whichResource = resource;
        if (useParent) {
            DAVResource parentResource = DAVResourceHelper.createParentResource(resource);
            if (parentResource == null) {
                throw new DAVException("Could not fetch parent resource. Unable to inherit locks from the parent and apply them to this resource.", 
                        HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 0);
            }
            whichResource = parentResource;
        }
        
        DAVLock lock = getLock(whichResource);
        if (lock == null) {
            return;
        }
        
        DAVInheritWalker inheritHandler = new DAVInheritWalker(resource, lock, !useParent);
        DAVResourceWalker walker = new DAVResourceWalker();
        walker.walk(this, whichResource, null, 0, null, DAVResourceWalker.DAV_WALKTYPE_NORMAL | DAVResourceWalker.DAV_WALKTYPE_LOCKNULL, 
                inheritHandler, DAVDepth.DEPTH_INFINITY);
    }
    
    public void appendLock(DAVResource resource, DAVLock lock) throws DAVException {
        //TODO: add here authz check later
        FSFS fsfs = resource.getFSFS();
        String path = resource.getResourceURI().getPath();
        if (!resource.exists()) {
            SVNProperties revisionProps = new SVNProperties();
            revisionProps.put(SVNRevisionProperty.AUTHOR, resource.getUserName());
            DAVConfig config = resource.getRepositoryManager().getDAVConfig();
            if (resource.isSVNClient()) {
                throw new DAVException("Subversion clients may not lock nonexistent paths.", HttpServletResponse.SC_METHOD_NOT_ALLOWED, 
                        DAVErrorCode.LOCK_SAVE_LOCK);
            } else if (!config.isAutoVersioning()) {
                throw new DAVException("Attempted to lock non-existent path; turn on autoversioning first.", 
                        HttpServletResponse.SC_METHOD_NOT_ALLOWED, DAVErrorCode.LOCK_SAVE_LOCK);
            }
            
            long youngestRev = SVNRepository.INVALID_REVISION;
            try {
                youngestRev = resource.getLatestRevision();
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not determine youngest revision", null);
            }
            
            FSTransactionInfo txnInfo = null;
            try {
                txnInfo = FSTransactionRoot.beginTransactionForCommit(youngestRev, revisionProps, fsfs);
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not begin a transaction", null);
            }
            
            FSTransactionRoot root = null;
            try {
                root = fsfs.createTransactionRoot(txnInfo);
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not begin a transaction", null);
            }
            
            FSCommitter committer = new FSCommitter(fsfs, root, txnInfo, resource.getLockTokens(), resource.getUserName());
            try {
                committer.makeFile(path);
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not create empty file.", null);
            }
            
            try {
                DAVServletUtil.attachAutoRevisionProperties(txnInfo, path, fsfs);
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Could not create empty file.", null);
            }
            
            StringBuffer conflictPath = new StringBuffer();
            try {
                committer.commitTxn(true, true, null, conflictPath);
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_CONFLICT, "Conflict when committing ''{0}''.", 
                        new Object[] { conflictPath.toString() });
            }
        }
        
        FSLock svnLock = convertDAVLockToSVNLock(lock, path, resource.isSVNClient(), ServletDAVHandler.getSAXParserFactory());
        try {
            fsfs.lockPath(path, svnLock.getID(), svnLock.getOwner(), svnLock.getComment(), svnLock.getExpirationDate(), myWorkingRevision, 
                    myIsStealLock, svnLock.isDAVComment());
        } catch (SVNException svne) {
            if (svne.getErrorMessage().getErrorCode() == SVNErrorCode.FS_NO_USER) {
                throw new DAVException("Anonymous lock creation is not allowed.", HttpServletResponse.SC_UNAUTHORIZED, 
                        DAVErrorCode.LOCK_SAVE_LOCK);
            }
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Failed to create new lock.", 
                    null);
        }
        
        myOwner.setResponseHeader(HTTPHeader.CREATION_DATE_HEADER, SVNDate.formatDate(svnLock.getCreationDate()));
        myOwner.setResponseHeader(HTTPHeader.LOCK_OWNER_HEADER, svnLock.getOwner());
        //TODO: add logging here later
    }
    
    public boolean hasLocks(DAVResource resource) throws DAVException {
        if (resource.getResourceURI().getPath() == null) {
            return false;
        }
        
        if (DAVHandlerFactory.METHOD_LOCK.equals(myOwner.getRequestMethod())) {
            return false;
        }
        
        //TODO: add authz check here later
        SVNLock lock = null;
        try {
            lock = resource.getLock(); 
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Failed to check path for a lock.", null);
        }
        return lock != null;
    }
    
    public DAVLock getLock(DAVResource resource) throws DAVException {
        if (resource.getResourceURI().getPath() == null) {
            return null;
        }

        if (DAVHandlerFactory.METHOD_LOCK.equals(myOwner.getRequestMethod())) {
            return null;
        }
        
        //TODO: add authz check here later

        DAVLock davLock = null;
        FSLock lock = null;
        try {
            lock = (FSLock) resource.getLock(); 
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Failed to check path for a lock.", null);
        }
        
        if (lock != null) {
            davLock = convertSVNLockToDAVLock(lock, myIsBreakLock, resource.exists());
            myOwner.setResponseHeader(HTTPHeader.CREATION_DATE_HEADER, SVNDate.formatDate(lock.getCreationDate()));
            myOwner.setResponseHeader(HTTPHeader.LOCK_OWNER_HEADER, lock.getOwner());
        }
        return davLock;
    }
    
    public DAVLock findLock(DAVResource resource, String lockToken) throws DAVException {
        //TODO: add here authz check later
        
        DAVLock davLock = null;
        FSLock lock = null;
        try {
            lock = (FSLock) resource.getLock(); 
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Failed to look up lock by path.", null);
        }
        
        if (lock != null) {
            if (!lockToken.equals(lock.getID())) {
                throw new DAVException("Incoming token doesn't match existing lock.", HttpServletResponse.SC_BAD_REQUEST, 
                        DAVErrorCode.LOCK_SAVE_LOCK);
            }
            davLock = convertSVNLockToDAVLock(lock, false, resource.exists());
            myOwner.setResponseHeader(HTTPHeader.CREATION_DATE_HEADER, SVNDate.formatDate(lock.getCreationDate()));
            myOwner.setResponseHeader(HTTPHeader.LOCK_OWNER_HEADER, lock.getOwner());
        }
        return davLock;
    }
        
    public void removeLock(DAVResource resource, String lockToken) throws DAVException {
        DAVResourceURI resourceURI = resource.getResourceURI();
        if (resourceURI.getPath() == null) {
            return;
        }
        
        if (isKeepLocks()) {
            return;
        }
        
        //TODO: add here authz check later
        String token = null;
        SVNLock lock = null;
        if (lockToken == null) {
            try {
                lock = resource.getLock();
            } catch (SVNException svne) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Failed to check path for a lock.", null);
            }
            if (lock != null) {
                token = lock.getID();
            }
        } else {
            token = lockToken;
        }
        
        if (token != null) {
            try {
                resource.unlock(token, isBreakLock());
            } catch (SVNException svne) {
                if (svne.getErrorMessage().getErrorCode() == SVNErrorCode.FS_NO_USER) {
                    throw new DAVException("Anonymous lock removal is not allowed.", HttpServletResponse.SC_UNAUTHORIZED, 
                            DAVErrorCode.LOCK_SAVE_LOCK);
                }
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "Failed to remove a lock.", null);
            }
            //TODO: add logging here
        }
    }
    
    public String getSupportedLock(DAVResource resource) {
        if (resource.isCollection()) {
            return null;
        }
        
        StringBuffer buffer = new StringBuffer();
        buffer.append('\n');
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_ENTRY.getName(), SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_SCOPE.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.EXCLUSIVE.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING | 
                SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_SCOPE.getName(), buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_TYPE.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.WRITE.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING | 
                SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_TYPE.getName(), buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_ENTRY.getName(), buffer);
        return buffer.toString();
    }
        
    public boolean isReadOnly() {
        return myIsReadOnly;
    }
    
    public boolean isStealLock() {
        return myIsStealLock;
    }
    
    public boolean isBreakLock() {
        return myIsBreakLock;
    }
    
    public boolean isKeepLocks() {
        return myIsKeepLocks;
    }
    
    public long getWorkingRevision() {
        return myWorkingRevision;
    }
    
    public static String getActiveLockXML(DAVLock lock) {
        StringBuffer buffer = new StringBuffer();
        if (lock == null) {
            return "";
        }
        
        if (lock.getRecType() == DAVLockRecType.INDIRECT_PARTIAL) {
            //TODO: add debugging message here?
        }
        
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.ACTIVE_LOCK.getName(), SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_TYPE.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        if (lock.getType() == DAVLockType.WRITE) {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.WRITE.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING | 
                    SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        } else {
            //TODO: internal error. log something?
        }
        
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_TYPE.getName(), buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_SCOPE.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        if (lock.getScope() == DAVLockScope.EXCLUSIVE) {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.EXCLUSIVE.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING | 
                    SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        } else if (lock.getScope() == DAVLockScope.SHARED) {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.SHARED.getName(), SVNXMLUtil.XML_STYLE_SELF_CLOSING | 
                    SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        } else {
            //TODO: internal error. log something?
        }
        
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_SCOPE.getName(), buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.DEPTH.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        buffer.append(lock.getDepth() == DAVDepth.DEPTH_INFINITY ? DAVDepth.DEPTH_INFINITY.toString() : DAVDepth.DEPTH_ZERO.toString());
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.DEPTH.getName(), buffer);
        
        if (lock.getOwner() != null) {
            buffer.append(lock.getOwner());
        }
        
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_TIMEOUT.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        if (lock.getTimeOutDate() == null) {
            buffer.append("Infinite");
        } else {
            Date timeOutDate = lock.getTimeOutDate();
            long now = System.currentTimeMillis();
            long diff = timeOutDate.getTime() - now;
            buffer.append("Second-");
            //TODO: I'm not sure if I clearly understand what we must send here, 
            //have to test and review this later
            buffer.append(diff);
        }
        
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_TIMEOUT.getName(), buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_TOKEN.getName(), SVNXMLUtil.XML_STYLE_NORMAL, null, buffer);
        SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.HREF.getName(), SVNXMLUtil.XML_STYLE_PROTECT_CDATA, null, buffer);
        buffer.append(lock.getLockToken());
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.HREF.getName(), buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_TOKEN.getName(), buffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.ACTIVE_LOCK.getName(), buffer);
        return buffer.toString();
    }
    
    public static FSLock convertDAVLockToSVNLock(DAVLock davLock, String path, boolean isSVNClient, SAXParserFactory saxParserFactory) throws DAVException {
        if (davLock.getType() != DAVLockType.WRITE) {
            throw new DAVException("Only 'write' locks are supported.", HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.LOCK_SAVE_LOCK);
        }
        if (davLock.getScope() != DAVLockScope.EXCLUSIVE) {
            throw new DAVException("Only exclusive locks are supported.", HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.LOCK_SAVE_LOCK);
        }
        
        boolean isDAVComment = false;
        String comment = null;
        if (davLock.getOwner() != null) {
            if (isSVNClient) {
                try {
                    SAXParser xmlParser = saxParserFactory.newSAXParser();
                    XMLReader reader = xmlParser.getXMLReader();
                    FetchXMLHandler handler = new FetchXMLHandler(DAVElement.LOCK_OWNER);
                    reader.setContentHandler(handler);
                    reader.setDTDHandler(handler);
                    reader.setErrorHandler(handler);
                    reader.setEntityResolver(handler);
                    String owner = "<?xml version=\"1.0\" encoding=\"utf-8\"?>" + davLock.getOwner();
                    reader.parse(new InputSource(new ByteArrayInputStream(owner.getBytes())));
                    comment = handler.getData();
                } catch (ParserConfigurationException e) {
                    throw new DAVException(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DAVErrorCode.LOCK_SAVE_LOCK);
                } catch (SAXException e) {
                    throw new DAVException(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DAVErrorCode.LOCK_SAVE_LOCK);
                } catch (IOException e) {
                    throw new DAVException(e.getMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, DAVErrorCode.LOCK_SAVE_LOCK);
                }
            } else {
                isDAVComment = true;
                comment = davLock.getOwner();
            }
        }
        
        return new FSLock(path, davLock.getLockToken(), davLock.getAuthUser(), comment, new Date(System.currentTimeMillis()), 
                davLock.getTimeOutDate(), isDAVComment);
    }
    
    public static DAVLock convertSVNLockToDAVLock(FSLock lock, boolean hideAuthUser, boolean exists) {
        String authUser = null;
        StringBuffer owner = null;
        if (lock.getComment() != null) {
            owner = new StringBuffer();
            if (!lock.isDAVComment()) {
                List namespaces = new ArrayList(1);
                namespaces.add(DAVElement.DAV_NAMESPACE);
                owner = DAVXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_OWNER.getName(), namespaces, 
                        null, owner, false, false);
                owner.append(SVNEncodingUtil.xmlEncodeCDATA(lock.getComment(), true));
                owner = SVNXMLUtil.addXMLFooter(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.LOCK_OWNER.getName(), owner);
            } else {
                owner.append(lock.getComment());
            }
        }
        
        if (!hideAuthUser) {
            authUser = lock.getOwner();
        }
        
        return new DAVLock(authUser, DAVDepth.DEPTH_ZERO, exists, lock.getID(), owner != null ? owner.toString() : null, DAVLockRecType.DIRECT, 
                DAVLockScope.EXCLUSIVE, DAVLockType.WRITE, lock.getExpirationDate());
    }
    
    public static class GetLocksCallType {
        public static final GetLocksCallType RESOLVED = new GetLocksCallType();
        public static final GetLocksCallType PARTIAL = new GetLocksCallType();
        public static final GetLocksCallType COMPLETE = new GetLocksCallType();
        
        private GetLocksCallType() {
        }
    }
    
    private static class FetchXMLHandler extends BasicDAVHandler {
        private String myData;
        private DAVElement myElement;
        
        public FetchXMLHandler(DAVElement element) {
            myElement = element;
            init();
        }
        
        public String getData() {
            return myData;
        }
        
        protected void endElement(DAVElement parent, DAVElement element, StringBuffer cdata) throws SVNException {
            if (element == myElement) {
                myData = cdata.toString();
            }
        }

        protected void startElement(DAVElement parent, DAVElement element, Attributes attrs) throws SVNException {
        }
        
    }
    
}
