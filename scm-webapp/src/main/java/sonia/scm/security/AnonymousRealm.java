package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.ConfigurationException;
import sonia.scm.SCMContext;
import sonia.scm.plugin.Extension;
import sonia.scm.user.UserDAO;

import javax.inject.Singleton;

import static com.google.common.base.Preconditions.checkArgument;

@Singleton
@Extension
public class AnonymousRealm extends AuthenticatingRealm {

  /**
   * realm name
   */
  @VisibleForTesting
  static final String REALM = "AnonymousRealm";

  /**
   * dao realm helper
   */
  private final DAORealmHelper helper;
  private final UserDAO userDAO;

  @Inject
  public AnonymousRealm(DAORealmHelperFactory helperFactory, UserDAO userDAO) {
    this.helper = helperFactory.create(REALM);
    this.userDAO = userDAO;

    setAuthenticationTokenClass(AnonymousToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
    if (!userDAO.contains(SCMContext.USER_ANONYMOUS)) {
     throw new ConfigurationException("trying to access anonymous but _anonymous user does not exist");
    }
    checkArgument(authenticationToken instanceof AnonymousToken, "%s is required", AnonymousToken.class);
    return helper.authenticationInfoBuilder(SCMContext.USER_ANONYMOUS).build();
  }
}
