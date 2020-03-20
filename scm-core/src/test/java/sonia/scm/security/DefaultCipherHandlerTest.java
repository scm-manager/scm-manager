/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
import org.mockito.junit.MockitoJUnitRunner;
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
