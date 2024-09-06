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

package sonia.scm.api.v2.resources;

import com.google.common.annotations.VisibleForTesting;
import de.otto.edison.hal.Links;
import jakarta.inject.Inject;
import org.mapstruct.Mapper;
import org.mapstruct.ObjectFactory;
import sonia.scm.repository.BlameLine;
import sonia.scm.repository.BlameResult;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.repository.Person;

import java.util.stream.Collectors;

@Mapper
public abstract class BlameResultToBlameDtoMapper implements InstantAttributeMapper {

  @Inject
  private ResourceLinks resourceLinks;

  BlameDto map(BlameResult result, NamespaceAndName namespaceAndName, String revision, String path) {
    BlameDto dto = createDto(namespaceAndName, revision, path);
    dto.setLines(result.getBlameLines().stream().map(this::map).collect(Collectors.toList()));
    return dto;
  }

  abstract BlameLineDto map(BlameLine line);

  abstract PersonDto map(Person person);

  @ObjectFactory
  BlameDto createDto(NamespaceAndName namespaceAndName, String revision, String path) {
    return new BlameDto(Links.linkingTo()
      .self(
        resourceLinks.annotate()
          .self(
            namespaceAndName.getNamespace(),
            namespaceAndName.getName(),
            revision,
            path))
      .build());
  }

  @VisibleForTesting
  void setResourceLinks(ResourceLinks resourceLinks) {
    this.resourceLinks = resourceLinks;
  }
}
