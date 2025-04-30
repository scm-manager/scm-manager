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

package sonia.scm.testing;

import javax.annotation.processing.Generated;
import sonia.scm.store.QueryableStore;

@Generated("sonia.scm.annotation.QueryableTypeAnnotationProcessor")
public final class IQueryFields {
  public static final QueryableStore.IdQueryField<I> INTERNAL_ID =
    new QueryableStore.IdQueryField<>();

  public static final QueryableStore.StringQueryField<I> BIRTHDAY =
    new QueryableStore.StringQueryField<>("birthday");

  private IQueryFields() {
  }
}
