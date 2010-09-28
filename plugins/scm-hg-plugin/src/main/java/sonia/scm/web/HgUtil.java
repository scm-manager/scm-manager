/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContext;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgUtil
{

  /** Field description */
  public static final String CGI_DIRECTORY = "cgi-bin";

  /** Field description */
  public static final String CGI_NAME = "hgweb.cgi";

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static File getCGI()
  {
    File cgiDirectory = new File(SCMContext.getContext().getBaseDirectory(),
                                 CGI_DIRECTORY);

    if (!cgiDirectory.exists() &&!cgiDirectory.mkdirs())
    {
      throw new RuntimeException(
          "could not create directory".concat(cgiDirectory.getPath()));
    }

    return new File(cgiDirectory, CGI_NAME);
  }
}
