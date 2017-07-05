/**
 * Copyright (c) 2014, Sebastian Sdorra All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 * this list of conditions and the following disclaimer. 2. Redistributions in
 * binary form must reproduce the above copyright notice, this list of
 * conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution. 3. Neither the name of SCM-Manager;
 * nor the names of its contributors may be used to endorse or promote products
 * derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

import static com.google.common.base.Preconditions.checkNotNull;

//~--- JDK imports ------------------------------------------------------------

/**
 * Import result of the {@link AdvancedImportHandler}.
 *
 * @author Sebastian Sdorra
 * @since 1.43
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlRootElement(name = "import-result")
public final class ImportResult
{

  /**
   * Constructs ...
   *
   */
  ImportResult() {}

  /**
   * Constructs a new import result.
   *
   *
   * @param importedDirectories imported directories
   * @param failedDirectories failed directories
   */
  public ImportResult(List<String> importedDirectories,
    List<String> failedDirectories)
  {
    this.importedDirectories = checkNotNull(importedDirectories,
      "list of imported directories is required");
    this.failedDirectories = checkNotNull(failedDirectories,
      "list of failed directories is required");
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Returns a import result builder.
   *
   *
   * @return import result builder
   */
  public static Builder builder()
  {
    return new Builder();
  }

  /**
   * {@inheritDoc}
   */
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

    final ImportResult other = (ImportResult) obj;

    return Objects.equal(importedDirectories, other.importedDirectories)
      && Objects.equal(failedDirectories, other.failedDirectories);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(importedDirectories, failedDirectories);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
                      .add("importedDirectories", importedDirectories)
                      .add("failedDirectories", failedDirectories)
                      .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Returns list of failed directories.
   *
   *
   * @return list of failed directories
   */
  public List<String> getFailedDirectories()
  {
    return failedDirectories;
  }

  /**
   * Returns list of successfully imported directories.
   *
   *
   * @return list of successfully imported directories
   */
  public List<String> getImportedDirectories()
  {
    return importedDirectories;
  }

  //~--- inner classes --------------------------------------------------------

  /**
   * Builder for {@link ImportResult}.
   */
  public static class Builder
  {

    /**
     * Constructs ...
     *
     */
    private Builder() {}

    //~--- methods ------------------------------------------------------------

    /**
     * Adds a failed directory to the import result.
     *
     *
     * @param name name of the directory
     *
     * @return {@code this}
     */
    public Builder addFailedDirectory(String name)
    {
      this.failedDirectories.add(name);

      return this;
    }

    /**
     * Adds a successfully imported directory to the import result.
     *
     *
     * @param name name of the directory
     *
     * @return {@code this}
     */
    public Builder addImportedDirectory(String name)
    {
      this.importedDirectories.add(name);

      return this;
    }

    /**
     * Builds the final import result.
     *
     *
     * @return final import result
     */
    public ImportResult build()
    {
      return new ImportResult(ImmutableList.copyOf(importedDirectories),
        ImmutableList.copyOf(failedDirectories));
    }

    //~--- fields -------------------------------------------------------------

    /** successfully imported directories */
    private final List<String> importedDirectories = Lists.newArrayList();

    /** failed directories */
    private final List<String> failedDirectories = Lists.newArrayList();
  }


  //~--- fields ---------------------------------------------------------------

  /** failed directories */
  private List<String> failedDirectories;

  /** successfully imported directories */
  private List<String> importedDirectories;
}
