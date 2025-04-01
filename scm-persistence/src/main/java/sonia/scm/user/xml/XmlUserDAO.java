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

package sonia.scm.user.xml;


import com.google.inject.Inject;
import com.google.inject.Singleton;
import sonia.scm.store.ConfigurationStoreFactory;
import sonia.scm.user.User;
import sonia.scm.user.UserDAO;
import sonia.scm.xml.AbstractXmlDAO;


@Singleton
public class XmlUserDAO extends AbstractXmlDAO<User, XmlUserDatabase>
        implements UserDAO
{

  public static final String STORE_NAME = "users";


 
  @Inject
  public XmlUserDAO(ConfigurationStoreFactory storeFactory)
  {
    super(storeFactory
      .withType(XmlUserDatabase.class)
      .withName(STORE_NAME)
      .build());
  }



  @Override
  protected User clone(User user)
  {
    return user.clone();
  }

  
  @Override
  protected XmlUserDatabase createNewDatabase()
  {
    return new XmlUserDatabase();
  }
}
