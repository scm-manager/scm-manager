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

import java.lang.Double;
import java.lang.Float;
import java.lang.Integer;
import java.lang.Long;
import sonia.scm.store.QueryableStore;

public final class DQueryFields {
  public static final QueryableStore.IdQueryField<D> INTERNAL_ID =
    new QueryableStore.IdQueryField<>();

  public static final QueryableStore.NumberQueryField<D, Integer> AGE =
    new QueryableStore.NumberQueryField<>("age");
  public static final QueryableStore.NumberQueryField<D, Integer> WEIGHT =
    new QueryableStore.NumberQueryField<>("weight");

  public static final QueryableStore.NumberQueryField<D, Long> CREATIONTIME =
    new QueryableStore.NumberQueryField<>("creationTime");
  public static final QueryableStore.NumberQueryField<D, Long> LASTMODIFIED =
    new QueryableStore.NumberQueryField<>("lastModified");

  public static final QueryableStore.NumberQueryField<D, Float> HEIGHT =
    new QueryableStore.NumberQueryField<>("height");
  public static final QueryableStore.NumberQueryField<D, Float> WIDTH =
    new QueryableStore.NumberQueryField<>("width");

  public static final QueryableStore.NumberQueryField<D, Double> PRICE =
    new QueryableStore.NumberQueryField<>("price");
  public static final QueryableStore.NumberQueryField<D, Double> MARGIN =
    new QueryableStore.NumberQueryField<>("margin");

  private DQueryFields() {
  }
}
