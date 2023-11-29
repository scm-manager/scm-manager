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

package sonia.scm.it.resource;

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import lombok.Getter;
import lombok.Setter;
import sonia.scm.api.v2.resources.LinkBuilder;
import sonia.scm.api.v2.resources.ScmPathInfoStore;

import static de.otto.edison.hal.Embedded.embeddedBuilder;
import static de.otto.edison.hal.Links.linkingTo;

/**
 * Web Service Resource to support integration tests.
 */
@Path(IntegrationTestResource.INTEGRATION_TEST_PATH_V2)
public class IntegrationTestResource {

  static final String INTEGRATION_TEST_PATH_V2 = "v2/integration-test";

  private final ScmPathInfoStore scmPathInfoStore;
  private final MergeDetectionHelper mergeDetectionHelper;

  @Inject
  public IntegrationTestResource(ScmPathInfoStore scmPathInfoStore, MergeDetectionHelper mergeDetectionHelper) {
    this.scmPathInfoStore = scmPathInfoStore;
    this.mergeDetectionHelper = mergeDetectionHelper;
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
