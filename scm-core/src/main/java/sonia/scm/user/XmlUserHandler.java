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



package sonia.scm.user;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.util.IOUtil;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlUserHandler implements UserHandler
{

  /** Field description */
  public static final String ADMIN_FILE = "scmadmin.xml";

  /** Field description */
  public static final String ADMIN_PATH = "/sonia/scm/config/admin-account.xml";

  /** Field description */
  public static final String FILE_EXTENSION = ".xml";

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "XML";

  /** Field description */
  public static final String TYPE_NAME = "xml";

  /** Field description */
  public static final Type type = new Type(TYPE_NAME, TYPE_DISPLAYNAME);

  /** Field description */
  public static final String DIRECTORY =
    "user".concat(File.separator).concat("xml");

  /** the logger for XmlUserHandler */
  private static final Logger logger =
    LoggerFactory.getLogger(XmlUserHandler.class);

  //~--- methods --------------------------------------------------------------

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
   * @param user
   *
   * @throws IOException
   * @throws UserException
   */
  @Override
  public void create(User user) throws UserException, IOException
  {
    File file = getFile(user.getName());

    if (file.exists())
    {
      throw new UserAllreadyExistException();
    }

    JAXB.marshal(user, file);
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   * @throws UserException
   */
  @Override
  public void delete(User user) throws UserException, IOException
  {
    File file = getFile(user.getName());

    if (file.exists())
    {
      IOUtil.delete(file);
    }
    else
    {
      throw new UserException("user does not exists");
    }
  }

  /**
   * Method description
   *
   *
   * @param context
   */
  @Override
  public void init(SCMContextProvider context)
  {
    File directory = context.getBaseDirectory();

    userDirectory = new File(directory, DIRECTORY);

    if (!userDirectory.exists())
    {
      IOUtil.mkdirs(userDirectory);
      createAdminAccount();
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   * @throws UserException
   */
  @Override
  public void modify(User user) throws UserException, IOException
  {
    File file = getFile(user.getName());

    if (file.exists())
    {
      JAXB.marshal(user, file);
    }
    else
    {
      throw new UserException("user does not exists");
    }
  }

  /**
   * Method description
   *
   *
   * @param user
   *
   * @throws IOException
   * @throws UserException
   */
  @Override
  public void refresh(User user) throws UserException, IOException
  {
    User fresh = get(user.getName());

    if (fresh == null)
    {
      throw new UserException("user does not exists");
    }

    user.setDisplayName(fresh.getDisplayName());
    user.setMail(fresh.getMail());
    user.setPassword(fresh.getPassword());
    user.setType(TYPE_NAME);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  @Override
  public User get(String id)
  {
    User user = null;
    File file = getFile(id);

    if (file.exists())
    {
      user = JAXB.unmarshal(file, User.class);
    }

    return user;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Collection<User> getAll()
  {
    List<User> users = new ArrayList<User>();
    File[] userFiles = userDirectory.listFiles(new FilenameFilter()
    {
      @Override
      public boolean accept(File dir, String name)
      {
        return name.endsWith(FILE_EXTENSION);
      }
    });

    for (File userFile : userFiles)
    {
      try
      {
        User user = JAXB.unmarshal(userFile, User.class);

        if (user != null)
        {
          users.add(user);
        }
      }
      catch (Exception ex)
      {
        logger.error(ex.getMessage(), ex);
      }
    }

    return users;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Type getType()
  {
    return type;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isConfigured()
  {
    return (userDirectory != null) && userDirectory.exists();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  private void createAdminAccount()
  {
    InputStream input = XmlUserHandler.class.getResourceAsStream(ADMIN_PATH);
    FileOutputStream output = null;

    try
    {
      output = new FileOutputStream(new File(userDirectory, ADMIN_FILE));
      IOUtil.copy(input, output);
    }
    catch (IOException ex)
    {
      logger.error("could not create AdminAccount", ex);
    }
    finally
    {
      IOUtil.close(input);
      IOUtil.close(output);
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param id
   *
   * @return
   */
  private File getFile(String id)
  {
    return new File(userDirectory, id.concat(FILE_EXTENSION));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File userDirectory;
}
