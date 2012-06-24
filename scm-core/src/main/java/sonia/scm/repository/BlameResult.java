/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

//~--- JDK imports ------------------------------------------------------------

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Changeset information by line for a given file.
 *
 * TODO for 2.0 implement {@link Iterable}
 *
 * @author Sebastian Sdorra
 * @since 1.8
 */
@XmlRootElement(name = "blame-result")
@XmlAccessorType(XmlAccessType.FIELD)
public class BlameResult implements Cloneable
{

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
   * Create a clone of this {@link blameResult} object.
   *
   *
   * @return clone of this {@link blameResult}
   *
   * @since 1.17
   */
  @Override
  public BlameResult clone()
  {
    BlameResult blameResult = null;

    try
    {
      blameResult = (BlameResult) super.clone();
    }
    catch (CloneNotSupportedException ex)
    {
      throw new RuntimeException(ex);
    }

    return blameResult;
  }

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
   * {@inheritDoc}
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
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
