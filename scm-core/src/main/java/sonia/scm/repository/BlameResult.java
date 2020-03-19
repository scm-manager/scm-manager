/**
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
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

//~--- JDK imports ------------------------------------------------------------

/**
 * Changeset information by line for a given file.
 *
 * @author Sebastian Sdorra
 * @since 1.8
 */
@XmlRootElement(name = "blame-result")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlameResult implements Serializable, Iterable<BlameLine>
{

  /** Field description */
  private static final long serialVersionUID = -8606237881465520606L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public BlameResult() {}

  /**
   * Constructs ...
   *
   *
   * @param blameLines
   */
  public BlameResult(List<BlameLine> blameLines)
  {
    this.blameLines = blameLines;
    this.total = blameLines.size();
  }

  /**
   * Constructs ...
   *
   *
   * @param total
   * @param blameLines
   */
  public BlameResult(int total, List<BlameLine> blameLines)
  {
    this.total = total;
    this.blameLines = blameLines;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * {@inheritDoc}
   *
   *
   * @param obj
   *
   * @return
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

    final BlameResult other = (BlameResult) obj;

    return Objects.equal(total, other.total)
           && Objects.equal(blameLines, other.blameLines);
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public int hashCode()
  {
    return Objects.hashCode(total, blameLines);
  }

  /**
   * Method description
   *
   *
   * @return
   * 
   * @since 1.17
   */
  @Override
  public Iterator<BlameLine> iterator()
  {
    return getBlameLines().iterator();
  }

  /**
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("total", total)
            .add("blameLines", blameLines)
            .toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public List<BlameLine> getBlameLines()
  {
    if ( blameLines == null ){
      blameLines = Lists.newArrayList();
    }
    return blameLines;
  }

  /**
   * Method description
   *
   *
   * @param i
   *
   * @return
   */
  public BlameLine getLine(int i)
  {
    return blameLines.get(i);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getTotal()
  {
    return total;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param blameLines
   */
  public void setBlameLines(List<BlameLine> blameLines)
  {
    this.blameLines = blameLines;
  }

  /**
   * Method description
   *
   *
   * @param total
   */
  public void setTotal(int total)
  {
    this.total = total;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "blameline")
  @XmlElementWrapper(name = "blamelines")
  private List<BlameLine> blameLines;

  /** Field description */
  private int total;
}
