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
    
package sonia.scm.group;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.Manager;
import sonia.scm.search.Searchable;

import java.util.Collection;

//~--- JDK imports ------------------------------------------------------------

/**
 * The central class for managing {@link Group}s.
 * This class is a singleton and is available via injection.
 * 
 * @author Sebastian Sdorra
 */
public interface GroupManager
        extends Manager<Group>, Searchable<Group>
{

  /**
   * Returns a {@link Collection} of all groups assigned to the given member.
   *
   *
   * @param member - The name of the member
   *
   * @return all groups assigned to the given member
   */
  public Collection<Group> getGroupsForMember(String member);
}
