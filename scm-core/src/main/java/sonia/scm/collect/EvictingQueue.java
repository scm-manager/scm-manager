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

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Preconditions;
import com.google.common.collect.ForwardingQueue;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import jakarta.validation.constraints.NotNull;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;

import java.util.ArrayDeque;
import java.util.Queue;

@XmlAccessorType(XmlAccessType.FIELD)
public final class EvictingQueue<E> extends ForwardingQueue<E> {

  private final ArrayDeque<E> delegate;
  @VisibleForTesting
  int maxSize;

  public EvictingQueue() {
    this.delegate = new ArrayDeque<>();
    this.maxSize = 100;
  }

  private EvictingQueue(int maxSize) {
    Preconditions.checkArgument(maxSize >= 0, "maxSize (%s) must >= 0", maxSize);
    this.delegate = new ArrayDeque<>(maxSize);
    this.maxSize = maxSize;
  }

  public static <E> EvictingQueue<E> create(int maxSize) {
    return new EvictingQueue<>(maxSize);
  }

  protected Queue<E> delegate() {
    return this.delegate;
  }

  @Override
  @CanIgnoreReturnValue
  public boolean add(@NotNull E e) {
    Preconditions.checkNotNull(e);
    if (this.maxSize == 0) {
      return false;
    } else {
      while (this.size() >= this.maxSize) {
        this.delegate.remove();
      }
      this.delegate.add(e);
      return true;
    }
  }

}
