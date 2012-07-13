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



package sonia.scm.repository.spi.javahg;

//~--- non-JDK imports --------------------------------------------------------

import com.aragost.javahg.DateTime;
import com.aragost.javahg.Repository;
import com.aragost.javahg.internals.AbstractCommand;
import com.aragost.javahg.internals.HgInputStream;

import com.google.common.collect.Lists;

import sonia.scm.repository.FileObject;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.List;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgFileviewCommand extends AbstractCommand
{

  /**
   * Constructs ...
   *
   *
   * @param repository
   */
  public HgFileviewCommand(Repository repository)
  {
    super(repository);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   *
   * @return
   */
  public static HgFileviewCommand on(Repository repository)
  {
    return new HgFileviewCommand(repository);
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public List<FileObject> execute() throws IOException
  {
    cmdAppend("-t");

    List<FileObject> files = Lists.newArrayList();

    HgInputStream stream = launchStream();

    while (stream.peek() != -1)
    {
      FileObject file = null;
      char type = (char) stream.read();

      if (type == 'd')
      {
        file = readDirectory(stream);
      }
      else if (type == 'f')
      {
        file = readFile(stream);
      }
      else if (type == 's') {}

      if (file != null)
      {
        files.add(file);
      }
    }

    return files;
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  public HgFileviewCommand path(String path)
  {
    cmdAppend("-p", path);

    return this;
  }

  /**
   * Method description
   *
   *
   * @param revision
   *
   * @return
   */
  public HgFileviewCommand rev(String revision)
  {
    cmdAppend("-r", rev);

    return this;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getCommandName()
  {
    return HgFileviewExtension.NAME;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   *
   * @return
   *
   * @throws IOException
   */
  private FileObject readDirectory(HgInputStream stream) throws IOException
  {
    FileObject directory = new FileObject();
    String path = removeTrailingSlash(stream.textUpTo('\0'));

    directory.setName(getNameFromPath(path));
    directory.setDirectory(true);
    directory.setPath(path);

    return directory;
  }

  /**
   * Method description
   *
   *
   * @param stream
   *
   * @return
   *
   * @throws IOException
   */
  private FileObject readFile(HgInputStream stream) throws IOException
  {
    FileObject file = new FileObject();
    String path = removeTrailingSlash(stream.textUpTo('\n'));

    file.setName(getNameFromPath(path));
    file.setPath(path);
    file.setDirectory(false);
    file.setLength(stream.decimalIntUpTo(' '));

    DateTime timestamp = stream.dateTimeUpTo(' ');

    file.setLastModified(timestamp.getDate().getTime());
    file.setDescription(stream.textUpTo('\0'));

    return file;
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String removeTrailingSlash(String path)
  {
    if (path.endsWith("/"))
    {
      path = path.substring(0, path.length() - 1);
    }

    return path;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   */
  private String getNameFromPath(String path)
  {
    int index = path.lastIndexOf("/");

    if (index > 0)
    {
      path = path.substring(index + 1);
    }

    return path;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String rev;
}
