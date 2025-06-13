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
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import sonia.scm.store.IdGenerator;
import sonia.scm.xml.XmlArrayStringAdapter;

@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@XmlAccessorType(XmlAccessType.FIELD)
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class QueryableTypeDescriptor extends NamedClassElement {

  @XmlElement(name = "value")
  @XmlJavaTypeAdapter(XmlArrayStringAdapter.class)
  private String[] types;

  private IdGenerator idGenerator;

  public QueryableTypeDescriptor(String name, String clazz, String[] types, IdGenerator idGenerator) {
    super(name, clazz);
    this.types = types;
    this.idGenerator = idGenerator;
  }

  public String[] getTypes() {
    return types == null ? new String[0] : types;
  }

  public IdGenerator getIdGenerator() {
    return idGenerator == null ? IdGenerator.DEFAULT : idGenerator;
  }
}
