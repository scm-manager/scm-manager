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

import com.google.common.collect.Sets;
import org.junit.Before;
import org.junit.Test;

import java.util.Set;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link RepositoryMatcher}.
 * 
 * @since 1.54
 */
public class RepositoryMatcherTest {
  
  private RepositoryMatcher matcher;
  
  @Before
  public void setUp() {
    Set<RepositoryPathMatcher> pathMatchers = Sets.<RepositoryPathMatcher>newHashSet(new AbcRepositoryPathMatcher());
    this.matcher = new RepositoryMatcher(pathMatchers);
  }

  @Test
  public void testMatches() {
    assertFalse(matcher.matches(repository("hg", "scm"), "hg", "namespace/scm-test/ka"));
    assertFalse(matcher.matches(repository("git", "scm-test"), "hg", "namespace/scm-test"));
    
    assertTrue(matcher.matches(repository("hg", "scm-test"), "hg", "namespace/scm-test/ka"));
    assertTrue(matcher.matches(repository("hg", "scm-test"), "hg", "namespace/scm-test"));
  }
  
  @Test
  public void testMatchesWithCustomPathMatcher() {
    assertFalse(matcher.matches(repository("abc", "scm"), "hg", "/long/path/with/abc"));
    assertTrue(matcher.matches(repository("abc", "scm"), "abc", "/long/path/with/abc"));
  }
  
  private Repository repository(String type, String name) {
    return new Repository(type + "-" + name, type, "namespace", name);
  }
  
  private static class AbcRepositoryPathMatcher implements RepositoryPathMatcher {

    @Override
    public boolean isPathMatching(Repository repository, String path) {
      return path.endsWith("abc");
    }

    @Override
    public String getType() {
      return "abc";
    }
    
  }
  
}
