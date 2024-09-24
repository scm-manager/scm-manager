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

package sonia.scm.i18n;

public class I18nException extends RuntimeException
{
  private static final long serialVersionUID = 1845326427312983227L;

  public I18nException() {}

  public I18nException(String message)
  {
    super(message);
  }

  public I18nException(Throwable cause)
  {
    super(cause);
  }

  public I18nException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
