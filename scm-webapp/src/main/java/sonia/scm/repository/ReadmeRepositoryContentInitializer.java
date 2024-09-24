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

import com.google.common.base.Strings;
import sonia.scm.Priority;
import sonia.scm.plugin.Extension;

import java.io.IOException;

@Extension
@Priority(1) // should always be the first, so that plugins can overwrite the readme.md
public class ReadmeRepositoryContentInitializer implements RepositoryContentInitializer {
  @Override
  public void initialize(InitializerContext context) throws IOException {
    Repository repository = context.getRepository();

    String content = "# " + repository.getName();
    String description = repository.getDescription();
    if (!Strings.isNullOrEmpty(description)) {
      content += "\n\n" + description;
    }
    context.create("README.md").from(content);
  }
}
