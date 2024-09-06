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

package sonia.scm.lifecycle.modules;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.lifecycle.modules.ScmSecurityModule.IdempotentPasswordService;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class IdempotentPasswordServiceTest {

  @Test
  void shouldEncryptCleartextPassword() {
    IdempotentPasswordService passwordService = new IdempotentPasswordService();

    String encryptedPassword = passwordService.encryptPassword("some simple password");

    assertThat(encryptedPassword).startsWith("$shiro1$");
  }

  @Test
  void shouldKeepAlreadyEncryptedPassword() {
    IdempotentPasswordService passwordService = new IdempotentPasswordService();

    String encryptedPassword = passwordService.encryptPassword("$shiro1$SHA-512$8192$XHkPE4rU53P/TsZNrAYdSw==$k5OehxvFr4C8rNk6pLYwtX9g5qbKKcsjFOwd0l29X3s=");

    assertThat(encryptedPassword).isEqualTo("$shiro1$SHA-512$8192$XHkPE4rU53P/TsZNrAYdSw==$k5OehxvFr4C8rNk6pLYwtX9g5qbKKcsjFOwd0l29X3s=");
  }
}
