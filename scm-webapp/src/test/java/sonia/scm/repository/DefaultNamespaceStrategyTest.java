package sonia.scm.repository;

import com.github.sdorra.shiro.ShiroRule;
import com.github.sdorra.shiro.SubjectAware;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

@SubjectAware(configuration = "classpath:sonia/scm/shiro-001.ini")
public class DefaultNamespaceStrategyTest {

  @Rule
  public ShiroRule shiroRule = new ShiroRule();

  private DefaultNamespaceStrategy namespaceStrategy = new DefaultNamespaceStrategy();

  @Test
  @SubjectAware(username = "trillian", password = "secret")
  public void testNamespaceStrategy() {
    assertEquals("trillian", namespaceStrategy.getNamespace());
  }

}
