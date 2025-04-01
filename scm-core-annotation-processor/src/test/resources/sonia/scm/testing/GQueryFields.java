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

public final class GQueryFields {
  public static final QueryableStore.IdQueryField<G> INTERNAL_ID =
    new QueryableStore.IdQueryField<>();

  public static final QueryableStore.MapQueryField<G> DICTIONARY =
    new QueryableStore.MapQueryField<>("dictionary");

  public static final QueryableStore.MapSizeQueryField<G> DICTIONARY_SIZE =
    new QueryableStore.MapSizeQueryField<>("dictionary");

  private GQueryFields() {
  }
}
