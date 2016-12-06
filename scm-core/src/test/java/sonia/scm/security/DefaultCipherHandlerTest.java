/**
 * Copyright (c) 2014, Sebastian Sdorra
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

package sonia.scm.security;

import java.io.File;
import java.io.IOException;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.junit.rules.TemporaryFolder;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.SCMContextProvider;

/**
 * Unit tests for {@link DefaultCipherHandler}.
 * 
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultCipherHandlerTest {
  
  @Mock
  private SCMContextProvider context;
  
  @Mock
  private KeyGenerator keyGenerator;
  
  @Rule
  public TemporaryFolder tempFolder = new TemporaryFolder();
  
  /**
   * Tests loading and storing default key.
   * 
   * @throws IOException 
   */
  @Test
  public void testLoadingAndStoringDefaultKey() throws IOException {
    File baseDirectory = tempFolder.newFolder();
    when(context.getBaseDirectory()).thenReturn(baseDirectory);
    when(keyGenerator.createKey()).thenReturn("secret");
    
    DefaultCipherHandler cipher = new DefaultCipherHandler(context, keyGenerator);
    File configDirectory = new File(baseDirectory, "config");
    assertTrue(new File(configDirectory, DefaultCipherHandler.CIPHERKEY_FILENAME).exists());
    
    // plain text for assertion
    String plain = "hallo123";
    
    // encrypt value with new generated key
    String encrypted = cipher.encode(plain);
    
    // load key from disk
    cipher = new DefaultCipherHandler(context, keyGenerator);
    
    // decrypt with loaded key
    assertEquals(plain, cipher.decode(encrypted));
  }

  /**
   * Test encode and decode method with a separate key.
   */
  @Test
  public void testEncodeDecodeWithSeparateKey(){
    char[] key = "testkey".toCharArray();
    DefaultCipherHandler cipher = new DefaultCipherHandler("somekey");
    assertEquals("hallo123", cipher.decode(key, cipher.encode(key, "hallo123")));
  }

  /**
   * Test encode and decode method with the default key.
   */  
  @Test
  public void testEncodeDecodeWithDefaultKey() {
    DefaultCipherHandler cipher = new DefaultCipherHandler("testkey");
    assertEquals("hallo123", cipher.decode(cipher.encode("hallo123")));
  }

}