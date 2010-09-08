/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.ServiceUtil;

/**
 *
 * @author Sebastian Sdorra
 */
public class SCMContext
{

  /** Field description */
  private static volatile SCMContextProvider provider;

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public static SCMContextProvider getContext()
  {
    if (provider == null)
    {
      synchronized (SCMContext.class)
      {
        if (provider == null)
        {
          provider = ServiceUtil.getService(SCMContextProvider.class);

          if (provider == null)
          {
            provider = new BasicContextProvider();
          }

          provider.init();
        }
      }
    }

    return provider;
  }
}
