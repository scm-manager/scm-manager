/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.security.PermissionActionCheck;
import sonia.scm.security.PermissionCheck;

/**
 * Permission checks for repository related permissions.
 *
 * @author Sebastian Sdorra
 * @since 2.0.0
 */
public final class RepositoryPermissions
{

  /** create permission */
  public static final String ACTION_CREATE = "create";

  /** delete permission */
  public static final String ACTION_DELETE = "delete";

  /** modify permission */
  public static final String ACTION_MODIFY = "modify";

  /** health check permission implies modify permission */
  public static final String ACTION_HC = "hc,".concat(ACTION_MODIFY);

  /** read permission */
  public static final String ACTION_READ = "read";

  /** write permission */
  public static final String ACTION_WRITE = "write";

  /** permission separator */
  public static final String SEPARATOR = ":";
  
  /** permission action separator */
  public static final String SEPERATOR_ACTION = ",";

  /** permission main type */
  public static final String TYPE = "repository";

  //~--- constructors ---------------------------------------------------------

  private RepositoryPermissions() {}

  //~--- methods --------------------------------------------------------------

  /**
   * Returns permission check for create action.
   *
   * @return permission check for create action
   */
  public static PermissionCheck create()
  {
    return check(ACTION_CREATE);
  }

  /**
   * Returns permission check for delete action.
   *
   * @param r repository for permission check
   *
   * @return permission check for delete action
   */
  public static PermissionCheck delete(Repository r)
  {
    return delete(r.getId());
  }

  /**
   * Returns permission check for delete action.
   *
   * @param id id of repository for permission check
   *
   * @return permission check for delete action
   */
  public static PermissionCheck delete(String id)
  {
    return check(ACTION_DELETE.concat(SEPARATOR).concat(id));
  }

  /**
   * Returns permission action check for delete action.
   *
   * @return permission action check for delete action
   */
  public static PermissionActionCheck<Repository> delete()
  {
    return actionCheck(ACTION_DELETE);
  }

  /**
   * Returns permission check for health check action.
   *
   * @param id id of repository for permission check
   *
   * @return permission check for health check action
   */
  public static PermissionCheck healthCheck(String id)
  {
    return check(ACTION_HC.concat(SEPARATOR).concat(id));
  }

  /**
   * Returns permission action check for health check action.
   *
   * @return permission action check for health check action.
   */
  public static PermissionActionCheck<Repository> healthCheck()
  {
    return new PermissionActionCheck<>(
      TYPE.concat(SEPARATOR).concat(ACTION_HC));
  }

  /**
   * Returns permission check for health check action.
   *
   * @param r repository for permission check
   *
   * @return permission check for health check action
   */
  public static PermissionCheck healthCheck(Repository r)
  {
    return healthCheck(r.getId());
  }

  /**
   * Returns permission action check for modify action.
   *
   * @param r repository for permission check
   *
   * @return permission action check for modify action
   */
  public static PermissionCheck modify(Repository r)
  {
    return modify(r.getId());
  }

  /**
   * Returns permission action check for modify action.
   *
   *
   * @param id id of repository for permission check
   *
   * @return permission action check for modify action
   */
  public static PermissionCheck modify(String id)
  {
    return check(ACTION_MODIFY.concat(SEPARATOR).concat(id));
  }

  /**
   * Returns permission action check for modify action.
   *
   * @return permission action check for modify action
   */
  public static PermissionActionCheck<Repository> modify()
  {
    return actionCheck(ACTION_MODIFY);
  }

  /**
   * Returns permission check for read action.
   *
   * @param id id of repository for permission check
   *
   * @return permission check for read action
   */
  public static PermissionCheck read(String id)
  {
    return check(ACTION_READ.concat(SEPARATOR).concat(id));
  }

  /**
   * Returns permission check for read action.
   *
   * @param r repository for permission check
   *
   * @return permission check for read action
   */
  public static PermissionCheck read(Repository r)
  {
    return read(r.getId());
  }

  /**
   * Returns permission action check for read action.
   *
   * @return permission action check for read action
   */
  public static PermissionActionCheck<Repository> read()
  {
    return actionCheck(ACTION_READ);
  }

  /**
   * Returns permission check for write action.
   *
   * @param id id of repository for permission check
   *
   * @return permission check for write action
   */
  public static PermissionCheck write(String id)
  {
    return check(ACTION_WRITE.concat(SEPARATOR).concat(id));
  }

  /**
   * Returns permission check for write action.
   *
   * @param r repository for permission check
   *
   * @return permission check for write action
   */
  public static PermissionCheck write(Repository r)
  {
    return write(r.getId());
  }

  /**
   * Return permission action check for write action.
   *
   * @return permission action check for write action
   */
  public static PermissionActionCheck<Repository> write()
  {
    return actionCheck(ACTION_WRITE);
  }

  private static PermissionActionCheck<Repository> actionCheck(String action)
  {
    return new PermissionActionCheck<>(TYPE.concat(SEPARATOR).concat(action));
  }

  private static PermissionCheck check(String permission)
  {
    return new PermissionCheck(TYPE.concat(SEPARATOR).concat(permission));
  }
}
