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
    
package sonia.scm.cache;

//~--- non-JDK imports --------------------------------------------------------

import com.google.common.base.MoreObjects;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.io.Serializable;

//~--- JDK imports ------------------------------------------------------------

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
    return MoreObjects.toStringHelper(this)
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
