/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Singleton;

import sonia.scm.web.cgi.AbstractCGIServlet;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class HgCGIServlet extends AbstractCGIServlet
{

  /** Field description */
  private static final long serialVersionUID = -3492811300905099810L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @throws ServletException
   */
  @Override
  public void init() throws ServletException
  {
    command = HgUtil.getCGI();
    super.init();
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param req
   *
   * @return
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected File getCommand(HttpServletRequest req)
          throws ServletException, IOException
  {
    return command;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private File command;
}
