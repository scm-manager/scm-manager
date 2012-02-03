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



package sonia.scm.util;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Sebastian Sdorra
 */
public class ChecksumUtil
{

  /** Field description */
  private static final String DIGEST_TYPE = "SHA-1";

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param in
   *
   * @return
   *
   * @throws IOException
   */
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

  /**
   * Method description
   *
   *
   * @param input
   *
   * @return
   *
   * @throws IOException
   */
  public static String createChecksum(String input) throws IOException
  {
    MessageDigest digest = getDigest();

    digest.update(input.getBytes());

    return Util.toString(digest.digest());
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   *
   * @throws IOException
   */
  public static String createChecksum(File file) throws IOException
  {
    return createChecksum(new FileInputStream(file));
  }

  /**
   * Method description
   *
   *
   * @param content
   *
   * @return
   *
   * @throws IOException
   * 
   * @since 1.12
   */
  public static String createChecksum(byte[] content) throws IOException
  {
    return createChecksum(new ByteArrayInputStream(content));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
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
