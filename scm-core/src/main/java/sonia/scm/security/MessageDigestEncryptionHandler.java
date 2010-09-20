/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
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
