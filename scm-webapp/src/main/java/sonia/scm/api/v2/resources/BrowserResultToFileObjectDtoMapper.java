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

import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.Links;
import org.mapstruct.Context;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Qualifier;
import sonia.scm.repository.BrowserResult;
import sonia.scm.repository.FileObject;
import sonia.scm.repository.NamespaceAndName;
import sonia.scm.web.EdisonHalAppender;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.time.Instant;
import java.util.Optional;
import java.util.OptionalLong;

@Mapper
public abstract class BrowserResultToFileObjectDtoMapper extends BaseFileObjectDtoMapper {

  FileObjectDto map(BrowserResult browserResult, NamespaceAndName namespaceAndName, int offset) {
    FileObjectDto fileObjectDto = fileObjectToDto(browserResult.getFile(), namespaceAndName, browserResult, offset);
    fileObjectDto.setRevision(browserResult.getRevision());

    return fileObjectDto;
  }

  @Mapping(target = "attributes", ignore = true) // We do not map HAL attributes
  @Mapping(target = "children", qualifiedBy = Children.class)
  @Mapping(target = "revision", ignore = true)
  @Children
  protected abstract FileObjectDto fileObjectToDto(FileObject fileObject, @Context NamespaceAndName namespaceAndName, @Context BrowserResult browserResult, @Context Integer offset);

  @Override
  void applyEnrichers(Links.Builder links, Embedded.Builder embeddedBuilder, NamespaceAndName namespaceAndName, BrowserResult browserResult, FileObject fileObject) {
    EdisonHalAppender appender = new EdisonHalAppender(links, embeddedBuilder);
    if (browserResult.getFile().equals(fileObject)) {
      // we call enrichers, which are only responsible for top level browseresults
      applyEnrichers(appender, browserResult, namespaceAndName);
    }
    // we call enrichers, which are responsible for all file object top level browse result and its children
    applyEnrichers(appender, fileObject, namespaceAndName, browserResult, browserResult.getRevision());
  }

  Optional<Instant> mapOptionalInstant(OptionalLong optionalLong) {
    if (optionalLong.isPresent()) {
      return Optional.of(Instant.ofEpochMilli(optionalLong.getAsLong()));
    } else {
      return Optional.empty();
    }
  }

  @Qualifier
  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.CLASS)
  @interface Children {
  }
}
