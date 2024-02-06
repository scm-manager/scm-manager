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
    
package sonia.scm.repository.client.spi;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
/**
 *
 * @since 1.18
 */
public final class TagRequest
{
  private String name;

  private String revision;

  private String username;

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

    final TagRequest other = (TagRequest) obj;

    return Objects.equal(revision, other.revision)
      && Objects.equal(name, other.name);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(revision, name);
  }

   public void reset()
  {
    this.name = null;
    this.revision = null;
    this.username = null;
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                  .add("revision", revision)
                  .add("name", name)
      .add("username", username)
                  .toString();
    //J+
  }



  public void setName(String name)
  {
    this.name = name;
  }


  public void setRevision(String revision)
  {
    this.revision = revision;
  }

  public void setUsername(String username) {
    this.username = username;
  }


  
  String getName()
  {
    return name;
  }

  
  String getRevision()
  {
    return revision;
  }

  public String getUserName() {
    return username;
  }

}
