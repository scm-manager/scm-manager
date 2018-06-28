package sonia.scm.api.v2.resources;

import javax.inject.Inject;
import javax.ws.rs.core.UriInfo;

class ResourceLinks {

  private final UriInfoStore uriInfoStore;

  @Inject
  ResourceLinks(UriInfoStore uriInfoStore) {
    this.uriInfoStore = uriInfoStore;
  }


  GroupLinks group() {
    return new GroupLinks(uriInfoStore.get());
  }

  static class GroupLinks {
    private final LinkBuilder groupLinkBuilder;

    private GroupLinks(UriInfo uriInfo) {
      groupLinkBuilder = new LinkBuilder(uriInfo, GroupRootResource.class, GroupResource.class);
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
    return new GroupCollectionLinks(uriInfoStore.get());
  }

  static class GroupCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    private GroupCollectionLinks(UriInfo uriInfo) {
      collectionLinkBuilder = new LinkBuilder(uriInfo, GroupRootResource.class, GroupCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getGroupCollectionResource").parameters().method("getAll").parameters().href();
    }

    String create() {
      return collectionLinkBuilder.method("getGroupCollectionResource").parameters().method("create").parameters().href();
    }
  }

  UserLinks user() {
    return new UserLinks(uriInfoStore.get());
  }

  static class UserLinks {
    private final LinkBuilder userLinkBuilder;

    private UserLinks(UriInfo uriInfo) {
      userLinkBuilder = new LinkBuilder(uriInfo, UserRootResource.class, UserResource.class);
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
  }

  UserCollectionLinks userCollection() {
    return new UserCollectionLinks(uriInfoStore.get());
  }

  static class UserCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    private UserCollectionLinks(UriInfo uriInfo) {
      collectionLinkBuilder = new LinkBuilder(uriInfo, UserRootResource.class, UserCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getUserCollectionResource").parameters().method("getAll").parameters().href();
    }

    String create() {
      return collectionLinkBuilder.method("getUserCollectionResource").parameters().method("create").parameters().href();
    }
  }
}
