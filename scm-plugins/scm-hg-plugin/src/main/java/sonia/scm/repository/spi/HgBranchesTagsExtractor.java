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

package sonia.scm.repository.spi;

import org.javahg.Branch;
import sonia.scm.repository.Tag;

import java.util.List;
import java.util.stream.Collectors;

@SuppressWarnings("java:S3252") // This is just how to use javahg
final class HgBranchesTagsExtractor {

  private HgBranchesTagsExtractor() {
  }

  static List<Tag> extractTags(HgCommandContext context) {
    return org.javahg.commands.TagsCommand.on(context.open()).execute().stream()
      .map(t -> new Tag(t.getName(), t.getChangeset().toString()))
      .collect(Collectors.toList());
  }

  static List<String> extractBranches(HgCommandContext context) {
    return org.javahg.commands.BranchesCommand.on(context.open()).execute().stream()
      .map(Branch::getName)
      .collect(Collectors.toList());
  }
}
