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

import sonia.scm.repository.NamespaceAndName;

import javax.inject.Inject;
import java.net.URI;
import java.net.URISyntaxException;

@SuppressWarnings("squid:S1192") // string literals should not be duplicated
class ResourceLinks {

  private final ScmPathInfoStore scmPathInfoStore;

  @Inject
  ResourceLinks(ScmPathInfoStore scmPathInfoStore) {
    this.scmPathInfoStore = scmPathInfoStore;
  }

  // we have to add the file path using URI, so that path separators (aka '/') will not be encoded as '%2F'
  private static String addPath(String sourceWithPath, String path) {
    try {
      return new URI(sourceWithPath).resolve(new URI(null, null, path, null)).toASCIIString();
    } catch (URISyntaxException e) {
      throw new RuntimeException(e);
    }
  }

  GroupLinks group() {
    return new GroupLinks(scmPathInfoStore.get());
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
    return new GroupCollectionLinks(scmPathInfoStore.get());
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
    return new UserLinks(scmPathInfoStore.get());
  }

  static class UserLinks {
    private final LinkBuilder userLinkBuilder;

    UserLinks(ScmPathInfo pathInfo) {
      userLinkBuilder = new LinkBuilder(pathInfo, UserRootResource.class, UserResource.class);
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
  }

  interface WithPermissionLinks {
    String permissions(String name);

    String overwritePermissions(String name);
  }

  UserPermissionLinks userPermissions() {
    return new UserPermissionLinks(scmPathInfoStore.get());
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
    return new GroupPermissionLinks(scmPathInfoStore.get());
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
    return new MeLinks(scmPathInfoStore.get(), this.user());
  }

  static class MeLinks {
    private final LinkBuilder meLinkBuilder;
    private UserLinks userLinks;

    MeLinks(ScmPathInfo pathInfo, UserLinks user) {
      meLinkBuilder = new LinkBuilder(pathInfo, MeResource.class);
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
  }

  UserCollectionLinks userCollection() {
    return new UserCollectionLinks(scmPathInfoStore.get());
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
    return new AutoCompleteLinks (scmPathInfoStore.get());
  }

  static class AutoCompleteLinks  {
    private final LinkBuilder linkBuilder;

    AutoCompleteLinks (ScmPathInfo pathInfo) {
      linkBuilder = new LinkBuilder(pathInfo, AutoCompleteResource.class);
    }

    String users() {
      return linkBuilder.method("searchUser").parameters().href();
    }

    String groups() {
      return linkBuilder.method("searchGroup").parameters().href();
    }
  }

  ConfigLinks config() {
    return new ConfigLinks(scmPathInfoStore.get());
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

  public RepositoryLinks repository() {
    return new RepositoryLinks(scmPathInfoStore.get());
  }

  static class RepositoryLinks {
    private final LinkBuilder repositoryLinkBuilder;

    RepositoryLinks(ScmPathInfo pathInfo) {
      repositoryLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class);
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
  }

  RepositoryCollectionLinks repositoryCollection() {
    return new RepositoryCollectionLinks(scmPathInfoStore.get());
  }

  static class RepositoryCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    RepositoryCollectionLinks(ScmPathInfo pathInfo) {
      collectionLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getRepositoryCollectionResource").parameters().method("getAll").parameters().href();
    }

    String create() {
      return collectionLinkBuilder.method("getRepositoryCollectionResource").parameters().method("create").parameters().href();
    }
  }

  public NamespaceStrategiesLinks namespaceStrategies() {
    return new NamespaceStrategiesLinks(scmPathInfoStore.get());
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
    return new RepositoryTypeLinks(scmPathInfoStore.get());
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
    return new RepositoryTypeCollectionLinks(scmPathInfoStore.get());
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
    return new TagCollectionLinks(scmPathInfoStore.get());
  }

  static class TagCollectionLinks {
    private final LinkBuilder tagLinkBuilder;

    TagCollectionLinks(ScmPathInfo pathInfo) {
      tagLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, TagRootResource.class);
    }

