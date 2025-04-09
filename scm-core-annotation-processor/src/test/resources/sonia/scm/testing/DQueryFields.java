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

import sonia.scm.store.QueryableStore;

public final class DQueryFields {
  public static final QueryableStore.IdQueryField<D> INTERNAL_ID =
    new QueryableStore.IdQueryField<>();

  public static final QueryableStore.IntegerQueryField<D> AGE =
    new QueryableStore.IntegerQueryField<>("age");
  public static final QueryableStore.IntegerQueryField<D> WEIGHT =
    new QueryableStore.IntegerQueryField<>("weight");

  public static final QueryableStore.LongQueryField<D> CREATIONTIME =
    new QueryableStore.LongQueryField<>("creationTime");
  public static final QueryableStore.LongQueryField<D> LASTMODIFIED =
    new QueryableStore.LongQueryField<>("lastModified");

  public static final QueryableStore.FloatQueryField<D> HEIGHT =
    new QueryableStore.FloatQueryField<>("height");
  public static final QueryableStore.FloatQueryField<D> WIDTH =
    new QueryableStore.FloatQueryField<>("width");

  public static final QueryableStore.DoubleQueryField<D> PRICE =
    new QueryableStore.DoubleQueryField<>("price");
  public static final QueryableStore.DoubleQueryField<D> MARGIN =
    new QueryableStore.DoubleQueryField<>("margin");

  private DQueryFields() {
  }
}
