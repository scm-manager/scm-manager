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
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedList;
import java.util.logging.Level;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.io.fs.FSCommitter;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSID;
import org.tmatesoft.svn.core.internal.io.fs.FSNodeHistory;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionNode;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionRoot;
import org.tmatesoft.svn.core.internal.io.fs.FSRoot;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionInfo;
import org.tmatesoft.svn.core.internal.io.fs.FSTransactionRoot;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVServletUtil {
    
    public static long getSafeCreatedRevision(FSRevisionRoot root, String path) {
        long revision = root.getRevision();
        FSFS fsfs = root.getOwner();
        FSID id = null;
        try {
            FSRevisionNode node = root.getRevisionNode(path);
            id = node.getId();
        } catch (SVNException svne) {
            return revision;
        }

        FSNodeHistory history = null;
        long historyRev = -1;
        try {
            history = root.getNodeHistory(path);
            history = history.getPreviousHistory(false);
            historyRev = history.getHistoryEntry().getRevision();
        } catch (SVNException svne) {
            return revision;
        }
        
        FSRevisionRoot otherRoot = null;
        try {
            otherRoot = fsfs.createRevisionRoot(historyRev);
        } catch (SVNException svne) {
            return revision;
        }
        
        FSID otherID = null;
        try {
            FSRevisionNode node = otherRoot.getRevisionNode(path);
            otherID = node.getId();
        } catch (SVNException svne) {
            return revision;
        }
        
        if (id.compareTo(otherID) == 0) {
            return historyRev;
        }
        
        return revision;
    }
    
    public static URI lookUpURI(String uri, HttpServletRequest request, boolean mustBeAbsolute) throws DAVException {
        URI parsedURI = null;
        try {
            parsedURI = new URI(uri);
        } catch (URISyntaxException urise) {
            throw new DAVException("Invalid syntax in Destination URI.", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        if (parsedURI.getScheme() == null && mustBeAbsolute) {
            throw new DAVException("Destination URI must be an absolute URI.", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        if (parsedURI.getQuery() != null || parsedURI.getFragment() != null) {
            throw new DAVException("Destination URI contains invalid components (a query or a fragment).", HttpServletResponse.SC_BAD_REQUEST, 0);
        }
        
        if (parsedURI.getScheme() != null || parsedURI.getPort() != -1 || mustBeAbsolute) {
            String scheme = request.getScheme();
            if (scheme == null) {
                //TODO: replace this code in future 
                scheme = "http";
            }
            int parsedPort = parsedURI.getPort();
            if (parsedURI.getPort() == -1) {
                parsedPort = request.getServerPort();
            }
            
            if (!scheme.equals(parsedURI.getScheme()) || parsedPort != request.getServerPort()) {
                throw new DAVException("Destination URI refers to different scheme or port ({0}://hostname:{1})\n(want: {2}://hostname:{3})", 
                        new Object[] { parsedURI.getScheme() != null ? parsedURI.getScheme() : scheme, String.valueOf(parsedPort), scheme, 
                                String.valueOf(request.getServerPort()) }, HttpServletResponse.SC_BAD_REQUEST, 0);
            }
        }
        
        String parsedHost = parsedURI.getHost();
        String serverHost = request.getServerName();
        String domain = null;
        int domainInd = serverHost != null ? serverHost.indexOf('.') : -1;  
        if (domainInd != -1) {
            domain = serverHost.substring(domainInd);
        }
        
        if (parsedHost != null && parsedHost.indexOf('.') == -1 && domain != null) {
            parsedHost += domain;
        }
        
        if (parsedHost != null && !parsedHost.equals(request.getServerName())) {
            throw new DAVException("Destination URI refers to a different server.", HttpServletResponse.SC_BAD_GATEWAY, 0);
        }
        return parsedURI;
    }
    
    public static void setAutoRevisionProperties(DAVResource resource) throws DAVException {
        if (!(resource.getType() == DAVResourceType.WORKING && resource.isAutoCheckedOut())) {
            throw new DAVException("Set_auto_revprops called on invalid resource.", HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 0);
        }
        
        try {
            attachAutoRevisionProperties(resource.getTxnInfo(), resource.getResourceURI().getPath(), resource.getFSFS());
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Error setting a revision property ", null);
        }
        
    }
    
    public static void attachAutoRevisionProperties(FSTransactionInfo txn, String path, FSFS fsfs) throws SVNException {
        String logMessage = "Autoversioning commit:  a non-deltaV client made a change to\n" + path;
        SVNProperties props = new SVNProperties();
        props.put(SVNRevisionProperty.LOG, logMessage);
        props.put(SVNRevisionProperty.AUTOVERSIONED, "*");
        fsfs.changeTransactionProperties(txn.getTxnId(), props);
    }
    
    public static void deleteActivity(DAVResource resource, String activityID) throws DAVException {
        File activitiesDB = resource.getActivitiesDB();
        String txnName = getTxn(activitiesDB, activityID);
        if (txnName == null) {
            throw new DAVException("could not find activity.", HttpServletResponse.SC_NOT_FOUND, 0);
        }
        
        FSFS fsfs = resource.getFSFS();
        FSTransactionInfo txn = null;
        if (txnName != null) {
            try {
                txn = fsfs.openTxn(txnName);
            } catch (SVNException svne) {
                if (svne.getErrorMessage().getErrorCode() != SVNErrorCode.FS_NO_SUCH_TRANSACTION) {
                    throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                            "could not open transaction.", null);
                }
            }
            
            if (txn != null) {
                try {
                    FSCommitter.abortTransaction(fsfs, txn.getTxnId());
                } catch (SVNException svne) {
                    throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                            "could not abort transaction.", null);
                }
            }
        }
        
        try {
            SVNFileUtil.deleteFile(DAVPathUtil.getActivityPath(activitiesDB, activityID));
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "unable to remove activity.", null);
        }
    }
    
    public static void storeActivity(DAVResource resource, String txnName) throws DAVException {
        DAVResourceURI resourceURI = resource.getResourceURI();
        String activityID = resourceURI.getActivityID();
        File activitiesDB = resource.getActivitiesDB();
        if (!activitiesDB.exists() && !activitiesDB.mkdirs()) {
            throw new DAVException("could not initialize activity db.", null, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, null, 
                    SVNLogType.NETWORK, Level.FINE, null, null, null, 0, null);
        }
        
        File finalActivityFile = DAVPathUtil.getActivityPath(activitiesDB, activityID);
        File tmpFile = null;
        try {
            tmpFile = SVNFileUtil.createUniqueFile(finalActivityFile.getParentFile(), finalActivityFile.getName(), "tmp", false);
        } catch (SVNException svne) {
            SVNErrorMessage err = svne.getErrorMessage().wrap("Can't open activity db");
            throw DAVException.convertError(err, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "could not open files.", null);
        }
        
        StringBuffer activitiesContents = new StringBuffer();
        activitiesContents.append(txnName);
        activitiesContents.append('\n');
        activitiesContents.append(activityID);
        activitiesContents.append('\n');
        
        try {
            SVNFileUtil.writeToFile(tmpFile, activitiesContents.toString(), null);
        } catch (SVNException svne) {
            SVNErrorMessage err = svne.getErrorMessage().wrap("Can't write to activity db");
            try {
                SVNFileUtil.deleteFile(tmpFile);
            } catch (SVNException e) {
            }
            throw DAVException.convertError(err, HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "could not write files.", null);
        }
        
        try {
            SVNFileUtil.rename(tmpFile, finalActivityFile);
        } catch (SVNException svne) {
            try {
                SVNFileUtil.deleteFile(tmpFile);
            } catch (SVNException e) {
            }
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "could not replace files.", null);
        }
    }

    public static FSTransactionInfo createActivity(DAVResource resource, FSFS fsfs) throws DAVException {
        SVNProperties properties = new SVNProperties();
        properties.put(SVNRevisionProperty.AUTHOR, resource.getUserName());
        long revision = SVNRepository.INVALID_REVISION;
        try {
            revision = fsfs.getYoungestRevision();
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "could not determine youngest revision", null);
        }
        
        FSTransactionInfo txnInfo = null;
        try {
            txnInfo = FSTransactionRoot.beginTransactionForCommit(revision, properties, fsfs);
        } catch (SVNException svne) {
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "could not begin a transaction", null);
        }
        
        return txnInfo;
    }

    public static LinkedList processIfHeader(String value) throws DAVException {
        if (value == null) {
            return null;
        }
        
        StringBuffer valueBuffer = new StringBuffer(value);
        ListType listType = ListType.UNKNOWN;
        String uri = null;
        LinkedList ifHeaders = new LinkedList();
        DAVIFHeader ifHeader = null;
        while (valueBuffer.length() > 0) {
            if (valueBuffer.charAt(0) == '<') {
                if (listType == ListType.NO_TAGGED || (uri = DAVServletUtil.fetchNextToken(valueBuffer, '>')) == null) {
                    throw new DAVException("Invalid If-header: unclosed \"<\" or unexpected tagged-list production.", 
                            HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.IF_TAGGED);
                }
                
                URI parsedURI = null;
                try {
                    parsedURI = new URI(uri);
                } catch (URISyntaxException urise) {
                    throw new DAVException("Invalid URI in tagged If-header.", HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.IF_TAGGED);
                }
                
                uri = parsedURI.getPath();
                uri = uri.length() > 1 && uri.endsWith("/") ? uri.substring(0, uri.length() - 1) : uri;
                listType = ListType.TAGGED;
            } else if (valueBuffer.charAt(0) == '(') {
                if (listType == ListType.UNKNOWN) {
                    listType = ListType.NO_TAGGED;
                }
                
                StringBuffer listBuffer = null;
                String list = null;
                if ((list = DAVServletUtil.fetchNextToken(valueBuffer, ')')) == null) {
                    throw new DAVException("Invalid If-header: unclosed \"(\".", HttpServletResponse.SC_BAD_REQUEST, 
                            DAVErrorCode.IF_UNCLOSED_PAREN);
                }
                
                ifHeader = new DAVIFHeader(uri);
                ifHeaders.addFirst(ifHeader);
                
                int condition = DAVIFState.IF_CONDITION_NORMAL;
                String stateToken = null;
                
                listBuffer = new StringBuffer(list);
                while (listBuffer.length() > 0) {
                    if (listBuffer.charAt(0) == '<') {
                        if ((stateToken = DAVServletUtil.fetchNextToken(listBuffer, '>')) == null) {
                            throw new DAVException(null, HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.IF_PARSE);
                        }
                        
                        addIfState(stateToken, DAVIFStateType.IF_OPAQUE_LOCK, condition, ifHeader);
                        condition = DAVIFState.IF_CONDITION_NORMAL;
                    } else if (listBuffer.charAt(0) == '[') {
                        if ((stateToken = fetchNextToken(listBuffer, ']')) == null) {
                            throw new DAVException(null, HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.IF_PARSE);
                        }
                        
                        addIfState(stateToken, DAVIFStateType.IF_ETAG, condition, ifHeader);
                        condition = DAVIFState.IF_CONDITION_NORMAL;
                    } else if (listBuffer.charAt(0) == 'N') {
                        if (listBuffer.length() > 2 && listBuffer.charAt(1) == 'o' && listBuffer.charAt(2) == 't') {
                            if (condition != DAVIFState.IF_CONDITION_NORMAL) {
                                throw new DAVException("Invalid \"If:\" header: Multiple \"not\" entries for the same state.", 
                                        HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.IF_MULTIPLE_NOT);
                            }
                            condition = DAVIFState.IF_CONDITION_NOT;
                        }
                        listBuffer.delete(0, 2);
                    } else if (listBuffer.charAt(0) != ' ' && listBuffer.charAt(0) != '\t') {
                        throw new DAVException("Invalid \"If:\" header: Unexpected character encountered ({0}, ''{1}'').", 
                                new Object[] { Integer.toHexString(listBuffer.charAt(0)), new Character(listBuffer.charAt(0)) }, 
                                HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.IF_UNK_CHAR);
                    }
                    listBuffer.deleteCharAt(0);
                }
            } else if (valueBuffer.charAt(0) != ' ' && valueBuffer.charAt(0) != '\t') {
                throw new DAVException("Invalid \"If:\" header: Unexpected character encountered ({0}, ''{1}'').", 
                        new Object[] { Integer.toHexString(valueBuffer.charAt(0)), new Character(valueBuffer.charAt(0)) }, 
                        HttpServletResponse.SC_BAD_REQUEST, DAVErrorCode.IF_UNK_CHAR);
                
            }
            valueBuffer.deleteCharAt(0);
        }
        
        return ifHeaders;
    }
    
    public static FSTransactionInfo openTxn(FSFS fsfs, String txnName) throws DAVException {
        FSTransactionInfo txnInfo = null;
        try {
            txnInfo = fsfs.openTxn(txnName);
        } catch (SVNException svne) {
            if (svne.getErrorMessage().getErrorCode() == SVNErrorCode.FS_NO_SUCH_TRANSACTION) {
                throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                        "The transaction specified by the activity does not exist", null);
            }
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "There was a problem opening the transaction specified by this activity.", null);
        }
        return txnInfo;
    }
    
    public static String getTxn(File activitiesDB, String activityID) {
        File activityFile = DAVPathUtil.getActivityPath(activitiesDB, activityID);
        return DAVServletUtil.readTxn(activityFile);
    }
    
    public static String readTxn(File activityFile) {
        String txnName = null;
        for (int i = 0; i < 10; i++) {
            try {
                txnName = SVNFileUtil.readSingleLine(activityFile);
            } catch (IOException e) {
                //ignore
            }
        }
        return txnName; 
    }
    
    public static SVNNodeKind checkPath(FSRoot root, String path) throws DAVException {
        try {
            return root.checkNodeKind(path);
        } catch (SVNException svne) {
            if (svne.getErrorMessage().getErrorCode() == SVNErrorCode.FS_NOT_DIRECTORY) {
                return SVNNodeKind.NONE;
            }
            throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                    "Error checking kind of path ''{0}'' in repository", new Object[] { path });
        }
    }

    private static void addIfState(String stateToken, DAVIFStateType type, int condition, DAVIFHeader ifHeader) {
        String eTag = null;
        String lockToken = null;
        if (type == DAVIFStateType.IF_OPAQUE_LOCK) {
            lockToken = stateToken;
        } else {
            eTag = stateToken;
        }
         
        DAVIFState ifState = new DAVIFState(condition, eTag, lockToken, type);
        ifHeader.addIFState(ifState);
    }
    
    private static String fetchNextToken(StringBuffer string, char term) {
        String token = string.substring(1);
        token = token.trim();
        int ind = -1;
        if ((ind = token.indexOf(term)) == -1) {
            return null;
        }
        
        token = token.substring(0, ind);
        string.delete(0, string.indexOf(token) + token.length());
        return token;
    }

    private static class ListType {
        public static final ListType NO_TAGGED = new ListType();
        public static final ListType TAGGED = new ListType();
        public static final ListType UNKNOWN = new ListType();
        
        private ListType() {
        }
    }

}
