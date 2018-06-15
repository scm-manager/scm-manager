package sonia.scm;

import org.junit.Test;
import org.mockito.Mock;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;

import static org.junit.Assert.*;

public class ManagerTest {

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
      return null;
    }

    @Override
    public Collection getAll(Comparator comparator) {
      return null;
    }

    @Override
    public Collection getAll(int start, int limit) {
      return null;
    }

    @Override
    public Collection getAll(Comparator comparator, int start, int limit) {
      if (start == 0 && (limit == 3) || (limit == 5)) {
        return Arrays.asList(1, 2, 3);
      } else if (start == 0 && limit == 6) {
        return Collections.emptyList();
      } else {
        return Arrays.asList(3);
      }
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
    PageResult singlePage = manager.getPage(comparator, 0, 5);
    assertFalse(singlePage.hasMore());
    assertEquals(0, singlePage.getEntities().size());
  }

  @Test
  public void getsSinglePage() {

    PageResult singlePage = manager.getPage(comparator, 0, 4);
    assertFalse(singlePage.hasMore());
    assertEquals(3, singlePage.getEntities().size() );
  }

  @Test
  public void getsTwoPages() {

    PageResult page1 = manager.getPage(comparator, 0, 2);
    assertTrue(page1.hasMore());
    assertEquals(2, page1.getEntities().size());

    PageResult page2 = manager.getPage(comparator, 1, 2);
    assertFalse(page2.hasMore());
    assertEquals(1, page2.getEntities().size());
  }
}
