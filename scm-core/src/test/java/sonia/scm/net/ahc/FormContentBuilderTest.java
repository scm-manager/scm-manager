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

package sonia.scm.net.ahc;

import com.google.common.collect.Lists;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;


@ExtendWith(MockitoExtension.class)
class FormContentBuilderTest {

  @Mock
  private AdvancedHttpRequestWithBody request;

  private FormContentBuilder builder;

  @Nested
  class Default {

    @BeforeEach
    void setUpObjectUnderTest() {
      // required because InjectMocks uses the wrong constructor
      builder = new FormContentBuilder(request);
    }

    @Test
    void shouldEncodeFieldValues() {
      builder.field("a", "ü", "ä", "ö").build();
      assertUrlEncodedContent("a=%C3%BC&a=%C3%A4&a=%C3%B6");
    }

    @Test
    void shouldApplySimpleUrlEncoded() {
      builder.field("a", "b").build();

      assertUrlEncodedContent("a=b");

      verify(request).contentType("application/x-www-form-urlencoded");
    }

    @Test
    void shouldUseMultipartFormData() throws IOException {
      builder.file("file", "test.txt", stream("hello"));
      buildMultipart();

      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      verify(request).contentType(captor.capture());

      assertThat(captor.getValue()).startsWith("multipart/form-data; boundary=");
    }

    @Test
    void shouldCreateValuesFromVarargs() {
      builder.field("a", "b").field("c", "d", "e").build();
      assertUrlEncodedContent("a=b&c=d&c=e");
    }

    @Test
    void shouldCreateValuesFromIterables() {
      Iterable<Object> i1 = Lists.newArrayList("b");
      builder.fields("a", i1)
        .fields("c", Lists.newArrayList("d", "e"))
        .build();
      assertUrlEncodedContent("a=b&c=d&c=e");
    }

  }

  @Nested
  class WithBoundary {

    @BeforeEach
    void setUpObjectUnderTest() {
      // required because InjectMocks uses the wrong constructor
      builder = new FormContentBuilder(request, () -> "boundary");
    }

    @Test
    void shouldAppendBoundaryToContentType() throws IOException {
      builder.file("file", "test.txt", stream("hello"));
      buildMultipart();

      ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
      verify(request).contentType(captor.capture());

      assertThat(captor.getValue()).startsWith("multipart/form-data; boundary=boundary");
    }

    @Test
    void shouldSendMultipartContent() throws IOException {
      builder.field("title", "Readme");
      builder.file("content", "README.md", stream("# hello"));

      String expected = String.join(System.lineSeparator(),
        "--boundary",
        "Content-Disposition: form-data; name=\"title\"",
        "",
        "Readme",
        "--boundary",
        "Content-Disposition: form-data; name=\"content\"; filename=\"README.md\"",
        "Content-Transfer-Encoding: binary",
        "",
        "# hello",
        "--boundary",
        ""
      );

      assertThat(buildMultipart()).isEqualTo(expected);
    }

  }

  private InputStream stream(String content) {
    return new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8));
  }


  private void assertUrlEncodedContent(String content) {
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(request).stringContent(captor.capture());
    assertThat(captor.getValue()).isEqualTo(content);
  }

  private String buildMultipart() throws IOException {
    builder.build();

    ArgumentCaptor<Content> contentCaptor = ArgumentCaptor.forClass(Content.class);
    verify(request).content(contentCaptor.capture());

    Content content = contentCaptor.getValue();
    content.prepare(request);

    ByteArrayOutputStream baos = new ByteArrayOutputStream();
    content.process(baos);

    return baos.toString();
  }

}
