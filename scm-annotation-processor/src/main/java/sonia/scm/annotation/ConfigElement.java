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

package sonia.scm.annotation;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import java.util.Map;

public class ConfigElement implements DescriptorElement {

  private final Map<String, String> attributes;
  private final String type;

  public ConfigElement(Map<String, String> attributes, String type) {
    this.attributes = attributes;
    this.type = type;
  }

  @Override
  public void append(Document doc, Element root) {

    Element configEl = doc.createElement("config-value");

    for (Map.Entry<String, String> entry : attributes.entrySet()) {
      Element attributeEl = doc.createElement(entry.getKey());
      attributeEl.setTextContent(entry.getValue());
      configEl.appendChild(attributeEl);
    }

    Element typeEl = doc.createElement("type");
    typeEl.setTextContent(type);
    configEl.appendChild(typeEl);

    root.appendChild(configEl);
  }
}
