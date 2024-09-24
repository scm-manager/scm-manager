/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm;


import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

import java.util.List;

/**
 * Priority constants and util methods to sort classes by {@link Priority}
 * annotation.
 *
 * @since 2.0.0
 */
public final class Priorities
{

  public static final int DEFAULT = 9999;


  private Priorities() {}


  /**
   * Returns a list sorted by priority.
   *
   * @param <T> type of class
   * @param unordered unordered classes
   *
   * @return sorted class list
   */
  public static <T> List<Class<? extends T>> sort(Iterable<Class<? extends T>> unordered)
  {
    return new PriorityOrdering<T>().sortedCopy(unordered);
  }

  /**
   * Returns a list of instances sorted by priority.
   *
   * @param <T> type of class
   * @param unordered unordered instances
   *
   * @return sorted instance list
   */
  public static <T> List<T> sortInstances(Iterable<T> unordered)
  {
    return new PriorityInstanceOrdering<T>().sortedCopy(unordered);
  }



  /**
   * Returns the priority of the given class.
   *
   * @param clazz class
   *
   * @return priority of class
   */
  public static int getPriority(Class<?> clazz)
  {
    int priority = DEFAULT;

    Priority annotation = clazz.getAnnotation(Priority.class);

    if (annotation != null)
    {
      priority = annotation.value();
    }

    return priority;
  }

  /**
   * {@link Ordering} which orders classes by priority.
   *
   * @param <T> type of class
   */
  public static class PriorityOrdering<T> extends Ordering<Class<? extends T>>
  {

    /**
     * Compares the left class with the right class.
     *
     *
     * @param left left class
     * @param right right class
     *
     * @return compare value
     */
    @Override
    public int compare(Class<? extends T> left, Class<? extends T> right)
    {
      return Ints.compare(getPriority(left), getPriority(right));
    }
  }

  /**
   * {@link Ordering} which orders instances by priority.
   *
   * @param <T> type of instance
   */
  public static class PriorityInstanceOrdering<T> extends Ordering<T>
  {

    /**
     * Compares the left instance with the right instance.
     *
     *
     * @param left left instance
     * @param right right instance
     *
     * @return compare value
     */
    @Override
    public int compare(T left, T right)
    {
      return Ints.compare(getPriority(left.getClass()), getPriority(right.getClass()));
    }
  }
}
