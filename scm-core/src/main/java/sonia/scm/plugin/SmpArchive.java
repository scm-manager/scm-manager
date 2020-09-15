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

package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Strings;
import com.google.common.io.ByteSource;
import com.google.common.io.ByteStreams;
import com.google.common.io.Files;
import com.google.common.io.Resources;
import sonia.scm.util.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

//~--- JDK imports ------------------------------------------------------------

/**
 * Smp plugin archive.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class SmpArchive {

  private final ByteSource archive;
  private final ZipInputStreamFactory zipInputStreamFactory;
  private InstalledPluginDescriptor plugin;

  /**
   * Constructs ...
   *
   * @param archive
   */
  public SmpArchive(ByteSource archive) {
    this(archive, source -> new ZipInputStream(archive.openStream(), StandardCharsets.UTF_8));
  }

  @VisibleForTesting
  SmpArchive(ByteSource archive, ZipInputStreamFactory zipInputStreamFactory) {
    this.archive = archive;
    this.zipInputStreamFactory = zipInputStreamFactory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param archive
   * @return
   */
  public static SmpArchive create(ByteSource archive) {
    return new SmpArchive(archive);
  }

  /**
   * Method description
   *
   * @param archive
   * @return
   */
  public static SmpArchive create(URL archive) {
    return create(Resources.asByteSource(archive));
  }

  /**
   * Method description
   *
   * @param archive
   * @return
   */
  public static SmpArchive create(Path archive) {
    return create(archive.toFile());
  }

  /**
   * Method description
   *
   * @param archive
   * @return
   */
  public static SmpArchive create(File archive) {
    return create(Files.asByteSource(archive));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   * @param entry
   * @return
   */
  private static String getPath(ZipEntry entry) {
    String path = entry.getName().replace("\\", "/");

    if (!path.startsWith("/")) {
      path = "/".concat(path);
    }

    return path;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   * @param target
   * @throws IOException
   */
  public void extract(File target) throws IOException {
    try (ZipInputStream zis = open()) {
      ZipEntry ze = zis.getNextEntry();

      while (ze != null) {

        String fileName = ze.getName();
        File file = new File(target, fileName);
        if (!IOUtil.isChild(target, file)) {
          throw new PluginException("smp contains illegal path, which tries to escape from the plugin directory: " + fileName);
        }

        IOUtil.mkdirs(file.getParentFile());

        if (ze.isDirectory()) {
          IOUtil.mkdirs(file);
        } else {
          try (FileOutputStream fos = new FileOutputStream(file)) {
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
   * @return
   * @throws IOException
   */
  public InstalledPluginDescriptor getPlugin() throws IOException {
    if (plugin == null) {
      plugin = createPlugin();

      PluginInformation info = plugin.getInformation();

      if (info == null) {
        throw new PluginException("could not find information section");
      }

      if (Strings.isNullOrEmpty(info.getName())) {
        throw new PluginException(
          "could not find name in plugin descriptor");
      }

      if (Strings.isNullOrEmpty(info.getVersion())) {
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
   * @return
   * @throws IOException
   */
  private InstalledPluginDescriptor createPlugin() throws IOException {
    InstalledPluginDescriptor p = null;
    NonClosingZipInputStream zis = null;

    try {
      zis = openNonClosing();

      ZipEntry entry = zis.getNextEntry();

      while (entry != null) {
        if (PluginConstants.PATH_DESCRIPTOR.equals(getPath(entry))) {
          p = Plugins.parsePluginDescriptor(new InputStreamByteSource(zis));
        }

        entry = zis.getNextEntry();
      }

      zis.closeEntry();
    } finally {
      if (zis != null) {
        zis.reallyClose();
      }
    }

    if (p == null) {
      throw new PluginLoadException("could not find plugin descriptor");
    }

    return p;
  }

  /**
   * Method description
   *
   * @return
   * @throws IOException
   */
  private ZipInputStream open() throws IOException {
    return zipInputStreamFactory.open(archive);
  }

  /**
   * Method description
   *
   * @return
   * @throws IOException
   */
  private NonClosingZipInputStream openNonClosing() throws IOException {
    return new NonClosingZipInputStream(archive.openStream(), StandardCharsets.UTF_8);
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   * @author Enter your name here...
   * @version Enter version here..., 14/07/13
   */
  private static class InputStreamByteSource extends ByteSource {

    /**
     * Constructs ...
     *
     * @param input
     */
    public InputStreamByteSource(InputStream input) {
      this.input = input;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     * @return
     * @throws IOException
     */
    @Override
    public InputStream openStream() throws IOException {
      return input;
    }

    //~--- fields -------------------------------------------------------------

    /**
     * Field description
     */
    private final InputStream input;
  }


  /**
   * Class description
   *
   * @author Enter your name here...
   * @version Enter version here..., 14/07/12
   */
  private static class NonClosingZipInputStream extends ZipInputStream {

    /**
     * Constructs ...
     *
     * @param in
     * @param charset
     */
    public NonClosingZipInputStream(InputStream in, Charset charset) {
      super(in, charset);
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {

      // do nothing
    }

    /**
     * Method description
     *
     * @throws IOException
     */
    public void reallyClose() throws IOException {
      super.close();
    }
  }

  @FunctionalInterface
  interface ZipInputStreamFactory {

    ZipInputStream open(ByteSource source) throws IOException;

  }
}
