/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sonia.scm.util;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sebastian Sdorra
 */
public class ServiceUtil
{

  /** Field description */
  private static final Logger logger =
    Logger.getLogger(ServiceUtil.class.getName());

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param def
   * @param <T>
   *
   * @return
   */
  public static <T> T getService(Class<T> type, T def)
  {
    T result = getService(type);

    if (result == null)
    {
      result = def;
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param <T>
   *
   * @return
   */
  public static <T> T getService(Class<T> type)
  {
    T result = null;

    try
    {
      ServiceLoader<T> loader = ServiceLoader.load(type);

      if (loader != null)
      {
        result = loader.iterator().next();
      }
    }
    catch (NoSuchElementException ex)
    {
      if (logger.isLoggable(Level.FINEST))
      {
        logger.log(Level.FINEST, null, ex);
      }
    }

    return result;
  }

  /**
   * Method description
   *
   *
   * @param type
   * @param <T>
   *
   * @return
   */
  public static <T> List<T> getServices(Class<T> type)
  {
    List<T> result = new ArrayList<T>();

    try
    {
      ServiceLoader<T> loader = ServiceLoader.load(type);

      if (loader != null)
      {
        for (T service : loader)
        {
          result.add(service);
        }
      }
    }
    catch (NoSuchElementException ex)
    {
      if (logger.isLoggable(Level.FINEST))
      {
        logger.log(Level.FINEST, null, ex);
      }
    }

    return result;
  }
}

