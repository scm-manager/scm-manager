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
    
package sonia.scm.debug;

import java.util.Collection;
import java.util.Date;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Received data from repository hook event.
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "hook")
@XmlAccessorType(XmlAccessType.FIELD)
public class DebugHookData
{
  private Date date;
  private Collection<String> changesets;

  /**
   * Constructs a new instance. This constructor should only be used by JAXB.
   */
  public DebugHookData()
  {
  }

  /**
   * Constructs a new instance.
   * 
   * @param changesets collection of changeset ids
   */
  public DebugHookData(Collection<String> changesets)
  {
    this.date = new Date();
    this.changesets = changesets;
  }

  /**
   * Returns the receiving date.
   * 
   * @return receiving date
   */
  public Date getDate()
  {
    return date;
  }

  /**
   * Return collection of changeset ids.
   * 
   * @return collection of changeset ids
   */
  public Collection<String> getChangesets()
  {
    return changesets;
  }
  
  
}
