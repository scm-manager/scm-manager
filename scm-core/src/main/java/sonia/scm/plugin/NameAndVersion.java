/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
