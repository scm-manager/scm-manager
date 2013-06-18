/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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



package sonia.scm.collect;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.collect.ForwardingSortedSet;
import com.google.common.collect.Sets;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * A sorted set which is limited to a specified maximum size. If the maximum
 * size is reached the last element is removed from the set.
 *
 * @author Sebastian Sdorra
 *
 * @param <E>
 * @since 1.32
 */
public class LimitedSortedSet<E> extends ForwardingSortedSet<E>
{

  /**
   * Constructs a new set with the specified maximum.
   *
   *
   * @param maxSize maximum size of the set
   */
  public LimitedSortedSet(int maxSize)
  {
    this.treeSet = Sets.newTreeSet();
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
    this.treeSet = Sets.newTreeSet(comparator);
    this.maxSize = maxSize;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   */
  @Override
  public boolean add(E o)
  {
    boolean added = super.add(o);

    cleanUp();

    return added;
  }

  /**
   * {@inheritDoc}
   */
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
    return treeSet;
  }

  /**
   * Remove the last entries, if the maximum size is reached.
   *
   */
  private void cleanUp()
  {
    while (size() > maxSize)
    {
      remove(last());
    }
  }

  //~--- fields ---------------------------------------------------------------

  /** maximum size of the set */
  private int maxSize;

  /** delegate set */
  private TreeSet treeSet;
}
