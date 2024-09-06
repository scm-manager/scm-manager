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
import com.google.common.collect.Multimap;
import java.io.IOException;
import java.util.Collection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;


@ExtendWith(MockitoExtension.class)
class BaseHttpRequestTest {

  @Mock
  private AdvancedHttpClient ahc;
  
  private BaseHttpRequest<AdvancedHttpRequest> request;
  
  @BeforeEach
  public void before(){
    request = new AdvancedHttpRequest(ahc, HttpMethod.GET, "https://www.scm-manager.org");
  }

  @Test
  void shouldAddAuthorizationHeaderWithBasicScheme() {
    request.basicAuth("tricia", "mcmillian123");
    Multimap<String,String> headers = request.getHeaders();
    assertThat(headers.get("Authorization").iterator().next()).isEqualTo("Basic dHJpY2lhOm1jbWlsbGlhbjEyMw==");
  }

  @Test
  void shouldAddAuthorizationHeaderWithBearerScheme() {
    request.bearerAuth("awesome-access-token");
    Multimap<String,String> headers = request.getHeaders();
    assertThat(headers.get("Authorization").iterator().next()).isEqualTo("Bearer awesome-access-token");
  }
  
  @Test
  void shouldAppendQueryString(){
    request.queryString("a", "b");
    assertThat(request.getUrl()).isEqualTo("https://www.scm-manager.org?a=b");
  }
  
  @Test
  void shouldAppendMultipleQueryStrings(){
    request.queryString("a", "b");
    request.queryString("c", "d", "e");
    assertThat(request.getUrl()).isEqualTo("https://www.scm-manager.org?a=b&c=d&c=e");
  }
  
  @Test
  void shouldEscapeQueryString(){
    request.queryString("a", "äüö");
    assertThat(request.getUrl()).isEqualTo("https://www.scm-manager.org?a=%C3%A4%C3%BC%C3%B6");
  }
  
  @Test
  void shouldAppendQueryStringFromIterable(){
    Iterable<?> i1 = Lists.newArrayList("b");
    Iterable<?> i2 = Lists.newArrayList("d", "e");
    request.queryStrings("a", i1);
    request.queryStrings("c", i2);

    assertThat(request.getUrl()).isEqualTo("https://www.scm-manager.org?a=b&c=d&c=e");
  }
  
  @Test
  void ShouldNotAppendQueryStringWithNullValue(){
    request.queryString("a", null, "b");
    assertThat(request.getUrl()).isEqualTo("https://www.scm-manager.org?a=&a=b");
  }
  
  @Test
  void shouldAddHeader(){
    request.header("a", "b");
    assertThat(request.getHeaders().get("a").iterator().next()).isEqualTo("b");
  }
  
  @Test
  void shouldAddHeaderWithMultipleValues(){
    request.header("a", "b", "c", "d");
    assertThat( request.getHeaders().get("a")).contains("b", "c", "d");
  }
  
  @Test
  void shouldExecuteWithClient() throws IOException{
    request.request();

    verify(ahc).request(request);
  }
  
  @Test
  void shouldApplyValueFromBuilderMethods(){
    Iterable<?> i1 = Lists.newArrayList("b");
    assertThat(request.decodeGZip(true)).isInstanceOf(AdvancedHttpRequest.class);
    assertThat(request.isDecodeGZip()).isTrue();
    assertThat(request.disableCertificateValidation(true)).isInstanceOf(AdvancedHttpRequest.class);
    assertThat(request.isDisableCertificateValidation()).isTrue();
    assertThat(request.disableHostnameValidation(true)).isInstanceOf(AdvancedHttpRequest.class);
    assertThat(request.isDisableHostnameValidation()).isTrue();
    assertThat(request.ignoreProxySettings(true)).isInstanceOf(AdvancedHttpRequest.class);
    assertThat(request.isIgnoreProxySettings()).isTrue();
    assertThat(request.header("a", "b")).isInstanceOf(AdvancedHttpRequest.class);
    assertThat(request.headers("a", i1)).isInstanceOf(AdvancedHttpRequest.class);
    assertThat(request.queryString("a", "b")).isInstanceOf(AdvancedHttpRequest.class);
    assertThat(request.queryStrings("a", i1)).isInstanceOf(AdvancedHttpRequest.class);
  }

}
