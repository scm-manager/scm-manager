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

package sonia.scm.xml;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

/**
 * JAXB adapter for {@link Instant} objects.
 *
 * @since 2.0.0
 */
public class XmlInstantAdapter extends XmlAdapter<String, Instant> {

  @Override
  public String marshal(Instant instant) {
    return DateTimeFormatter.ISO_INSTANT.format(instant);
  }

  @Override
  public Instant unmarshal(String text) {
    TemporalAccessor parsed = DateTimeFormatter.ISO_INSTANT.parse(text);
    return Instant.from(parsed);
  }
}
