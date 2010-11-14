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

import java.io.File;
import java.io.OutputStream;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.logging.Level;

import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.ISVNDirEntryHandler;
import org.tmatesoft.svn.core.SVNDirEntry;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNLock;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSRepository;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionNode;
import org.tmatesoft.svn.core.internal.io.fs.FSRoot;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionInfo;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNEncodingUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.util.SVNDebugLog;
import org.tmatesoft.svn.util.SVNLogType;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVResource {

    public static final long INVALID_REVISION = SVNRepository.INVALID_REVISION;

    public static final String DEFAULT_COLLECTION_CONTENT_TYPE = "text/html; charset=\"utf-8\"";
    public static final String DEFAULT_FILE_CONTENT_TYPE = "text/plain";

    private DAVRepositoryManager myRepositoryManager;
    private DAVResourceURI myResourceURI;
    private FSRepository myRepository;
    private long myRevision;
    private long myVersion;
    private boolean myIsCollection;
    private boolean myIsSVNClient;
    private boolean myIsAutoCheckedOut;
    private String myDeltaBase;
    private String myClientOptions;
    private String myBaseChecksum;
    private String myResultChecksum;
    private String myUserName;
    private SVNProperties mySVNProperties;
    private Collection myDeadProperties;
    private Collection myEntries;
    private File myActivitiesDB;
    private FSFS myFSFS;
    private String myTxnName;
    private FSRoot myRoot;
    private FSTransactionInfo myTxnInfo;
    private Map myClientCapabilities;
    private Collection myLockTokens;
    
    /**
     * DAVResource  constructor
     *
     * @param repository   repository resource connect to
     * @param context      contains requested url requestContext and name of repository if servlet use SVNParentPath directive.
     * @param uri          special uri for DAV requests can be /path or /SPECIAL_URI/xxx/path
     * @param label        request's label header
     * @param useCheckedIn special case for VCC resource
     * @throws SVNException if an error occurs while fetching repository properties.
     */
    public DAVResource(SVNRepository repository, DAVRepositoryManager manager, DAVResourceURI resourceURI, boolean isSVNClient, String deltaBase, long version, 
            String clientOptions, String baseChecksum, String resultChecksum, String userName, File activitiesDB, 
            Collection lockTokens, Map clientCapabilities) throws DAVException {
        myRepositoryManager = manager;
        myRepository = (FSRepository) repository;
        try {
            myRepository.testConnection();//this should create an FSFS object
        } catch (SVNException svne) {
            SVNDebugLog.getDefaultLog().logFine(SVNLogType.FSFS, svne.getMessage());
            SVNErrorMessage err = SVNErrorMessage.create(svne.getErrorMessage().getErrorCode(), "Could not open the requested SVN filesystem");
            throw DAVException.convertError(err, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "Could not fetch resource information.", null);
        }
        myLockTokens = lockTokens;
        myClientCapabilities = clientCapabilities;
        myFSFS = myRepository.getFSFS();
        myResourceURI = resourceURI;
        myIsSVNClient = isSVNClient;
        myDeltaBase = deltaBase;
        myVersion = version;
        myClientOptions = clientOptions;
        myBaseChecksum = baseChecksum;
        myResultChecksum = resultChecksum;
        myRevision = resourceURI.getRevision();
        myUserName = userName;
        myActivitiesDB = activitiesDB;
        DAVResourceHelper.prepareResource(this);
    }

    public DAVResource(DAVRepositoryManager manager, SVNRepository repository, DAVResourceURI resourceURI, long revision, boolean isSVNClient, String deltaBase, 
            long version, String clientOptions, String baseChecksum, String resultChecksum, String userName, File activitiesDB, 
            Collection lockTokens, Map clientCapabilities) {
        myRepositoryManager = manager;
        myResourceURI = resourceURI;
        myRepository = (FSRepository) repository;
        myFSFS = myRepository.getFSFS();
        myRevision = revision;
        myIsSVNClient = isSVNClient;
        myDeltaBase = deltaBase;
        myVersion = version;
        myClientOptions = clientOptions;
        myBaseChecksum = baseChecksum;
        myResultChecksum = resultChecksum;
        myUserName = userName;
        myActivitiesDB = activitiesDB;
        myLockTokens = lockTokens;
        myClientCapabilities = clientCapabilities;
    }

    public DAVResource() {
    }
    
    public void setRoot(FSRoot root) {
        myRoot = root;
    }

    public FSRoot getRoot() {
        return myRoot;
    }
    
    public FSTransactionInfo getTxnInfo() {
        return myTxnInfo;
    }
    
    public void setTxnInfo(FSTransactionInfo txnInfo) {
        myTxnInfo = txnInfo;
    }

    public DAVResourceURI getResourceURI() {
        return myResourceURI;
    }

    public SVNRepository getRepository() {
        return myRepository;
    }

    public long getRevision() {
        return myRevision;
    }

    public boolean exists() {
        return myResourceURI.exists();
    }

    public boolean isVersioned() {
        return myResourceURI.isVersioned();
    }
    
    public boolean isWorking() {
        return myResourceURI.isWorking();
    }
    
    public boolean isBaseLined() {
        return myResourceURI.isBaseLined();
    }
    
    public DAVResourceType getType() {
        return getResourceURI().getType();
    }

    //TODO: refactor DAVResourceKind later and name
    //this method as getPrivateResourceKind()
    public DAVResourceKind getKind() {
        return getResourceURI().getKind();
    }
    
    public String getActivityID() {
        return myResourceURI.getActivityID();
    }
    
    public boolean lacksETagPotential() {
        DAVResourceType type = getResourceURI().getType();
        return !exists() || (type != DAVResourceType.REGULAR && type != DAVResourceType.VERSION) || 
               (type == DAVResourceType.VERSION && isBaseLined()); 
    }
    
    public boolean canBeActivity() {
        return isAutoCheckedOut() || (getType() == DAVResourceType.ACTIVITY && !exists());
    }
    
    public boolean isCollection() {
        return myIsCollection;
    }

    public boolean isSVNClient() {
        return myIsSVNClient;
    }
    
    public DAVAutoVersion getAutoVersion() {
        if (getType() == DAVResourceType.VERSION && isBaseLined()) {
            return DAVAutoVersion.ALWAYS;
        }
        
        DAVConfig config = myRepositoryManager.getDAVConfig(); 
        if (config.isAutoVersioning()) {
            if (getType() == DAVResourceType.REGULAR) {
                return DAVAutoVersion.ALWAYS;
            }
            
            if (getType() == DAVResourceType.WORKING && isAutoCheckedOut()) {
                return DAVAutoVersion.ALWAYS;
            }
        }
        return DAVAutoVersion.NEVER;
    }
    
    public String getUserName() {
        return myUserName;
    }

    public String getDeltaBase() {
        return myDeltaBase;
    }

    public long getVersion() {
        return myVersion;
    }

    public String getClientOptions() {
        return myClientOptions;
    }

    public String getBaseChecksum() {
        return myBaseChecksum;
    }

    public String getResultChecksum() {
        return myResultChecksum;
    }

    public File getActivitiesDB() {
        return myActivitiesDB;
    }

    public void versionControl(String target) throws DAVException {
        if (exists()) {
            throw new DAVException("vsn_control called on already-versioned resource.", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        if (target != null) {
            throw new DAVException("vsn_control called with non-null target.", null, HttpServletResponse.SC_NOT_IMPLEMENTED, null, 
                    SVNLogType.NETWORK, Level.FINE, null, DAVXMLUtil.SVN_DAV_ERROR_TAG, DAVElement.SVN_DAV_ERROR_NAMESPACE, 
                    SVNErrorCode.UNSUPPORTED_FEATURE.getCode(), null);
        }
    }
    
    public Iterator getChildren() throws SVNException {
        return new Iterator() {
            Iterator entriesIterator = getEntries().iterator();

            public void remove() {
            }

            public boolean hasNext() {
                return entriesIterator.hasNext();
            }

            public Object next() {
                SVNDirEntry entry = (SVNDirEntry) entriesIterator.next();
                String childURI = DAVPathUtil.append(getResourceURI().getURI(), entry.getName());
                try {
                    DAVResourceURI newResourceURI = new DAVResourceURI(getResourceURI().getContext(), childURI, null, false);
                    return new DAVResource(myRepositoryManager, getRepository(), newResourceURI, getRevision(), isSVNClient(), getDeltaBase(), 
                            getVersion(), getClientOptions(), null, null, getUserName(), getActivitiesDB(), getLockTokens(), getClientCapabilities());
                } catch (SVNException e) {
                    return null;
                }
            }
        };
    }
    
    public Map getClientCapabilities() {
        return myClientCapabilities;
    }

    public Collection getLockTokens() {
        return myLockTokens;
    }
    
    public void setLockTokens(Collection lockTokens) {
        if (myLockTokens != null) {
            myLockTokens.addAll(lockTokens);
        }
        myLockTokens = lockTokens;
    }

    public Collection getEntries() throws SVNException {
        if (isCollection() && myEntries == null) {
            myEntries = new LinkedList();
            getRepository().getDir(getResourceURI().getPath(), getRevision(), null, SVNDirEntry.DIRENT_KIND, myEntries);
        }
        return myEntries;
    }

    public long getCreatedRevision() throws SVNException {
        String revisionParameter = getProperty(SVNProperty.COMMITTED_REVISION);
        try {
            return Long.parseLong(revisionParameter);
        } catch (NumberFormatException e) {
            return getRevision();
        }
    }

    public long getCreatedRevisionUsingFS(String path) throws SVNException {
        path = path == null ? getResourceURI().getPath() : path;
        FSRevisionNode node = myRoot.getRevisionNode(path);
        return node.getCreatedRevision();
    }
    
    public Date getLastModified() throws SVNException {
        if (lacksETagPotential()) {
            return null;
        }
        return getRevisionDate(getCreatedRevision());
    }

    public Date getRevisionDate(long revision) throws SVNException {
        //TODO: insert here later an authz check
        return SVNDate.parseDate(getRevisionProperty(revision, SVNRevisionProperty.DATE));
    }

    public String getETag() {
        if (lacksETagPotential()) {
            return null;
        }
        
        long createdRevision = -1;
        try {
            createdRevision = getCreatedRevisionUsingFS(null);
        } catch (SVNException svne) {
            return null;
        }
        
        StringBuffer eTag = new StringBuffer();
        eTag.append(isCollection() ? "W/" : "");
        eTag.append("\"");
        
        eTag.append(createdRevision);
        eTag.append("/");
        eTag.append(SVNEncodingUtil.xmlEncodeCDATA(getResourceURI().getPath(), true));
        eTag.append("\"");
        return eTag.toString();
    }

    public String getRepositoryUUID(boolean forceConnect) throws SVNException {
        return getRepository().getRepositoryUUID(forceConnect);
    }

    public String getContentType() throws SVNException {
        if (getResourceURI().isBaseLined() && getResourceURI().getType() == DAVResourceType.VERSION) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_PROPS_NOT_FOUND, "Failed to determine property"), SVNLogType.NETWORK);
            return null;
        }
        if (getResourceURI().getType() == DAVResourceType.PRIVATE && getResourceURI().getKind() == DAVResourceKind.VCC) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_PROPS_NOT_FOUND, "Failed to determine property"), SVNLogType.NETWORK);
            return null;
        }
        if (isCollection()) {
            return DEFAULT_COLLECTION_CONTENT_TYPE;
        }
        String contentType = getProperty(SVNProperty.MIME_TYPE);
        if (contentType != null) {
            return contentType;
        }
        return DEFAULT_FILE_CONTENT_TYPE;
    }

    public long getLatestRevision() throws SVNException {
        return getRepository().getLatestRevision();
    }

    //TODO: remove this method later, use getContentLength(String path) instead
    /**
     * @deprecated
     */
    public long getContentLength() throws SVNException {
        SVNDirEntry entry = getRepository().getDir(getResourceURI().getPath(), getRevision(), false, null);
        return entry.getSize();
    }

    public long getContentLength(String path) throws SVNException {
        path = path == null ? getResourceURI().getPath() : path;
        FSRevisionNode node = myRoot.getRevisionNode(path);
        return node.getFileLength();
    }

    public SVNLock[] getLocks() throws SVNException {
        if (getResourceURI().getPath() == null) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "get-locks-report run on resource which doesn't represent a path within a repository."), SVNLogType.NETWORK);
        }
        return getRepository().getLocks(getResourceURI().getPath());
    }
    
    public SVNLock getLock() throws SVNException {
        return getRepository().getLock(getResourceURI().getPath());
    }

    public void unlock(String token, boolean force) throws SVNException {
        Map pathsToTokens = new HashMap();
        pathsToTokens.put(getResourceURI().getPath(), token);
        getRepository().unlock(pathsToTokens, force, null);
    }
    
    public String getAuthor(long revision) throws SVNException {
        return getRevisionProperty(revision, SVNRevisionProperty.AUTHOR);
    }

    /**
     * @deprecated use getMD5Checksum() instead
     */
    public String getMD5Checksum() throws SVNException {
        return getProperty(SVNProperty.CHECKSUM);
    }

    public String getMD5Checksum(String path) throws SVNException {
        path = path == null ? getResourceURI().getPath() : path;
        FSRevisionNode node = myRoot.getRevisionNode(path);
        return node.getFileMD5Checksum();
    }

    public String getLog(long revision) throws SVNException {
        return getRevisionProperty(revision, SVNRevisionProperty.LOG);
    }

    //TODO: replace later with getProperty(path, propName)
    /**
     * @deprecated
     */
    public String getProperty(String propertyName) throws SVNException {
        return getSVNProperties().getStringValue(propertyName);
    }

    public SVNPropertyValue getProperty(String path, String propertyName) throws SVNException {
        return getSVNProperties(path).getSVNPropertyValue(propertyName);
    }

    public String getRevisionProperty(long revision, String propertyName) throws SVNException {
        SVNPropertyValue value = getRepository().getRevisionPropertyValue(revision, propertyName);
        return value == null ? null : value.getString();
    }

    public void writeTo(OutputStream out) throws SVNException {
        if (isCollection()) {
            SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED), SVNLogType.NETWORK);
        }
        getRepository().getFile(getResourceURI().getPath(), getRevision(), null, out);
    }

    public boolean isAutoCheckedOut() {
        return myIsAutoCheckedOut;
    }

    public void setIsAutoCkeckedOut(boolean isAutoCheckedOut) {
        myIsAutoCheckedOut = isAutoCheckedOut;
    }
    
    public String getTxnName() {
        return myTxnName;
    }

    public void setExists(boolean exists) {
        myResourceURI.setExists(exists);
    }
    
    public void setVersioned(boolean isVersioned) {
        myResourceURI.setVersioned(isVersioned);
    }

    public void setWorking(boolean isWorking) {
        myResourceURI.setWorking(isWorking);
    }
    
    public void setBaseLined(boolean isBaseLined) {
        myResourceURI.setBaseLined(isBaseLined);
    }
    
    public void setCollection(boolean isCollection) {
        myIsCollection = isCollection;
    }
    
    public void setTxnName(String txnName) {
        myTxnName = txnName;
    }
    
    public void setRevision(long revision) {
        myRevision = revision;
        myResourceURI.setRevision(revision);
    }
    
    public void setResourceURI(DAVResourceURI resourceURI) {
        myResourceURI = resourceURI;
    }

    public boolean equals(Object o) {
        if (o == null || o.getClass() != this.getClass()) {
            return false;
        }
        
        if (o == this) {
            return true;
        }
        
        DAVResource otherResource = (DAVResource) o;
        if (!isOurResource(otherResource)) {
            return false;
        }
        
        String myRequestURI = myResourceURI.getRequestURI(); 
        String otherRequestURI = otherResource.getResourceURI().getRequestURI();
        return myRequestURI.equals(otherRequestURI);
    }
    
    public DAVResource dup() {
        DAVResource copy = new DAVResource();
        copyTo(copy);
        return copy;
    }
    
    public FSFS getFSFS() {
        return myFSFS;
    }
    
    public DAVRepositoryManager getRepositoryManager() {
        return myRepositoryManager;
    }

    public boolean isParentResource(DAVResource resource) {
        if (!isOurResource(resource)) {
            return false;
        }
        
        String thisURIPath = myResourceURI.getURI();
        String otherURIPath = resource.getResourceURI().getURI();
        return otherURIPath.length() > thisURIPath.length() && otherURIPath.startsWith(thisURIPath) && 
        otherURIPath.charAt(thisURIPath.length()) == '/'; 
    }

    public SVNProperties getSVNProperties(String path) throws SVNException {
        path = path == null ? getResourceURI().getPath() : path;
        if (mySVNProperties == null) {
            mySVNProperties = myFSFS.getProperties(myRoot.getRevisionNode(path));
        }
        return mySVNProperties;
    }

    private boolean isOurResource(DAVResource resource) {
        File reposRoot1 = myFSFS.getDBRoot();
        File reposRoot2 = resource.myFSFS.getDBRoot();
        if (!reposRoot1.equals(reposRoot2)) {
            return false;
        }
        return true;
    }

    //TODO: replace occurances of getSVNProperties() with getSVNProperties(path)
    /**
     * @deprecated
     */
    public SVNProperties getSVNProperties() throws SVNException {
        if (mySVNProperties == null) {
            mySVNProperties = new SVNProperties();
            if (getResourceURI().getType() == DAVResourceType.REGULAR) {
                if (isCollection()) {
                    getRepository().getDir(getResourceURI().getPath(), getRevision(), mySVNProperties, (ISVNDirEntryHandler) null);
                } else {
                    getRepository().getFile(getResourceURI().getPath(), getRevision(), mySVNProperties, null);
                }
            }
        }
        return mySVNProperties;
    }

    protected void copyTo(DAVResource copy) {
        copy.myRepositoryManager = myRepositoryManager;
        copy.myResourceURI = myResourceURI.dup();
        copy.myRepository = myRepository;
        copy.myRevision = myRevision;
        copy.myIsCollection = myIsCollection;
        copy.myIsSVNClient = myIsCollection;
        copy.myIsAutoCheckedOut = myIsAutoCheckedOut;
        copy.myDeltaBase = myDeltaBase;
        copy.myVersion = myVersion;
        copy.myClientOptions = myClientOptions;
        copy.myBaseChecksum = myBaseChecksum;
        copy.myResultChecksum = myResultChecksum;
        copy.myUserName = myUserName;
        copy.mySVNProperties = mySVNProperties;
        copy.myDeadProperties = myDeadProperties;
        copy.myEntries = myEntries;
        copy.myActivitiesDB = myActivitiesDB;
        copy.myFSFS = myFSFS;
        copy.myTxnName = myTxnName;
        copy.myRoot = myRoot;
        copy.myTxnInfo = myTxnInfo;
        copy.myClientCapabilities = myClientCapabilities;
        copy.myLockTokens = myLockTokens;
    }

}
