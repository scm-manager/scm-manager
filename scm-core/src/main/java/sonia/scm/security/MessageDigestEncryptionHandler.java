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

package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.AssertUtil;

//~--- JDK imports ------------------------------------------------------------

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 *
 * @author Sebastian Sdorra
 */
public class MessageDigestEncryptionHandler implements EncryptionHandler
{

  /** Field description */
  public static final String DEFAULT_DIGEST = "SHA-1";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public MessageDigestEncryptionHandler()
  {
    this.digest = DEFAULT_DIGEST;
  }

  /**
   * Constructs ...
   *
   *
   * @param digest
   */
  public MessageDigestEncryptionHandler(String digest)
  {
    this.digest = digest;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  @Override
  public String encrypt(String value)
  {
    String result = null;

    try
    {
      AssertUtil.assertIsNotEmpty(value);

      MessageDigest messageDigest = MessageDigest.getInstance(digest);

      result = encrypt(messageDigest, value);
    }
    catch (NoSuchAlgorithmException ex)
    {
      throw new EncryptionException(ex);
    }

    return result;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getDigest()
  {
    return digest;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param messageDigest
   * @param value
   *
   * @return
   */
  private String encrypt(MessageDigest messageDigest, String value)
  {
    messageDigest.reset();
    messageDigest.update(value.getBytes());

    byte hashCode[] = messageDigest.digest();
    StringBuilder hashString = new StringBuilder();

    for (int i = 0; i < hashCode.length; i++)
    {
      int x = hashCode[i] & 0xff;

      if (x < 16)
      {
        hashString.append('0');
      }

      hashString.append(Integer.toString(x, 16));
    }

    return hashString.toString();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String digest;
}
