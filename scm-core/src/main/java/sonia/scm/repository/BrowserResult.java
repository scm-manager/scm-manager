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
    
package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

/**
 *
 * @author Sebastian Sdorra
 * @since 1.5
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "browser-result")
public class BrowserResult implements Serializable {

  private String revision;
  private String requestedRevision;
  private FileObject file;

  public BrowserResult() {
  }

  public BrowserResult(String revision, FileObject file) {
    this(revision, revision, file);
  }

  public BrowserResult(String revision, String requestedRevision, FileObject file) {
    this.revision = revision;
    this.requestedRevision = requestedRevision;
    this.file = file;
  }

  public String getRevision() {
    return revision;
  }

  public String getRequestedRevision() {
    return requestedRevision;
  }

  public FileObject getFile() {
    return file;
  }

  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }

    if (getClass() != obj.getClass()) {
      return false;
    }

    final BrowserResult other = (BrowserResult) obj;

    return Objects.equal(revision, other.revision)
      && Objects.equal(file, other.file);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(revision, file);
  }


  @Override
  public String toString() {
    return MoreObjects.toStringHelper(this)
      .add("revision", revision)
      .add("files", file)
      .toString();
  }


}
