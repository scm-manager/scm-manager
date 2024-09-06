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

import jakarta.inject.Inject;

public class LuceneQueryBuilderFactory {

  private final IndexManager indexManager;
  private final AnalyzerFactory analyzerFactory;

  @Inject
  public LuceneQueryBuilderFactory(IndexManager indexManager, AnalyzerFactory analyzerFactory) {
    this.indexManager = indexManager;
    this.analyzerFactory = analyzerFactory;
  }

  public <T> LuceneQueryBuilder<T> create(IndexParams indexParams) {
    return new LuceneQueryBuilder<>(
      indexManager,
      indexParams.getIndex(),
      indexParams.getSearchableType(),
      analyzerFactory.create(indexParams.getSearchableType())
    );
  }

}
