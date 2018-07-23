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

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Provider;
import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAuthenticationInfo;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.mgt.DefaultSecurityManager;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.apache.shiro.util.ThreadContext;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import sonia.scm.SCMContextProvider;
import sonia.scm.Type;
import sonia.scm.cache.GuavaCacheManager;
import sonia.scm.config.ScmConfiguration;
import sonia.scm.security.AuthorizationCollector;
import sonia.scm.security.DefaultKeyGenerator;
import sonia.scm.security.KeyGenerator;
import sonia.scm.user.UserTestData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Performance test for {@link RepositoryManager#getAll()}.
 * 
 * @see <a href="https://goo.gl/PD1AeM">Issue 781</a>
 * @author Sebastian Sdorra
 * @since 1.52
 */
@RunWith(MockitoJUnitRunner.class)
public class DefaultRepositoryManagerPerfTest {
  
  private static final int REPOSITORY_COUNT = 2000;
  
  private static final String REPOSITORY_TYPE = "perf";
  
  @Mock
  private SCMContextProvider contextProvider;
  
  @Mock
  private RepositoryDAO repositoryDAO;
  
  private final ScmConfiguration configuration = new ScmConfiguration();
  
  private final KeyGenerator keyGenerator = new DefaultKeyGenerator();

  @Mock
  private RepositoryHandler repositoryHandler;
  
  private DefaultRepositoryManager repositoryManager;
  
  @Mock
  private AuthorizationCollector authzCollector;
  
  /**
   * Setup object under test.
   */
  @Before
  public void setUpObjectUnderTest(){
    when(repositoryHandler.getType()).thenReturn(new Type(REPOSITORY_TYPE, REPOSITORY_TYPE));
    Set<RepositoryHandler> handlerSet = ImmutableSet.of(repositoryHandler);
    RepositoryMatcher repositoryMatcher = new RepositoryMatcher(Collections.<RepositoryPathMatcher>emptySet());
    NamespaceStrategy namespaceStrategy = mock(NamespaceStrategy.class);
    repositoryManager = new DefaultRepositoryManager(
      configuration, 
      contextProvider, 
      keyGenerator, 
      repositoryDAO,
      handlerSet, 
      repositoryMatcher,
      namespaceStrategy
    );
    
    setUpTestRepositories();
    
    GuavaCacheManager cacheManager = new GuavaCacheManager();
    DefaultSecurityManager securityManager = new DefaultSecurityManager(new DummyRealm(authzCollector, cacheManager));
    
    ThreadContext.bind(securityManager);
  }

  @After
  public void tearDown(){
    ThreadContext.unbindSecurityManager();
  }
  
  /**
   * Start performance test and ensure that the timeout is not reached.
   */
  @Test(timeout = 6000l)
  public void perfTestGetAll(){
    SecurityUtils.getSubject().login(new UsernamePasswordToken("trillian", "secret"));
    
    List<Long> times = new ArrayList<>();
    for ( int i=0; i<3; i++ ) {
      times.add(benchGetAll());
    }
    
    long average = calculateAverage(times);
    double value = (double) average / TimeUnit.MILLISECONDS.convert(1, TimeUnit.SECONDS);

    // Too bad this functionality is not exposed as a regular method call
    System.out.println( String.format("%.4g s", value) );
  }
  
private long calculateAverage(List<Long> times) {
  Long sum = 0l;
  if(!times.isEmpty()) {
    for (Long time : times) {
        sum += time;
    }
    return Math.round(sum.doubleValue() / times.size());
  }
  return sum;
}
  
  private long benchGetAll(){
    Stopwatch sw = Stopwatch.createStarted();
    System.out.append("found ").append(String.valueOf(repositoryManager.getAll().size()));
    sw.stop();
    System.out.append(" in ").println(sw);
    return sw.elapsed(TimeUnit.MILLISECONDS);
  }
  
  private void setUpTestRepositories() {
    Map<String,Repository> repositories = new LinkedHashMap<>();
    for ( int i=0; i<REPOSITORY_COUNT; i++ ) {
      Repository repository = createTestRepository(i);
      repositories.put(repository.getId(), repository);
    }
    when(repositoryDAO.getAll()).thenReturn(repositories.values());
  }
  
  private Repository createTestRepository(int number) {
    Repository repository = new Repository(keyGenerator.createKey(), REPOSITORY_TYPE, "namespace", "repo-" + number);
    repository.getPermissions().add(new Permission("trillian", PermissionType.READ));
    return repository;
  }
  
  static class DummyRealm extends AuthorizingRealm {

    private final AuthorizationCollector authzCollector;

    public DummyRealm(AuthorizationCollector authzCollector, org.apache.shiro.cache.CacheManager cacheManager) {
      this.authzCollector = authzCollector;
      setCredentialsMatcher(new AllowAllCredentialsMatcher());
      setCacheManager(cacheManager);
    }
    
    @Override
    protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
      SimplePrincipalCollection spc = new SimplePrincipalCollection(token.getPrincipal(), REPOSITORY_TYPE);
      spc.add(UserTestData.createTrillian(), REPOSITORY_TYPE);
      return new SimpleAuthenticationInfo(spc, REPOSITORY_TYPE);
    }
    
    @Override
    protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
      return authzCollector.collect();
    }
    
  }
  
  private static class SetProvider implements Provider {

    @Override
    public Object get() {
      return Collections.emptySet();
    }
  
  }
  
}
