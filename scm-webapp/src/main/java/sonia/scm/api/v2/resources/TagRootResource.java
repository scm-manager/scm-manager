package sonia.scm.api.v2.resources;

import com.webcohesion.enunciate.metadata.rs.ResponseCode;
import com.webcohesion.enunciate.metadata.rs.StatusCodes;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import sonia.scm.NotFoundException;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryPermissions;
import sonia.scm.repository.Tag;
import sonia.scm.repository.Tags;
import sonia.scm.repository.api.RepositoryService;
import sonia.scm.repository.api.RepositoryServiceFactory;
import sonia.scm.web.VndMediaType;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Response;
import java.io.IOException;

import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

public class TagRootResource {

  private final RepositoryServiceFactory serviceFactory;
  private final TagCollectionToDtoMapper tagCollectionToDtoMapper;
  private final TagToTagDtoMapper tagToTagDtoMapper;

  @Inject
  public TagRootResource(RepositoryServiceFactory serviceFactory,
                         TagCollectionToDtoMapper tagCollectionToDtoMapper,
                         TagToTagDtoMapper tagToTagDtoMapper) {
    this.serviceFactory = serviceFactory;
    this.tagCollectionToDtoMapper = tagCollectionToDtoMapper;
    this.tagToTagDtoMapper = tagToTagDtoMapper;
  }


  @GET
  @Path("")
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the tags"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.TAG_COLLECTION)
  @TypeHint(CollectionDto.class)
  public Response getAll(@PathParam("namespace") String namespace, @PathParam("name") String name) throws IOException {
    try (RepositoryService repositoryService = serviceFactory.create(new NamespaceAndName(namespace, name))) {
      Tags tags = getTags(repositoryService);
      if (tags != null && tags.getTags() != null) {
        return Response.ok(tagCollectionToDtoMapper.map(namespace, name, tags.getTags())).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Error on getting tag from repository.")
          .build();
      }
    }
  }


  @GET
  @StatusCodes({
    @ResponseCode(code = 200, condition = "success"),
    @ResponseCode(code = 401, condition = "not authenticated / invalid credentials"),
    @ResponseCode(code = 403, condition = "not authorized, the current user has no privileges to read the tags"),
    @ResponseCode(code = 404, condition = "not found, no tag available in the repository"),
    @ResponseCode(code = 500, condition = "internal server error")
  })
  @Produces(VndMediaType.TAG)
  @TypeHint(TagDto.class)
  @Path("{tagName}")
  public Response get(@PathParam("namespace") String namespace, @PathParam("name") String name, @PathParam("tagName") String tagName) throws IOException {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    try (RepositoryService repositoryService = serviceFactory.create(namespaceAndName)) {
      Tags tags = getTags(repositoryService);
      if (tags != null && tags.getTags() != null) {
        Tag tag = tags.getTags().stream()
          .filter(t -> tagName.equals(t.getName()))
          .findFirst()
          .orElseThrow(() -> createNotFoundException(namespace, name, tagName));
        return Response.ok(tagToTagDtoMapper.map(tag, namespaceAndName)).build();
      } else {
        return Response.status(Response.Status.INTERNAL_SERVER_ERROR)
          .entity("Error on getting tag from repository.")
          .build();
      }
    }
  }

  private NotFoundException createNotFoundException(String namespace, String name, String tagName) {
    return notFound(entity("Tag", tagName).in("Repository", namespace + "/" + name));
  }

  private Tags getTags(RepositoryService repositoryService) throws IOException {
    Repository repository = repositoryService.getRepository();
    RepositoryPermissions.read(repository).check();
    return repositoryService.getTagsCommand().getTags();
  }


}
