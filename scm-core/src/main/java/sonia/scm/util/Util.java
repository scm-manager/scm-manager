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
public class Util
{

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static boolean isEmpty(String value)
  {
    return (value == null) || (value.trim().length() == 0);
  }

  /**
   * Method description
   *
   *
   * @param collection
   *
   * @return
   */
  public static boolean isEmpty(Collection<?> collection)
  {
    return (collection == null) || collection.isEmpty();
  }

  /**
   * Method description
   *
   *
   * @param array
   *
   * @return
   */
  public static boolean isEmpty(Object[] array)
  {
    return (array == null) || (array.length == 0);
  }

  /**
   * Method description
   *
   *
   * @param value
   *
   * @return
   */
  public static boolean isNotEmpty(String value)
  {
    return (value != null) && (value.trim().length() > 0);
  }

  /**
   * Method description
   *
   *
   * @param collection
   *
   * @return
   */
  public static boolean isNotEmpty(Collection<?> collection)
  {
    return (collection != null) &&!collection.isEmpty();
  }

  /**
   * Method description
   *
   *
   * @param array
   *
   * @return
   */
  public static boolean isNotEmpty(Object[] array)
  {
    return (array != null) && (array.length > 0);
  }
}
