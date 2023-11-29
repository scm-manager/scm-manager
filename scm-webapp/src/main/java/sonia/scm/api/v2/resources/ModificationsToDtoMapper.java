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

import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.AfterMapping;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import sonia.scm.repository.Added;
import sonia.scm.repository.Modifications;
import sonia.scm.repository.Modified;
import sonia.scm.repository.Removed;
import sonia.scm.repository.Renamed;
import sonia.scm.repository.Repository;

import static de.otto.edison.hal.Links.linkingTo;

@Mapper
public abstract class ModificationsToDtoMapper {

  @Inject
  private ResourceLinks resourceLinks;

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  public abstract ModificationsDto map(Modifications modifications, @Context Repository repository);

  @AfterMapping
  void appendLinks(@MappingTarget ModificationsDto target, @Context Repository repository) {
    Links.Builder linksBuilder = linkingTo()
      .self(resourceLinks.modifications().self(repository.getNamespace(), repository.getName(), target.getRevision()));
    target.add(linksBuilder.build());
  }

  String map(Added added) {
    return added.getPath();
  }

  String map(Removed removed) {
    return removed.getPath();
  }

  String map(Modified modified) {
    return modified.getPath();
  }

  abstract ModificationsDto.RenamedDto map(Renamed renamed);
}
