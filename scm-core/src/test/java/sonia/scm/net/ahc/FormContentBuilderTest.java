/**
 * Copyright (c) 2014, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */

package sonia.scm.net.ahc;

import com.google.common.collect.Lists;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.*;
import org.mockito.runners.MockitoJUnitRunner;

/**
 *
 * @author Sebastian Sdorra
 */
@RunWith(MockitoJUnitRunner.class)
public class FormContentBuilderTest {

  @Mock
  private AdvancedHttpRequestWithBody request;
  
  @InjectMocks
  private FormContentBuilder builder;

  @Test
  public void testFieldEncoding()
  {
    builder.field("a", "ü", "ä", "ö").build();
    assertContent("a=%C3%BC&a=%C3%A4&a=%C3%B6");
  }
  
  @Test
  public void testBuild()
  {
    builder.field("a", "b").build();
    assertContent("a=b");
    verify(request).contentType("application/x-www-form-urlencoded");
  }

  @Test
  public void testFieldWithArray()
  { 
    builder.field("a", "b").field("c", "d", "e").build();
    assertContent("a=b&c=d&c=e");
  }
  
  @Test
  public void testFieldWithIterable()
  { 
    Iterable<? extends Object> i1 = Lists.newArrayList("b");
    builder.fields("a", i1)
           .fields("c", Lists.newArrayList("d", "e"))
           .build();
    assertContent("a=b&c=d&c=e");
  }
  
  private void assertContent(String content){
    ArgumentCaptor<String> captor = ArgumentCaptor.forClass(String.class);
    verify(request).stringContent(captor.capture());
    assertEquals(content, captor.getValue());
  }

}