/*
 * ====================================================================
 * Copyright (c) 2004-2009 TMate Software Ltd.  All rights reserved.
 *
 * This software is licensed as described in the file COPYING, which
 * you should have received as part of this distribution.  The terms
 * are also available at http://svnkit.com/license.html.
 * If newer versions of this license are posted there, you may use a
 * newer version instead, at your option.
 * ====================================================================
 */
package org.tmatesoft.svn.core.internal.io.fs;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;

import org.tmatesoft.svn.core.ISVNCanceller;
import org.tmatesoft.svn.core.SVNErrorCode;
import org.tmatesoft.svn.core.SVNErrorMessage;
import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNPropertyValue;
import org.tmatesoft.svn.core.SVNRevisionProperty;
import org.tmatesoft.svn.core.internal.delta.SVNDeltaCombiner;
import org.tmatesoft.svn.core.internal.util.SVNDate;
import org.tmatesoft.svn.core.internal.util.SVNHashMap;
import org.tmatesoft.svn.core.internal.util.SVNPathUtil;
import org.tmatesoft.svn.core.internal.util.SVNUUIDGenerator;
import org.tmatesoft.svn.core.internal.wc.IOExceptionWrapper;
import org.tmatesoft.svn.core.internal.wc.ISVNCommitPathHandler;
import org.tmatesoft.svn.core.internal.wc.SVNCommitUtil;
import org.tmatesoft.svn.core.internal.wc.SVNErrorManager;
import org.tmatesoft.svn.core.internal.wc.SVNFileUtil;
import org.tmatesoft.svn.core.io.ISVNEditor;
import org.tmatesoft.svn.core.io.diff.SVNDeltaGenerator;
import org.tmatesoft.svn.core.io.diff.SVNDiffWindow;
import org.tmatesoft.svn.util.SVNLogType;


/**
 * @version 1.3
 * @author  TMate Software Ltd.
 */
public class FSRepositoryUtil {
    public static final int MAX_KEY_SIZE = 200;

    private static final byte[] ourCopyBuffer = new byte[1024*16];

    public static String generateLockToken() throws SVNException {
        String uuid = SVNUUIDGenerator.formatUUID(SVNUUIDGenerator.generateUUID());
        return FSFS.SVN_OPAQUE_LOCK_TOKEN + uuid;

    }

    public static void replay(FSFS fsfs, FSRoot root, String basePath, long lowRevision, boolean sendDeltas, ISVNEditor editor) throws SVNException {
        Map fsChanges = root.getChangedPaths();
        basePath = basePath.startsWith("/") ? basePath.substring(1) : basePath;
        Collection interestingPaths = new LinkedList();
        Map changedPaths = new SVNHashMap();
        for (Iterator paths = fsChanges.keySet().iterator(); paths.hasNext();) {
            String path = (String) paths.next();
            FSPathChange change = (FSPathChange) fsChanges.get(path);

            path = path.startsWith("/") ? path.substring(1) : path;
            if (SVNPathUtil.isWithinBasePath(basePath, path)) {
                interestingPaths.add(path);
                changedPaths.put(path, change);
            } else if (SVNPathUtil.isWithinBasePath(path, basePath)) {
                interestingPaths.add(path);
                changedPaths.put(path, change);
            }
        }
        if (FSRepository.isInvalidRevision(lowRevision)) {
            lowRevision = 0;
        }

        FSRoot compareRoot = null;
        if (sendDeltas) {
            long revision = -1;
            if (root instanceof FSRevisionRoot) {
                FSRevisionRoot revRoot = (FSRevisionRoot) root;
                revision = revRoot.getRevision() - 1;
            } else if (root instanceof FSTransactionRoot) {
                FSTransactionRoot txnRoot = (FSTransactionRoot) root;
                revision = txnRoot.getTxn().getBaseRevision();
            }
            compareRoot = fsfs.createRevisionRoot(revision);
        }

        if (root instanceof FSRevisionRoot) {
            FSRevisionRoot revRoot = (FSRevisionRoot) root;
            editor.targetRevision(revRoot.getRevision());
        }

        ISVNCommitPathHandler handler = new FSReplayPathHandler(fsfs, root, compareRoot, changedPaths, basePath, lowRevision);
        SVNCommitUtil.driveCommitEditor(handler, interestingPaths, editor, -1);
    }

