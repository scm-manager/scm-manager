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

package sonia.scm.repository.spi.javahg;

import org.javahg.internals.HgInputStream;
import org.junit.jupiter.api.Test;
import sonia.scm.repository.FileObject;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Iterator;
import java.util.Optional;
import java.util.OptionalLong;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.Optional.empty;
import static java.util.Optional.of;
import static org.assertj.core.api.Assertions.assertThat;

class HgFileviewCommandResultReaderTest {

  @Test
  void shouldParseSimpleAttributes() throws IOException {
    Instant time1 = Instant.now().truncatedTo(ChronoUnit.SECONDS);
    Instant time2 = time1.minus(1, ChronoUnit.DAYS);

    HgFileviewCommandResultReader reader = new MockInput()
      .dir("")
      .dir("dir")
      .file("a.txt", 10, time1.toEpochMilli(), "file a")
      .file("b.txt", 100, time2.toEpochMilli(), "file b\nwith some\nmore text")
      .build();

    FileObject fileObject = reader.parseResult().get();

    assertThat(fileObject.isDirectory()).isTrue();
    assertThat(fileObject.getChildren())
      .extracting("name")
      .containsExactly("dir", "a.txt", "b.txt");
    assertThat(fileObject.getChildren())
      .extracting("directory")
      .containsExactly(true, false, false);
    assertThat(fileObject.getChildren())
      .extracting("length")
      .containsExactly(OptionalLong.empty(), OptionalLong.of(10L), OptionalLong.of(100L));
    assertThat(fileObject.getChildren())
      .extracting("description")
      .containsExactly(empty(), of("file a"), of("file b\nwith some\nmore text"));
    assertThat(fileObject.getChildren())
      .extracting("commitDate")
      .containsExactly(OptionalLong.empty(), OptionalLong.of(time1.toEpochMilli()), OptionalLong.of(time2.toEpochMilli()));
    assertThat(fileObject.isTruncated()).isFalse();
  }

  @Test
  void shouldParseTruncatedFlag() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("")
      .dir("dir")
      .file("a.txt")
      .truncated();

    FileObject fileObject = reader.parseResult().get();

