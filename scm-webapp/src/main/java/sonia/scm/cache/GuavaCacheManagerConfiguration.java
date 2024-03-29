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

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlRootElement;

import java.util.Collections;
import java.util.List;


@XmlRootElement(name = "caches")
@XmlAccessorType(XmlAccessType.FIELD)
public class GuavaCacheManagerConfiguration
{
  @XmlElement(name = "cache")
  private List<GuavaNamedCacheConfiguration> caches;

  @XmlElement(name = "defaultCache")
  private GuavaCacheConfiguration defaultCache;

  public GuavaCacheManagerConfiguration() {}

 
  public GuavaCacheManagerConfiguration(GuavaCacheConfiguration defaultCache,
    List<GuavaNamedCacheConfiguration> caches)
  {
    this.defaultCache = defaultCache;
    this.caches = caches;
  }


  
  @SuppressWarnings("unchecked")
  public List<GuavaNamedCacheConfiguration> getCaches()
  {
    if (caches == null)
    {
      caches = Collections.emptyList();
    }

    return caches;
  }

  
  public GuavaCacheConfiguration getDefaultCache()
  {
    return defaultCache;
  }

}
