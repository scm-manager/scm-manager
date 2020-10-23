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
import java.util.Collection;
import java.util.List;
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
  @Children
  protected abstract FileObjectDto fileObjectToDto(FileObject fileObject, @Context NamespaceAndName namespaceAndName, @Context BrowserResult browserResult, @Context Integer offset);

  @Children
  abstract List<FileObjectDto> map(Collection<FileObject> value);

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
