/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;
import sonia.scm.web.ScmWebPluginContext;
import sonia.scm.web.WebResource;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.Collection;

import javax.servlet.ServletException;

/**
 *
 * @author Sebastian Sdorra
 */
@Singleton
public class ScriptResourceServlet extends AbstractResourceServlet
{

  /** Field description */
  public static final String CONTENT_TYPE = "text/javascript";

  /** Field description */
  private static final long serialVersionUID = -5769146163848821050L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   *
   * @throws IOException
   * @throws ServletException
   */
  @Override
  protected void appendResources(OutputStream stream)
          throws ServletException, IOException
  {
    stream.write(
        "function sayPluginHello(){ alert('Plugin Hello !'); }".concat(
          System.getProperty("line.separator")).getBytes());

    Collection<WebResource> scriptResources =
      webPluginContext.getScriptResources();

    if (Util.isNotEmpty(scriptResources))
    {
      for (WebResource scriptResource : scriptResources)
      {
        appendResource(stream, scriptResource);
      }
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected String getContentType()
  {
    return CONTENT_TYPE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param stream
   * @param script
   *
   * @throws IOException
   * @throws ServletException
   */
  private void appendResource(OutputStream stream, WebResource script)
          throws ServletException, IOException
  {
    InputStream input = script.getContent();

    if (input != null)
    {
      try
      {
        IOUtil.copy(input, stream);
      }
      finally
      {
        IOUtil.close(input);
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private ScmWebPluginContext webPluginContext;
}
