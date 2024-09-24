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
