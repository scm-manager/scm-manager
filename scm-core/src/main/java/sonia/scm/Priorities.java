/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
