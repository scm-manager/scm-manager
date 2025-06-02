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

package sonia.scm.store.sqlite;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import lombok.EqualsAndHashCode;
import sonia.scm.store.Id;
import sonia.scm.store.QueryableType;

@QueryableType
@XmlAccessorType(XmlAccessType.FIELD)
@EqualsAndHashCode
class SpaceshipWithId {
  @Id
  private String name;
  int flightCount;

  public SpaceshipWithId() {
  }

  public SpaceshipWithId(String name, int flightCount) {
    this.name = name;
    this.flightCount = flightCount;
  }

  public String getName() {
    return name;
  }

  public int getFlightCount() {
    return flightCount;
  }

  public void setFlightCount(int flightCount) {
    this.flightCount = flightCount;
  }
}
