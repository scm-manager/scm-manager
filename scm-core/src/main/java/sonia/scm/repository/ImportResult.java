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
