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

import org.junit.jupiter.api.Test;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class ManagerTest {

  private int givenItemCount = 0;

  private Manager manager = new ManagerForTesting();

  @Mock
  private Comparator comparator;
  private Predicate predicate = x -> true;

  @Test
  public void validatesPageNumber() {
    assertThatThrownBy(() -> manager.getPage(predicate, comparator, -1, 5))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void validatesPageSize() {
    assertThatThrownBy(() -> manager.getPage(predicate, comparator, 2, 0))
      .isInstanceOf(IllegalArgumentException.class);
  }

  @Test
  public void getsNoPage() {
    givenItemCount = 0;
    PageResult singlePage = manager.getPage(predicate, comparator, 0, 5);
    assertThat(singlePage.getEntities()).hasSize(0);
    assertThat(singlePage.getOverallCount()).hasValue(givenItemCount);
  }

  @Test
  public void getsSinglePageWithoutEnoughItems() {
    givenItemCount = 3;
    PageResult singlePage = manager.getPage(predicate, comparator, 0, 4);
    assertThat(singlePage.getEntities()).hasSize(3);
    assertThat(singlePage.getOverallCount()).hasValue(givenItemCount);
  }

  @Test
  public void getsSinglePageWithExactCountOfItems() {
    givenItemCount = 3;
    PageResult singlePage = manager.getPage(predicate, comparator, 0, 3);
    assertThat(singlePage.getEntities()).hasSize(3);
    assertThat(singlePage.getOverallCount()).hasValue(givenItemCount);
  }

  @Test
  public void getsTwoPages() {
    givenItemCount = 3;
    PageResult page1 = manager.getPage(predicate, comparator, 0, 2);
    assertThat(page1.getEntities()).hasSize(2);
    assertThat(page1.getOverallCount()).hasValue(givenItemCount);

    PageResult page2 = manager.getPage(predicate, comparator, 1, 2);
    assertThat(page2.getEntities()).hasSize(1);
    assertThat(page2.getOverallCount()).hasValue(givenItemCount);
  }

  private class ManagerForTesting implements Manager {

    @Override
    public void refresh(ModelObject object) {}

    @Override
    public ModelObject get(String id) { return null; }

    @Override
    public Collection getAll() {
      return IntStream.range(0, givenItemCount).boxed().collect(toList());
    }

    @Override
    public Collection getAll(Predicate filter, Comparator comparator) { return getAll(); }

    @Override
    public Collection getAll(int start, int limit) { return null; }

    @Override
    public Collection getAll(Comparator comparator, int start, int limit) {  return null; }

    @Override
    public TypedObject create(TypedObject object) { return null; }

    @Override
    public void delete(TypedObject object) {}

    @Override
    public void modify(TypedObject object) {}

    @Override
    public void close() {}

    @Override
    public void init(SCMContextProvider context) {}

    @Override
    public Long getLastModified() { return null; }
  }
}
