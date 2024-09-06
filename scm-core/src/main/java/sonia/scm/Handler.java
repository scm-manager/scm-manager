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
 * Base interface for all handler objects.
 *
 *
 * @param <T> a typed object
 */
public interface Handler<T extends TypedObject> extends HandlerBase<T> {

  /**
   * Returns the type object of the handler.
   */
  Type getType();

  /**
   * Returns true if the hanlder is configured.
   */
  boolean isConfigured();
}
