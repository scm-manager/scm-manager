/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
    
package sonia.scm.net.ahc;

import com.google.common.collect.Lists;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 *
 * @author Sebastian Sdorra
 */
@ExtendWith(MockitoExtension.class)
class FormContentBuilderTest {

  @Mock
  private AdvancedHttpRequestWithBody request;
  
  @InjectMocks
  private FormContentBuilder builder;

  @Test
  void shouldEncodeFieldValues() {
    builder.field("a", "ü", "ä", "ö").build();
    assertContent("a=%C3%BC&a=%C3%A4&a=%C3%B6");
  }
  
  @Test
  void shouldApplySimpleUrlEncoded() {
    builder.field("a", "b").build();

    assertContent("a=b");

    verify(request).contentType("application/x-www-form-urlencoded");
  }

  @Test
  void shouldCreateValuesFromVarargs() {
    builder.field("a", "b").field("c", "d", "e").build();
    assertContent("a=b&c=d&c=e");
  }
  
  @Test
  void shouldCreateValuesFromIterables() {
    Iterable<Object> i1 = Lists.newArrayList("b");
    builder.fields("a", i1)
           .fields("c", Lists.newArrayList("d", "e"))
           .build();
    assertContent("a=b&c=d&c=e");
  }
  
  private void assertContent(String content){
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(request).stringContent(captor.capture());
    assertThat(captor.getValue()).isEqualTo(content);
  }

}
