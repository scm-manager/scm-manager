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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.util.Set;

@XmlRootElement(name = "element")
@XmlAccessorType(XmlAccessType.FIELD)
public class XmlMapMultiStringElement {

  private String key;
  @XmlJavaTypeAdapter(XmlSetStringAdapter.class)
  private Set<String> value;

  public XmlMapMultiStringElement() {}

  public XmlMapMultiStringElement(String key, Set<String> value) {
    this.key = key;
    this.value = value;
  }

  public String getKey() {
    return key;
  }

  public Set<String> getValue() {
    return value;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public void setValue(Set<String> value) {
    this.value = value;
  }
}
