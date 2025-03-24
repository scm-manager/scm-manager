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

package sonia.scm.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import static org.mockito.Mockito.verify;
import sonia.scm.security.SecureKeyResolver;

@ExtendWith(MockitoExtension.class)
class SecureKeyServiceTest {

  private SecureKeyService secureKeyService;
  @Mock
  private SecureKeyResolver configRepository;

  @BeforeEach
  void prepareEnvironment() {
    this.secureKeyService = new SecureKeyService(configRepository);
  }

  @Test
  void shouldDeleteStore() {
    this.secureKeyService.clearAllTokens();
    verify(configRepository).deleteStore();
  }
}
