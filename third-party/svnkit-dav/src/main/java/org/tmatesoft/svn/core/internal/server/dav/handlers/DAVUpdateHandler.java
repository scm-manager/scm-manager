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

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tmatesoft.svn.core.SVNCommitInfo;
import org.tmatesoft.svn.core.SVNDepth;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.internal.io.dav.DAVElement;
import org.tmatesoft.svn.core.internal.io.fs.FSFS;
import org.tmatesoft.svn.core.internal.io.fs.FSRepository;
import org.tmatesoft.svn.core.internal.io.fs.FSRevisionRoot;
import org.tmatesoft.svn.core.internal.io.fs.FSTranslateReporter;
import org.tmatesoft.svn.core.internal.server.dav.DAVException;
import org.tmatesoft.svn.core.internal.server.dav.DAVPathUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVRepositoryManager;
import org.tmatesoft.svn.core.internal.server.dav.DAVResource;
import org.tmatesoft.svn.core.internal.server.dav.DAVResourceKind;
import org.tmatesoft.svn.core.internal.server.dav.DAVServletUtil;
import org.tmatesoft.svn.core.internal.server.dav.DAVXMLUtil;
import org.tmatesoft.svn.core.internal.util.SVNHashMap;
import org.tmatesoft.svn.core.internal.util.SVNHashSet;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNXMLUtil;
import org.tmatesoft.svn.core.internal.wc.SVNAdminDeltifier;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.core.wc.SVNRevision;
import org.tmatesoft.svn.util.SVNLogType;
import org.xml.sax.Attributes;

/**
 * @author TMate Software Ltd.
 * @version 1.2.0
 */
public class DAVUpdateHandler extends DAVReportHandler implements ISVNEditor {

    private static Set UPDATE_REPORT_NAMESPACES = new SVNHashSet();

