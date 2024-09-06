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

import com.fasterxml.jackson.annotation.JsonInclude;
import de.otto.edison.hal.Embedded;
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.OptionalLong;

@Getter
@Setter
public class FileObjectDto extends HalRepresentation {
  private String name;
  private String path;
  private boolean directory;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Optional<String> description;
  private OptionalLong length;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private Optional<Instant> commitDate;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private SubRepositoryDto subRepository;
  @JsonInclude(JsonInclude.Include.NON_EMPTY)
  private String revision;
  private boolean partialResult;
  private boolean computationAborted;
  private boolean truncated;

  public FileObjectDto(Links links, Embedded embedded) {
    super(links, embedded);
  }

  public void setChildren(List<FileObjectDto> children) {
    if (!children.isEmpty()) {
      // prevent empty embedded attribute in json
      this.withEmbedded("children", children);
    }
  }
}
