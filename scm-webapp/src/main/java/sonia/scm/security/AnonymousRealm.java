package sonia.scm.security;

import com.google.common.annotations.VisibleForTesting;
import com.google.inject.Inject;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.credential.AllowAllCredentialsMatcher;
import org.apache.shiro.realm.AuthenticatingRealm;
import sonia.scm.SCMContext;
import sonia.scm.plugin.Extension;

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

  @Inject
  public AnonymousRealm(DAORealmHelperFactory helperFactory) {
    this.helper = helperFactory.create(REALM);

    setAuthenticationTokenClass(AnonymousToken.class);
    setCredentialsMatcher(new AllowAllCredentialsMatcher());
  }

  @Override
  protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken authenticationToken) {
    checkArgument(authenticationToken instanceof AnonymousToken, "%s is required", AnonymousToken.class);
    return helper.authenticationInfoBuilder(SCMContext.USER_ANONYMOUS).build();
  }
}
