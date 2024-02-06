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
