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

package sonia.scm.api.v2.resources;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.core.MediaType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.github.sdorra.jse.ShiroExtension;
import org.github.sdorra.jse.SubjectAware;
import org.jboss.resteasy.mock.MockHttpRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.store.ConfigurationStore;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.web.JsonMockHttpRequest;
import sonia.scm.web.JsonMockHttpResponse;
import sonia.scm.web.RestDispatcher;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith({MockitoExtension.class, ShiroExtension.class})
@SubjectAware(value = "trillian")
class ConfigurationAdapterBaseTest {

  @Mock
  private ConfigurationStore<TestConfiguration> configStore;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ConfigurationStoreFactory configurationStoreFactory;

  @Mock
  private Provider<ScmPathInfoStore> scmPathInfoStore;

  private RestDispatcher dispatcher;
  private final JsonMockHttpResponse response = new JsonMockHttpResponse();

  @BeforeEach
  void initResource() {
    when(configurationStoreFactory.withType(TestConfiguration.class).withName("test-config").build()).thenReturn(configStore);
    ScmPathInfoStore pathInfoStore = new ScmPathInfoStore();
    pathInfoStore.set(() -> URI.create("v2/test"));
    lenient().when(scmPathInfoStore.get()).thenReturn(pathInfoStore);
    TestConfigurationAdapter resource = new TestConfigurationAdapter(configurationStoreFactory, scmPathInfoStore);

    dispatcher = new RestDispatcher();
    dispatcher.addSingletonResource(resource);
  }

  @Test
  void shouldThrowBadRequestErrorWhenUpdatingWithNullPayload() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.put("/v2/test")
      .contentType(MediaType.APPLICATION_JSON);

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(400);
  }

  @Test
  void shouldThrowAuthorizationExceptionWithoutPermissionOnRead() throws URISyntaxException {
    MockHttpRequest request = MockHttpRequest.get("/v2/test");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Test
  void shouldThrowAuthorizationExceptionWithoutPermissionOnUpdate() throws URISyntaxException {
    JsonMockHttpRequest request = JsonMockHttpRequest.put("/v2/test")
      .contentType(MediaType.APPLICATION_JSON)
      .json("{\"number\": 3, \"text\" : \"https://scm-manager.org/\"}");

    dispatcher.invoke(request, response);

    assertThat(response.getStatus()).isEqualTo(403);
  }

  @Nested
  @SubjectAware(permissions = "configuration:read,write:testConfig")
  class WithPermission {

    @Test
    void shouldGetDefaultDao() throws URISyntaxException {
      when(configStore.getOptional()).thenReturn(Optional.empty());
      MockHttpRequest request = MockHttpRequest.get("/v2/test");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsJson().get("number").asInt()).isEqualTo(4);
      assertThat(response.getContentAsJson().get("text").textValue()).isEqualTo("myTest");
    }

    @Test
    void shouldGetStoredDao() throws URISyntaxException {
      when(configStore.getOptional()).thenReturn(Optional.of(new TestConfiguration(3, "secret")));

      MockHttpRequest request = MockHttpRequest.get("/v2/test");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(200);
      assertThat(response.getContentAsJson().get("number").asInt()).isEqualTo(3);
      assertThat(response.getContentAsJson().get("text").textValue()).isEqualTo("secret");
    }

    @Test
    void shouldThrowBadRequestErrorWhenUpdatingWithInvalidPayload() throws URISyntaxException {
      JsonMockHttpRequest request = JsonMockHttpRequest.put("/v2/test")
        .contentType(MediaType.APPLICATION_JSON)
        .json("{\"number\": 42, \"text\" : \"https://scm-manager.org/\"}");

      dispatcher.invoke(request, response);

      assertThat(response.getStatus()).isEqualTo(400);
    }
  }

  @AllArgsConstructor
  @NoArgsConstructor
  @Getter
  @Setter
  static class TestConfiguration {
    private int number = 4;
    private String text = "myTest";
  }

  @NoArgsConstructor
  @Getter
  @Setter
  static class TestConfigurationDto extends HalRepresentation {

    @Min(2)
    @Max(6)
    private int number;

    @NotBlank
    private String text;

    public TestConfigurationDto(int number, String text) {
      super(Links.emptyLinks(), Embedded.emptyEmbedded());
      this.number = number;
      this.text = text;
    }

    public static TestConfigurationDto from(TestConfiguration configuration, Links links) {
      return new TestConfigurationDto(configuration.number, configuration.text);
    }

    public TestConfiguration toEntity() {
      return new TestConfiguration(number, text);
    }
  }

  @Path("/v2/test")
  private static class TestConfigurationAdapter extends ConfigurationAdapterBase<TestConfiguration, TestConfigurationDto> {

    @Inject
    public TestConfigurationAdapter(ConfigurationStoreFactory configurationStoreFactory, Provider<ScmPathInfoStore> scmPathInfoStoreProvider) {
      super(configurationStoreFactory, scmPathInfoStoreProvider, TestConfiguration.class, TestConfigurationDto.class);
    }

    @Override
    protected String getName() {
      return "testConfig";
    }
  }
}
