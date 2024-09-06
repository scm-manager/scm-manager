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

package sonia.scm.user;


public final class UserTestData
{

  private UserTestData() {}


  
  public static User createAdams()
  {
    return new User("adams", "Douglas Adams", "douglas.adams@hitchhiker.com");
  }

  
  public static User createDent()
  {
    return new User("dent", "Arthur Dent", "arthur.dent@hitchhiker.com");
  }

  
  public static User createMarvin()
  {
    return new User("marvin", "Marvin", "paranoid.android@hitchhiker.com");
  }

  
  public static User createPerfect()
  {
    return new User("perfect", "Ford Prefect", "ford.perfect@hitchhiker.com");
  }

  
  public static User createSlarti()
  {
    return new User("slarti", "Slartibartfaß", "slartibartfass@hitchhiker.com");
  }

  
  public static User createTrillian()
  {
    User user = new User("trillian", "Tricia McMillan", "tricia.mcmillan@hitchhiker.com");
    user.setPassword("$shiro1$trillisSecret");
    user.setType("xml");
    return user;
  }

  
  public static User createZaphod()
  {
    User user = new User("zaphod", "Zaphod Beeblebrox", "zaphod.beeblebrox@hitchhiker.com");
    user.setType("xml");
    user.setPassword("$shiro1$zaphodsSecret");
    return user;
  }
}
