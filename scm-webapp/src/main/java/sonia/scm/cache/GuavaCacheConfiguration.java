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

import com.google.common.base.MoreObjects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import java.io.Serializable;

@XmlRootElement(name = "cache")
@XmlAccessorType(XmlAccessType.PROPERTY)
public class GuavaCacheConfiguration implements Serializable {

  private static final long serialVersionUID = -8734373158089010603L;

  private Integer concurrencyLevel;
  private CopyStrategy copyStrategy;
  private Long expireAfterAccess;
  private Long expireAfterWrite;
  private Integer initialCapacity;
  private Long maximumSize;
  private Long maximumWeight;
  private Boolean recordStats;
  private Boolean softValues;
  private Boolean weakKeys;
  private Boolean weakValues;

  @Override
  public String toString() {
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
  }

  public Integer getConcurrencyLevel() {
    return concurrencyLevel;
  }

  public CopyStrategy getCopyStrategy() {
    return copyStrategy;
  }

  public Long getExpireAfterAccess() {
    return expireAfterAccess;
  }

  public Long getExpireAfterWrite() {
    return expireAfterWrite;
  }

  public Integer getInitialCapacity() {
    return initialCapacity;
  }

  public Long getMaximumSize() {
    return maximumSize;
  }

  public Long getMaximumWeight() {
    return maximumWeight;
  }

  public Boolean getRecordStats() {
    return recordStats;
  }

  public Boolean getSoftValues() {
    return softValues;
  }

  public Boolean getWeakKeys() {
    return weakKeys;
  }

  public Boolean getWeakValues() {
    return weakValues;
  }

  @XmlAttribute
  void setConcurrencyLevel(Integer concurrencyLevel) {
    this.concurrencyLevel = concurrencyLevel;
  }

  @XmlAttribute
  @XmlJavaTypeAdapter(XmlCopyStrategyAdapter.class)
  void setCopyStrategy(CopyStrategy copyStrategy) {
    this.copyStrategy = copyStrategy;
  }

  @XmlAttribute
  void setExpireAfterAccess(Long expireAfterAccess) {
    this.expireAfterAccess = expireAfterAccess;
  }

  @XmlAttribute
  void setExpireAfterWrite(Long expireAfterWrite) {
    this.expireAfterWrite = expireAfterWrite;
  }

  @XmlAttribute
  void setInitialCapacity(Integer initialCapacity) {
    this.initialCapacity = initialCapacity;
  }

  @XmlAttribute
  void setMaximumSize(Long maximumSize) {
    this.maximumSize = maximumSize;
  }

  @XmlAttribute
  void setMaximumWeight(Long maximumWeight) {
    this.maximumWeight = maximumWeight;
  }

  @XmlAttribute
  void setRecordStats(Boolean recordStats) {
    this.recordStats = recordStats;
  }

  @XmlAttribute
  void setSoftValues(Boolean softValues) {
    this.softValues = softValues;
  }

  @XmlAttribute
  void setWeakKeys(Boolean weakKeys) {
    this.weakKeys = weakKeys;
  }

  @XmlAttribute
  void setWeakValues(Boolean weakValues) {
    this.weakValues = weakValues;
  }
}
