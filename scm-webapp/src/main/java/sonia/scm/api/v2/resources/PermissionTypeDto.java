package sonia.scm.api.v2.resources;

/**
 * Type of permissionPrefix for a {@link RepositoryDto}.
 *
 * @author mkarray
 */

public enum PermissionTypeDto {

  /**
   * read permission
   */
  READ,

  /**
   * read and write permission
   */
  WRITE,

  /**
   * read, write and manage the properties and permissions
   */
  OWNER

}