    assertThat(fileObject.isTruncated()).isTrue();
  }

  @Test
  void shouldParseSubDirectory() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("dir")
      .file("dir/a.txt")
      .build();

    FileObject fileObject = reader.parseResult().get();

    assertThat(fileObject.isDirectory()).isTrue();
    assertThat(fileObject.getName()).isEqualTo("dir");
    assertThat(fileObject.getChildren())
      .extracting("name")
      .containsExactly("a.txt");
  }

  @Test
  void shouldParseRecursiveResult() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("")
      .dir("dir")
      .dir("dir/more")
      .file("dir/more/c.txt")
      .file("dir/a.txt")
      .file("dir/b.txt")
      .file("d.txt")
      .build();

    FileObject fileObject = reader.parseResult().get();

    assertThat(fileObject.getChildren())
      .extracting("name")
      .containsExactly("dir", "d.txt");
    assertThat(fileObject.getChildren())
      .extracting("directory")
      .containsExactly(true, false);

    FileObject subDir = fileObject.getChildren().iterator().next();
    assertThat(subDir.getChildren())
      .extracting("name")
      .containsExactly("more", "a.txt", "b.txt");
    assertThat(subDir.getChildren())
      .extracting("directory")
      .containsExactly(true, false, false);

    FileObject subSubDir = subDir.getChildren().iterator().next();
    assertThat(subSubDir.getChildren())
      .extracting("name")
      .containsExactly("c.txt");
    assertThat(subSubDir.getChildren())
      .extracting("directory")
      .containsExactly(false);
  }

  @Test
  void shouldCreateDirectoriesImplicitly() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("")
      .file("dir/a.txt")
      .file("dir/b.txt")
      .file("d.txt")
      .build();

    FileObject fileObject = reader.parseResult().get();

    assertThat(fileObject.getChildren())
      .extracting("name")
      .containsExactly("dir", "d.txt");
    assertThat(fileObject.getChildren())
      .extracting("directory")
      .containsExactly(true, false);

    FileObject subDir = fileObject.getChildren().iterator().next();
    assertThat(subDir.getChildren())
      .extracting("name")
      .containsExactly("a.txt", "b.txt");
    assertThat(subDir.getChildren())
      .extracting("directory")
      .containsExactly(false, false);
  }

  @Test
  void shouldCreateSubSubDirectoriesImplicitly() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("")
      .file("dir/more/a.txt")
      .file("dir/b.txt")
      .file("d.txt")
      .build();

    FileObject fileObject = reader.parseResult().get();

    assertThat(fileObject.getChildren())
      .extracting("name")
      .containsExactly("dir", "d.txt");
    assertThat(fileObject.getChildren())
      .extracting("directory")
      .containsExactly(true, false);

    FileObject subDir = fileObject.getChildren().iterator().next();
    assertThat(subDir.getChildren())
      .extracting("name")
      .containsExactly("more", "b.txt");
    assertThat(subDir.getChildren())
      .extracting("directory")
      .containsExactly(true, false);

    FileObject subSubDir = subDir.getChildren().iterator().next();
    assertThat(subSubDir.getChildren())
      .extracting("name")
      .containsExactly("a.txt");
    assertThat(subSubDir.getChildren())
      .extracting("directory")
      .containsExactly(false);
  }

  @Test
  void shouldCreateSimilarSubDirectoriesCorrectly() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("")
      .file("dir/a.txt")
      .file("directory/b.txt")
      .build();

    FileObject fileObject = reader.parseResult().get();

    assertThat(fileObject.getChildren())
      .extracting("name")
      .containsExactly("dir", "directory");
    assertThat(fileObject.getChildren())
      .extracting("directory")
      .containsExactly(true, true);

    Iterator<FileObject> fileIterator = fileObject.getChildren().iterator();
    FileObject firstSubDir = fileIterator.next();
    assertThat(firstSubDir.getChildren())
      .extracting("name")
      .containsExactly("a.txt");
    assertThat(firstSubDir.getChildren())
      .extracting("directory")
      .containsExactly(false);

    FileObject secondSubDir = fileIterator.next();
    assertThat(secondSubDir.getChildren())
      .extracting("name")
      .containsExactly("b.txt");
    assertThat(secondSubDir.getChildren())
      .extracting("directory")
      .containsExactly(false);
  }

  @Test
  void shouldIgnoreTimeAndCommentWhenDisabled() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("")
      .dir("c")
      .file("a.txt")
      .build();

    FileObject fileObject = reader.parseResult().get();

    assertThat(fileObject.getChildren())
      .extracting("description")
      .containsOnly(empty());
    assertThat(fileObject.getChildren())
      .extracting("commitDate")
      .containsOnly(OptionalLong.empty());
  }

  @Test
  void shouldIgnoreSingleEmptyDir() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("empty")
      .build();

    Optional<FileObject> fileObject = reader.parseResult();

    assertThat(fileObject).isEmpty();
  }

  @Test
  void shouldReturnEmptyRootDir() throws IOException {
    HgFileviewCommandResultReader reader = new MockInput()
      .dir("")
      .build();

    Optional<FileObject> fileObject = reader.parseResult();

    assertThat(fileObject).isNotEmpty();
  }

  private HgInputStream createInputStream(String input) {
    return new HgInputStream(new ByteArrayInputStream(input.getBytes(UTF_8)), UTF_8.newDecoder());
  }

  private class MockInput {
    private final StringBuilder stringBuilder = new StringBuilder();
    private boolean disableLastCommit = false;

    MockInput dir(String name) {
      stringBuilder
        .append('d')
        .append(name)
        .append('/')
        .append('\0');
      return this;
    }

    MockInput file(String name) {
      disableLastCommit = true;
      return file(name, 1024, 0, "n/a");
    }

    MockInput file(String name, int length, long time, String comment) {
      stringBuilder
        .append('f')
        .append(name)
        .append('\n')
        .append(length)
        .append(' ')
        .append(time/1000)
        .append(' ')
        .append(0)
        .append(' ')
        .append(comment)
        .append('\0');
      return this;
    }

    HgFileviewCommandResultReader truncated() {
      stringBuilder.append("t");
      return build();
    }

    HgFileviewCommandResultReader build() {
      HgInputStream inputStream = createInputStream(stringBuilder.toString());
      return new HgFileviewCommandResultReader(inputStream, disableLastCommit);
    }
  }
}