    String self(String namespace, String name, String tagName) {
      return tagLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("tags").parameters().method("get").parameters(tagName).href();
    }

    String all(String namespace, String name) {
      return tagLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("tags").parameters().method("getAll").parameters().href();
    }
  }

  public DiffLinks diff() {
    return new DiffLinks(scmPathInfoStore.get());
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
    return new BranchLinks(scmPathInfoStore.get());
  }

  static class BranchLinks {
    private final LinkBuilder branchLinkBuilder;

    BranchLinks(ScmPathInfo pathInfo) {
      branchLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, BranchRootResource.class);
    }

    String self(NamespaceAndName namespaceAndName, String branch) {
      return branchLinkBuilder.method("getRepositoryResource").parameters(namespaceAndName.getNamespace(), namespaceAndName.getName()).method("branches").parameters().method("get").parameters(branch).href();
    }

    public String history(NamespaceAndName namespaceAndName, String branch) {
      return branchLinkBuilder.method("getRepositoryResource").parameters(namespaceAndName.getNamespace(), namespaceAndName.getName()).method("branches").parameters().method("history").parameters(branch).href();
    }

    public String create(String namespace, String name) {
      return branchLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("branches").parameters().method("create").parameters().href();
    }
  }

  public IncomingLinks incoming() {
    return new IncomingLinks(scmPathInfoStore.get());
  }

  static class IncomingLinks {
    private final LinkBuilder incomingLinkBuilder;

    IncomingLinks(ScmPathInfo pathInfo) {
      incomingLinkBuilder = new LinkBuilder(pathInfo, RepositoryRootResource.class, RepositoryResource.class, IncomingRootResource.class);
    }

    public String changesets(String namespace, String name) {
      return toTemplateParams(incomingLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("incoming").parameters().method("incomingChangesets").parameters("source","target").href());
    }

    public String changesets(String namespace, String name, String source, String target) {
      return incomingLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("incoming").parameters().method("incomingChangesets").parameters(source,target).href();
    }

    public String diff(String namespace, String name) {
      return toTemplateParams(incomingLinkBuilder.method("getRepositoryResource").parameters(namespace, name).method("incoming").parameters().method("incomingDiff").parameters("source", "target").href());
    }

    public String diffParsed(String namespace, String name) {
      return toTemplateParams(diffParsed(namespace, name, "source", "target"));
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
      return href.replace("source", "{source}").replace("target", "{target}");
    }
  }

  public BranchCollectionLinks branchCollection() {
    return new BranchCollectionLinks(scmPathInfoStore.get());
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
    return new ChangesetLinks(scmPathInfoStore.get());
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
    return new ModificationsLinks(scmPathInfoStore.get());
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
    return new FileHistoryLinks(scmPathInfoStore.get());
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
    return new SourceLinks(scmPathInfoStore.get());
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
  }

  RepositoryVerbLinks repositoryVerbs() {
    return new RepositoryVerbLinks(scmPathInfoStore.get());
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
    return new RepositoryRoleLinks(scmPathInfoStore.get());
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
    return new RepositoryRoleCollectionLinks(scmPathInfoStore.get());
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
    return new RepositoryPermissionLinks(scmPathInfoStore.get());
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
    return new UIPluginLinks(scmPathInfoStore.get());
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
    return new UIPluginCollectionLinks(scmPathInfoStore.get());
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
    return new InstalledPluginLinks(scmPathInfoStore.get());
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
    return new InstalledPluginCollectionLinks(scmPathInfoStore.get());
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
    return new AvailablePluginLinks(scmPathInfoStore.get());
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
    return new AvailablePluginCollectionLinks(scmPathInfoStore.get());
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
    return new PendingPluginCollectionLinks(scmPathInfoStore.get());
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
    return new AuthenticationLinks(scmPathInfoStore.get());
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
    return new IndexLinks(scmPathInfoStore.get());
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
    return new PermissionsLinks(scmPathInfoStore.get());
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
}
