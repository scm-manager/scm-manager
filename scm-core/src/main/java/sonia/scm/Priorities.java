/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.Ordering;
import com.google.common.primitives.Ints;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

/**
 * Priority constants and util methods to sort classes by {@link Priority}
 * annotation.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class Priorities
{

  /** authentication priority */
  public static final int AUTHENTICATION = 5000;

  /** authorization priority */
  public static final int AUTHORIZATION = 6000;

  /** base url priority */
  public static final int BASEURL = 1000;

  /** default priority */
  public static final int DEFAULT = 9999;

  /** post authentication priority */
  public static final int POST_AUTHENTICATION = 5500;

  /** pre authorization priority */
  public static final int POST_AUTHORIZATION = 6500;

  /** post base url priority */
  public static final int POST_BASEURL = 1500;

  /** pre authentication priority */
  public static final int PRE_AUTHENTICATION = 4500;

  /** pre authorization priority */
  public static final int PRE_AUTHORIZATION = 5500;

  /** pre base url priority */
  public static final int PRE_BASEURL = 500;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  private Priorities() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Returns a list sorted by priority.
   *
   * @param <T> type of class
   * @param unordered unordered classes
   *
   * @return sorted class list
   */
  public static <T> List<Class<? extends T>> sort(
    Iterable<Class<? extends T>> unordered)
  {
    return new PriorityOrdering<T>().sortedCopy(unordered);
  }

  //~--- get methods ----------------------------------------------------------

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

  //~--- inner classes --------------------------------------------------------

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
}
