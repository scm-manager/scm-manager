/**
 * Copyright (c) 2010, Sebastian Sdorra All rights reserved.
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



package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.Objects;

//~--- JDK imports ------------------------------------------------------------

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "cache")
@XmlAccessorType(XmlAccessType.FIELD)
public class GuavaCacheConfiguration implements Serializable
{

  /** Field description */
  private static final long serialVersionUID = -8734373158089010603L;

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    //J-
    return Objects.toStringHelper(this)
                  .add("concurrencyLevel", concurrencyLevel)
                  .add("copyStrategy", copyStrategy)
                  .add("expireAfterAccess", expireAfterAccess)
                  .add("expireAfterWrite", expireAfterWrite)
                  .add("initialCapacity", initialCapacity)
                  .add("maximumSize", maximumSize)
                  .add("maximumWeight", maximumWeight)
                  .add("recordStats", recordStats)
                  .add("softValues", softValues)
                  .add("weakKeys", weakKeys)
                  .add("weakValues", weakValues)
                  .omitNullValues().toString();
    //J+
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Integer getConcurrencyLevel()
  {
    return concurrencyLevel;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public CopyStrategy getCopyStrategy()
  {
    return copyStrategy;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getExpireAfterAccess()
  {
    return expireAfterAccess;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getExpireAfterWrite()
  {
    return expireAfterWrite;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Integer getInitialCapacity()
  {
    return initialCapacity;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getMaximumSize()
  {
    return maximumSize;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Long getMaximumWeight()
  {
    return maximumWeight;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Boolean getRecordStats()
  {
    return recordStats;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Boolean getSoftValues()
  {
    return softValues;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Boolean getWeakKeys()
  {
    return weakKeys;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Boolean getWeakValues()
  {
    return weakValues;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlAttribute
  private Integer concurrencyLevel;

  /** Field description */
  @XmlAttribute
  @XmlJavaTypeAdapter(XmlCopyStrategyAdapter.class)
  private CopyStrategy copyStrategy;

  /** Field description */
  @XmlAttribute
  private Long expireAfterAccess;

  /** Field description */
  @XmlAttribute
  private Long expireAfterWrite;

  /** Field description */
  @XmlAttribute
  private Integer initialCapacity;

  /** Field description */
  @XmlAttribute
  private Long maximumSize;

  /** Field description */
  @XmlAttribute
  private Long maximumWeight;

  /** Field description */
  @XmlAttribute
  private Boolean recordStats;

  /** Field description */
  @XmlAttribute
  private Boolean softValues;

  /** Field description */
  @XmlAttribute
  private Boolean weakKeys;

  /** Field description */
  @XmlAttribute
  private Boolean weakValues;
}
