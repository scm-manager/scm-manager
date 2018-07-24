/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */
package sonia.scm.repository;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * Unit tests for {@link GitRepositoryPathMatcher}.
 * 
 * @author Sebastian Sdorra
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
