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

package sonia.scm.group.xml;


import com.google.inject.Inject;
import com.google.inject.Singleton;

import sonia.scm.group.Group;
import sonia.scm.group.GroupDAO;
import sonia.scm.xml.AbstractXmlDAO;

import sonia.scm.store.ConfigurationStoreFactory;


@Singleton
public class XmlGroupDAO extends AbstractXmlDAO<Group, XmlGroupDatabase>
        implements GroupDAO
{

  public static final String STORE_NAME = "groups";


 
  @Inject
  public XmlGroupDAO(ConfigurationStoreFactory storeFactory) {
    super(storeFactory
      .withType(XmlGroupDatabase.class)
      .withName(STORE_NAME)
      .build());
  }



  @Override
  protected Group clone(Group group)
  {
    return group.clone();
  }

  
  @Override
  protected XmlGroupDatabase createNewDatabase()
  {
    return new XmlGroupDatabase();
  }
}
