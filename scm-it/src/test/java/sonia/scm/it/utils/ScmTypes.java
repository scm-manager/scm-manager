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

package sonia.scm.it.utils;

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import sonia.scm.util.IOUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Stream;

public class ScmTypes implements ArgumentsProvider {
  public static Collection<String> availableScmTypes() {
    Collection<String> params = new ArrayList<>();

    params.add("git");
    params.add("svn");

    if (IOUtil.search("hg") != null) {
      params.add("hg");
    }

    return params;
  }

  @Override
  public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
    return availableScmTypes().stream().map(Arguments::of);
  }
}
