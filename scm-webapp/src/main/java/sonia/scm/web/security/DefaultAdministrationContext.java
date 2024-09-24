/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
