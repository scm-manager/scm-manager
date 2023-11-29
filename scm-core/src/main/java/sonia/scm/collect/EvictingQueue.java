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
