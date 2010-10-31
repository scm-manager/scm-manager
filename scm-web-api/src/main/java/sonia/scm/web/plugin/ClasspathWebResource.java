/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.plugin;

//~--- JDK imports ------------------------------------------------------------

import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public class ClasspathWebResource implements WebResource
{

  /**
   * Constructs ...
   *
   *
   * @param contentPath
   */
  public ClasspathWebResource(String contentPath)
  {
    this.contentPath = contentPath;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public InputStream getContent()
  {
    return ClasspathWebResource.class.getResourceAsStream(contentPath);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String contentPath;
}
