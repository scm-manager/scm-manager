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

/**
 * This type of exception is only thrown by scm-manager backend implementation, 
 * such as the xml or jdbc backend.
 *
 * @since 1.14
 */
public class BackendException extends RuntimeException
{
  private static final long serialVersionUID = -6885276113482167556L;

  public BackendException()
  {
    super();
  }

  public BackendException(String message)
  {
    super(message);
  }

  public BackendException(Throwable cause)
  {
    super(cause);
  }

  public BackendException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
