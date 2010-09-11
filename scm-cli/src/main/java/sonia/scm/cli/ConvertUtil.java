/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.cli;

//~--- JDK imports ------------------------------------------------------------

import java.math.BigInteger;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Sebastian Sdorra
 */
public class ConvertUtil
{

  /** Field description */
  private static final Logger logger =
    Logger.getLogger(ConvertUtil.class.getName());

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param type
   * @param value
   *
   * @return
   */
  public static Object convertString(Class<?> type, String value)
  {
    Object result = null;

    try
    {
      if (type.isAssignableFrom(String.class))
      {
        result = value;
      }
      else if (type.isAssignableFrom(Short.class))
      {
        result = Short.parseShort(value);
      }
      else if (type.isAssignableFrom(Integer.class))
      {
        result = Integer.parseInt(value);
      }
      else if (type.isAssignableFrom(Long.class))
      {
        result = Long.parseLong(value);
      }
      else if (type.isAssignableFrom(BigInteger.class))
      {
        result = new BigInteger(value);
      }
      else if (type.isAssignableFrom(Float.class))
      {
        result = Float.parseFloat(value);
      }
      else if (type.isAssignableFrom(Double.class))
      {
        result = Double.parseDouble(value);
      }
      else if (type.isAssignableFrom(Boolean.class))
      {
        result = Boolean.parseBoolean(value);
      }
    }
    catch (NumberFormatException ex)
    {
      logger.log(Level.FINER, null, ex);
    }

    return result;
  }
}
