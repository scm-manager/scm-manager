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
