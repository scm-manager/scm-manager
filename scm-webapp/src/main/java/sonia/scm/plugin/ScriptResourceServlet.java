/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.ScmWebPlugin;
import sonia.scm.ScmWebPluginContext;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import java.util.List;

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

    List<ScmWebPlugin> plugins = webPluginContext.getPlugins();

    if (Util.isNotEmpty(plugins))
    {
      for (ScmWebPlugin plugin : plugins)
      {
        appendResource(stream, plugin);
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
   * @param plugin
   *
   * @throws IOException
   * @throws ServletException
   */
  private void appendResource(OutputStream stream, ScmWebPlugin plugin)
          throws ServletException, IOException
  {
    InputStream input = plugin.getScript();

    if (input != null)
    {
      try
      {
        Util.copy(input, stream);
      }
      finally
      {
        Util.close(input);
      }
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Inject
  private ScmWebPluginContext webPluginContext;
}
