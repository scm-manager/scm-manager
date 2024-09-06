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
