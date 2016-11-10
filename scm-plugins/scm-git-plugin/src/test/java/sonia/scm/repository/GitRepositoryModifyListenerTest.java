/**
 * Copyright (c) 2014, Sebastian Sdorra
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
import static org.junit.Assert.*;
import org.junit.Before;
import sonia.scm.HandlerEvent;

/**
 * Unit tests for {@link GitRepositoryModifyListener}.
 * 
 * @author Sebastian Sdorra
 */
public class GitRepositoryModifyListenerTest {

  private GitRepositoryModifyTestListener repositoryModifyListener;
  
  /**
   * Set up test object.
   */
  @Before
  public void setUpObjectUnderTest(){
    repositoryModifyListener = new GitRepositoryModifyTestListener();
  }

  /**
   * Tests happy path.
   */
  @Test
  public void testHandleEvent() {
    Repository old = RepositoryTestData.createHeartOfGold("git");
    old.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "master");
    Repository current = RepositoryTestData.createHeartOfGold("git");
    current.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "develop");
    
    RepositoryModificationEvent event = new RepositoryModificationEvent(current, old, HandlerEvent.MODIFY);
    repositoryModifyListener.handleEvent(event);
    
    assertNotNull(repositoryModifyListener.repository);
    assertSame(current, repositoryModifyListener.repository);
  }
  
  /**
   * Tests with new default branch.
   */
  @Test
  public void testWithNewDefaultBranch() {
    Repository old = RepositoryTestData.createHeartOfGold("git");
    Repository current = RepositoryTestData.createHeartOfGold("git");
    current.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "develop");
    
    RepositoryModificationEvent event = new RepositoryModificationEvent(current, old, HandlerEvent.MODIFY);
    repositoryModifyListener.handleEvent(event);
    
    assertNotNull(repositoryModifyListener.repository);
    assertSame(current, repositoryModifyListener.repository);
  }
  
  /**
   * Tests with non git repositories.
   */
  @Test
  public void testNonGitRepository(){
    Repository old = RepositoryTestData.createHeartOfGold("hg");
    old.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "master");
    Repository current = RepositoryTestData.createHeartOfGold("hg");
    current.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "develop");
    
    RepositoryModificationEvent event = new RepositoryModificationEvent(current, old, HandlerEvent.MODIFY);
    repositoryModifyListener.handleEvent(event);
    
    assertNull(repositoryModifyListener.repository);
  }
  
  /**
   * Tests without default branch.
   */
  @Test
  public void testWithoutDefaultBranch(){
    Repository old = RepositoryTestData.createHeartOfGold("git");
    Repository current = RepositoryTestData.createHeartOfGold("git");
    
    RepositoryModificationEvent event = new RepositoryModificationEvent(current, old, HandlerEvent.MODIFY);
    repositoryModifyListener.handleEvent(event);
    
    assertNull(repositoryModifyListener.repository);
  }
  
  /**
   * Tests with non modify event.
   */
  @Test
  public void testNonModifyEvent(){
    Repository old = RepositoryTestData.createHeartOfGold("git");
    old.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "master");
    Repository current = RepositoryTestData.createHeartOfGold("git");
    current.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "develop");
    
    RepositoryModificationEvent event = new RepositoryModificationEvent(current, old, HandlerEvent.CREATE);
    repositoryModifyListener.handleEvent(event);
    
    assertNull(repositoryModifyListener.repository);
  }
  
  /**
   * Tests with non git repositories.
   */
  @Test
  public void testNoModification(){
    Repository old = RepositoryTestData.createHeartOfGold("git");
    old.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "master");
    Repository current = RepositoryTestData.createHeartOfGold("git");
    current.setProperty(GitConstants.PROPERTY_DEFAULT_BRANCH, "master");

    RepositoryModificationEvent event = new RepositoryModificationEvent(current, old, HandlerEvent.MODIFY);
    repositoryModifyListener.handleEvent(event);
    
    assertNull(repositoryModifyListener.repository);
  }
 
  private static class GitRepositoryModifyTestListener extends GitRepositoryModifyListener {
    
    private Repository repository;
    
    @Override
    protected void sendClearRepositoryCacheEvent(Repository repository) {
      this.repository = repository;
    } 
    
  }
  

}