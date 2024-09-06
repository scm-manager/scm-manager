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

package sonia.scm.version;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;


public enum VersionType
{
  EARLY_ACESS("ea", 0, "early", "earlyaccess"), MILESTONE("M", 1, "milestone"),
  ALPHA("alpha", 2), BETA("beta", 3),
  RELEASE_CANDIDAT("RC", 4, "releasecandidate"), RELEASE(10);

  public String[] aliases;

  private String id;

  private int value;

  private VersionType(int value)
  {
    this(null, value);
  }

  private VersionType(String id, int value, String... aliases)
  {
    this.id = id;
    this.value = value;
    this.aliases = aliases;
  }


  
  public String[] getAliases()
  {
    return aliases;
  }

  
  public String getId()
  {
    return id;
  }

  
  public Collection<String> getNames()
  {
    List<String> names = new ArrayList<String>();

    if (id != null)
    {
      names.add(id);
    }

    if (aliases != null)
    {
      names.addAll(Arrays.asList(aliases));
    }

    return names;
  }

  
  public int getValue()
  {
    return value;
  }
}
