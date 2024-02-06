/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
