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

package sonia.scm.api.v2.resources;

import com.google.common.collect.ImmutableSet;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.util.ThreadContext;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import sonia.scm.SCMContext;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.group.GroupCollector;
import sonia.scm.user.EMail;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class MeDtoFactoryTest {

  private final URI baseUri = URI.create("https://scm.hitchhiker.com/scm/");

  @Mock
  private GroupCollector groupCollector;

  @Mock
  private Subject subject;

  @Mock
  private ScmConfiguration scmConfiguration;

  @Mock
  private EMail eMail;

  private MeDtoFactory meDtoFactory;

  @BeforeEach
  void setUpContext() {
    ThreadContext.bind(subject);
    ResourceLinks resourceLinks = ResourceLinksMock.createMock(baseUri);
    meDtoFactory = new MeDtoFactory(resourceLinks, groupCollector, scmConfiguration, eMail);
  }

  @AfterEach
  void unbindSubject() {
    ThreadContext.unbindSubject();
  }

  @Test
  void shouldCreateMeDtoFromUser() {
    prepareSubject(UserTestData.createTrillian());

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getName()).isEqualTo("trillian");
    assertThat(dto.getDisplayName()).isEqualTo("Tricia McMillan");
    assertThat(dto.getMail()).isEqualTo("tricia.mcmillan@hitchhiker.com");
  }

  @Test
  void shouldCreateMeDtoWithEmptyGroups() {
    prepareSubject(UserTestData.createTrillian());
    MeDto dto = meDtoFactory.create();
    assertThat(dto.getGroups()).isEmpty();
  }

  @Test
  void shouldCreateMeDtoWithGroups() {
    when(groupCollector.collect("trillian")).thenReturn(ImmutableSet.of("HeartOfGold", "Puzzle42"));
    prepareSubject(UserTestData.createTrillian());
    MeDto dto = meDtoFactory.create();
    assertThat(dto.getGroups()).containsOnly("HeartOfGold", "Puzzle42");
  }

  private void prepareSubject(User user) {
    PrincipalCollection collection = mock(PrincipalCollection.class);
    when(subject.getPrincipals()).thenReturn(collection);
    when(collection.oneByType(User.class)).thenReturn(user);
  }

  @Test
  void shouldAppendSelfLink() {
    prepareSubject(UserTestData.createTrillian());

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("self").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/me/");
  }

  @Test
  void shouldAppendDeleteLink() {
    prepareSubject(UserTestData.createTrillian());
    when(subject.isPermitted("user:delete:trillian")).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("delete").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/users/trillian");
  }

  @Test
  void shouldNotAppendDeleteLink() {
    prepareSubject(UserTestData.createTrillian());

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("delete")).isNotPresent();
  }

  @Test
  void shouldAppendUpdateLink() {
    prepareSubject(UserTestData.createTrillian());
    when(subject.isPermitted("user:modify:trillian")).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("update").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/users/trillian");
  }

  @Test
  void shouldNotAppendUpdateLink() {
    prepareSubject(UserTestData.createTrillian());

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldNotGetPasswordLinkWithoutPermission() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("password")).isNotPresent();
  }

  @Test
  void shouldAppendOnlySelfLinkIfAnonymousUser() {
    User user = SCMContext.ANONYMOUS;
    prepareSubject(user);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("self")).isPresent();
    assertThat(dto.getLinks().getLinkBy("password")).isNotPresent();
    assertThat(dto.getLinks().getLinkBy("delete")).isNotPresent();
    assertThat(dto.getLinks().getLinkBy("update")).isNotPresent();
  }

  @Test
  void shouldAppendPublicKeysLink() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);

    when(subject.isPermitted("user:changePublicKeys:trillian")).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("publicKeys").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/users/trillian/public_keys");
  }

  @Test
  void shouldNotAppendPublicKeysLink() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);

    when(subject.isPermitted("user:changePublicKeys:trillian")).thenReturn(false);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("publicKeys")).isNotPresent();
  }

  @Test
  void shouldAppendLinks() {
    prepareSubject(UserTestData.createTrillian());

    HalEnricherRegistry registry = new HalEnricherRegistry();
    meDtoFactory.setRegistry(registry);

    registry.register(Me.class, (ctx, appender) -> {
      User user = ctx.oneRequireByType(User.class);
      appender.appendLink("profile", "http://hitchhiker.com/users/" + user.getName());
    });

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("profile").get().getHref()).isEqualTo("http://hitchhiker.com/users/trillian");
  }

  @Test
  void shouldUserGeneratedMailOnlyWhenUserHasNone() {
    User user = UserTestData.createTrillian();
    user.setMail(null);
    prepareSubject(user);
    when(eMail.getMailOrFallback(user)).thenReturn("trillian@hitchhiker.local");

    MeDto dto = meDtoFactory.create();

    assertThat(dto.getMail()).isNull();
    assertThat(dto.getFallbackMail()).isEqualTo("trillian@hitchhiker.local");
  }

  @Test
  void shouldAppendApiKeysLink() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);

    when(scmConfiguration.isEnabledApiKeys()).thenReturn(true);
    when(subject.isPermitted("user:changeApiKeys:trillian")).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("apiKeys").get().getHref()).isEqualTo("https://scm.hitchhiker.com/scm/v2/users/trillian/api_keys");
  }

  @Test
  void shouldNotAppendApiKeysLinkIfMissingPermission() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);

    when(subject.isPermitted("user:changeApiKeys:trillian")).thenReturn(false);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("apiKeys")).isNotPresent();
  }

  @Test
  void shouldNotAppendApiKeysLinkIfConfigDisabled() {
    User user = UserTestData.createTrillian();
    prepareSubject(user);
    when(scmConfiguration.isEnabledApiKeys()).thenReturn(false);
    when(subject.isPermitted("user:changeApiKeys:trillian")).thenReturn(true);

    MeDto dto = meDtoFactory.create();
    assertThat(dto.getLinks().getLinkBy("apiKeys")).isNotPresent();
  }
}
