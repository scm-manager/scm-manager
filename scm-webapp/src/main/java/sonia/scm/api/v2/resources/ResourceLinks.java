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

package sonia.scm.api.v2.resources;

import com.google.inject.OutOfScopeException;
import com.google.inject.ProvisionException;
import jakarta.inject.Inject;
import jakarta.inject.Provider;
import sonia.scm.security.gpg.UserPublicKeyResource;

import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("squid:S1192")
// string literals should not be duplicated
class ResourceLinks {

  private final Provider<ScmPathInfoStore> scmPathInfoStore;

  @Inject
  ResourceLinks(Provider<ScmPathInfoStore> scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  // we have to add the file path using URI, so that path separators (aka '/') will not be encoded as '%2F'
  private static String addPath(String sourceWithPath, String path) {
    try {
      return new URI(sourceWithPath).resolve(new URI(null, null, "./" + path, null)).toASCIIString();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  private ScmPathInfoStore accessScmPathInfoStore() {
    try {
      return scmPathInfoStore.get();
    } catch (ProvisionException e) {

      if (e.getCause() instanceof OutOfScopeException) {
        throw new OutOfRequestScopeException(e);
      }
      throw e;
    }
  }

  GroupLinks group() {
    return new GroupLinks(accessScmPathInfoStore().get());
  }

  static class GroupLinks {
    private final LinkBuilder groupLinkBuilder;

    GroupLinks(ScmPathInfo pathInfo) {
      groupLinkBuilder = new LinkBuilder(pathInfo, GroupRootResource.class, GroupResource.class);
    }

    String self(String name) {
      return groupLinkBuilder.method("getGroupResource").parameters(name).method("get").parameters().href();
    }

    String delete(String name) {
      return groupLinkBuilder.method("getGroupResource").parameters(name).method("delete").parameters().href();
    }

    String update(String name) {
      return groupLinkBuilder.method("getGroupResource").parameters(name).method("update").parameters().href();
    }
  }

  GroupCollectionLinks groupCollection() {
    return new GroupCollectionLinks(accessScmPathInfoStore().get());
  }

  static class GroupCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    GroupCollectionLinks(ScmPathInfo pathInfo) {
      collectionLinkBuilder = new LinkBuilder(pathInfo, GroupRootResource.class, GroupCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getGroupCollectionResource").parameters().method("getAll").parameters().href();
    }

    String create() {
      return collectionLinkBuilder.method("getGroupCollectionResource").parameters().method("create").parameters().href();
    }
  }

  UserLinks user() {
    return new UserLinks(accessScmPathInfoStore().get());
  }

  static class UserLinks {
    private final LinkBuilder userLinkBuilder;
    private final LinkBuilder publicKeyLinkBuilder;
    private final LinkBuilder apiKeyLinkBuilder;

    UserLinks(ScmPathInfo pathInfo) {
      userLinkBuilder = new LinkBuilder(pathInfo, UserRootResource.class, UserResource.class);
      publicKeyLinkBuilder = new LinkBuilder(pathInfo, UserPublicKeyResource.class);
      apiKeyLinkBuilder = new LinkBuilder(pathInfo, UserApiKeyResource.class);
    }

    String self(String name) {
      return userLinkBuilder.method("getUserResource").parameters(name).method("get").parameters().href();
    }

    String delete(String name) {
      return userLinkBuilder.method("getUserResource").parameters(name).method("delete").parameters().href();
    }

    String update(String name) {
      return userLinkBuilder.method("getUserResource").parameters(name).method("update").parameters().href();
    }

    public String passwordChange(String name) {
      return userLinkBuilder.method("getUserResource").parameters(name).method("overwritePassword").parameters().href();
    }

    public String toExternal(String name) {
      return userLinkBuilder.method("getUserResource").parameters(name).method("toExternal").parameters().href();
    }

    public String toInternal(String name) {
      return userLinkBuilder.method("getUserResource").parameters(name).method("toInternal").parameters().href();
    }

    public String permissionOverview(String name) {
      return userLinkBuilder.method("getUserResource").parameters(name).method("permissionOverview").parameters().href();
    }

    public String publicKeys(String name) {
      return publicKeyLinkBuilder.method("findAll").parameters(name).href();
    }

    public String apiKeys(String name) {
      return apiKeyLinkBuilder.method("findAll").parameters(name).href();
    }
  }

  interface WithPermissionLinks {
    String permissions(String name);

    String overwritePermissions(String name);
  }

  UserPermissionLinks userPermissions() {
    return new UserPermissionLinks(accessScmPathInfoStore().get());
  }

  static class UserPermissionLinks implements WithPermissionLinks {
    private final LinkBuilder userPermissionLinkBuilder;

    UserPermissionLinks(ScmPathInfo pathInfo) {
      this.userPermissionLinkBuilder = new LinkBuilder(pathInfo, UserRootResource.class, UserResource.class, UserPermissionResource.class);
    }

    public String permissions(String name) {
      return userPermissionLinkBuilder.method("getUserResource").parameters(name).method("permissions").parameters().method("getPermissions").parameters().href();
    }

    public String overwritePermissions(String name) {
      return userPermissionLinkBuilder.method("getUserResource").parameters(name).method("permissions").parameters().method("overwritePermissions").parameters().href();
    }
  }

  GroupPermissionLinks groupPermissions() {
    return new GroupPermissionLinks(accessScmPathInfoStore().get());
  }

  static class GroupPermissionLinks implements WithPermissionLinks {
    private final LinkBuilder groupPermissionLinkBuilder;

    GroupPermissionLinks(ScmPathInfo pathInfo) {
      this.groupPermissionLinkBuilder = new LinkBuilder(pathInfo, GroupRootResource.class, GroupResource.class, GroupPermissionResource.class);
    }

    public String permissions(String name) {
      return groupPermissionLinkBuilder.method("getGroupResource").parameters(name).method("permissions").parameters().method("getPermissions").parameters().href();
    }

    public String overwritePermissions(String name) {
      return groupPermissionLinkBuilder.method("getGroupResource").parameters(name).method("permissions").parameters().method("overwritePermissions").parameters().href();
    }
  }

  MeLinks me() {
    return new MeLinks(accessScmPathInfoStore().get(), this.user());
  }

  static class MeLinks {
    private final LinkBuilder meLinkBuilder;
    private final LinkBuilder notificationLinkBuilder;
    private UserLinks userLinks;

    MeLinks(ScmPathInfo pathInfo, UserLinks user) {
      meLinkBuilder = new LinkBuilder(pathInfo, MeResource.class);
      notificationLinkBuilder = new LinkBuilder(pathInfo, MeResource.class, NotificationResource.class);
      userLinks = user;
    }

    String self() {
      return meLinkBuilder.method("get").parameters().href();
    }

    String delete(String name) {
      return userLinks.delete(name);
    }

    String update(String name) {
      return userLinks.update(name);
    }

    public String passwordChange() {
      return meLinkBuilder.method("changePassword").parameters().href();
    }

    String notifications() {
      return notificationLinkBuilder.method("notifications").parameters().method("getAll").parameters().href();
    }
  }

  public ApiKeyCollectionLinks apiKeyCollection() {
    return new ApiKeyCollectionLinks(accessScmPathInfoStore().get());
  }

  static class ApiKeyCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    ApiKeyCollectionLinks(ScmPathInfo pathInfo) {
      this.collectionLinkBuilder = new LinkBuilder(pathInfo, UserRootResource.class, UserApiKeyResource.class);
    }

    String self(String username) {
      return collectionLinkBuilder.method("apiKeys").parameters().method("findAll").parameters(username).href();
    }

    String create(String username) {
      return collectionLinkBuilder.method("apiKeys").parameters().method("create").parameters(username).href();
    }
  }

  public ApiKeyLinks apiKey() {
    return new ApiKeyLinks(accessScmPathInfoStore().get());
  }

  static class ApiKeyLinks {
    private final LinkBuilder apiKeyLinkBuilder;

    ApiKeyLinks(ScmPathInfo pathInfo) {
      this.apiKeyLinkBuilder = new LinkBuilder(pathInfo, UserRootResource.class, UserApiKeyResource.class);
    }

    String self(String id, String name) {
      return apiKeyLinkBuilder.method("apiKeys").parameters(name).method("get").parameters(id).href();
    }

    String delete(String id, String name) {
      return apiKeyLinkBuilder.method("apiKeys").parameters(name).method("delete").parameters(id).href();
    }
  }

  UserCollectionLinks userCollection() {
    return new UserCollectionLinks(accessScmPathInfoStore().get());
  }

  static class UserCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    UserCollectionLinks(ScmPathInfo pathInfo) {
      collectionLinkBuilder = new LinkBuilder(pathInfo, UserRootResource.class, UserCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getUserCollectionResource").parameters().method("getAll").parameters().href();
    }

    String create() {
      return collectionLinkBuilder.method("getUserCollectionResource").parameters().method("create").parameters().href();
    }
  }

  AutoCompleteLinks autoComplete() {
    return new AutoCompleteLinks(accessScmPathInfoStore().get());
  }

  static class AutoCompleteLinks {
    private final LinkBuilder linkBuilder;

    AutoCompleteLinks(ScmPathInfo pathInfo) {
      linkBuilder = new LinkBuilder(pathInfo, AutoCompleteResource.class);
    }

    String users() {
      return linkBuilder.method("searchUser").parameters().href();
    }

    String groups() {
      return linkBuilder.method("searchGroup").parameters().href();
    }

    String namespaces() {
      return linkBuilder.method("searchNamespace").parameters().href();
    }
  }

  ConfigLinks config() {
    return new ConfigLinks(accessScmPathInfoStore().get());
  }

  static class ConfigLinks {
    private final LinkBuilder configLinkBuilder;

    ConfigLinks(ScmPathInfo pathInfo) {
      configLinkBuilder = new LinkBuilder(pathInfo, ConfigResource.class);
    }

    String self() {
      return configLinkBuilder.method("get").parameters().href();
    }

    String update() {
      return configLinkBuilder.method("update").parameters().href();
    }
  }

  InvalidationLinks invalidationLinks() {
    return new InvalidationLinks(accessScmPathInfoStore().get());
  }

  static class InvalidationLinks {
    private final LinkBuilder invalidationLinkBuilder;

    InvalidationLinks(ScmPathInfo pathInfo) {
      this.invalidationLinkBuilder = new LinkBuilder(pathInfo, InvalidationResource.class);
    }

    String caches() {
      return invalidationLinkBuilder.method("invalidateCaches").parameters().href();
    }


    String searchIndex() {
      return invalidationLinkBuilder.method("invalidateSearchIndex").parameters().href();
    }
  }

  AdminInfoLinks adminInfo() {
    return new AdminInfoLinks(accessScmPathInfoStore().get());
  }

  static class AdminInfoLinks {
    private final LinkBuilder adminInfoLinkBuilder;

    AdminInfoLinks(ScmPathInfo pathInfo) {
      adminInfoLinkBuilder = new LinkBuilder(pathInfo, AdminInfoResource.class);
    }

    String updateInfo() {
      return adminInfoLinkBuilder.method("getUpdateInfo").parameters().href();
    }
  }

  public RepositoryLinks repository() {
    return new RepositoryLinks(accessScmPathInfoStore().get());
  }

  static class RepositoryLinks {
    private final LinkBuilder repositoryLinkBuilder;
    private final LinkBuilder repositoryImportLinkBuilder;
    private final LinkBuilder repositoryExportLinkBuilder;
    private final LinkBuilder repositoryPathsLinkBuilder;

    RepositoryLinks(ScmPathInfo pathInfo) {
      repositoryLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class);
      repositoryImportLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryImportResource.class);
      repositoryExportLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, RepositoryExportResource.class);
      repositoryPathsLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, RepositoryPathsResource.class);
    }

    String self(String namespace, String name) {
      return repositoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("get").parameters().href();
    }

    String delete(String namespace, String name) {
      return repositoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("delete").parameters().href();
    }

    String update(String namespace, String name) {
      return repositoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("update").parameters().href();
    }

    String rename(String namespace, String name) {
      return repositoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("rename").parameters().href();
    }

    String importFromUrl(String type) {
      return repositoryImportLinkBuilder.method("getRepositoryImportResource").parameters().method("importFromUrl").parameters(type).href();
    }

    String importFromBundle(String type) {
      return repositoryImportLinkBuilder.method("getRepositoryImportResource").parameters().method("importFromBundle").parameters(type).href();
    }

    String fullImport(String type) {
      return repositoryImportLinkBuilder.method("getRepositoryImportResource").parameters().method("importFullRepository").parameters(type).href();
    }

    String importLog(String importLogId) {
      return repositoryImportLinkBuilder.method("getRepositoryImportResource").parameters().method("getImportLog").parameters(importLogId).href();
    }

    String archive(String namespace, String name) {
      return repositoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("archive").parameters().href();
    }

    String unarchive(String namespace, String name) {
      return repositoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("unarchive").parameters().href();
    }

    String export(String namespace, String name, String type) {
      return repositoryExportLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("export").parameters().method("exportRepository").parameters(type).href();
    }

    String fullExport(String namespace, String name, String type) {
      return repositoryExportLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("export").parameters().method("exportFullRepository").parameters(type).href();
    }

    String downloadExport(String namespace, String name) {
      return repositoryExportLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("export").parameters().method("downloadExport").parameters().href();
    }

    String exportInfo(String namespace, String name) {
      return repositoryExportLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("export").parameters().method("getExportInformation").parameters().href();
    }

    String paths(String namespace, String name) {
      return repositoryPathsLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("paths").parameters().method("collect").parameters("_REVISION_").href().replace("_REVISION_", "{revision}");
    }

    String runHealthCheck(String namespace, String name) {
      return repositoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("runHealthCheck").parameters().href();
    }

    String reindex(String namespace, String name) {
      return repositoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("reindex").parameters().href();
    }
  }

  RepositoryCollectionLinks repositoryCollection() {
    return new RepositoryCollectionLinks(accessScmPathInfoStore().get());
  }

  static class RepositoryCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    RepositoryCollectionLinks(ScmPathInfo pathInfo) {
      collectionLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getRepositoryCollectionResource").parameters().method("getAll").parameters().href();
    }

    String forNamespace(String namespace) {
      return collectionLinkBuilder.method("getRepositoryCollectionResource").parameters().method("getByNamespace").parameters(namespace).href();
    }

    String create() {
      return collectionLinkBuilder.method("getRepositoryCollectionResource").parameters().method("create").parameters().href();
    }
  }

  public NamespaceStrategiesLinks namespaceStrategies() {
    return new NamespaceStrategiesLinks(accessScmPathInfoStore().get());
  }

  static class NamespaceStrategiesLinks {

    private final LinkBuilder namespaceStrategiesLinkBuilder;

    NamespaceStrategiesLinks(ScmPathInfo pathInfo) {
      namespaceStrategiesLinkBuilder = new LinkBuilder(pathInfo, NamespaceStrategyResource.class);
    }

    String self() {
      return namespaceStrategiesLinkBuilder.method("get").parameters().href();
    }
  }

  public RepositoryTypeLinks repositoryType() {
    return new RepositoryTypeLinks(accessScmPathInfoStore().get());
  }

  static class RepositoryTypeLinks {
    private final LinkBuilder repositoryTypeLinkBuilder;

    RepositoryTypeLinks(ScmPathInfo pathInfo) {
      repositoryTypeLinkBuilder = new LinkBuilder(pathInfo, RepositoryTypeRootResource.class, RepositoryTypeResource.class);
    }

    String self(String name) {
      return repositoryTypeLinkBuilder.method("getRepositoryTypeResource").parameters(name).method("get").parameters().href();
    }
  }

  public RepositoryTypeCollectionLinks repositoryTypeCollection() {
    return new RepositoryTypeCollectionLinks(accessScmPathInfoStore().get());
  }

  static class RepositoryTypeCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    RepositoryTypeCollectionLinks(ScmPathInfo pathInfo) {
      collectionLinkBuilder = new LinkBuilder(pathInfo, RepositoryTypeRootResource.class, RepositoryTypeCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getRepositoryTypeCollectionResource").parameters().method("getAll").parameters().href();
    }
  }


  public TagCollectionLinks tag() {
    return new TagCollectionLinks(accessScmPathInfoStore().get());
  }

  static class TagCollectionLinks {
    private final LinkBuilder tagLinkBuilder;

    TagCollectionLinks(ScmPathInfo pathInfo) {
      tagLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, TagRootResource.class);
    }

    String self(String namespace, String name, String tagName) {
      return tagLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("tags").parameters().method("get").parameters(tagName).href();
    }

    String delete(String namespace, String name, String tagName) {
      return tagLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("tags").parameters().method("delete").parameters(tagName).href();
    }

    String create(String namespace, String name) {
      return tagLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("tags").parameters().method("create").parameters().href();
    }

    String all(String namespace, String name) {
      return tagLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("tags").parameters().method("getAll").parameters().href();
    }
  }

  public DiffLinks diff() {
    return new DiffLinks(accessScmPathInfoStore().get());
  }

  static class DiffLinks {
    private final LinkBuilder diffLinkBuilder;

    DiffLinks(ScmPathInfo pathInfo) {
      diffLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, DiffRootResource.class);
    }

    String self(String namespace, String name, String id) {
      return diffLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("diff").parameters().method("get").parameters(id).href();
    }

    String parsed(String namespace, String name, String id) {
      return diffLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("diff").parameters().method("getParsed").parameters(id).href();
    }

    String all(String namespace, String name) {
      return diffLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("diff").parameters().method("getAll").parameters().href();
    }
  }

  public BranchLinks branch() {
    return new BranchLinks(accessScmPathInfoStore().get());
  }

  static class BranchLinks {
    private final LinkBuilder branchLinkBuilder;

    BranchLinks(ScmPathInfo pathInfo) {
      branchLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, BranchRootResource.class);
    }

    String self(String namespace, String name, String branch) {
      return branchLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("branches").parameters().method("get").parameters(branch).href();
    }

    public String history(String namespace, String name, String branch) {
      return branchLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("branches").parameters().method("history").parameters(branch).href();
    }

    public String create(String namespace, String name) {
      return branchLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("branches").parameters().method("create").parameters().href();
    }

    public String delete(String namespace, String name, String branch) {
      return branchLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("branches").parameters().method("delete").parameters(branch).href();
    }
  }

  public BranchDetailsLinks branchDetails() {
    return new BranchDetailsLinks(accessScmPathInfoStore().get());
  }

  static class BranchDetailsLinks {
    private final LinkBuilder branchDetailsLinkBuilder;

    BranchDetailsLinks(ScmPathInfo pathInfo) {
      branchDetailsLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, BranchDetailsResource.class);
    }

    String self(String namespace, String name, String branch) {
      return branchDetailsLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("branchDetails").parameters().method("getBranchDetails").parameters(branch).href();
    }
  }

  public BranchDetailsCollectionLinks branchDetailsCollection() {
    return new BranchDetailsCollectionLinks(accessScmPathInfoStore().get());
  }

  static class BranchDetailsCollectionLinks {
    private final LinkBuilder branchDetailsLinkBuilder;

    BranchDetailsCollectionLinks(ScmPathInfo pathInfo) {
      branchDetailsLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, BranchDetailsResource.class);
    }

    String self(String namespace, String name) {
      return branchDetailsLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("branchDetails").parameters().method("getBranchDetailsCollection").parameters().href();
    }
  }

  public IncomingLinks incoming() {
    return new IncomingLinks(accessScmPathInfoStore().get());
  }

  static class IncomingLinks {
    private final LinkBuilder incomingLinkBuilder;

    private static final String SOURCE_PLACEHOLDER = "sourcebranchplaceholderfortemplating";
    private static final String TARGET_PLACEHOLDER = "targetbranchplaceholderfortemplating";

    IncomingLinks(ScmPathInfo pathInfo) {
      incomingLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, IncomingRootResource.class);
    }

    public String changesets(String namespace, String name) {
      return toTemplateParams(incomingLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("incoming").parameters().method("incomingChangesets").parameters(SOURCE_PLACEHOLDER, TARGET_PLACEHOLDER).href());
    }

    public String changesets(String namespace, String name, String source, String target) {
      return incomingLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("incoming").parameters().method("incomingChangesets").parameters(source, target).href();
    }

    public String diff(String namespace, String name) {
      return toTemplateParams(incomingLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("incoming").parameters().method("incomingDiff").parameters(SOURCE_PLACEHOLDER, TARGET_PLACEHOLDER).href());
    }

    public String diffParsed(String namespace, String name) {
      return toTemplateParams(diffParsed(namespace, name, SOURCE_PLACEHOLDER, TARGET_PLACEHOLDER));
    }

    public String diffParsed(String namespace, String name, String source, String target) {
      return incomingLinkBuilder
        .method("getRepositoryResource")
        .parameters(namespace, name)
        .method("incoming")
        .parameters()
        .method("incomingDiffParsed")
        .parameters(source, target)
        .href();
    }

    public String toTemplateParams(String href) {
      return href.replace(SOURCE_PLACEHOLDER, "{source}").replace(TARGET_PLACEHOLDER, "{target}");
    }
  }

  public BranchCollectionLinks branchCollection() {
    return new BranchCollectionLinks(accessScmPathInfoStore().get());
  }

  static class BranchCollectionLinks {
    private final LinkBuilder branchLinkBuilder;

    BranchCollectionLinks(ScmPathInfo pathInfo) {
      branchLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, BranchRootResource.class);
    }

    String self(String namespace, String name) {
      return branchLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("branches").parameters().method("getAll").parameters().href();
    }
  }

  public ChangesetLinks changeset() {
    return new ChangesetLinks(accessScmPathInfoStore().get());
  }

  static class ChangesetLinks {
    private final LinkBuilder changesetLinkBuilder;

    ChangesetLinks(ScmPathInfo pathInfo) {
      changesetLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, ChangesetRootResource.class);
    }

    String self(String namespace, String name, String changesetId) {
      return changesetLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("changesets").parameters().method("get").parameters(changesetId).href();
    }

    String all(String namespace, String name) {
      return changesetLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("changesets").parameters().method("getAll").parameters().href();
    }

    public String changeset(String namespace, String name, String revision) {
      return changesetLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("changesets").parameters().method("get").parameters(revision).href();
    }
  }

  public ModificationsLinks modifications() {
    return new ModificationsLinks(accessScmPathInfoStore().get());
  }

  static class ModificationsLinks {
    private final LinkBuilder modificationsLinkBuilder;

    ModificationsLinks(ScmPathInfo pathInfo) {
      modificationsLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, ModificationsRootResource.class);
    }

    String self(String namespace, String name, String revision) {
      return modificationsLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("modifications").parameters().method("get").parameters(revision).href();
    }
  }

  public FileHistoryLinks fileHistory() {
    return new FileHistoryLinks(accessScmPathInfoStore().get());
  }

  static class FileHistoryLinks {
    private final LinkBuilder fileHistoryLinkBuilder;

    FileHistoryLinks(ScmPathInfo pathInfo) {
      fileHistoryLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, FileHistoryRootResource.class);
    }

    String self(String namespace, String name, String changesetId, String path) {
      return addPath(fileHistoryLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("history").parameters().method("getAll").parameters(changesetId, "").href(), path);
    }

  }

  public SourceLinks source() {
    return new SourceLinks(accessScmPathInfoStore().get());
  }

  static class SourceLinks {
    private final LinkBuilder sourceLinkBuilder;

    SourceLinks(ScmPathInfo pathInfo) {
      sourceLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, SourceRootResource.class);
    }

    String self(String namespace, String name, String revision) {
      return sourceLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("sources").parameters().method("getAll").parameters(revision).href();
    }

    String selfWithoutRevision(String namespace, String name) {
      return sourceLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("sources").parameters().method("getAllWithoutRevision").parameters().href();
    }

    public String source(String namespace, String name, String revision) {
      return sourceLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("sources").parameters().method("get").parameters(revision).href();
    }

    public String sourceWithPath(String namespace, String name, String revision, String path) {
      return addPath(sourceLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("sources").parameters().method("get").parameters(revision, "").href(), path);
    }

    public String content(String namespace, String name, String revision, String path) {
      return addPath(sourceLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("content").parameters().method("get").parameters(revision, "").href(), path);
    }

    public String content(String namespace, String name) {
      return content(namespace, name, "_REVISION_", "_PATH_")
        .replace("_REVISION_", "{revision}")
        .replace("_PATH_", "{path}");
    }
  }

  public AnnotateLinks annotate() {
    return new AnnotateLinks(accessScmPathInfoStore().get());
  }

  static class AnnotateLinks {
    private final LinkBuilder annotateLinkBuilder;

    AnnotateLinks(ScmPathInfo pathInfo) {
      this.annotateLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, AnnotateResource.class);
    }

    String self(String namespace, String name, String revision, String path) {
      return annotateLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("annotate").parameters().method("annotate").parameters(revision, path).href();
    }
  }

  RepositoryVerbLinks repositoryVerbs() {
    return new RepositoryVerbLinks(accessScmPathInfoStore().get());
  }

  static class RepositoryVerbLinks {
    private final LinkBuilder repositoryVerbLinkBuilder;

    RepositoryVerbLinks(ScmPathInfo pathInfo) {
      repositoryVerbLinkBuilder = new LinkBuilder(pathInfo, RepositoryVerbResource.class);
    }

    String self() {
      return repositoryVerbLinkBuilder.method("getAll").parameters().href();
    }
  }

  RepositoryRoleLinks repositoryRole() {
    return new RepositoryRoleLinks(accessScmPathInfoStore().get());
  }

  static class RepositoryRoleLinks {
    private final LinkBuilder repositoryRoleLinkBuilder;

    RepositoryRoleLinks(ScmPathInfo pathInfo) {
      repositoryRoleLinkBuilder = new LinkBuilder(pathInfo, RepositoryRoleRootResource.class, RepositoryRoleResource.class);
    }

    String self(String name) {
      return repositoryRoleLinkBuilder.method("getRepositoryRoleResource").parameters(name).method("get").parameters().href();
    }

    String delete(String name) {
      return repositoryRoleLinkBuilder.method("getRepositoryRoleResource").parameters(name).method("delete").parameters().href();
    }

    String update(String name) {
      return repositoryRoleLinkBuilder.method("getRepositoryRoleResource").parameters(name).method("update").parameters().href();
    }
  }

  RepositoryRoleCollectionLinks repositoryRoleCollection() {
    return new RepositoryRoleCollectionLinks(accessScmPathInfoStore().get());
  }

  static class RepositoryRoleCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    RepositoryRoleCollectionLinks(ScmPathInfo pathInfo) {
      collectionLinkBuilder = new LinkBuilder(pathInfo, RepositoryRoleRootResource.class, RepositoryRoleCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getRepositoryRoleCollectionResource").parameters().method("getAll").parameters().href();
    }

    String create() {
      return collectionLinkBuilder.method("getRepositoryRoleCollectionResource").parameters().method("create").parameters().href();
    }
  }

  public RepositoryPermissionLinks repositoryPermission() {
    return new RepositoryPermissionLinks(accessScmPathInfoStore().get());
  }

  static class RepositoryPermissionLinks {
    private final LinkBuilder permissionLinkBuilder;

    RepositoryPermissionLinks(ScmPathInfo pathInfo) {
      permissionLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, RepositoryPermissionRootResource.class);
    }

    String all(String namespace, String name) {
      return permissionLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("permissions").parameters().method("getAll").parameters().href();
    }

    String create(String repositoryNamespace, String repositoryName) {
      return permissionLinkBuilder.method("getRepositoryResource").parameters(repositoryNamespace, repositoryName).method("permissions").parameters().method("create").parameters().href();
    }

    String self(String repositoryNamespace, String repositoryName, String permissionName) {
      return getLink(repositoryNamespace, repositoryName, permissionName, "get");
    }

    String update(String repositoryNamespace, String repositoryName, String permissionName) {
      return getLink(repositoryNamespace, repositoryName, permissionName, "update");
    }

    String delete(String repositoryNamespace, String repositoryName, String permissionName) {
      return getLink(repositoryNamespace, repositoryName, permissionName, "delete");
    }

    private String getLink(String repositoryNamespace, String repositoryName, String permissionName, String methodName) {
      return permissionLinkBuilder.method("getRepositoryResource").parameters(repositoryNamespace, repositoryName).method("permissions").parameters().method(methodName).parameters(permissionName).href();
    }
  }

  public UIPluginLinks uiPlugin() {
    return new UIPluginLinks(accessScmPathInfoStore().get());
  }

  static class UIPluginLinks {
    private final LinkBuilder uiPluginLinkBuilder;

    UIPluginLinks(ScmPathInfo pathInfo) {
      uiPluginLinkBuilder = new LinkBuilder(pathInfo, UIRootResource.class, UIPluginResource.class);
    }

    String self(String id) {
      return uiPluginLinkBuilder.method("plugins").parameters().method("getInstalledPlugin").parameters(id).href();
    }
  }

  public UIPluginCollectionLinks uiPluginCollection() {
    return new UIPluginCollectionLinks(accessScmPathInfoStore().get());
  }

  static class UIPluginCollectionLinks {
    private final LinkBuilder uiPluginCollectionLinkBuilder;

    UIPluginCollectionLinks(ScmPathInfo pathInfo) {
      uiPluginCollectionLinkBuilder = new LinkBuilder(pathInfo, UIRootResource.class, UIPluginResource.class);
    }

    String self() {
      return uiPluginCollectionLinkBuilder.method("plugins").parameters().method("getInstalledPlugins").parameters().href();
    }
  }

  public InstalledPluginLinks installedPlugin() {
    return new InstalledPluginLinks(accessScmPathInfoStore().get());
  }

  static class InstalledPluginLinks {
    private final LinkBuilder installedPluginLinkBuilder;

    InstalledPluginLinks(ScmPathInfo pathInfo) {
      installedPluginLinkBuilder = new LinkBuilder(pathInfo, PluginRootResource.class, InstalledPluginResource.class);
    }

    String self(String id) {
      return installedPluginLinkBuilder.method("installedPlugins").parameters().method("getInstalledPlugin").parameters(id).href();
    }

    public String uninstall(String name) {
      return installedPluginLinkBuilder.method("installedPlugins").parameters().method("uninstallPlugin").parameters(name).href();
    }
  }

  public InstalledPluginCollectionLinks installedPluginCollection() {
    return new InstalledPluginCollectionLinks(accessScmPathInfoStore().get());
  }

  static class InstalledPluginCollectionLinks {
    private final LinkBuilder installedPluginCollectionLinkBuilder;

    InstalledPluginCollectionLinks(ScmPathInfo pathInfo) {
      installedPluginCollectionLinkBuilder = new LinkBuilder(pathInfo, PluginRootResource.class, InstalledPluginResource.class);
    }

    String self() {
      return installedPluginCollectionLinkBuilder.method("installedPlugins").parameters().method("getInstalledPlugins").parameters().href();
    }

    String update() {
      return installedPluginCollectionLinkBuilder.method("installedPlugins").parameters().method("updateAll").parameters().href();
    }
  }

  public AvailablePluginLinks availablePlugin() {
    return new AvailablePluginLinks(accessScmPathInfoStore().get());
  }

  static class AvailablePluginLinks {
    private final LinkBuilder availablePluginLinkBuilder;

    AvailablePluginLinks(ScmPathInfo pathInfo) {
      availablePluginLinkBuilder = new LinkBuilder(pathInfo, PluginRootResource.class, AvailablePluginResource.class);
    }

    String self(String name) {
      return availablePluginLinkBuilder.method("availablePlugins").parameters().method("getAvailablePlugin").parameters(name).href();
    }

    String install(String name) {
      return availablePluginLinkBuilder.method("availablePlugins").parameters().method("installPlugin").parameters(name).href();
    }
  }

  public AvailablePluginCollectionLinks availablePluginCollection() {
    return new AvailablePluginCollectionLinks(accessScmPathInfoStore().get());
  }

  static class AvailablePluginCollectionLinks {
    private final LinkBuilder availablePluginCollectionLinkBuilder;

    AvailablePluginCollectionLinks(ScmPathInfo pathInfo) {
      availablePluginCollectionLinkBuilder = new LinkBuilder(pathInfo, PluginRootResource.class, AvailablePluginResource.class);
    }

    String self() {
      return availablePluginCollectionLinkBuilder.method("availablePlugins").parameters().method("getAvailablePlugins").parameters().href();
    }
  }

  public PendingPluginCollectionLinks pendingPluginCollection() {
    return new PendingPluginCollectionLinks(accessScmPathInfoStore().get());
  }

  static class PendingPluginCollectionLinks {
    private final LinkBuilder pendingPluginCollectionLinkBuilder;

    PendingPluginCollectionLinks(ScmPathInfo pathInfo) {
      pendingPluginCollectionLinkBuilder = new LinkBuilder(pathInfo, PluginRootResource.class, PendingPluginResource.class);
    }

    String executePending() {
      return pendingPluginCollectionLinkBuilder.method("pendingPlugins").parameters().method("executePending").parameters().href();
    }

    String cancelPending() {
      return pendingPluginCollectionLinkBuilder.method("pendingPlugins").parameters().method("cancelPending").parameters().href();
    }

    String self() {
      return pendingPluginCollectionLinkBuilder.method("pendingPlugins").parameters().method("getPending").parameters().href();
    }
  }

  public AuthenticationLinks authentication() {
    return new AuthenticationLinks(accessScmPathInfoStore().get());
  }

  static class AuthenticationLinks {
    private final LinkBuilder loginLinkBuilder;

    AuthenticationLinks(ScmPathInfo pathInfo) {
      this.loginLinkBuilder = new LinkBuilder(pathInfo, AuthenticationResource.class);
    }

    String formLogin() {
      return loginLinkBuilder.method("authenticateViaForm").parameters().href();
    }

    String jsonLogin() {
      return loginLinkBuilder.method("authenticateViaJSONBody").parameters().href();
    }

    String logout() {
      return loginLinkBuilder.method("logout").parameters().href();
    }
  }

  public IndexLinks index() {
    return new IndexLinks(accessScmPathInfoStore().get());
  }

  static class IndexLinks {
    private final LinkBuilder indexLinkBuilder;

    IndexLinks(ScmPathInfo pathInfo) {
      indexLinkBuilder = new LinkBuilder(pathInfo, IndexResource.class);
    }

    String self() {
      return indexLinkBuilder.method("getIndex").parameters().href();
    }
  }

  public PermissionsLinks permissions() {
    return new PermissionsLinks(accessScmPathInfoStore().get());
  }

  static class PermissionsLinks {
    private final LinkBuilder permissionsLinkBuilder;

    PermissionsLinks(ScmPathInfo scmPathInfo) {
      this.permissionsLinkBuilder = new LinkBuilder(scmPathInfo, GlobalPermissionResource.class);
    }

    String self() {
      return permissionsLinkBuilder.method("getAll").parameters().href();
    }
  }

  public NamespaceCollectionLinks namespaceCollection() {
    return new NamespaceCollectionLinks(accessScmPathInfoStore().get());
  }

  static class NamespaceCollectionLinks {
    private final LinkBuilder namespaceCollectionLinkBuilder;

    NamespaceCollectionLinks(ScmPathInfo scmPathInfo) {
      this.namespaceCollectionLinkBuilder = new LinkBuilder(scmPathInfo, NamespaceRootResource.class, NamespaceCollectionResource.class);
    }

    String self() {
      return namespaceCollectionLinkBuilder.method("getNamespaceCollectionResource").parameters().method("getAll").parameters().href();
    }
  }

  public NamespaceLinks namespace() {
    return new NamespaceLinks(accessScmPathInfoStore().get());
  }

  static class NamespaceLinks {
    private final LinkBuilder namespaceLinkBuilder;

    NamespaceLinks(ScmPathInfo scmPathInfo) {
      this.namespaceLinkBuilder = new LinkBuilder(scmPathInfo, NamespaceRootResource.class, NamespaceResource.class);
    }

    String self(String namespace) {
      return namespaceLinkBuilder.method("getNamespaceResource").parameters().method("get").parameters(namespace).href();
    }

    String repositories(String namespace) {
      return namespaceLinkBuilder.method("getNamespaceResource").parameters().method("get").parameters(namespace).href();
    }
  }

  public NamespacePermissionLinks namespacePermission() {
    return new NamespacePermissionLinks(accessScmPathInfoStore().get());
  }

  static class NamespacePermissionLinks {
    private final LinkBuilder permissionLinkBuilder;

    NamespacePermissionLinks(ScmPathInfo pathInfo) {
      permissionLinkBuilder = new LinkBuilder(pathInfo, NamespaceRootResource.class, NamespaceResource.class, NamespacePermissionResource.class);
    }

    String all(String namespace) {
      return permissionLinkBuilder.method("getNamespaceResource").parameters(namespace).method("permissions").parameters().method("getAll").parameters().href();
    }

    String create(String namespace) {
      return permissionLinkBuilder.method("getNamespaceResource").parameters(namespace).method("permissions").parameters().method("create").parameters().href();
    }

    String self(String namespace, String permissionName) {
      return getLink(namespace, permissionName, "get");
    }

    String update(String namespace, String permissionName) {
      return getLink(namespace, permissionName, "update");
    }

    String delete(String namespace, String permissionName) {
      return getLink(namespace, permissionName, "delete");
    }

    private String getLink(String namespace, String permissionName, String methodName) {
      return permissionLinkBuilder.method("getNamespaceResource").parameters(namespace).method("permissions").parameters().method(methodName).parameters(permissionName).href();
    }
  }

  public MetricsLinks metrics() {
    return new MetricsLinks(new LinkBuilder(accessScmPathInfoStore().get(), MetricsResource.class));
  }

  public static class MetricsLinks {

    private final LinkBuilder metricsLinkBuilder;

    private MetricsLinks(LinkBuilder metricsLinkBuilder) {
      this.metricsLinkBuilder = metricsLinkBuilder;
    }

    public String forType(String type) {
      return metricsLinkBuilder.method("metrics").parameters(type).href();
    }
  }

  public SearchLinks search() {
    return new SearchLinks(accessScmPathInfoStore().get());
  }

  public static class SearchLinks {

    private final LinkBuilder searchLinkBuilder;

    SearchLinks(ScmPathInfo pathInfo) {
      this.searchLinkBuilder = new LinkBuilder(pathInfo, SearchResource.class, SearchResource.SearchEndpoints.class);
    }

    public String query(String type) {
      return searchLinkBuilder.method("query").parameters().method("globally").parameters(type).href();
    }

    public String queryForNamespace(String namespace, String type) {
      return searchLinkBuilder.method("query").parameters().method("forNamespace").parameters(type, namespace).href();
    }

    public String queryForRepository(String namespace, String name, String type) {
      return searchLinkBuilder.method("query").parameters().method("forRepository").parameters(namespace, name, type).href();
    }
  }

  public SearchableTypesLinks searchableTypes() {
    return new SearchableTypesLinks(accessScmPathInfoStore().get());
  }

  public static class SearchableTypesLinks {

    private final LinkBuilder searchLinkBuilder;

    SearchableTypesLinks(ScmPathInfo pathInfo) {
      this.searchLinkBuilder = new LinkBuilder(pathInfo, SearchResource.class, SearchResource.SearchableTypesEndpoints.class);
    }

    public String searchableTypes() {
      return searchLinkBuilder.method("searchableTypes").parameters().method("globally").parameters().href();
    }

    public String searchableTypesForNamespace(String namespace) {
      return searchLinkBuilder.method("searchableTypes").parameters().method("forNamespace").parameters(namespace).href();
    }

    public String searchableTypesForRepository(String namespace, String name) {
      return searchLinkBuilder.method("searchableTypes").parameters().method("forRepository").parameters(namespace, name).href();
    }
  }

  public InitialAdminAccountLinks initialAdminAccount() {
    return new InitialAdminAccountLinks(new LinkBuilder(accessScmPathInfoStore().get(), InitializationResource.class, AdminAccountStartupResource.class));
  }

  public static class InitialAdminAccountLinks {
    private final LinkBuilder initializationLinkBuilder;

    private InitialAdminAccountLinks(LinkBuilder initializationLinkBuilder) {
      this.initializationLinkBuilder = initializationLinkBuilder;
    }

    public String indexLink(String stepName) {
      return initializationLinkBuilder
        .method("step").parameters(stepName)
        .method("postAdminInitializationData").parameters()
        .href();
    }
  }

  public PluginWizardLinks pluginWizard() {
    return new PluginWizardLinks(new LinkBuilder(accessScmPathInfoStore().get(), InitializationResource.class, PluginWizardStartupResource.class));
  }

  public static class PluginWizardLinks {
    private final LinkBuilder initializationLinkBuilder;

    private PluginWizardLinks(LinkBuilder initializationLinkBuilder) {
      this.initializationLinkBuilder = initializationLinkBuilder;
    }

    public String indexLink(String stepName) {
      return initializationLinkBuilder
        .method("step").parameters(stepName)
        .method("installPluginSets").parameters()
        .href();
    }
  }

  public PluginCenterAuthLinks pluginCenterAuth() {
    return new PluginCenterAuthLinks(scmPathInfoStore.get().get());
  }

  static class PluginCenterAuthLinks {
    private final LinkBuilder indexLinkBuilder;

    PluginCenterAuthLinks(ScmPathInfo pathInfo) {
      indexLinkBuilder = new LinkBuilder(pathInfo, PluginRootResource.class, PluginCenterAuthResource.class);
    }

    String auth() {
      return indexLinkBuilder.method("authResource").parameters().method("authenticationInfo").parameters().href();
    }
  }

  public AlertsLinks alerts() {
    return new AlertsLinks(scmPathInfoStore.get().get());
  }

  static class AlertsLinks {

    private final LinkBuilder indexLinkBuilder;

    AlertsLinks(ScmPathInfo pathInfo) {
      indexLinkBuilder = new LinkBuilder(pathInfo, AlertsResource.class);
    }

    String get() {
      return indexLinkBuilder.method("getAlertsRequest").parameters().href();
    }

  }

}
