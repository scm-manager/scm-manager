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

package sonia.scm.it;

import org.junit.Test;
import sonia.scm.it.utils.ScmRequests;

import static org.assertj.core.api.Assertions.assertThat;

public class I18nServletITCase {

  @Test
  public void shouldGetCollectedPluginTranslations() {
    ScmRequests.start()
      .requestPluginTranslations("de")
      .assertStatusCode(200)
      .assertSingleProperty(value -> assertThat(value).isNotNull(), "scm-git-plugin")
      .assertSingleProperty(value -> assertThat(value).isNotNull(), "scm-hg-plugin")
      .assertSingleProperty(value -> assertThat(value).isNotNull(), "scm-svn-plugin");
  }
}
