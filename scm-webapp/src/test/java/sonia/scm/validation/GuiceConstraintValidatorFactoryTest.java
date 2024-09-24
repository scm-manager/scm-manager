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

package sonia.scm.validation;

import com.google.inject.Guice;
import com.google.inject.Injector;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import sonia.scm.repository.RepositoryManager;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class GuiceConstraintValidatorFactoryTest {

  @Mock
  private RepositoryManager repositoryManager;

  @Test
  void shouldUseInjectorToCreateConstraintInstance() {
    Injector injector = Guice.createInjector(new RepositoryManagerModule(repositoryManager));
    GuiceConstraintValidatorFactory factory = new GuiceConstraintValidatorFactory(injector);
    RepositoryTypeConstraintValidator instance = factory.getInstance(RepositoryTypeConstraintValidator.class);
    assertThat(instance.getRepositoryManager()).isSameAs(repositoryManager);
  }

}
