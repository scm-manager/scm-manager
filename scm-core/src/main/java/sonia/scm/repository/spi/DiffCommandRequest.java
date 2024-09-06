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

package sonia.scm.repository.spi;


import com.google.common.base.Strings;
import lombok.EqualsAndHashCode;
import sonia.scm.Validateable;
import sonia.scm.repository.api.DiffFormat;
import sonia.scm.repository.api.IgnoreWhitespaceLevel;

/**
 *
 * @since 1.17
 */
@EqualsAndHashCode(callSuper = true)
public class DiffCommandRequest extends FileBaseCommandRequest
  implements Validateable {

  private static final long serialVersionUID = 4026911212676859626L;

  private DiffFormat format = DiffFormat.NATIVE;

  private String ancestorChangeset;

  private IgnoreWhitespaceLevel ignoreWhitespace;

  @Override
  public DiffCommandRequest clone()
  {
    DiffCommandRequest clone = null;

    try
    {
      clone = (DiffCommandRequest) super.clone();
    }
    catch (CloneNotSupportedException e)
    {

      // this shouldn't happen, since we are Cloneable
      throw new InternalError("DiffCommandRequest seems not to be cloneable");
    }

    return clone;
  }

  @Override
  public boolean isValid()
  {
    return !Strings.isNullOrEmpty(getPath())
      ||!Strings.isNullOrEmpty(getRevision());
  }

  /**
   * Sets the diff format which should be used for the output.
   *
   *
   * @param format format of the diff output
   *
   * @since 1.34
   */
  public void setFormat(DiffFormat format)
  {
    this.format = format;
  }

  public void setAncestorChangeset(String ancestorChangeset) {
    this.ancestorChangeset = ancestorChangeset;
  }

  /**
   * Return the output format of the diff command.
   * @since 1.34
   */
  public DiffFormat getFormat()
  {
    return format;
  }

  public String getAncestorChangeset() {
    return ancestorChangeset;
  }

  public IgnoreWhitespaceLevel getIgnoreWhitespaceLevel() { return ignoreWhitespace; }

  public void setIgnoreWhitespaceLevel(IgnoreWhitespaceLevel ignoreWhitespace) {
    this.ignoreWhitespace = ignoreWhitespace;
  }

}
