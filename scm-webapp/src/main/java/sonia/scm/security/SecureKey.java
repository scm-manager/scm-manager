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

package sonia.scm.security;


import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlRootElement;

/**
 * Secure key can be used for singing messages and tokens.
 *
 * @since 2.0.0
 */
@XmlRootElement(name = "secure-key")
@XmlAccessorType(XmlAccessType.FIELD)
public final class SecureKey
{
  /** bytes of key */
  private byte[] bytes;

  private long creationDate;

  /**
   * This constructor should only be used by jaxb.
   */
  SecureKey() {}

  public SecureKey(byte[] bytes, long creationDate)
  {
    this.bytes = bytes;
    this.creationDate = creationDate;
  }


 
  @Override
  public boolean equals(Object obj)
  {
    if (obj == null)
    {
      return false;
    }

    if (getClass() != obj.getClass())
    {
      return false;
    }

    final SecureKey other = (SecureKey) obj;

    return Objects.equal(bytes, other.bytes)
      && Objects.equal(creationDate, other.creationDate);
  }

 
  @Override
  public int hashCode()
  {
    return Objects.hashCode(bytes, creationDate);
  }


  public byte[] getBytes()
  {
    return bytes;
  }

  public long getCreationDate()
  {
    return creationDate;
  }

}
