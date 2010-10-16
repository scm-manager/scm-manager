/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.cli;

//~--- non-JDK imports --------------------------------------------------------

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//~--- JDK imports ------------------------------------------------------------

import java.math.BigInteger;

/**
 *
 * @author Sebastian Sdorra
 */
public class ConvertUtil
{

  /** Field description */
  private static final Logger logger =
    LoggerFactory.getLogger(ConvertUtil.class);

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
      logger.debug(ex.getMessage(), ex);
    }

    return result;
  }
}