    public synchronized static void copy(InputStream src, OutputStream dst, ISVNCanceller canceller) throws SVNException {
        try {
            while (true) {
                if (canceller != null) {
                    canceller.checkCancelled();
                }
                int length = src.read(ourCopyBuffer);
                if (length > 0) {
                    dst.write(ourCopyBuffer, 0, length);
                }
                if (length < 0) {
                    break;
                }
            }
        } catch (IOExceptionWrapper ioew) {
            throw ioew.getOriginalException();
        } catch (IOException ioe) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, ioe.getLocalizedMessage());
            SVNErrorManager.error(err, ioe, SVNLogType.FSFS);
        }
    }

    public static boolean arePropertiesEqual(FSRevisionNode revNode1, FSRevisionNode revNode2) {
        return areRepresentationsEqual(revNode1, revNode2, true);
    }

    public static boolean arePropertiesChanged(FSRoot root1, String path1, FSRoot root2, String path2) throws SVNException {
        FSRevisionNode node1 = root1.getRevisionNode(path1);
        FSRevisionNode node2 = root2.getRevisionNode(path2);
        return !areRepresentationsEqual(node1, node2, true);
    }

    public static boolean areFileContentsChanged(FSRoot root1, String path1, FSRoot root2, String path2) throws SVNException {
        if (root1.checkNodeKind(path1) != SVNNodeKind.FILE) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_GENERAL, "''{0}'' is not a file", path1);
            SVNErrorManager.error(err, SVNLogType.FSFS);
        }
        if (root2.checkNodeKind(path2) != SVNNodeKind.FILE) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_GENERAL, "''{0}'' is not a file", path2);
            SVNErrorManager.error(err, SVNLogType.FSFS);
        }
        FSRevisionNode revNode1 = root1.getRevisionNode(path1);
        FSRevisionNode revNode2 = root2.getRevisionNode(path2);
        return !areRepresentationsEqual(revNode1, revNode2, false);
    }

    public static SVNProperties getPropsDiffs(SVNProperties sourceProps, SVNProperties targetProps){
        SVNProperties result = new SVNProperties();

        if(sourceProps == null){
            sourceProps = new SVNProperties();
        }

        if(targetProps == null){
            targetProps = new SVNProperties();
        }

        for(Iterator names = sourceProps.nameSet().iterator(); names.hasNext();){
            String propName = (String) names.next();
            SVNPropertyValue srcPropVal = sourceProps.getSVNPropertyValue(propName);
            SVNPropertyValue targetPropVal = targetProps.getSVNPropertyValue(propName);

            if (targetPropVal == null) {
                result.put(propName, targetPropVal);
            } else if (!targetPropVal.equals(srcPropVal)) {
                result.put(propName, targetPropVal);
            }
        }

        for(Iterator names = targetProps.nameSet().iterator(); names.hasNext();){
            String propName = (String)names.next();
            SVNPropertyValue targetPropVal = targetProps.getSVNPropertyValue(propName);
            SVNPropertyValue sourceValue = sourceProps.getSVNPropertyValue(propName);
            if (sourceValue == null){
                result.put(propName, targetPropVal);
            }
        }

        return result;
    }

    public static boolean checkFilesDifferent(FSRoot root1, String path1, FSRoot root2,
            String path2, SVNDeltaCombiner deltaCombiner) throws SVNException {
        boolean changed = FSRepositoryUtil.areFileContentsChanged(root1, path1, root2, path2);
        if (!changed) {
            return false;
        }

        FSRevisionNode revNode1 = root1.getRevisionNode(path1);
        FSRevisionNode revNode2 = root2.getRevisionNode(path2);
        if (revNode1.getFileLength() != revNode2.getFileLength()) {
            return true;
        }

        if (!revNode1.getFileMD5Checksum().equals(revNode2.getFileMD5Checksum())) {
            return true;
        }

        InputStream file1IS = null;
        InputStream file2IS = null;
        try {
            file1IS = root1.getFileStreamForPath(deltaCombiner, path1);
            file2IS = root2.getFileStreamForPath(deltaCombiner, path2);

            int r1 = -1;
            int r2 = -1;
            while (true) {
                r1 = file1IS.read();
                r2 = file2IS.read();
                if (r1 != r2) {
                    return true;
                }
                if (r1 == -1) {// we've finished - files do not differ
                    break;
                }
            }
        } catch (IOException ioe) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, ioe.getLocalizedMessage());
            SVNErrorManager.error(err, ioe, SVNLogType.FSFS);
        } finally {
            SVNFileUtil.closeFile(file1IS);
            SVNFileUtil.closeFile(file2IS);
        }
        return false;
    }

    public static void sendTextDelta(ISVNEditor editor, String editPath, String sourcePath,
            String hexDigest, FSRevisionRoot sourceRoot, String targetPath,
            FSRevisionRoot targetRoot, boolean sendDeltas, SVNDeltaCombiner deltaCombiner,
            SVNDeltaGenerator deltaGenerator, FSFS fsfs) throws SVNException {
        editor.applyTextDelta(editPath, hexDigest);

        if (sendDeltas) {
            InputStream sourceStream = null;
            InputStream targetStream = null;
            try {
                if (sourceRoot != null && sourcePath != null) {
                    sourceStream = sourceRoot.getFileStreamForPath(deltaCombiner, sourcePath);
                } else {
                    sourceStream = FSInputStream.createDeltaStream(deltaCombiner, (FSRevisionNode) null, fsfs);
                }
                targetStream = targetRoot.getFileStreamForPath(deltaCombiner, targetPath);
                deltaGenerator.sendDelta(editPath, sourceStream, 0, targetStream, editor, false);
            } finally {
                SVNFileUtil.closeFile(sourceStream);
                SVNFileUtil.closeFile(targetStream);
            }
        } else {
            editor.textDeltaChunk(editPath, SVNDiffWindow.EMPTY);
            editor.textDeltaEnd(editPath);
        }
    }

    public static void loadRootChangesOffset(FSFS fsfs, long revision, FSFile file, long[] rootOffset, long[] changesOffset) throws SVNException {
        ByteBuffer buffer = ByteBuffer.allocate(64);
        long offset = 0;

        if (fsfs.isPackedRevision(revision) && ((revision + 1) % fsfs.getMaxFilesPerDirectory()) != 0) {
            offset = fsfs.getPackedOffset(revision + 1);
        } else {
            offset = file.size();
        }

        long revOffset = 0;
        if (fsfs.isPackedRevision(revision)) {
            revOffset = fsfs.getPackedOffset(revision);
        }

        file.seek(offset - 64);
        try {
            file.read(buffer);
        } catch (IOException e) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.IO_ERROR, e.getMessage());
            SVNErrorManager.error(err, e, SVNLogType.FSFS);
        }

        buffer.flip();
        if (buffer.get(buffer.limit() - 1) != '\n') {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_CORRUPT,
                    "Revision file lacks trailing newline");
            SVNErrorManager.error(err, SVNLogType.FSFS);
        }

        int spaceIndex = -1;
        int eolIndex = -1;
        for (int i = buffer.limit() - 2; i >= 0; i--) {
            byte b = buffer.get(i);
            if (b == ' ' && spaceIndex < 0) {
                spaceIndex = i;
            } else if (b == '\n' && eolIndex < 0) {
                eolIndex = i;
                break;
            }
        }
        if (eolIndex < 0) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_CORRUPT,
                    "Final line in revision file longer than 64 characters");
            SVNErrorManager.error(err, SVNLogType.FSFS);
        }
        if (spaceIndex < 0) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_CORRUPT,
                    "Final line in revision file missing space");
            SVNErrorManager.error(err, SVNLogType.FSFS);
        }
        CharsetDecoder decoder = Charset.forName("UTF-8").newDecoder();
        try {
            buffer.limit(buffer.limit() - 1);
            buffer.position(spaceIndex + 1);
            String line = decoder.decode(buffer).toString();
            if (changesOffset != null && changesOffset.length > 0) {
                changesOffset[0] = revOffset + Long.parseLong(line);
            }

            buffer.limit(spaceIndex);
            buffer.position(eolIndex + 1);
            line = decoder.decode(buffer).toString();
            if (rootOffset != null && rootOffset.length > 0) {
                rootOffset[0] = revOffset + Long.parseLong(line);
            }
        } catch (NumberFormatException nfe) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_CORRUPT,
                    "Final line in revision file missing changes and root offsets");
            SVNErrorManager.error(err, nfe, SVNLogType.FSFS);
        } catch (CharacterCodingException e) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_CORRUPT,
                    "Final line in revision file missing changes and root offsets");
            SVNErrorManager.error(err, e, SVNLogType.FSFS);
        }
    }

    public static String generateNextKey(String oldKey) throws SVNException {
        char[] nextKey = new char[oldKey.length() + 1];
        boolean carry = true;
        if (oldKey.length() > 1 && oldKey.charAt(0) == '0') {
            return null;
        }
        for (int i = oldKey.length() - 1; i >= 0; i--) {
            char c = oldKey.charAt(i);
            if (!((c >= '0' && c <= '9') || (c >= 'a' && c <= 'z'))) {
                return null;
            }
            if (carry) {
                if (c == 'z') {
                    nextKey[i] = '0';
                } else {
                    carry = false;
                    if (c == '9') {
                        nextKey[i] = 'a';
                    } else {
                        nextKey[i] = (char) (c + 1);
                    }
                }
            } else {
                nextKey[i] = c;
            }
        }
        int nextKeyLength = oldKey.length() + (carry ? 1 : 0);
        if (nextKeyLength >= MAX_KEY_SIZE) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.UNKNOWN,
                    "FATAL error: new key length is greater than the threshold {0}",
                    new Integer(MAX_KEY_SIZE));
            SVNErrorManager.error(err, SVNLogType.FSFS);
        }
        if (carry) {
            System.arraycopy(nextKey, 0, nextKey, 1, oldKey.length());
            nextKey[0] = '1';
        }
        return new String(nextKey, 0, nextKeyLength);
    }

    public static void checkReposDBFormat(int format) throws SVNException {
        if (format < FSFS.DB_FORMAT_LOW || format > FSFS.DB_FORMAT) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.FS_UNSUPPORTED_FORMAT,
                    "Expected FS format between ''{0}'' and ''{1}''; found format ''{2}''",
                    new Object[] {new Integer(FSFS.DB_FORMAT_LOW), new Integer(FSFS.DB_FORMAT),
                    new Integer(format)});
            SVNErrorManager.error(err, SVNLogType.FSFS);
        }
    }

    public static void validateProperty(String propertyName, SVNPropertyValue propertyValue) throws SVNException {
        if (!SVNProperty.isRegularProperty(propertyName)) {
            SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.REPOS_BAD_ARGS,
                    "Storage of non-regular property ''{0}'' is disallowed through the repository interface, and could indicate a bug in your client", propertyName);
            SVNErrorManager.error(err, SVNLogType.FSFS);
        }

        if (SVNProperty.isSVNProperty(propertyName) && propertyValue != null) {
            if (SVNRevisionProperty.DATE.equals(propertyName)) {
                try {
                    SVNDate.parseDateString(propertyValue.getString());
                } catch (SVNException svne) {
                    SVNErrorMessage err = SVNErrorMessage.create(SVNErrorCode.BAD_PROPERTY_VALUE);
                    SVNErrorManager.error(err, SVNLogType.FSFS);
                }
            }
        }
    }

    private static boolean areRepresentationsEqual(FSRevisionNode revNode1, FSRevisionNode revNode2, boolean forProperties) {
        if(revNode1 == revNode2){
            return true;
        }else if(revNode1 == null || revNode2 == null){
            return false;
        }
        return FSRepresentation.compareRepresentations(forProperties ? revNode1.getPropsRepresentation() : revNode1.getTextRepresentation(), forProperties ? revNode2.getPropsRepresentation() : revNode2.getTextRepresentation());
    }

}
