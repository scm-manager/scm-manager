# Snippet: Authentication

```java
package sonia.scm.snippets;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.SCMContextProvider;
import sonia.scm.plugin.ext.Extension;
import sonia.scm.user.User;
import sonia.scm.web.security.AuthenticationHandler;
import sonia.scm.web.security.AuthenticationResult;
import sonia.scm.web.security.AuthenticationState;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Simple {@link AuthenticationHandler} example.
 *
 * @author Sebastian Sdorra
 */
@Extension
public class SnippetAuthentication implements AuthenticationHandler
{

  /** Type of the {@link AuthenticationHandler} */
  public static final String TYPE = "snippet";

  //~--- methods --------------------------------------------------------------

  /**
   * Authenticate the user by username and password.
   *
   *
   * @param request current http request
   * @param response current http response
   * @param username username of the user
   * @param password password of the password
   *
   * @return
   */
  @Override
  public AuthenticationResult authenticate(HttpServletRequest request,
          HttpServletResponse response, String username, String password)
  {
    AuthenticationResult result = AuthenticationResult.NOT_FOUND;

    if ("scm".equals(username))
    {
      // user found
      if ("scmpwd".equals(password))
      {
        // authentication success
        // create a user object
        User user = new User(username);

        // create the authentication result
        result = new AuthenticationResult(user, AuthenticationState.SUCCESS);
      }
      else
      {
        // user found but authentication failed
        result = AuthenticationResult.FAILED;
      }
    }

    return result;
  }

  /**
   * Close database connections or something else.
   *
   *
   * @throws IOException
   */
  @Override
  public void close() throws IOException
  {

    // do nothing
  }

  /**
   * Init database connections or something else.
   *
   *
   * @param context the context of the current SCM-Manager instance.
   */
  @Override
  public void init(SCMContextProvider context)
  {

    // do nothing
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns the type of the {@link AuthenticationHandler}
   *
   *
   * @return type of the {@link AuthenticationHandler}
   */
  @Override
  public String getType()
  {
    return TYPE;
  }
}
```

[Complete source](https://bitbucket.org/sdorra/scm-code-snippets/src/tip/004-authentication)
