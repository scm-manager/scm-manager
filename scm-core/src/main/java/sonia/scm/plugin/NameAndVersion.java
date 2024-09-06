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

import com.google.common.base.Strings;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;
import jakarta.xml.bind.annotation.adapters.XmlAdapter;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import sonia.scm.version.Version;

import java.util.Optional;

/**
 * @since 2.4.0
 */
@Getter
@EqualsAndHashCode
@XmlAccessorType(XmlAccessType.FIELD)
public class NameAndVersion {

  @XmlValue
  private String name;

  @XmlAttribute(name = "version")
  @XmlJavaTypeAdapter(VersionXmlAdapter.class)
  private Version version;

  NameAndVersion() {
    // required for jaxb
  }

  public NameAndVersion(String name) {
    this(name, null);
  }

  public NameAndVersion(String name, String version) {
    this.name = name;
    if (!Strings.isNullOrEmpty(version)) {
      this.version = Version.parse(version);
    }
  }

  public Optional<Version> getVersion() {
    return Optional.ofNullable(version);
  }

  public Version mustGetVersion() {
    if (version == null) {
      throw new IllegalStateException("version is not set");
    }
    return version;
  }

  @Override
  public String toString() {
    return name + (version != null ? ":" + version.getParsedVersion() : "");
  }

  static class VersionXmlAdapter extends XmlAdapter<String, Version> {

    @Override
    public Version unmarshal(String v) {
      if (Strings.isNullOrEmpty(v)) {
        return null;
      }
      return Version.parse(v);
    }

    @Override
    public String marshal(Version v) {
      if (v != null) {
        return v.getUnparsedVersion();
      }
      return null;
    }
  }
}
