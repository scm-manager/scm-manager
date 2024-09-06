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

package sonia.scm.search;


import lombok.Value;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

class SearchableTypesTest {

  @Test
  void shouldNotReturnStoredOnlyFields() {
    LuceneSearchableType luceneSearchableType = SearchableTypes.create(IndexedObject.class);
    List<String> fields = luceneSearchableType.getFields()
      .stream()
      .map(LuceneSearchableField::getName)
      .collect(Collectors.toList());

    assertThat(fields).containsOnly("searchable", "tokenized");
  }

  @Value
  @IndexedType
  public static class IndexedObject {

    @Indexed(type = Indexed.Type.STORED_ONLY)
    String storedOnly;

    @Indexed
    String searchable;

    @Indexed
    String tokenized;

  }

}
