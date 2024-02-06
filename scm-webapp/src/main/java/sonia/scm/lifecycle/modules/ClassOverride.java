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
    
package sonia.scm.lifecycle.modules;


import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import sonia.scm.Validateable;


@XmlAccessorType(XmlAccessType.FIELD)
public class ClassOverride implements Validateable
{
  private Class<?> bind;

  private Class<?> to;

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

    final ClassOverride other = (ClassOverride) obj;

    return Objects.equal(bind, other.bind) && Objects.equal(to, other.to);
  }

  
  @Override
  public int hashCode()
  {
    return Objects.hashCode(bind, to);
  }

  
  @Override
  public String toString()
  {
    //J-
    return MoreObjects.toStringHelper(this)
            .add("bind", bind)
            .add("to", to)
            .toString();
    //J+
  }


  
  public Class<?> getBind()
  {
    return bind;
  }

  
  public Class<?> getTo()
  {
    return to;
  }

  
  @Override
  public boolean isValid()
  {
    return (bind != null) && (to != null);
  }



  public void setBind(Class<?> bind)
  {
    this.bind = bind;
  }


  public void setTo(Class<?> to)
  {
    this.to = to;
  }

}
