/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */


package sonia.scm.cli.config;

import com.google.common.base.Charsets;
import com.google.common.io.ByteStreams;
import org.junit.Test;
import sonia.scm.security.KeyGenerator;

import java.io.*;

import static org.junit.Assert.assertEquals;

public class AesCipherStreamHandlerTest {

  private final KeyGenerator keyGenerator = new SecureRandomKeyGenerator();

  @Test
  public void testEncryptAndDecrypt() throws IOException {
    AesCipherStreamHandler cipherStreamHandler = new AesCipherStreamHandler(keyGenerator.createKey());

    // douglas adams
    String content = "If you try and take a cat apart to see how it works, the first thing you have on your hands is a nonworking cat.";

    // encrypt
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    OutputStream encryptedOutput = cipherStreamHandler.encrypt(output);
    encryptedOutput.write(content.getBytes(Charsets.UTF_8));
    encryptedOutput.close();

    InputStream input = new ByteArrayInputStream(output.toByteArray());
    input = cipherStreamHandler.decrypt(input);
    byte[] decrypted = ByteStreams.toByteArray(input);

    assertEquals(content, new String(decrypted, Charsets.UTF_8));
  }

}
