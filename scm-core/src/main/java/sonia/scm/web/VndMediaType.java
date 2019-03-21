package sonia.scm.web;

import javax.ws.rs.core.MediaType;

/**
 * Vendor media types used by SCMM.
 */
public class VndMediaType {

  private static final String VERSION = "2";
  private static final String TYPE = "application";
  private static final String SUBTYPE_PREFIX = "vnd.scmm-";
  public static final String PREFIX = TYPE + "/" + SUBTYPE_PREFIX;
  public static final String SUFFIX = "+json;v=" + VERSION;
  public static final String PLAIN_TEXT_PREFIX = "text/" + SUBTYPE_PREFIX;
  public static final String PLAIN_TEXT_SUFFIX = "+plain;v=" + VERSION;

  public static final String INDEX = PREFIX + "index" + SUFFIX;
  public static final String USER = PREFIX + "user" + SUFFIX;
  public static final String GROUP = PREFIX + "group" + SUFFIX;
  public static final String AUTOCOMPLETE =  PREFIX + "autocomplete" + SUFFIX;
  public static final String REPOSITORY = PREFIX + "repository" + SUFFIX;
  public static final String REPOSITORY_PERMISSION = PREFIX + "repositoryPermission" + SUFFIX;
  public static final String CHANGESET = PREFIX + "changeset" + SUFFIX;
  public static final String CHANGESET_COLLECTION = PREFIX + "changesetCollection" + SUFFIX;
  public static final String MODIFICATIONS = PREFIX + "modifications" + SUFFIX;
  public static final String TAG = PREFIX + "tag" + SUFFIX;
  public static final String TAG_COLLECTION = PREFIX + "tagCollection" + SUFFIX;
  public static final String BRANCH = PREFIX + "branch" + SUFFIX;
  public static final String DIFF = PLAIN_TEXT_PREFIX + "diff" + PLAIN_TEXT_SUFFIX;
  public static final String USER_COLLECTION = PREFIX + "userCollection" + SUFFIX;
  public static final String GROUP_COLLECTION = PREFIX + "groupCollection" + SUFFIX;
  public static final String REPOSITORY_COLLECTION = PREFIX + "repositoryCollection" + SUFFIX;
  public static final String BRANCH_COLLECTION = PREFIX + "branchCollection" + SUFFIX;
  public static final String CONFIG = PREFIX + "config" + SUFFIX;
  public static final String REPOSITORY_PERMISSION_COLLECTION = PREFIX + "repositoryPermissionCollection" + SUFFIX;
  public static final String REPOSITORY_TYPE_COLLECTION = PREFIX + "repositoryTypeCollection" + SUFFIX;
  public static final String REPOSITORY_TYPE = PREFIX + "repositoryType" + SUFFIX;
  public static final String UI_PLUGIN = PREFIX + "uiPlugin" + SUFFIX;
  public static final String UI_PLUGIN_COLLECTION = PREFIX + "uiPluginCollection" + SUFFIX;
  @SuppressWarnings("squid:S2068")
  public static final String PASSWORD_CHANGE = PREFIX + "passwordChange" + SUFFIX;
  @SuppressWarnings("squid:S2068")
  public static final String PASSWORD_OVERWRITE = PREFIX + "passwordOverwrite" + SUFFIX;
  public static final String PERMISSION_COLLECTION = PREFIX + "permissionCollection" + SUFFIX;
  public static final String MERGE_RESULT = PREFIX + "mergeResult" + SUFFIX;
  public static final String MERGE_COMMAND = PREFIX + "mergeCommand" + SUFFIX;

  public static final String NAMESPACE_STRATEGIES = PREFIX + "namespaceStrategies" + SUFFIX;

  public static final String ME = PREFIX + "me" + SUFFIX;
  public static final String SOURCE = PREFIX + "source" + SUFFIX;
  public static final String ERROR_TYPE = PREFIX + "error" + SUFFIX;

  private VndMediaType() {
  }

  /**
   * Checks whether the given media type is a media type used by SCMM.
   */
  public static boolean isVndType(MediaType type) {
    return type.getType().equals(TYPE) && type.getSubtype().startsWith(SUBTYPE_PREFIX);
  }
}
