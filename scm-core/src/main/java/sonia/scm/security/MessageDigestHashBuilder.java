/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.security;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.UnsupportedEncodingException;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author Sebastian Sdorra
 * @since 1.13
 */
public class MessageDigestHashBuilder implements HashBuilder
{

  /** Field description */
  public static final int DEFAULT_SALT_LENGTH = 8;

  /** Field description */
  public static final String ENCODING = "UTF-8";

  /** Field description */
  public static final String RANDOM_INSTANCE = "SHA1PRNG";

  /** Field description */
  private static Pattern PATTERN = Pattern.compile("\\{([^\\}]+)\\}(.*)");

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param digest
   * @param value
   * @param salt
   * @param iterations
   * @param appendSalt
   * @param enableLabel
   */
  public MessageDigestHashBuilder(String digest, String value, byte[] salt,
                                  int iterations, boolean appendSalt,
                                  boolean enableLabel)
  {
    this.digest = digest;
    this.value = value;
    this.salt = salt;
    this.iterations = iterations;
    this.appendSalt = appendSalt;
    this.enableLable = enableLabel;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param hash
   *
   * @return
   */
  public static Extractor createExtractor(String hash)
  {
    return new Extractor(hash);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HashBuilder appendSalt()
  {
    this.appendSalt = true;

    return this;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HashBuilder createSalt()
  {
    return createSalt(DEFAULT_SALT_LENGTH);
  }

  /**
   * Method description
   *
   *
   * @param length
   *
   * @return
   */
  @Override
  public HashBuilder createSalt(int length)
  {
    try
    {
      SecureRandom random = SecureRandom.getInstance(RANDOM_INSTANCE);

      this.salt = new byte[length];
      random.nextBytes(salt);
    }
    catch (NoSuchAlgorithmException ex)
    {
      throw new SecurityException("could not find secure random instance");
    }

    return this;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public HashBuilder enableLabel()
  {
    this.enableLable = true;

    return this;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public byte[] toByteArray()
  {
    byte[] input = null;

    try
    {
      MessageDigest md = MessageDigest.getInstance(digest);

      md.reset();

      if (salt != null)
      {
        md.update(salt);
      }

      input = md.digest(value.getBytes(ENCODING));

      if (iterations > 0)
      {
        for (int i = 0; i < iterations; i++)
        {
          md.reset();
          input = md.digest(input);
        }
      }

      if ((salt != null) && appendSalt)
      {
        byte[] content = new byte[input.length + salt.length];

        System.arraycopy(input, 0, content, 0, input.length);
        System.arraycopy(salt, 0, content, input.length, salt.length);
        input = content;
      }
    }
    catch (UnsupportedEncodingException ex)
    {
      throw new SecurityException("unknown encoding", ex);
    }
    catch (NoSuchAlgorithmException ex)
    {
      throw new SecurityException("unknown digest", ex);
    }

    return input;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toHexString()
  {
    String hexString = null;

    if (enableLable)
    {
      StringBuilder buffer = new StringBuilder();

      buffer.append("{").append(digest).append("}");
      buffer.append(Util.toString(toByteArray()));
      hexString = buffer.toString();
    }
    else
    {
      hexString = Util.toString(toByteArray());
    }

    return hexString;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getHexSalt()
  {
    String hexSalt = null;

    if (salt != null)
    {
      hexSalt = Util.toString(salt);
    }

    return hexSalt;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public byte[] getSalt()
  {
    return salt;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param iterations
   *
   * @return
   */
  @Override
  public HashBuilder setIterations(int iterations)
  {
    this.iterations = iterations;

    return this;
  }

  /**
   * Method description
   *
   *
   * @param salt
   *
   * @return
   */
  @Override
  public HashBuilder setSalt(byte[] salt)
  {
    this.salt = salt;

    return this;
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  @Override
  public HashBuilder setValue(String value)
  {
    this.value = value;

    return this;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 12/02/17
   * @author         Enter your name here...
   */
  public static class Extractor
  {

    /**
     * Constructs ...
     *
     *
     * @param hash
     */
    public Extractor(String hash)
    {
      this.hash = hash;
    }

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    public MessageDigestHashBuilder getHashBuilder()
    {
      return getHashBuilder(-1);
    }

    /**
     * Method description
     *
     *
     * @param saltLength
     *
     * @return
     */
    public MessageDigestHashBuilder getHashBuilder(int saltLength)
    {
      MessageDigestHashBuilder hashBuilder = null;
      Matcher m = PATTERN.matcher(hash);

      if (m.matches())
      {
        String digest = m.group(1);

        if (digest != null)
        {
          byte[] salt = null;

          if (saltLength > 0)
          {
            String hashWithoutPrefix = m.group(2);

            salt = getSalt(hashWithoutPrefix, saltLength);
          }

          hashBuilder = new MessageDigestHashBuilder(digest, null, salt, 0,
                  salt != null, true);
        }
      }

      return hashBuilder;
    }

    /**
     * Method description
     *
     *
     * @return
     */
    public String getLabel()
    {
      String label = null;
      Matcher m = PATTERN.matcher(hash);

      if (m.matches())
      {
        label = m.group(1);
      }

      return label;
    }

    /**
     * Method description
     *
     *
     * @param length
     *
     * @return
     */
    public byte[] getSalt(int length)
    {
      Matcher m = PATTERN.matcher(hash);
      String hashWithoutPrefix = hash;

      if (m.matches())
      {
        hashWithoutPrefix = m.group(2);
      }

      return getSalt(hashWithoutPrefix, length);
    }

    /**
     * Method description
     *
     *
     * @param hashWithoutPrefix
     * @param length
     *
     * @return
     */
    private byte[] getSalt(String hashWithoutPrefix, int length)
    {
      byte[] content = Util.fromHexString(hashWithoutPrefix);
      byte[] salt = new byte[length];

      System.arraycopy(content, content.length - length, salt, 0, length);

      return salt;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String hash;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean appendSalt;

  /** Field description */
  private String digest;

  /** Field description */
  private boolean enableLable;

  /** Field description */
  private int iterations;

  /** Field description */
  private byte[] salt;

  /** Field description */
  private String value;
}
