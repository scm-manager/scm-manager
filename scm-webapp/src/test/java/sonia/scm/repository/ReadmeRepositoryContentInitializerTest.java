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

package sonia.scm.repository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ReadmeRepositoryContentInitializerTest {

  @Mock
  private RepositoryContentInitializer.InitializerContext context;

  @Mock
  private RepositoryContentInitializer.CreateFile createFile;

  private Repository repository;

  private final ReadmeRepositoryContentInitializer initializer = new ReadmeRepositoryContentInitializer();

  @BeforeEach
  void setUpContext() {
    repository = RepositoryTestData.createHeartOfGold("hg");
    when(context.getRepository()).thenReturn(repository);
    when(context.create("README.md")).thenReturn(createFile);
  }

  @Test
  void shouldCreateReadme() throws IOException {
    initializer.initialize(context);

    verify(createFile).from("# HeartOfGold\n\n" + repository.getDescription());
  }

  @Test
  void shouldCreateReadmeWithoutDescription() throws IOException {
    repository.setDescription(null);

    initializer.initialize(context);

    verify(createFile).from("# HeartOfGold");
  }

}
