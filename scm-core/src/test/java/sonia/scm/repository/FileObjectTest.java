package sonia.scm.repository;

import org.junit.Test;

import static org.junit.Assert.*;

public class FileObjectTest {

  @Test
  public void getParentPath() {
    FileObject file = create("a/b/c");
    assertEquals("a/b", file.getParentPath());
  }

  @Test
  public void getParentPathWithoutParent() {
    FileObject file = create("a");
    assertEquals("", file.getParentPath());
  }

  @Test
  public void getParentPathOfRoot() {
    FileObject file = create("");
    assertNull(file.getParentPath());
  }

  private FileObject create(String path) {
    FileObject file = new FileObject();
    file.setPath(path);
    return file;
  }
}
