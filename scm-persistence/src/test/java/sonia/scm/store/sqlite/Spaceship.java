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
import jakarta.xml.bind.annotation.XmlRootElement;
import lombok.EqualsAndHashCode;
import sonia.scm.store.QueryableType;

import java.time.Instant;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;

@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
@QueryableType
@EqualsAndHashCode
class Spaceship {
  String name;
  SQLiteQueryableStoreTest.Range range;
  Collection<String> crew;
  Map<String, Boolean> destinations;
  Instant inServiceSince;
  int flightCount;

  public Spaceship() {
  }

  public Spaceship(String name, SQLiteQueryableStoreTest.Range range) {
    this.name = name;
    this.range = range;
  }

  public Spaceship(String name, String... crew) {
    this.name = name;
    this.crew = Arrays.asList(crew);
  }

  public Spaceship(String name, Map<String, Boolean> destinations) {
    this.name = name;
    this.destinations = destinations;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public SQLiteQueryableStoreTest.Range getRange() {
    return range;
  }

  public void setRange(SQLiteQueryableStoreTest.Range range) {
    this.range = range;
  }

  public Collection<String> getCrew() {
    return crew;
  }

  public void setCrew(Collection<String> crew) {
    this.crew = crew;
  }

  public Map<String, Boolean> getDestinations() {
    return destinations;
  }

  public void setDestinations(Map<String, Boolean> destinations) {
    this.destinations = destinations;
  }

  public Instant getInServiceSince() {
    return inServiceSince;
  }

  public void setInServiceSince(Instant inServiceSince) {
    this.inServiceSince = inServiceSince;
  }

  public int getFlightCount() {
    return flightCount;
  }

  public void setFlightCount(int flightCount) {
    this.flightCount = flightCount;
  }
}
