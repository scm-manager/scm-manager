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



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Charsets;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;

import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import java.net.URL;

import java.nio.charset.Charset;
import java.nio.file.Path;

import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

/**
 * Smp plugin archive.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class SmpArchive
{

  /**
   * Constructs ...
   *
   *
   * @param archive
   */
  public SmpArchive(ByteSource archive)
  {
    this.archive = archive;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param archive
   *
   * @return
   */
  public static SmpArchive create(ByteSource archive)
  {
    return new SmpArchive(archive);
  }

  /**
   * Method description
   *
   *
   * @param archive
   *
   * @return
   */
  public static SmpArchive create(URL archive)
  {
    return create(Resources.asByteSource(archive));
  }

  /**
   * Method description
   *
   *
   * @param archive
   *
   * @return
   */
  public static SmpArchive create(Path archive)
  {
    return create(archive.toFile());
  }

  /**
   * Method description
   *
   *
   * @param archive
   *
   * @return
   */
  public static SmpArchive create(File archive)
  {
    return create(Files.asByteSource(archive));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param entry
   *
   * @return
   */
  private static String getPath(ZipEntry entry)
  {
    String path = entry.getName().replace("\\", "/");

    if (!path.startsWith("/"))
    {
      path = "/".concat(path);
    }

    return path;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param target
   *
   * @throws IOException
   */
  public void extract(File target) throws IOException
  {
    try (ZipInputStream zis = open())
    {
      ZipEntry ze = zis.getNextEntry();

      while (ze != null)
      {

        String fileName = ze.getName();
        File file = new File(target, fileName);

        IOUtil.mkdirs(file.getParentFile());

        if (ze.isDirectory())
        {
          IOUtil.mkdirs(file);
        }
        else
        {
          try (FileOutputStream fos = new FileOutputStream(file))
          {
            ByteStreams.copy(zis, fos);
          }
        }

        ze = zis.getNextEntry();
      }

      zis.closeEntry();
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public Plugin getPlugin() throws IOException
  {
    if (plugin == null)
    {
      plugin = createPlugin();

      PluginInformation info = plugin.getInformation();

      if (info == null)
      {
        throw new PluginException("could not find information section");
      }

      if (Strings.isNullOrEmpty(info.getGroupId()))
      {
        throw new PluginException(
          "could not find groupId in plugin descriptor");
      }

      if (Strings.isNullOrEmpty(info.getArtifactId()))
      {
        throw new PluginException(
          "could not find artifactId in plugin descriptor");
      }

      if (Strings.isNullOrEmpty(info.getVersion()))
      {
        throw new PluginException(
          "could not find version in plugin descriptor");
      }
    }

    return plugin;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private Plugin createPlugin() throws IOException
  {
    Plugin p = null;
    NonClosingZipInputStream zis = null;

    try
    {
      zis = openNonClosing();

      ZipEntry entry = zis.getNextEntry();

      while (entry != null)
      {
        if (PluginConstants.PATH_DESCRIPTOR.equals(getPath(entry)))
        {
          p = Plugins.parsePluginDescriptor(new InputStreamByteSource(zis));
        }

        entry = zis.getNextEntry();
      }

      zis.closeEntry();
    }
    finally
    {
      if (zis != null)
      {
        zis.reallyClose();
      }
    }

    if (p == null)
    {
      throw new PluginLoadException("could not find plugin descriptor");
    }

    return p;
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private ZipInputStream open() throws IOException
  {
    return new ZipInputStream(archive.openStream(), Charsets.UTF_8);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  private NonClosingZipInputStream openNonClosing() throws IOException
  {
    return new NonClosingZipInputStream(archive.openStream(), Charsets.UTF_8);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/07/13
   * @author         Enter your name here...
   */
  private static class InputStreamByteSource extends ByteSource
  {

    /**
     * Constructs ...
     *
     *
     * @param input
     */
    public InputStreamByteSource(InputStream input)
    {
      this.input = input;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     *
     * @throws IOException
     */
    @Override
    public InputStream openStream() throws IOException
    {
      return input;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private final InputStream input;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 14/07/12
   * @author         Enter your name here...
   */
  private static class NonClosingZipInputStream extends ZipInputStream
  {

    /**
     * Constructs ...
     *
     *
     * @param in
     * @param charset
     */
    public NonClosingZipInputStream(InputStream in, Charset charset)
    {
      super(in, charset);
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException
    {

      // do nothing
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    public void reallyClose() throws IOException
    {
      super.close();
    }
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private final ByteSource archive;

  /** Field description */
  private Plugin plugin;
}
