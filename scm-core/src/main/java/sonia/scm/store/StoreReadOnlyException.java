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

package sonia.scm.store;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.ExceptionWithContext;

import static sonia.scm.ContextEntry.ContextBuilder.noContext;

public class StoreReadOnlyException extends ExceptionWithContext {

  private static final Logger LOG = LoggerFactory.getLogger(StoreReadOnlyException.class);

  public static final String CODE = "3FSIYtBJw1";

  public StoreReadOnlyException(String location) {
    super(noContext(), String.format("Store is read only, could not write location %s", location));
    LOG.error(getMessage());
  }

  public StoreReadOnlyException(Object object) {
    super(noContext(), String.format("Store is read only, could not write object of type %s: %s", object.getClass(), object));
    LOG.error(getMessage());
  }

  public StoreReadOnlyException() {
    super(noContext(), "Store is read only, could not delete store");
    LOG.error(getMessage());
  }

    @Override
    public String getCode () {
      return CODE;
    }
  }
