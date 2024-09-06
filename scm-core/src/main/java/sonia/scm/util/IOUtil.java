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


public final class IOUtil
{

  public static final int DEFAULT_BUFFERSIZE = 8192;

  private static final String DEFAULT_CHECKPARAMETER = "--version";

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

  private static final String[] EXTENSION_SCRIPT_UNIX = { ".sh", ".csh",
    ".bsh" };

  private static final String[] EXTENSION_SCRIPT_WINDOWS = { ".bat", ".cmd",
    ".exe" };

  private static final Logger logger =
    LoggerFactory.getLogger(IOUtil.class.getName());


  private IOUtil() {}

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

  public static void copy(Reader reader, Writer writer, int bufferSize)
    throws IOException
  {
    char[] buffer = new char[bufferSize];

    for (int len; (len = reader.read(buffer)) != -1; )
    {
      writer.write(buffer, 0, len);
    }
  }

  public static void copy(Reader reader, Writer writer) throws IOException
  {
    copy(reader, writer, DEFAULT_BUFFERSIZE);
  }

  public static void copy(InputStream in, OutputStream out) throws IOException
  {
    copy(in, out, DEFAULT_BUFFERSIZE);
  }

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

  public static void copyThread(Reader reader, Writer writer)
  {
    copyThread(reader, writer, DEFAULT_BUFFERSIZE);
  }

  public static void copyThread(InputStream input, OutputStream output)
  {
    copyThread(input, output, DEFAULT_BUFFERSIZE);
  }

  public static void copyThread(Reader reader, Writer writer, int bufferSize)
  {
    new Thread(new IOCopyThread(reader, writer, bufferSize)).start();
  }

  public static void copyThread(InputStream input, OutputStream output,
    int bufferSize)
  {
    new Thread(new IOStreamCopyThread(input, output, bufferSize)).start();
  }

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

  public static void extract(File archive, File outputDirectory)
    throws IOException
  {
    String name = archive.getName().toLowerCase(Locale.ENGLISH);

    extract(archive, outputDirectory, name);
  }

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

  public static String search(String cmd)
  {
    return search(DEFAULT_PATH, cmd, DEFAULT_CHECKPARAMETER);
  }

  public static String search(String[] path, String cmd)
  {
    return search(path, cmd, DEFAULT_CHECKPARAMETER);
  }

  /**
   * TODO check for windows
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

  public static List<String> searchAll(String cmd)
  {
    return searchAll(DEFAULT_PATH, cmd, DEFAULT_CHECKPARAMETER);
  }

  public static List<String> searchAll(String[] path, String cmd)
  {
    return searchAll(path, cmd, DEFAULT_CHECKPARAMETER);
  }

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


  /**
   * @since 1.8
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
   * @since 1.6
   */
  public static File getScript(String basePath)
  {
    return getScript(new File(basePath), basePath);
  }

  /**
   * @since 1.6
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
   * @since 1.16
   */
  public static boolean isEmpty(File directory)
  {
    return Util.isEmpty(directory.listFiles());
  }

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


  /**
   * Returns a list of file extensions to use when searching for executables.
   * The list is in priority order, with the highest priority first.
   *
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
   * @since 1.6
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

  private static class IOCopyThread implements Runnable
  {
    private int bufferSize;

    private Reader reader;

    private Writer writer;

    public IOCopyThread(Reader reader, Writer writer, int bufferSize)
    {
      this.reader = reader;
      this.writer = writer;
      this.bufferSize = bufferSize;
    }

    

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
  }

  private static class IOStreamCopyThread implements Runnable
  {
    private int bufferSize;

    private InputStream input;

    private OutputStream output;

    public IOStreamCopyThread(InputStream input, OutputStream output,
      int bufferSize)
    {
      this.input = input;
      this.output = output;
      this.bufferSize = bufferSize;
    }

    

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
  }
}
