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
import de.otto.edison.hal.HalRepresentation;
import de.otto.edison.hal.Links;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class ChangesetDto extends HalRepresentation {

  /**
   * The changeset identification string
   */
  private String id;

  /**
   * The author of the changeset
   */
  private PersonDto author;

  /**
   * The date when the changeset was committed
   */
  private Instant date;

  /**
   * The text of the changeset description
   */
  private String description;

  private List<ContributorDto> contributors;

  private List<SignatureDto> signatures;

  public ChangesetDto(Links links, Embedded embedded) {
    super(links, embedded);
  }
}
