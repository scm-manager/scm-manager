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

package sonia.scm.web.data;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.Changeset;

public final class ImmutableEncodedChangeset {

  private final Changeset changeset;

  public ImmutableEncodedChangeset(Changeset changeset) {
    this.changeset = changeset;
  }

  public ImmutableEncodedPerson getAuthor() {
    return new ImmutableEncodedPerson(changeset.getAuthor());
  }

  public EncodedStringList getBranches() {
    return new EncodedStringList(changeset.getBranches());
  }

  public Long getDate() {
    return changeset.getDate();
  }

  public String getDescription() {
    return Encoder.encode(changeset.getDescription());
  }

  public String getId() {
    return changeset.getId();
  }

  public EncodedStringList getParents() {
    return new EncodedStringList(changeset.getParents());
  }

  public EncodedStringList getTags() {
    return new EncodedStringList(changeset.getTags());
  }
}
