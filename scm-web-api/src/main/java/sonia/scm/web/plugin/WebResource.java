/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.plugin;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 */
public interface WebResource
{

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws IOException
   */
  public InputStream getContent() throws IOException;
}
