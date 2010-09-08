/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;

/**
 *
 * @author Sebastian Sdorra
 */
public class AssertUtil
{

  /**
   * Method description
   *
   *
   * @param value
   */
  public static void assertIsNotEmpty(String value)
  {
    if (Util.isEmpty(value))
    {
      throw new IllegalStateException("value is empty");
    }
  }

  /**
   * Method description
   *
   *
   * @param array
   */
  public static void assertIsNotEmpty(Object[] array)
  {
    if (Util.isEmpty(array))
    {
      throw new IllegalStateException("array is empty");
    }
  }

  /**
   * Method description
   *
   *
   * @param collection
   */
  public static void assertIsNotEmpty(Collection<?> collection)
  {
    if (Util.isEmpty(collection))
    {
      throw new IllegalStateException("collection is empty");
    }
  }

  /**
   * Method description
   *
   *
   * @param object
   */
  public static void assertIsNotNull(Object object)
  {
    if (object == null)
    {
      throw new IllegalStateException("object is required");
    }
  }
}