    private static final DAVElement ENTRY = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "entry");
    private static final DAVElement MISSING = DAVElement.getElement(DAVElement.SVN_NAMESPACE, "missing");

    private DAVUpdateRequest myDAVRequest;

    private FSTranslateReporter myReporter;
    private boolean myInitialized = false;
    private boolean myResourceWalk = false;
    private FSRepository mySourceRepository;
    private FSRevisionRoot myRevisionRoot;
    private long myRevision = DAVResource.INVALID_REVISION;
    private SVNURL myDstURL = null;
    private String myDstPath = null;
    private String myAnchor = null;
    private SVNDepth myDepth = SVNDepth.UNKNOWN;
    private SVNDepth myRequestedDepth = SVNDepth.UNKNOWN;
    private Map myPathMap = null;

    private long myEntryRevision = DAVResource.INVALID_REVISION;
    private String myEntryLinkPath = null;
    private boolean myEntryStartEmpty = false;
    private String myEntryLockToken = null;

    private String myFileBaseChecksum = null;
    private boolean myFileTextChanged = false;
    private EditorEntry myFileEditorEntry;
    private DAVReportHandler myCommonReportHandler;

    Stack myEditorEntries;

    static {
        UPDATE_REPORT_NAMESPACES.add(DAVElement.SVN_NAMESPACE);
        UPDATE_REPORT_NAMESPACES.add(DAVElement.SVN_DAV_PROPERTY_NAMESPACE);
    }

    public DAVUpdateHandler(DAVRepositoryManager repositoryManager, HttpServletRequest request, HttpServletResponse response, 
            DAVReportHandler commonReportHandler) {
        super(repositoryManager, request, response);
        setSVNDiffVersion(getSVNDiffVersion());
        myCommonReportHandler = commonReportHandler;
    }

    public DAVRequest getDAVRequest() {
        return getUpdateRequest();
    }

    private DAVUpdateRequest getUpdateRequest() {
        if (myDAVRequest == null) {
            myDAVRequest = new DAVUpdateRequest();
        }
        return myDAVRequest;
    }

    private FSTranslateReporter getReporter() {
        return myReporter;
    }

    private void setReporter(FSTranslateReporter reporter) {
        myReporter = reporter;
    }

    private long getRevision() {
        return myRevision;
    }

    private void setRevision(long revision) {
        myRevision = revision;
    }

    private SVNURL getDstURL() {
        return myDstURL;
    }

    private void setDstURL(SVNURL dstURL) {
        myDstURL = dstURL;
    }

    private String getDstPath() {
        return myDstPath;
    }

    private void setDstPath(String dstPath) {
        myDstPath = dstPath;
    }

    private String getAnchor() {
        return myAnchor;
    }

    private void setAnchor(String anchor) {
        myAnchor = anchor;
    }

    private SVNDepth getDepth() {
        return myDepth;
    }

    private void setDepth(SVNDepth depth) {
        myDepth = depth;
    }

    private Map getPathMap() {
        if (myPathMap == null) {
            myPathMap = new SVNHashMap();
        }
        return myPathMap;
    }

    private void addToPathMap(String path, String linkPath) {
        String normalizedPath = DAVPathUtil.normalize(path);
        String repositoryPath = linkPath == null ? normalizedPath : linkPath;
        getPathMap().put(SVNPathUtil.getAbsolutePath(path), repositoryPath);
    }

    private boolean isInitialized() {
        return myInitialized;
    }

    private void setInitialized(boolean initialized) {
        myInitialized = initialized;
    }

    private boolean isResourceWalk() {
        return myResourceWalk;
    }

    private void setResourceWalk(boolean resourceWalk) {
        myResourceWalk = resourceWalk;
    }

    private FSRepository getSourceRepository() {
        return mySourceRepository;
    }

    private void setSourceRepository(FSRepository sourceRepository) {
        mySourceRepository = sourceRepository;
    }

    private long getEntryRevision() {
        return myEntryRevision;
    }

    private void setEntryRevision(long entryRevision) {
        myEntryRevision = entryRevision;
    }

    private String getEntryLinkPath() {
        return myEntryLinkPath;
    }

    private void setEntryLinkPath(String entryLinkPath) {
        myEntryLinkPath = entryLinkPath;
    }

    private boolean isEntryStartEmpty() {
        return myEntryStartEmpty;
    }

    private void setEntryStartEmpty(boolean entryStartEmpty) {
        myEntryStartEmpty = entryStartEmpty;
    }

    private String getEntryLockToken() {
        return myEntryLockToken;
    }

    private void setEntryLockToken(String entryLockToken) {
        myEntryLockToken = entryLockToken;
    }

    private String getFileBaseChecksum() {
        return myFileBaseChecksum;
    }

    private void setFileBaseChecksum(String fileBaseChecksum) {
        myFileBaseChecksum = fileBaseChecksum;
    }

    private boolean isFileTextChanged() {
        return myFileTextChanged;
    }

    private void setFileTextChanged(boolean fileTextChanged) {
        myFileTextChanged = fileTextChanged;
    }

    private void setFileIsAdded(boolean isAdded) {
        if (myFileEditorEntry == null) {
            myFileEditorEntry = new EditorEntry(isAdded);
        } else {
            myFileEditorEntry.setAdded(isAdded);
        }
    }

    private EditorEntry getFileEditorEntry() {
        return myFileEditorEntry;
    }

    private Stack getEditorEntries() {
        if (myEditorEntries == null) {
            myEditorEntries = new Stack();
        }
        return myEditorEntries;
    }

    private void initialize() throws SVNException {
        if (!isInitialized()) {
            getUpdateRequest().init();

            setDAVResource(getRequestedDAVResource(false, false));
            
            long targetRevision = getUpdateRequest().getRevision();
            if (!SVNRevision.isValidRevisionNumber(targetRevision)) {
                try {
                    targetRevision = getDAVResource().getLatestRevision();
                } catch (SVNException svne) {
                    throw DAVException.convertError(svne.getErrorMessage(), HttpServletResponse.SC_INTERNAL_SERVER_ERROR, 
                            "Could not determine the youngest revision for the update process.", null);
                }
            } 
            
            setRevision(targetRevision);

            myRequestedDepth = getUpdateRequest().getDepth();
            if (!getUpdateRequest().isDepthRequested() && !getUpdateRequest().isRecursiveRequested() && myRequestedDepth == SVNDepth.UNKNOWN) {
                myRequestedDepth = SVNDepth.INFINITY;
            }

            String srcPath = getRepositoryManager().getRepositoryRelativePath(getUpdateRequest().getSrcURL());
            setAnchor(srcPath);

            SVNURL dstURL = getUpdateRequest().getDstURL();
            String dstPath;
            if (dstURL != null) {
                dstPath = getRepositoryManager().getRepositoryRelativePath(dstURL);
                setDstPath(dstPath);
                setDstURL(getRepositoryManager().convertHttpToFile(dstURL));
                addToPathMap(SVNPathUtil.getAbsolutePath(SVNPathUtil.append(srcPath, getUpdateRequest().getTarget())), dstPath);
            }

            FSFS fsfs = getDAVResource().getFSFS();
            myRevisionRoot = fsfs.createRevisionRoot(targetRevision);
            
            SVNURL repositoryURL = getRepositoryManager().convertHttpToFile(getUpdateRequest().getSrcURL());
            FSRepository repository = (FSRepository) SVNRepositoryFactory.create(repositoryURL);

            FSTranslateReporter reporter = repository.beginReport(getRevision(),
                    getDstURL(),
                    getUpdateRequest().getTarget(),
                    getUpdateRequest().isIgnoreAncestry(),
                    getUpdateRequest().isTextDeltas(),
                    getUpdateRequest().isSendCopyFromArgs(),
                    myRequestedDepth,
                    this);
            setReporter(reporter);
            setSourceRepository(repository);
            setInitialized(true);
        }
    }

    protected void handleAttributes(DAVElement parent, DAVElement element, Attributes attrs) throws SVNException {
        if (element == ENTRY && parent == ServletDAVHandler.UPDATE_REPORT) {
            setEntryLinkPath(attrs.getValue(LINKPATH_ATTR));
            setEntryLockToken(attrs.getValue(LOCK_TOKEN_ATTR));
            String revisionString = attrs.getValue(REVISION_ATTR);
            if (revisionString == null) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "Missing XML attribute: rev"), SVNLogType.NETWORK);
            }
            try {
                setEntryRevision(Long.parseLong(revisionString));
            } catch (NumberFormatException nfe) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, nfe), SVNLogType.NETWORK);
            }
            setDepth(SVNDepth.fromString(attrs.getValue(DEPTH_ATTR)));
            if (attrs.getValue(START_EMPTY_ATTR) != null) {
                setEntryStartEmpty(true);
            }
        } else if (element != MISSING || parent != ServletDAVHandler.UPDATE_REPORT) {
            if (isInitialized()) {
                SVNErrorManager.error(SVNErrorMessage.create(SVNErrorCode.RA_DAV_REQUEST_FAILED, "Invalid XML elements order: entry elements should follow any other."), SVNLogType.NETWORK);
            }
            getDAVRequest().startElement(parent, element, attrs);
        }
    }

    protected void handleCData(DAVElement parent, DAVElement element, StringBuffer cdata) throws SVNException {
        if (element == ENTRY && parent == ServletDAVHandler.UPDATE_REPORT) {
            handleEntry(cdata.toString(), false);
        } else if (element == MISSING && parent == ServletDAVHandler.UPDATE_REPORT) {
            handleEntry(cdata.toString(), true);
        } else {
            getDAVRequest().endElement(parent, element, cdata);
        }
    }

    private void handleEntry(String entryPath, boolean deletePath) throws SVNException {
        initialize();
        try {
            if (deletePath) {
                getReporter().deletePath(entryPath);
            } else {
                if (getEntryLinkPath() == null) {
                    getReporter().setPath(entryPath, getEntryLockToken(), getEntryRevision(), getDepth(), isEntryStartEmpty());
                } else {
                    SVNURL linkURL = getDAVResource().getRepository().getLocation().appendPath(getEntryLinkPath(), true);
                    getReporter().linkPath(linkURL, entryPath, getEntryLockToken(), getEntryRevision(), getDepth(), isEntryStartEmpty());
                }
                if (getEntryLinkPath() != null && getDstPath() == null) {
                    String path = SVNPathUtil.append(getAnchor(), SVNPathUtil.append(getUpdateRequest().getTarget(), entryPath));
                    addToPathMap(path, getEntryLinkPath());
                }
                refreshEntry();
            }
        } catch (SVNException e) {
            getReporter().abortReport();
            getReporter().closeRepository();
            throw e;
        }
    }

    private void refreshEntry() {
        setEntryLinkPath(null);
        setEntryLockToken(null);
        setEntryRevision(DAVResource.INVALID_REVISION);
        setEntryStartEmpty(false);
    }

    private String getRealPath(String path) {
        path = SVNPathUtil.getAbsolutePath(SVNPathUtil.append(getAnchor(), path));
        if (getPathMap().isEmpty()) {
            return path;
        }

        String repositoryPath = (String) getPathMap().get(path);
        if (repositoryPath != null) {
            return repositoryPath;
        }

        String tmpPath = path;
        do {
            tmpPath = SVNPathUtil.removeTail(tmpPath);
            repositoryPath = (String) getPathMap().get(tmpPath);
            if (repositoryPath != null) {
                return SVNPathUtil.append(repositoryPath, path.substring(tmpPath.length()));
            }
        } while (SVNPathUtil.getSegmentsCount(tmpPath) > 0);

        return path;
    }

    public void execute() throws SVNException {
        myCommonReportHandler.checkSVNNamespace("The request does not contain the 'svn:' namespace, so it is not going to have" + 
                " an svn:target-revision element. That element is required.");
        writeXMLHeader(null);

        try {
            getReporter().finishReport();
        } catch (SVNException e) {
            getReporter().abortReport();
            throw e;
        } finally {
            getReporter().closeRepository();
        }

        if (getDstPath() != null && getUpdateRequest().isResourceWalk()) {
            if (SVNNodeKind.DIR == getDAVResource().getRepository().checkPath(getDstPath(), getRevision())) {
                setResourceWalk(true);
            }
        }

        if (isResourceWalk()) {
            StringBuffer xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "resource-walk", SVNXMLUtil.XML_STYLE_NORMAL, null, null);
            write(xmlBuffer);

            FSFS fsfs = getSourceRepository().getFSFS();
            SVNAdminDeltifier deltifier = new SVNAdminDeltifier(fsfs, myRequestedDepth, true, false, false, this);

            FSRevisionRoot zeroRoot = fsfs.createRevisionRoot(0);
            FSRevisionRoot requestedRoot = fsfs.createRevisionRoot(getRevision());
            deltifier.deltifyDir(zeroRoot, "", getUpdateRequest().getTarget(), requestedRoot, getDstPath());

            xmlBuffer = SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "resource-walk", null);
            write(xmlBuffer);
        }
        writeXMLFooter(null);
    }

    protected void addXMLHeader(StringBuffer xmlBuffer, String tagName) {
        Map attrs = new SVNHashMap();
        if (getUpdateRequest().isSendAll()) {
            attrs.put(SEND_ALL_ATTR, Boolean.TRUE.toString());
        }
        
        DAVElementProperty rootElement = getDAVRequest().getRootElement();
        SVNXMLUtil.addXMLHeader(xmlBuffer);
        tagName = tagName == null ? rootElement.getName().getName() : tagName;
        DAVXMLUtil.openNamespaceDeclarationTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, 
                UPDATE_REPORT_NAMESPACES, attrs, xmlBuffer, true, false);
    }

    public void targetRevision(long revision) throws SVNException {
        if (!isResourceWalk()) {
            StringBuffer xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "target-revision", 
                    SVNXMLUtil.XML_STYLE_SELF_CLOSING, REVISION_ATTR, String.valueOf(revision), null);
            write(xmlBuffer);
        }
    }

    public void openRoot(long revision) throws SVNException {
        EditorEntry entry = new EditorEntry(false);
        getEditorEntries().push(entry);
        StringBuffer xmlBuffer = null;

        if (isResourceWalk()) {
            xmlBuffer = openResourceTag("", xmlBuffer);
        } else {
            xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "open-directory", SVNXMLUtil.XML_STYLE_NORMAL, REVISION_ATTR, String.valueOf(revision), null);
        }
        if (getUpdateRequest().getTarget().length() == 0) {
            addVersionURL(getRealPath(""), xmlBuffer);
        }
        if (isResourceWalk()) {
            closeResourceTag(xmlBuffer);
        }
        write(xmlBuffer);
    }

    public void deleteEntry(String path, long revision) throws SVNException {
        writeEntryTag("delete-entry", path);
    }

    public void absentDir(String path) throws SVNException {
        if (!isResourceWalk()) {
            writeEntryTag("absent-directory", path);
        }
    }

    public void absentFile(String path) throws SVNException {
        if (!isResourceWalk()) {
            writeEntryTag("absent-file", path);
        }
    }

    public void addDir(String path, String copyFromPath, long copyFromRevision) throws SVNException {
        EditorEntry directoryEntry = new EditorEntry(true);
        getEditorEntries().push(directoryEntry);
        writeAddEntryTag(true, path, copyFromPath, copyFromRevision);
    }

    public void openDir(String path, long revision) throws SVNException {
        EditorEntry directoryEntry = new EditorEntry(false);
        getEditorEntries().push(directoryEntry);
        writeEntryTag("open-directory", path, revision);
    }

    public void changeDirProperty(String name, SVNPropertyValue value) throws SVNException {
        if (!isResourceWalk()) {
            EditorEntry entry = (EditorEntry) getEditorEntries().peek();
            changeProperties(entry, name, value);
        }
    }

    public void closeDir() throws SVNException {
        EditorEntry entry = (EditorEntry) getEditorEntries().pop();
        closeEntry(entry, true, null);
    }

    public void addFile(String path, String copyFromPath, long copyFromRevision) throws SVNException {
        setFileIsAdded(true);
        writeAddEntryTag(false, path, copyFromPath, copyFromRevision);
    }

    public void openFile(String path, long revision) throws SVNException {
        setFileIsAdded(false);
        writeEntryTag("open-file", path, revision);
    }

    public void changeFileProperty(String path, String name, SVNPropertyValue value) throws SVNException {
        if (!isResourceWalk()) {
            changeProperties(getFileEditorEntry(), name, value);
        }
    }

    public void closeFile(String path, String textChecksum) throws SVNException {
        if (!getUpdateRequest().isSendAll() && !getFileEditorEntry().isAdded() && isFileTextChanged()) {
            StringBuffer xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "fetch-file", SVNXMLUtil.XML_STYLE_SELF_CLOSING, BASE_CHECKSUM_ATTR, getFileBaseChecksum(), null);
            write(xmlBuffer);
        }

        closeEntry(getFileEditorEntry(), false, textChecksum);
        getFileEditorEntry().refresh();
        setFileTextChanged(false);
        setFileBaseChecksum(null);
    }

    public SVNCommitInfo closeEdit() throws SVNException {
        return null;
    }

    public void abortEdit() throws SVNException {
    }

    public void applyTextDelta(String path, String baseChecksum) throws SVNException {
        setFileTextChanged(true);
        setFileBaseChecksum(baseChecksum);
        if (isResourceWalk()) {
            return;
        }
        StringBuffer xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "txdelta", SVNXMLUtil.XML_STYLE_NORMAL, null, null);
        write(xmlBuffer);
    }

    public OutputStream textDeltaChunk(String path, SVNDiffWindow diffWindow) throws SVNException {
        if (!isResourceWalk()) {
            writeTextDeltaChunk(diffWindow);
        }
        return null;
    }

    public void textDeltaEnd(String path) throws SVNException {
        if (!isResourceWalk()) {
            textDeltaChunkEnd();
            setWriteTextDeltaHeader(true);
            StringBuffer xmlBuffer = SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "txdelta", null);
            write(xmlBuffer);
        }
    }

    private StringBuffer openResourceTag(String path, StringBuffer xmlBuffer) {
        return SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "resource", SVNXMLUtil.XML_STYLE_NORMAL, PATH_ATTR, path, xmlBuffer);
    }

    private StringBuffer closeResourceTag(StringBuffer xmlBuffer) {
        return SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "resource", xmlBuffer);
    }

    private void writeEntryTag(String tagName, String path) throws SVNException {
        String directoryName = SVNPathUtil.tail(path);
        StringBuffer xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, SVNXMLUtil.XML_STYLE_SELF_CLOSING, NAME_ATTR, directoryName, null);
        write(xmlBuffer);
    }

    private void writeEntryTag(String tagName, String path, long revision) throws SVNException {
        Map attrs = new SVNHashMap();
        attrs.put(NAME_ATTR, SVNPathUtil.tail(path));
        attrs.put(REVISION_ATTR, String.valueOf(revision));
        StringBuffer xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, SVNXMLUtil.XML_STYLE_NORMAL, attrs, null);
        addVersionURL(getRealPath(path), xmlBuffer);
        write(xmlBuffer);
    }

    private void writeAddEntryTag(boolean isDirectory, String path, String copyFromPath, long copyFromRevision) throws SVNException {
        StringBuffer xmlBuffer = null;
        String realPath = getRealPath(path);
        if (isResourceWalk()) {
            String resourcePath = getUpdateRequest().getTarget() == null || getUpdateRequest().getTarget().length() == 0 ?
                    path : SVNPathUtil.append(getUpdateRequest().getTarget(), SVNPathUtil.removeHead(path));
            xmlBuffer = openResourceTag(resourcePath, xmlBuffer);
        } else {
            Map attrs = new SVNHashMap();
            attrs.put(NAME_ATTR, SVNPathUtil.tail(path));
            if (isDirectory) {
                long createdRevision = DAVServletUtil.getSafeCreatedRevision(myRevisionRoot, realPath);
                String bcURL = DAVPathUtil.buildURI(getDAVResource().getResourceURI().getContext(), DAVResourceKind.BASELINE_COLL, createdRevision, realPath, false);
                attrs.put(BC_URL_ATTR, bcURL);
            }
            if (copyFromPath != null) {
                attrs.put(COPYFROM_PATH_ATTR, copyFromPath);
                attrs.put(COPYFROM_REVISION_ATTR, String.valueOf(copyFromRevision));
            }
            String tagName = isDirectory ? "add-directory" : "add-file";
            xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, SVNXMLUtil.XML_STYLE_NORMAL, attrs, null);
        }
        addVersionURL(realPath, xmlBuffer);
        if (isResourceWalk()) {
            closeResourceTag(xmlBuffer);
        }
        write(xmlBuffer);
    }

    private void changeProperties(EditorEntry entry, String name, SVNPropertyValue value) throws SVNException {
        //String quotedName = SVNEncodingUtil.xmlEncodeCDATA(name, true);
        if (getUpdateRequest().isSendAll()) {
            if (value != null) {
                writePropertyTag("set-prop", name, value);
            } else {
                writeEntryTag("remove-prop", name);
            }
        } else {
            if (SVNProperty.isEntryProperty(name)) {
                if (SVNProperty.COMMITTED_REVISION.equals(name)) {
                    entry.setCommitedRevision(value.getString());
                } else if (SVNProperty.COMMITTED_DATE.equals(name)) {
                    entry.setCommitedDate(value.getString());
                } else if (SVNProperty.LAST_AUTHOR.equals(name)) {
                    entry.setLastAuthor(value.getString());
                } else if (SVNProperty.LOCK_TOKEN.equals(name) && value == null) {
                    entry.addRemovedProperty(name);
                }
                return;
            }

            if (value == null) {
                entry.addRemovedProperty(name);
            } else {
                entry.setHasChangedProperty(true);
            }
        }
    }

    private void closeEntry(EditorEntry entry, boolean isDirectory, String textCheckSum) throws SVNException {
        if (isResourceWalk()) {
            return;
        }
        StringBuffer xmlBuffer = new StringBuffer();
        if (!entry.removedPropertiesCollectionIsEmpty() && !entry.isAdded()) {
            for (Iterator iterator = entry.getRemovedProperies(); iterator.hasNext();) {
                String name = (String) iterator.next();
                SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "remove-prop", SVNXMLUtil.XML_STYLE_SELF_CLOSING, NAME_ATTR, name, xmlBuffer);
            }
        }
        if (!getUpdateRequest().isSendAll() && entry.hasChangedProperties() && !entry.isAdded()) {
            SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "fetch-props", SVNXMLUtil.XML_STYLE_SELF_CLOSING, null, xmlBuffer);
        }
        SVNXMLUtil.openXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "prop", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);

        if (textCheckSum != null) {
            SVNXMLUtil.openCDataTag(SVNXMLUtil.SVN_DAV_PROPERTY_PREFIX, "md5-checksum", textCheckSum, xmlBuffer);
        }
        if (entry.getCommitedRevision() != null) {
            SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.VERSION_NAME.getName(), entry.getCommitedRevision(), xmlBuffer);
        }
        if (entry.getCommitedDate() != null) {
            SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.CREATION_DATE.getName(), entry.getCommitedDate(), xmlBuffer);
        }
        if (entry.getLastAuthor() != null) {
            SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, DAVElement.CREATOR_DISPLAY_NAME.getName(), entry.getLastAuthor(), xmlBuffer);
        }

        SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, "prop", xmlBuffer);

        String tagName = entry.isAdded() ? "add-" : "open-";
        tagName += isDirectory ? "directory" : "file";
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.SVN_NAMESPACE_PREFIX, tagName, xmlBuffer);
        write(xmlBuffer);
    }

    private StringBuffer addVersionURL(String path, StringBuffer xmlBuffer) {
        long revision = DAVServletUtil.getSafeCreatedRevision(myRevisionRoot, path);
        String url = DAVPathUtil.buildURI(getDAVResource().getResourceURI().getContext(), DAVResourceKind.VERSION, revision, path, false);
        xmlBuffer = SVNXMLUtil.openXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "checked-in", SVNXMLUtil.XML_STYLE_NORMAL, null, xmlBuffer);
        SVNXMLUtil.openCDataTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "href", url, xmlBuffer);
        SVNXMLUtil.closeXMLTag(SVNXMLUtil.DAV_NAMESPACE_PREFIX, "checked-in", xmlBuffer);
        return xmlBuffer;
    }

    private class EditorEntry {
        boolean myAdded = false;
        private String myCommitedRevision = null;
        private String myCommitedDate = null;
        private String myLastAuthor = null;
        private Collection myRemovedProperties;
        boolean myHasChangedProperties = false;


        public EditorEntry(boolean isAdded) {
            myAdded = isAdded;
        }

        private void setAdded(boolean isAdded) {
            myAdded = isAdded;
        }

        private boolean isAdded() {
            return myAdded;
        }

        private void setHasChangedProperty(boolean hasChangedProperties) {
            myHasChangedProperties = hasChangedProperties;
        }

        private boolean hasChangedProperties() {
            return myHasChangedProperties;
        }

        private void addRemovedProperty(String name) {
            if (myRemovedProperties == null) {
                myRemovedProperties = new ArrayList();
            }
            myRemovedProperties.add(name);
        }

        private boolean removedPropertiesCollectionIsEmpty() {
            return myRemovedProperties == null || myRemovedProperties.isEmpty();
        }

        private Iterator getRemovedProperies() {
            if (!removedPropertiesCollectionIsEmpty()) {
                return myRemovedProperties.iterator();
            }
            return null;
        }

        private String getCommitedRevision() {
            return myCommitedRevision;
        }

        private void setCommitedRevision(String commitedRevision) {
            myCommitedRevision = commitedRevision;
        }

        private String getCommitedDate() {
            return myCommitedDate;
        }

        private void setCommitedDate(String commitedDate) {
            myCommitedDate = commitedDate;
        }

        private String getLastAuthor() {
            return myLastAuthor;
        }

        private void setLastAuthor(String lastAuthor) {
            myLastAuthor = lastAuthor;
        }

        private void refresh() {
            myCommitedRevision = null;
            myCommitedDate = null;
            myLastAuthor = null;
            myHasChangedProperties = false;
            if (myRemovedProperties != null) {
                myRemovedProperties.clear();
            }
        }
    }
}
