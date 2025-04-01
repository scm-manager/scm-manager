/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.it.resource;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Repository;
import sonia.scm.repository.RepositoryManager;
import store.RepositoryTestData;
import store.RepositoryTestDataStoreFactory;

import java.util.Collection;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;
import static sonia.scm.ContextEntry.ContextBuilder.entity;
import static sonia.scm.NotFoundException.notFound;

/**
 * Web Service Resource to support integration tests.
 */
@Path(IntegrationTestResource.INTEGRATION_TEST_PATH_V2)
public class IntegrationTestResource {

  static final String INTEGRATION_TEST_PATH_V2 = "v2/integration-test";

  private final ScmPathInfoStore scmPathInfoStore;
  private final MergeDetectionHelper mergeDetectionHelper;
  private final RepositoryManager repositoryManager;
  private final RepositoryTestDataStoreFactory testDataStoreFactory;

  @Inject
  public IntegrationTestResource(ScmPathInfoStore scmPathInfoStore, MergeDetectionHelper mergeDetectionHelper, RepositoryManager repositoryManager, RepositoryTestDataStoreFactory testDataStoreFactory) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.mergeDetectionHelper = mergeDetectionHelper;
    this.repositoryManager = repositoryManager;
    this.testDataStoreFactory = testDataStoreFactory;
  }

  @GET
  @Path("")
  @Produces("application/json")
  public CollectionDto get() {
    Links links = linkingTo()
      .self(self())
      .build();
    Embedded embedded = embeddedBuilder()
      .with("preMergeDetection", mergeDetectionHelper.getPreMergeDetections())
      .with("postMergeDetection", mergeDetectionHelper.getPostMergeDetections())
      .build();
    return new CollectionDto(links, embedded);
  }

  @POST
  @Path("merge-detection")
  @Consumes("application/json")
  public void initMergeDetection(MergeDetectionConfigurationDto mergeDetectionConfiguration) {
    mergeDetectionHelper.initialize(mergeDetectionConfiguration.getTarget(), mergeDetectionConfiguration.getBranch());
  }

  @GET
  @Path("{namespace}/{name}/test-data")
  @Produces("application/json")
  public Collection<RepositoryTestData> getData(@PathParam("namespace") String namespace, @PathParam("name") String name) {
    Repository repository = repositoryManager.get(new NamespaceAndName(namespace, name));
    return testDataStoreFactory.get(repository).query().findAll();
  }

  @POST
  @Path("{namespace}/{name}/test-data")
  @Consumes("application/json")
  public void storeData(@PathParam("namespace") String namespace, @PathParam("name") String name, RepositoryTestData testData) {
    NamespaceAndName namespaceAndName = new NamespaceAndName(namespace, name);
    Repository repository = repositoryManager.get(namespaceAndName);
    if (repository == null) {
      throw notFound(entity(namespaceAndName));
    }
    testDataStoreFactory.getMutable(repository).put(testData);
  }

  private String self() {
    LinkBuilder linkBuilder = new LinkBuilder(scmPathInfoStore.get(), IntegrationTestResource.class);
    return linkBuilder.method("get").parameters().href();
  }

  static class CollectionDto extends HalRepresentation {

    CollectionDto(Links links, Embedded embedded) {
      super(links, embedded);
    }

    @Override
    protected HalRepresentation withEmbedded(String rel, HalRepresentation embeddedItem) {
      return super.withEmbedded(rel, embeddedItem);
    }
  }

  @Getter
  @Setter
  static class MergeDetectionConfigurationDto {
    private String target;
    private String branch;
  }
}
