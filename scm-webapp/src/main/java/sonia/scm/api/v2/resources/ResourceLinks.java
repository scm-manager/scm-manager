package sonia.scm.api.v2.resources;

import javax.ws.rs.core.UriInfo;

class ResourceLinks {

  private ResourceLinks() {
  }

  static GroupLinks group(UriInfo uriInfo) {
    return new GroupLinks(uriInfo);
  }

  static class GroupLinks {
    private final LinkBuilder groupLinkBuilder;

    private GroupLinks(UriInfo uriInfo) {
      groupLinkBuilder = new LinkBuilder(uriInfo, GroupV2Resource.class, GroupSubResource.class);
    }

    String self(String name) {
      return groupLinkBuilder.method("getGroupSubResource").parameters(name).method("get").parameters().href();
    }

    String delete(String name) {
      return groupLinkBuilder.method("getGroupSubResource").parameters(name).method("delete").parameters().href();
    }

    String update(String name) {
      return groupLinkBuilder.method("getGroupSubResource").parameters(name).method("update").parameters().href();
    }
  }

  static GroupCollectionLinks groupCollection(UriInfo uriInfo) {
    return new GroupCollectionLinks(uriInfo);
  }

  static class GroupCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    private GroupCollectionLinks(UriInfo uriInfo) {
      collectionLinkBuilder = new LinkBuilder(uriInfo, GroupV2Resource.class, GroupCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getGroupCollectionResource").parameters().method("getAll").parameters().href();
    }

    String create() {
      return collectionLinkBuilder.method("getGroupCollectionResource").parameters().method("create").parameters().href();
    }
  }

  static UserLinks user(UriInfo uriInfo) {
    return new UserLinks(uriInfo);
  }

  static class UserLinks {
    private final LinkBuilder userLinkBuilder;

    private UserLinks(UriInfo uriInfo) {
      userLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserSubResource.class);
    }

    String self(String name) {
      return userLinkBuilder.method("getUserSubResource").parameters(name).method("get").parameters().href();
    }

    String delete(String name) {
      return userLinkBuilder.method("getUserSubResource").parameters(name).method("delete").parameters().href();
    }

     String update(String name) {
      return userLinkBuilder.method("getUserSubResource").parameters(name).method("update").parameters().href();
    }
  }

  static UserCollectionLinks userCollection(UriInfo uriInfo) {
    return new UserCollectionLinks(uriInfo);
  }

  static class UserCollectionLinks {
    private final LinkBuilder collectionLinkBuilder;

    private UserCollectionLinks(UriInfo uriInfo) {
      collectionLinkBuilder = new LinkBuilder(uriInfo, UserV2Resource.class, UserCollectionResource.class);
    }

    String self() {
      return collectionLinkBuilder.method("getUserCollectionResource").parameters().method("getAll").parameters().href();
    }

    String create() {
      return collectionLinkBuilder.method("getUserCollectionResource").parameters().method("create").parameters().href();
    }
  }
}
