/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class BasicContextProvider implements SCMContextProvider
{

  /** Field description */
  public static final String DIRECTORY_DEFAULT = ".scm";

  /** Field description */
  public static final String DIRECTORY_ENVIRONMENT = "SCM_HOME";

  /** Field description */
  public static final String DIRECTORY_PROPERTY = "scm.home";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public BasicContextProvider()
  {
    baseDirectory = findBaseDirectory();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException {}

  /**
   * Method description
   *
   */
  @Override
  public void init() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public File getBaseDirectory()
  {
    return baseDirectory;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private File findBaseDirectory()
  {
    String path = System.getProperty(DIRECTORY_PROPERTY);

    if (Util.isEmpty(path))
    {
      path = System.getenv(DIRECTORY_ENVIRONMENT);

      if (Util.isEmpty(path))
      {
        path = System.getProperty("user.home").concat(File.separator).concat(
          DIRECTORY_DEFAULT);
      }
    }

    File directory = new File(path);

    if (!directory.exists() &&!directory.mkdirs())
    {
      throw new IllegalStateException("could not create directory");
    }

    return directory;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File baseDirectory;
}
