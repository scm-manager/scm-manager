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

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.BranchDetails;
import sonia.scm.repository.Repository;
import sonia.scm.web.EdisonHalAppender;

import javax.inject.Inject;
import java.util.Optional;

@Mapper
public abstract class BranchDetailsMapper extends BaseMapper<BranchDetails, BranchDetailsDto> {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  abstract BranchDetailsDto map(@Context Repository repository, String branchName, BranchDetails result);

  @ObjectFactory
  BranchDetailsDto createDto(@Context Repository repository, String branchName, BranchDetails result) {
    Links.Builder linksBuilder = createLinks(repository, branchName);
    Embedded.Builder embeddedBuilder = Embedded.embeddedBuilder();

    applyEnrichers(new EdisonHalAppender(linksBuilder, embeddedBuilder), result, repository);

    return new BranchDetailsDto(linksBuilder.build(), embeddedBuilder.build());
  }

  Integer map(Optional<Integer> o) {
    return o.orElse(null);
  }

  private Links.Builder createLinks(@Context Repository repository, String branch) {
    return Links.linkingTo()
      .self(
        resourceLinks.branchDetails()
          .self(
            repository.getNamespace(),
            repository.getName(),
            branch)
      );
  }

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }
}
