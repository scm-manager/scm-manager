package sonia.scm;

import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Collection;
import java.util.Comparator;
import java.util.stream.IntStream;

import static java.util.stream.Collectors.toList;
import static org.junit.Assert.assertEquals;

public class ManagerTest {

  private int givenItemCount = 0;

  private Manager manager = new Manager() {
    @Override
    public void refresh(ModelObject object) throws IOException {

    }

    @Override
    public ModelObject get(String id) {
      return null;
    }

    @Override
    public Collection getAll() {
      return IntStream.range(0, givenItemCount).boxed().collect(toList());
    }

    @Override
    public Collection getAll(Comparator comparator) {
      return getAll();
    }

    @Override
    public Collection getAll(int start, int limit) {
      return null;
    }

    @Override
    public Collection getAll(Comparator comparator, int start, int limit) {
      return null;
    }

    @Override
    public void create(TypedObject object) throws Exception, IOException {}

    @Override
    public void delete(TypedObject object) throws Exception, IOException {}

    @Override
    public void modify(TypedObject object) throws Exception, IOException {}

    @Override
    public void close() throws IOException {}

    @Override
    public void init(SCMContextProvider context) {}

    @Override
    public Long getLastModified() { return null; }
  };

  @Mock
  private Comparator comparator;


  @Test(expected = IllegalArgumentException.class)
  public void validatesPageNumber() {
    manager.getPage(comparator, -1, 5);
  }

  @Test(expected = IllegalArgumentException.class)
  public void validatesPageSize() {
    manager.getPage(comparator, 2, 0);
  }

  @Test
  public void getsNoPage() {
    givenItemCount = 0;
    PageResult singlePage = manager.getPage(comparator, 0, 5);
    assertEquals(0, singlePage.getEntities().size());
    assertEquals(givenItemCount, singlePage.getOverallCount());
  }

  @Test
  public void getsSinglePageWithoutEnoughItems() {
    givenItemCount = 3;
    PageResult singlePage = manager.getPage(comparator, 0, 4);
    assertEquals(3, singlePage.getEntities().size() );
    assertEquals(givenItemCount, singlePage.getOverallCount());
  }

  @Test
  public void getsSinglePageWithExactCountOfItems() {
    givenItemCount = 3;
    PageResult singlePage = manager.getPage(comparator, 0, 3);
    assertEquals(3, singlePage.getEntities().size() );
    assertEquals(givenItemCount, singlePage.getOverallCount());
  }

  @Test
  public void getsTwoPages() {
    givenItemCount = 3;
    PageResult page1 = manager.getPage(comparator, 0, 2);
    assertEquals(2, page1.getEntities().size());
    assertEquals(givenItemCount, page1.getOverallCount());

    PageResult page2 = manager.getPage(comparator, 1, 2);
    assertEquals(1, page2.getEntities().size());
    assertEquals(givenItemCount, page2.getOverallCount());
  }
}
