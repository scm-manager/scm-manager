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

package sonia.scm;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class WebResourceRequestClassifierTest {

  @Test
  void shouldDetectPublicStaticResources() {
    assertThat(WebResourceRequestClassifier.isPublicStaticResource("/assets/webapp.bundle.js")).isTrue();
    assertThat(WebResourceRequestClassifier.isPublicStaticResource("/images/logo.png")).isTrue();
    assertThat(WebResourceRequestClassifier.isPublicStaticResource("/locales/en/commons.json")).isTrue();
    assertThat(WebResourceRequestClassifier.isPublicStaticResource("/favicon.ico")).isTrue();
    assertThat(WebResourceRequestClassifier.isPublicStaticResource("/manifest.json")).isTrue();
  }

  @Test
  void shouldNotDetectOtherFileLikePathsAsPublicStaticResources() {
    assertThat(WebResourceRequestClassifier.isPublicStaticResource("/admin.json")).isFalse();
    assertThat(WebResourceRequestClassifier.isPublicStaticResource("/api/v2/repositories/space/name/content/file.jpg")).isFalse();
    assertThat(WebResourceRequestClassifier.isPublicStaticResource("/repo/space/name/file.js")).isFalse();
  }
}
