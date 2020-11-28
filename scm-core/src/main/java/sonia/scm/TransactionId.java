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

import com.google.common.annotations.VisibleForTesting;
import org.slf4j.MDC;

import java.util.Optional;

/**
 * Id of the current transaction.
 * The transaction id is mainly used for logging and debugging.
 *
 * @since 2.10.0
 */
public final class TransactionId {

  @VisibleForTesting
  public static final String KEY = "transaction_id";

  private TransactionId() {
  }

  /**
   * Binds the given transaction id to the current thread.
   *
   * @param transactionId transaction id
   */
  public static void set(String transactionId) {
    MDC.put(KEY, transactionId);
  }

  /**
   * Returns an optional transaction id.
   * If there is no transaction id bound to the thread, the method will return an empty optional.
   *
   * @return optional transaction id
   */
  public static Optional<String> get() {
    return Optional.ofNullable(MDC.get(KEY));
  }

  /**
   * Removes a bound transaction id from the current thread.
   */
  public static void clear() {
    MDC.remove(KEY);
  }
}
