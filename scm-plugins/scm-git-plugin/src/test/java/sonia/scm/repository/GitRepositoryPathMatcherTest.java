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

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link GitRepositoryPathMatcher}.
 * 
 * @since 1.54
 */
public class GitRepositoryPathMatcherTest {

  private final GitRepositoryPathMatcher pathMatcher = new GitRepositoryPathMatcher();

  @Test
  public void testIsPathMatching() {
    assertFalse(pathMatcher.isPathMatching(repository("space", "my-repo"), "my-repoo"));
    assertFalse(pathMatcher.isPathMatching(repository("space", "my"), "my-repo"));
    assertFalse(pathMatcher.isPathMatching(repository("space", "my"), "my-repo/with/path"));
    
    assertTrue(pathMatcher.isPathMatching(repository("space", "my-repo"), "my-repo"));
    assertTrue(pathMatcher.isPathMatching(repository("space", "my-repo"), "my-repo.git"));
    assertTrue(pathMatcher.isPathMatching(repository("space", "my-repo"), "my-repo/with/path"));
    assertTrue(pathMatcher.isPathMatching(repository("space", "my-repo"), "my-repo.git/with/path"));
  }
  
  private Repository repository(String namespace, String name) {
    return new Repository(name, GitRepositoryHandler.TYPE_NAME, namespace, name);
  }
  
}
