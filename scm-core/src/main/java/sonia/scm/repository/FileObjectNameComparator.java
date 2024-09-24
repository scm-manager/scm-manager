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

package sonia.scm.repository;


import sonia.scm.util.Util;

import java.io.Serializable;

import java.util.Comparator;

/**
 * Compare {@link FileObject}'s by its name.
 *
 */
public class FileObjectNameComparator
  implements Comparator<FileObject>, Serializable
{

  public static final FileObjectNameComparator instance =
    new FileObjectNameComparator();

  private static final long serialVersionUID = -2133224334287527874L;



  @Override
  public int compare(FileObject o1, FileObject o2)
  {
    int result;

    if (o1.isDirectory() &&!o2.isDirectory())
    {
      result = -1;
    }
    else if (!o1.isDirectory() && o2.isDirectory())
    {
      result = 1;
    }
    else
    {
      result = Util.compare(o1.getName(), o2.getName());
    }

    return result;
  }
}
