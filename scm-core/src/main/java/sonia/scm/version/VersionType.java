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
