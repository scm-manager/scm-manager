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
