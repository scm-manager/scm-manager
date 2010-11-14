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

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Writer;

import org.tmatesoft.svn.core.internal.util.SVNBase64;


/**
 * @version 1.2.0
 * @author  TMate Software Ltd.
 */
public class DAVBase64OutputStream extends OutputStream {
    
    private static final int BASE64_LINE_LENGTH = 57;
    
    private Writer myWriter;
    private ByteArrayOutputStream myBuffer;

    public DAVBase64OutputStream(Writer dst) {
        myWriter = dst;
        myBuffer = new ByteArrayOutputStream(100);
    }

    public void write(int b) throws IOException {
        write(new byte[] {(byte) (b & 0xff)}, 0, 1);
    }

    public void write(byte[] b) throws IOException {
        write(b, 0, b.length);
    }

    public void close() throws IOException {
        flush();
    }

    public void write(byte[] b, int off, int len) throws IOException {
        while (len > 0) {
            int needed = BASE64_LINE_LENGTH - myBuffer.size();
            int toWrite = Math.min(needed, len);
            myBuffer.write(b, off, toWrite);
            off += toWrite;
            len -= toWrite;
            if (myBuffer.size() == BASE64_LINE_LENGTH) {
                flushBuffer();
            }
        }
    }

    public void flush() throws IOException {
        if (myBuffer.size() > 0) {
            flushBuffer();
        }
    }
    
    private void flushBuffer() throws IOException {
        myWriter.write(SVNBase64.byteArrayToBase64(myBuffer.toByteArray()) + "\n");
        myBuffer.reset();
    }
    
    

}
