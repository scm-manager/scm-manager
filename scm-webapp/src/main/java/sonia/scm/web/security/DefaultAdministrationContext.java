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

package sonia.scm.web.security;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import org.apache.shiro.subject.PrincipalCollection;
import org.apache.shiro.subject.SimplePrincipalCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import sonia.scm.security.Authentications;
import sonia.scm.security.Impersonator;
import sonia.scm.user.User;
import sonia.scm.util.AssertUtil;


@Singleton
public class DefaultAdministrationContext implements AdministrationContext {

  private static final User SYSTEM_ACCOUNT = new User(
    Authentications.PRINCIPAL_SYSTEM,
    "SCM-Manager System Account",
    null
  );

  static final String REALM = "AdminRealm";

  private static final Logger LOG = LoggerFactory.getLogger(DefaultAdministrationContext.class);

  private final Injector injector;
  private final Impersonator impersonator;
  private final PrincipalCollection adminPrincipal;

  @Inject
  public DefaultAdministrationContext(Injector injector, Impersonator impersonator) {
    this.injector = injector;
    this.impersonator = impersonator;
    this.adminPrincipal = createAdminPrincipal();
  }

  public static PrincipalCollection createAdminPrincipal() {
    SimplePrincipalCollection collection = new SimplePrincipalCollection();

    collection.add(SYSTEM_ACCOUNT.getId(), REALM);
    collection.add(SYSTEM_ACCOUNT, REALM);
    collection.add(AdministrationContextMarker.MARKER, REALM);

    return collection;
  }

  @Override
  public void runAsAdmin(PrivilegedAction action) {
    AssertUtil.assertIsNotNull(action);
    LOG.debug("execute action {} in administration context", action.getClass().getName());
    try (Impersonator.Session session = impersonator.impersonate(adminPrincipal)) {
      action.run();
    }
  }

  @Override
  public void runAsAdmin(Class<? extends PrivilegedAction> actionClass) {
    PrivilegedAction action = injector.getInstance(actionClass);
    runAsAdmin(action);
  }

}
