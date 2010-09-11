/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.util;

//~--- JDK imports ------------------------------------------------------------

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

    byte[] b = digest.digest();

    return toHexString(b);
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
   * @param byteArray
   *
   * @return
   */
  private static String toHexString(byte[] byteArray)
  {
    StringBuilder buffer = new StringBuilder();

    for (byte b : byteArray)
    {
      buffer.append(Integer.toHexString(b));
    }

    return buffer.toString();
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
