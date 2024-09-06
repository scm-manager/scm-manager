/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
