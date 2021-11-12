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

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.io.Command;
import sonia.scm.io.CommandResult;
import sonia.scm.io.SimpleCommand;
import sonia.scm.io.ZipUnArchiver;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 */
public final class IOUtil
{

  /** Field description */
  public static final int DEFAULT_BUFFERSIZE = 8192;

  /** Field description */
  private static final String DEFAULT_CHECKPARAMETER = "--version";

  /** Field description */
  private static final String[] DEFAULT_PATH = new String[]
  {

    // default path
    "/usr/bin",

    // manually installed
    "/usr/local/bin",

    // mac ports
    "/opt/local/bin",

    // opencsw
    "/opt/csw/bin"
  };

  /** Field description */
  private static final String[] EXTENSION_SCRIPT_UNIX = { ".sh", ".csh",
    ".bsh" };

  /** Field description */
  private static final String[] EXTENSION_SCRIPT_WINDOWS = { ".bat", ".cmd",
    ".exe" };

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(IOUtil.class.getName());

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private IOUtil() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param closeable
   */
  public static void close(Closeable closeable)
  {
    if (closeable != null)
    {
      try
      {
        closeable.close();
      }
      catch (IOException ex)
      {
        logger.warn("IOException thrown while closing Closeable.", ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   * @param bufferSize
   *
   * @throws IOException
   */
  public static void copy(Reader reader, Writer writer, int bufferSize)
    throws IOException
  {
    char[] buffer = new char[bufferSize];

    for (int len; (len = reader.read(buffer)) != -1; )
    {
      writer.write(buffer, 0, len);
    }
  }

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   *
   * @throws IOException
   */
  public static void copy(Reader reader, Writer writer) throws IOException
  {
    copy(reader, writer, DEFAULT_BUFFERSIZE);
  }

  /**
   * Method description
   *
   *
   * @param in
   * @param out
   *
   * @throws IOException
   */
  public static void copy(InputStream in, OutputStream out) throws IOException
  {
    copy(in, out, DEFAULT_BUFFERSIZE);
  }

  /**
   * Method description
   *
   *
   * @param in
   * @param out
   * @param bufferSize
   *
   * @throws IOException
   */
  public static void copy(InputStream in, OutputStream out, int bufferSize)
    throws IOException
  {
    byte[] buffer = new byte[bufferSize];

    for (int len; (len = in.read(buffer)) != -1; )
    {
      out.write(buffer, 0, len);
    }

    out.flush();
  }

  /**
   * Method description
   *
   *
   * @param in
   * @param out
   * @param bufferSize
   * @param byteCount
   *
   * @throws IOException
   */
  public static void copy(InputStream in, OutputStream out, int bufferSize,
    int byteCount)
    throws IOException
  {
    byte buffer[] = new byte[bufferSize];
    int len = bufferSize;

    if (byteCount >= 0)
    {
      while (byteCount > 0)
      {
        int max = (byteCount < bufferSize)
          ? (int) byteCount
          : bufferSize;

        len = in.read(buffer, 0, max);

        if (len == -1)
        {
          break;
        }

        byteCount -= len;
        out.write(buffer, 0, len);
      }
    }
    else
    {
      while (true)
      {
        len = in.read(buffer, 0, bufferSize);

        if (len < 0)
        {
          break;
        }

        out.write(buffer, 0, len);
      }
    }

    out.flush();
  }

  /**
   * Method description
   *
   *
   * @param source
   * @param target
   *
   * @throws IOException
   */
  public static void copy(File source, File target) throws IOException
  {
    if (source.isDirectory())
    {
      if (!target.exists())
      {
        mkdirs(target);
      }

      String[] children = source.list();

      for (String child : children)
      {
        copy(new File(source, child), new File(target, child));
      }
    }
    else
    {
      FileInputStream input = null;
      FileOutputStream output = null;

      try
      {
        input = new FileInputStream(source);
        output = new FileOutputStream(target);
        copy(input, output);
      }
      finally
      {
        close(input);
        close(output);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   */
  public static void copyThread(Reader reader, Writer writer)
  {
    copyThread(reader, writer, DEFAULT_BUFFERSIZE);
  }

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   */
  public static void copyThread(InputStream input, OutputStream output)
  {
    copyThread(input, output, DEFAULT_BUFFERSIZE);
  }

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   * @param bufferSize
   */
  public static void copyThread(Reader reader, Writer writer, int bufferSize)
  {
    new Thread(new IOCopyThread(reader, writer, bufferSize)).start();
  }

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   * @param bufferSize
   */
  public static void copyThread(InputStream input, OutputStream output,
    int bufferSize)
  {
    new Thread(new IOStreamCopyThread(input, output, bufferSize)).start();
  }

  /**
   *   Method description
   *
   *
   *   @param file
   *
   *   @throws IOException
   */
  public static void delete(File file) throws IOException
  {
    delete(file, false);
  }

  public static void deleteSilently(File file)
  {
    try {
      delete(file, true);
    } catch (IOException e) {
      // silent delete throws no exception
    }
  }

  /**
   * Method description
   *
   *
   * @param file
   * @param silent
   *
   * @throws IOException
   */
  public static void delete(File file, boolean silent) throws IOException
  {
    if (file.isDirectory())
    {
      File[] children = file.listFiles();

      if (children != null)
      {
        for (File child : children)
        {
          delete(child, silent);
        }
      }
    }

    for (int i = 20; !file.delete() && i > 0; i--)
    {
      String message = "could not delete file ".concat(file.getPath());

      if (silent)
      {
        logger.error(message);
      }
      else
      {
        throw new IOException(message);
      }

      try
      {
        logger.warn("sleep 250ms, because of delete for file {} failed", file);
        Thread.sleep(250);
      }
      catch (InterruptedException ex)
      {
        logger.warn("sleep of delete method interrupted", ex);
      }
    }
  }

  /**
   * Method description
   *
   *
   * @param archive
   * @param outputDirectory
   *
   * @throws IOException
   */
  public static void extract(File archive, File outputDirectory)
    throws IOException
  {
    String name = archive.getName().toLowerCase(Locale.ENGLISH);

    extract(archive, outputDirectory, name);
  }

  /**
   * Method description
   *
   *
   * @param archive
   * @param outputDirectory
   * @param type
   *
   * @throws IOException
   */
  public static void extract(File archive, File outputDirectory, String type)
    throws IOException
  {
    if (type.endsWith(ZipUnArchiver.EXTENSION))
    {
      new ZipUnArchiver().extract(archive, outputDirectory);
    }
    else
    {
      throw new IOException("no unarchiver found for ".concat(type));
    }
  }

  /**
   * Method description
   *
   *
   * @param directory
   */
  public static void mkdirs(File directory)
  {
    if (!directory.exists() &&!directory.mkdirs())
    {
      // Sometimes, the previous check simply has the wrong result (either the 'exists()' returnes false though the
      // directory exists or 'mkdirs()' returns false though the directory was created successfully.
      // We therefore have to double check here. Funny though, in these cases a second check with 'directory.exists()'
      // still returns false. As it seems, 'directory.getAbsoluteFile().exists()' creates a new object that fixes this
      // problem.
      if (!directory.getAbsoluteFile().exists()) {
        throw new IllegalStateException("could not create directory ".concat(directory.getPath()));
      }
    }
  }

  /**
   *
   *
   * @param cmd
   *
   * @return
   */
  public static String search(String cmd)
  {
    return search(DEFAULT_PATH, cmd, DEFAULT_CHECKPARAMETER);
  }

  /**
   *
   *
   * @param path
   * @param cmd
   *
   * @return
   */
  public static String search(String[] path, String cmd)
  {
    return search(path, cmd, DEFAULT_CHECKPARAMETER);
  }

  /**
   * TODO check for windows
   *
   *
   *
   * @param path
   * @param cmd
   * @param checkParameter
   *
   * @return
   */
  public static String search(String[] path, String cmd, String checkParameter)
  {
    String cmdPath = null;

    if (isCommandAvailable(cmd, checkParameter))
    {
      cmdPath = cmd;
    }

    if (cmdPath == null)
    {
      for (String pathPart : path)
      {
        List<String> extensions = getExecutableSearchExtensions();
        File file = findFileByExtension(pathPart, cmd, extensions);

        if (file != null)
        {
          cmdPath = file.getAbsolutePath();

          break;
        }
      }
    }

    if (cmdPath != null)
    {
      if (logger.isInfoEnabled())
      {
        logger.info("found {} at {}", cmd, cmdPath);
      }
    }
    else if (logger.isWarnEnabled())
    {
      logger.warn("could not find {}", cmd);
    }

    return cmdPath;
  }

  /**
   * Method description
   *
   *
   * @param cmd
   *
   * @return
   */
  public static List<String> searchAll(String cmd)
  {
    return searchAll(DEFAULT_PATH, cmd, DEFAULT_CHECKPARAMETER);
  }

  /**
   * Method description
   *
   *
   * @param path
   * @param cmd
   *
   * @return
   */
  public static List<String> searchAll(String[] path, String cmd)
  {
    return searchAll(path, cmd, DEFAULT_CHECKPARAMETER);
  }

  /**
   * Method description
   *
   *
   * @param path
   * @param cmd
   * @param checkParameter
   *
   * @return
   */
  public static List<String> searchAll(String[] path, String cmd,
    String checkParameter)
  {
    List<String> cmds = new ArrayList<String>();

    if (isCommandAvailable(cmd, checkParameter))
    {
      cmds.add(cmd);
    }

    for (String pathPart : path)
    {
      List<String> extensions = getExecutableSearchExtensions();
      File file = findFileByExtension(pathPart, cmd, extensions);

      if (file != null)
      {
        cmds.add(file.getAbsolutePath());
      }
    }

    return cmds;
  }

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   * @since 1.9
   */
  public static String trimSeperatorChars(String name)
  {
    if (name.startsWith(File.separator))
    {
      name = name.substring(1);
    }

    if (name.endsWith(File.separator))
    {
      name = name.substring(0, name.length() - 1);
    }

    return name;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param in
   *
   * @return
   * @since 1.8
   *
   * @throws IOException
   */
  public static String getContent(InputStream in) throws IOException
  {
    String content = null;
    ByteArrayOutputStream baos = null;

    try
    {
      baos = new ByteArrayOutputStream();
      copy(in, baos);
      content = baos.toString();
    }
    finally
    {
      close(baos);
      close(in);
    }

    return content;
  }

  /**
   * Method description
   *
   *
   * @param basePath
   *
   * @since 1.6
   * @return
   */
  public static File getScript(String basePath)
  {
    return getScript(new File(basePath), basePath);
  }

  /**
   * Method description
   *
   *
   * @param baseFile
   *
   * @since 1.6
   * @return
   */
  public static File getScript(File baseFile)
  {
    return getScript(baseFile, baseFile.getAbsolutePath());
  }

  /**
   * Returns true if the second file parameter is a child of the first one.
   *
   *
   * @param parent parent file
   * @param child chile file
   * @since 1.9
   *
   * @return true if the second file parameter is a child of the first one
   *
   */
  public static boolean isChild(File parent, File child)
  {
    boolean ischild = false;

    try
    {
      String path = child.getCanonicalPath();
      String parentPath = parent.getCanonicalPath();

      if (!parentPath.equals(path))
      {
        ischild = path.startsWith(parentPath);
      }
    }
    catch (IOException ex)
    {
      throw new RuntimeException(ex);
    }

    return ischild;
  }

  /**
   * Returns true if the given directory is empty.
   *
   *
   * @param directory directory to check
   *
   * @return true if the directory is empty
   * @since 1.16
   */
  public static boolean isEmpty(File directory)
  {
    return Util.isEmpty(directory.listFiles());
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param parentPath
   * @param cmd
   * @param potentialExtensions
   *
   * @return
   */
  private static File findFileByExtension(String parentPath, String cmd,
    List<String> potentialExtensions)
  {
    File file = null;

    for (String potentialExtension : potentialExtensions)
    {
      String fileName = cmd.concat(potentialExtension);
      File potentialFile = new File(parentPath, fileName);

      if (potentialFile.exists())
      {
        file = potentialFile;

        break;
      }
    }

    return file;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns a list of file extensions to use when searching for executables.
   * The list is in priority order, with the highest priority first.
   *
   * @return
   */
  private static List<String> getExecutableSearchExtensions()
  {
    List<String> extensions;

    if (SystemUtil.isWindows())
    {
      extensions = Arrays.asList(".exe");
    }
    else
    {
      extensions = Arrays.asList("");
    }

    return extensions;
  }

  /**
   * Method description
   *
   *
   * @param baseFile
   * @param basePath
   *
   * @since 1.6
   * @return
   */
  private static File getScript(File baseFile, String basePath)
  {
    File script = null;

    if (baseFile.exists())
    {
      script = baseFile;
    }
    else
    {
      String[] extensions = null;

      if (SystemUtil.isWindows())
      {
        extensions = EXTENSION_SCRIPT_WINDOWS;
      }
      else
      {
        extensions = EXTENSION_SCRIPT_UNIX;
      }

      for (String ext : extensions)
      {
        File file = new File(basePath.concat(ext));

        if (file.exists())
        {
          script = file;

          break;
        }
      }
    }

    return script;
  }

  /**
   * Method description
   *
   *
   * @param cmd
   * @param checkParameter
   *
   * @return
   */
  private static boolean isCommandAvailable(String cmd, String checkParameter)
  {
    boolean success = false;

    try
    {
      Command command = new SimpleCommand(cmd, checkParameter);
      CommandResult result = command.execute();

      success = result.isSuccessfull();
    }
    catch (IOException ex)
    {
      if (logger.isTraceEnabled())
      {
        logger.trace("could not execute command", ex);
      }
    }

    return success;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 10/09/28
   * @author         Enter your name here...
   */
  private static class IOCopyThread implements Runnable
  {

    /**
     * Constructs ...
     *
     *
     * @param reader
     * @param writer
     * @param bufferSize
     */
    public IOCopyThread(Reader reader, Writer writer, int bufferSize)
    {
      this.reader = reader;
      this.writer = writer;
      this.bufferSize = bufferSize;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     */
    @Override
    public void run()
    {
      try
      {
        copy(reader, writer, bufferSize);
      }
      catch (IOException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
      finally
      {
        close(reader);
        close(writer);
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private int bufferSize;

    /** Field description */
    private Reader reader;

    /** Field description */
    private Writer writer;
  }


  /**
   * Class description
   *
   *
   * @version        Enter version here..., 2010-12-27
   * @author         Sebastian Sdorra
   */
  private static class IOStreamCopyThread implements Runnable
  {

    /**
     * Constructs ...
     *
     *
     * @param input
     * @param output
     * @param bufferSize
     */
    public IOStreamCopyThread(InputStream input, OutputStream output,
      int bufferSize)
    {
      this.input = input;
      this.output = output;
      this.bufferSize = bufferSize;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     */
    @Override
    public void run()
    {
      try
      {
        copy(input, output, bufferSize);
      }
      catch (IOException ex)
      {
        logger.error(ex.getMessage(), ex);
      }
      finally
      {
        close(input);
        close(output);
      }
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private int bufferSize;

    /** Field description */
    private InputStream input;

    /** Field description */
    private OutputStream output;
  }
}
