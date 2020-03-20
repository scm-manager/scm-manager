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

import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.inject.Provider;
import com.google.inject.util.Providers;
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
import org.mockito.junit.MockitoJUnitRunner;
import sonia.scm.SCMContextProvider;
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
    when(repositoryHandler.getType()).thenReturn(new RepositoryType(REPOSITORY_TYPE, REPOSITORY_TYPE, Sets.newHashSet()));
    Set<RepositoryHandler> handlerSet = ImmutableSet.of(repositoryHandler);
    NamespaceStrategy namespaceStrategy = mock(NamespaceStrategy.class);
    repositoryManager = new DefaultRepositoryManager(
      configuration, 
      contextProvider, 
      keyGenerator, 
      repositoryDAO,
      handlerSet,
      Providers.of(namespaceStrategy)
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
  @Test(timeout = 6000L)
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
  Long sum = 0L;
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
    return new Repository(keyGenerator.createKey(), REPOSITORY_TYPE, "namespace", "repo-" + number);

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
      return authzCollector.collect(principals);
    }
    
  }
  
  private static class SetProvider implements Provider {

    @Override
    public Object get() {
      return Collections.emptySet();
    }
  
  }
  
}
