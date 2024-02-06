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

package sonia.scm.collect;


import com.google.common.collect.ForwardingSortedSet;
import com.google.common.collect.Sets;

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A sorted set which is limited to a specified maximum size. If the maximum
 * size is reached the last element is removed from the set.
 *
 *
 * @param <E>
 * @since 1.32
 */
public class LimitedSortedSet<E> extends ForwardingSortedSet<E>
{
  private int maxSize;

  /** delegate set */
  private SortedSet<E> sortedSet;

  /**
   * Constructs a new set with the specified maximum.
   *
   *
   * @param maxSize maximum size of the set
   */
  public LimitedSortedSet(int maxSize)
  {
    this.sortedSet = new TreeSet<>();
    this.maxSize = maxSize;
  }

  /**
   * Constructs a new set with the specified comparator and maximum.
   *
   *
   *
   * @param comparator comparator to order the set
   * @param maxSize maximum size of the set
   */
  public LimitedSortedSet(Comparator<E> comparator, int maxSize)
  {
    this.sortedSet = Sets.newTreeSet(comparator);
    this.maxSize = maxSize;
  }



  @Override
  public boolean add(E o)
  {
    boolean added = super.add(o);

    cleanUp();

    return added;
  }


  @Override
  public boolean addAll(Collection<? extends E> c)
  {
    boolean added = super.addAll(c);

    cleanUp();

    return added;
  }

  /**
   * Returns the underlying delegate set.
   *
   *
   * @return delegate set
   */
  @Override
  protected SortedSet<E> delegate()
  {
    return sortedSet;
  }

  /**
   * Remove the last entries, if the maximum size is reached.
   */
  private void cleanUp()
  {
    while (size() > maxSize)
    {
      remove(last());
    }
  }

}
