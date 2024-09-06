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

package sonia.scm.plugin;


import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sonia.scm.xml.XmlArrayStringAdapter;

import java.util.Arrays;

/**
 * Descriptor for web elements such as filter or servlets. A web element can be registered by using the
 * {@link sonia.scm.filter.WebElement} annotation.
 *
 * @since 2.0.0
 */
@Getter
@ToString
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@XmlRootElement(name = "web-element")
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public final class WebElementDescriptor extends ClassElement {

  @XmlElement(name = "value")
  private String pattern;

  @XmlJavaTypeAdapter(XmlArrayStringAdapter.class)
  private String[] morePatterns = {};

  private boolean regex = false;

  public String[] getMorePatterns() {
    String[] patterns;
    if (morePatterns != null) {
      patterns = Arrays.copyOf(morePatterns, morePatterns.length);
    } else {
      patterns = new String[0];
    }
    return patterns;
  }

}


