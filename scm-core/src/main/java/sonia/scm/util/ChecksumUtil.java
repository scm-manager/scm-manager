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
    
package sonia.scm.util;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public final class ChecksumUtil
{

  private static final String DIGEST_TYPE = "SHA-1";


  private ChecksumUtil() {}


  public static String createChecksum(InputStream in) throws IOException
  {
    MessageDigest digest = null;

    try
    {
      byte[] buffer = new byte[1024];

      digest = getDigest();

      int numRead = 0;

      do
      {
        numRead = in.read(buffer);

        if (numRead > 0)
        {
          digest.update(buffer, 0, numRead);
        }
      }
      while (numRead != -1);
    }
    finally
    {
      if (in != null)
      {
        in.close();
      }
    }

    return Util.toString(digest.digest());
  }

  public static String createChecksum(String input) throws IOException
  {
    MessageDigest digest = getDigest();

    digest.update(input.getBytes());

    return Util.toString(digest.digest());
  }

  public static String createChecksum(File file) throws IOException
  {
    return createChecksum(new FileInputStream(file));
  }

  /**
   * @since 1.12
   */
  public static String createChecksum(byte[] content) throws IOException
  {
    return createChecksum(new ByteArrayInputStream(content));
  }


  
  private static MessageDigest getDigest()
  {
    MessageDigest digest = null;

    try
    {
      digest = MessageDigest.getInstance(DIGEST_TYPE);
    }
    catch (NoSuchAlgorithmException ex)
    {
      throw new RuntimeException("no such digest");
    }

    return digest;
  }
}
