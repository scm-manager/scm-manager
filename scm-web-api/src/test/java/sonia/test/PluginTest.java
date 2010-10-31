/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package sonia.test;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXB;
import org.junit.Test;
import sonia.scm.ConfigChangedListener;
import sonia.scm.SCMContextProvider;
import sonia.scm.User;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryException;
import sonia.scm.repository.RepositoryHandler;
import sonia.scm.repository.RepositoryType;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.web.plugin.SCMPlugin;
import sonia.scm.web.plugin.SecurityConfig;
import sonia.scm.web.security.Authenticator;

/**
 *
 * @author Sebastian Sdorra
 */
public class PluginTest {

  @Test
  public void test()
  {
    SCMPlugin plugin = new SCMPlugin();
    plugin.addHandler( DemoRepositoryHandler.class );
    plugin.addHandler(OtherRepositoryHandler.class);
    SecurityConfig config = new SecurityConfig();
    config.setAuthenticator( DemoAuthenticator.class );
    config.setEncryptionHandler( EncryptionHandler.class );
    plugin.setSecurityConfig( config );
    JAXB.marshal(plugin, new File("/tmp/test.xml"));
  }
  
  static class DemoAuthenticator implements Authenticator {

    @Override
    public User authenticate(HttpServletRequest request, HttpServletResponse response, String username, String password)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(SCMContextProvider context)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
    
  }

  static class DemoEncryptionHander implements EncryptionHandler {

    @Override
    public String encrypt(String value)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

  static class OtherRepositoryHandler implements RepositoryHandler {

    @Override
    public RepositoryType getType()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isConfigured()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void create(Repository object) throws RepositoryException, IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Repository object) throws RepositoryException, IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void modify(Repository object) throws RepositoryException, IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Repository object) throws RepositoryException, IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Repository get(String id)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Repository> getAll()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(SCMContextProvider context)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addListener(ConfigChangedListener listener)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeListener(ConfigChangedListener listener)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

  }

  static class DemoRepositoryHandler implements RepositoryHandler {

    @Override
    public RepositoryType getType()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public boolean isConfigured()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void create(Repository object) throws RepositoryException, IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void delete(Repository object) throws RepositoryException, IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void modify(Repository object) throws RepositoryException, IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void refresh(Repository object) throws RepositoryException, IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Repository get(String id)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Collection<Repository> getAll()
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void init(SCMContextProvider context)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws IOException
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void addListener(ConfigChangedListener listener)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void removeListener(ConfigChangedListener listener)
    {
      throw new UnsupportedOperationException("Not supported yet.");
    }
  }

}
