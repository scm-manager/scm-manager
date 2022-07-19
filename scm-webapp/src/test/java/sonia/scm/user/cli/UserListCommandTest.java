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

package sonia.scm.user.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.cli.TemplateTestRenderer;
import sonia.scm.user.User;
import sonia.scm.user.UserManager;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserListCommandTest {

  @Mock
  private UserManager manager;

  private final UserCommandBeanMapper beanMapper = new UserCommandBeanMapperImpl();
  private final TemplateTestRenderer testRenderer = new TemplateTestRenderer();

  private UserListCommand command;

  @BeforeEach
  void initCommand() {
    command = new UserListCommand(testRenderer.createTemplateRenderer(), manager, beanMapper);
  }

  @BeforeEach
  void mockGroups() {
    User internalUser = new User("havelock", "Havelock Vetinari", "havelock.vetinari@discworld");
    User externalUser = new User("mustrum", "Mustrum Ridcully", "mustrum.ridcully@discworld");
    externalUser.setExternal(true);
    externalUser.setActive(false);
    when(manager.getAll())
      .thenReturn(
        asList(
          internalUser,
          externalUser));
  }

  @Test
  void shouldRenderShortTableInEnglish() {
    testRenderer.setLocale("en");
    command.setSpec(testRenderer.getMockedSpec());
    command.setUseShortTemplate(true);

    command.run();

    assertThat(testRenderer.getStdOut()).isEqualTo("havelock\nmustrum\n");
    assertThat(testRenderer.getStdErr()).isEmpty();
  }

  @Test
  void shouldRenderShortTableInGerman() {
    testRenderer.setLocale("de");
    command.setSpec(testRenderer.getMockedSpec());
    command.setUseShortTemplate(true);

    command.run();

    assertThat(testRenderer.getStdOut()).isEqualTo("havelock\nmustrum\n");
    assertThat(testRenderer.getStdErr()).isEmpty();
  }

  @Test
  void shouldRenderLongTableInEnglish() {
    testRenderer.setLocale("en");
    command.setSpec(testRenderer.getMockedSpec());

    command.run();

    assertThat(testRenderer.getStdOut())
      .isEqualTo("USERNAME DISPLAY NAME      EMAIL ADDRESS               EXTERNAL ACTIVE\n" +
        "havelock Havelock Vetinari havelock.vetinari@discworld no       yes   \n" +
        "mustrum  Mustrum Ridcully  mustrum.ridcully@discworld  yes      no    \n");
    assertThat(testRenderer.getStdErr()).isEmpty();
  }

  @Test
  void shouldRenderLongTableInGerman() {
    testRenderer.setLocale("de");
    command.setSpec(testRenderer.getMockedSpec());

    command.run();

    assertThat(testRenderer.getStdOut())
      .isEqualTo("BENUTZERNAME ANZEIGENAME       E-MAIL-ADRESSE              EXTERN AKTIV\n" +
        "havelock     Havelock Vetinari havelock.vetinari@discworld nein   ja   \n" +
        "mustrum      Mustrum Ridcully  mustrum.ridcully@discworld  ja     nein \n");
    assertThat(testRenderer.getStdErr()).isEmpty();
  }
}
