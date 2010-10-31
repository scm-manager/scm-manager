/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sonia.scm.SCMContextProvider;
import sonia.scm.User;
import sonia.scm.security.EncryptionHandler;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import javax.xml.bind.JAXB;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class XmlAuthenticator implements Authenticator
{

  /** Field description */
  public static final String NAME_DIRECTORY = "users";

  /** the logger for XmlAuthenticator */
  private static final Logger logger =
    LoggerFactory.getLogger(XmlAuthenticator.class);

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param request
   * @param response
   * @param username
   * @param password
   *
   * @return
   */
  @Override
  public User authenticate(HttpServletRequest request,
                           HttpServletResponse response, String username,
                           String password)
  {
    User user = null;
    File userFile = new File(baseDirectory, username.concat(".xml"));

    if ((userFile != null) && userFile.exists())
    {
      user = JAXB.unmarshal(userFile, User.class);

      String encryptedPassword = encryptionHandler.encrypt(password);

      if (!encryptedPassword.equalsIgnoreCase(user.getPassword()))
      {
        user = null;

        if (logger.isDebugEnabled())
        {
          logger.debug("password for user {} is wrong", username);
        }
      }
      else
      {
        user.setPassword(null);
      }
    }
    else if (logger.isDebugEnabled())
    {
      logger.debug("could not find user {}", username);
    }

    return user;
  }

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
   * @param provider
   */
  @Override
  public void init(SCMContextProvider provider)
  {
    baseDirectory = new File(provider.getBaseDirectory(), NAME_DIRECTORY);

    if (logger.isInfoEnabled())
    {
      logger.info("init XmlAuthenticator with directory {}",
                  baseDirectory.getAbsolutePath());
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;

  /** Field description */
  @Inject
  private EncryptionHandler encryptionHandler;
}
