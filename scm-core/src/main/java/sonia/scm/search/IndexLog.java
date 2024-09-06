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

package sonia.scm.search;

import com.google.common.annotations.Beta;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.Data;
import sonia.scm.xml.XmlInstantAdapter;

import java.time.Instant;

/**
 * A marker keeping track of when and with which model version an object type was last indexed.
 *
 * @since 2.21.0
 */
@Beta
@Data
@XmlRootElement
@XmlAccessorType(XmlAccessType.FIELD)
public class IndexLog {

  private int version = 1;
  @XmlJavaTypeAdapter(XmlInstantAdapter.class)
  private Instant date = Instant.now();

  public IndexLog() {
  }

  public IndexLog(int version) {
    this.version = version;
  }
}
