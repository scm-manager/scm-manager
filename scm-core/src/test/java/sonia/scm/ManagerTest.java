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

import org.junit.Test;
import org.mockito.Mock;

import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class ManagerTest {

  private int givenItemCount = 0;

  private Manager manager = new ManagerForTesting();

  @Mock
  private Comparator comparator;
  private Predicate predicate = x -> true;

  @Test(expected = IllegalArgumentException.class)
  public void validatesPageNumber() {
    manager.getPage(predicate, comparator, -1, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void validatesPageSize() {
    manager.getPage(predicate, comparator, 2, 0);
  }

  @Test
  public void getsNoPage() {
    givenItemCount = 0;
    PageResult singlePage = manager.getPage(predicate, comparator, 0, 5);
    assertEquals(0, singlePage.getEntities().size());
    assertEquals(givenItemCount, singlePage.getOverallCount());
  }

  @Test
  public void getsSinglePageWithoutEnoughItems() {
    givenItemCount = 3;
    PageResult singlePage = manager.getPage(predicate, comparator, 0, 4);
    assertEquals(3, singlePage.getEntities().size() );
    assertEquals(givenItemCount, singlePage.getOverallCount());
  }

  @Test
  public void getsSinglePageWithExactCountOfItems() {
    givenItemCount = 3;
    PageResult singlePage = manager.getPage(predicate, comparator, 0, 3);
    assertEquals(3, singlePage.getEntities().size() );
    assertEquals(givenItemCount, singlePage.getOverallCount());
  }

  @Test
  public void getsTwoPages() {
    givenItemCount = 3;
    PageResult page1 = manager.getPage(predicate, comparator, 0, 2);
    assertEquals(2, page1.getEntities().size());
    assertEquals(givenItemCount, page1.getOverallCount());

    PageResult page2 = manager.getPage(predicate, comparator, 1, 2);
    assertEquals(1, page2.getEntities().size());
    assertEquals(givenItemCount, page2.getOverallCount());
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
