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



package sonia.scm.web.security;

//~--- non-JDK imports --------------------------------------------------------

import com.google.inject.Provider;

import org.junit.Test;

import sonia.scm.AbstractTestBase;
import sonia.scm.SCMContextProvider;
import sonia.scm.cache.CacheManager;
import sonia.scm.cache.EhCacheManager;
import sonia.scm.security.MessageDigestEncryptionHandler;
import sonia.scm.user.User;
import sonia.scm.user.UserTestData;
import sonia.scm.util.MockUtil;

import static org.junit.Assert.*;

import static org.mockito.Mockito.*;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import java.util.HashSet;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Sebastian Sdorra
 */
public class ChainAuthenticationManagerTest extends AbstractTestBase
{

  /**
   * Method description
   *
   */
  @Test
  public void testAuthenticateFailed()
  {
    AuthenticationResult result = manager.authenticate(request, response,
                                    trillian.getName(), "trillian");

    assertNull(result);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthenticateNotFound()
  {
    AuthenticationResult result = manager.authenticate(request, response,
                                    "dent", "trillian");

    assertNull(result);
  }

  /**
   * Method description
   *
   */
  @Test
  public void testAuthenticateSuccess()
  {
    AuthenticationResult result = manager.authenticate(request, response,
                                    trillian.getName(), "trillian123");

    assertNotNull(result);
    assertUserEquals(trillian, result.getUser());
    assertEquals("trilliansType", result.getUser().getType());
    result = manager.authenticate(request, response, perfect.getName(),
                                  "perfect123");
    assertNotNull(perfect);
    assertUserEquals(perfect, result.getUser());
    assertEquals("perfectsType", result.getUser().getType());
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  protected void postSetUp() throws Exception
  {
    Set<AuthenticationHandler> handlerSet =
      new HashSet<AuthenticationHandler>();

    perfect = UserTestData.createPerfect();
    perfect.setPassword("perfect123");
    handlerSet.add(new SingleUserAuthenticaionHandler("perfectsType", perfect));
    trillian = UserTestData.createTrillian();
    trillian.setPassword("trillian123");
    handlerSet.add(new SingleUserAuthenticaionHandler("trilliansType",
            trillian));

    Provider<Set<AuthenticationListener>> listenerProvider =
      mock(Provider.class);

    when(listenerProvider.get()).thenReturn(
        new HashSet<AuthenticationListener>());
    manager = new ChainAuthenticatonManager(handlerSet,
            new MessageDigestEncryptionHandler(), cacheManager,
            listenerProvider);
    manager.init(contextProvider);
    request = MockUtil.getHttpServletRequest();
    response = MockUtil.getHttpServletResponse();
  }

  /**
   * Method description
   *
   *
   * @throws Exception
   */
  @Override
  protected void preTearDown() throws Exception
  {
    manager.close();
  }

  /**
   * Method description
   *
   *
   * @param user
   * @param other
   */
  private void assertUserEquals(User user, User other)
  {
    assertEquals(user.getName(), other.getName());
    assertEquals(user.getDisplayName(), other.getDisplayName());
    assertEquals(user.getMail(), other.getMail());
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Class description
   *
   *
   * @version        Enter version here..., 2010-12-07
   * @author         Sebastian Sdorra
   */
  private static class SingleUserAuthenticaionHandler
          implements AuthenticationHandler
  {

    /**
     * Constructs ...
     *
     *
     * @param type
     * @param user
     */
    public SingleUserAuthenticaionHandler(String type, User user)
    {
      this.type = type;
      this.user = user;
    }

    //~--- methods ------------------------------------------------------------

    /**
     * Method description
     *
     *
     * @param request
     * @param response
     * @param username
     * @param password
     *
     * @return
     */
    @Override
    public AuthenticationResult authenticate(HttpServletRequest request,
            HttpServletResponse response, String username, String password)
    {
      AuthenticationResult result = null;

      if (username.equals(user.getName()))
      {
        if (password.equals(user.getPassword()))
        {
          result = new AuthenticationResult(user);
        }
        else
        {
          result = AuthenticationResult.FAILED;
        }
      }
      else
      {
        result = AuthenticationResult.NOT_FOUND;
      }

      return result;
    }

    /**
     * Method description
     *
     *
     * @throws IOException
     */
    @Override
    public void close() throws IOException {}

    /**
     * Method description
     *
     *
     * @param context
     */
    @Override
    public void init(SCMContextProvider context) {}

    //~--- get methods --------------------------------------------------------

    /**
     * Method description
     *
     *
     * @return
     */
    @Override
    public String getType()
    {
      return type;
    }

    //~--- fields -------------------------------------------------------------

    /** Field description */
    private String type;

    /** Field description */
    private User user;
  }


  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private CacheManager cacheManager = new EhCacheManager();

  /** Field description */
  private ChainAuthenticatonManager manager;

  /** Field description */
  private User perfect;

  /** Field description */
  private HttpServletRequest request;

  /** Field description */
  private HttpServletResponse response;

  /** Field description */
  private User trillian;
}
