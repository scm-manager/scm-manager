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


public enum Compatibility
{
  NONE(false, false, false, false, false),
  PRE14(true, true, true, true, false), PRE15(false, true, true, true, false),
  PRE16(false, false, true, true, false),
  PRE17(false, false, false, true, false),
  WITH17(false, false, false, false, true);

  private boolean pre14Compatible;

  private boolean pre15Compatible;

  private boolean pre16Compatible;

  private boolean pre17Compatible;

  private boolean with17Compatible;

  private Compatibility(boolean pre14Compatible, boolean pre15Compatible,
                        boolean pre16Compatible, boolean pre17Compatible,
                        boolean with17Compatible)
  {
    this.pre14Compatible = pre14Compatible;
    this.pre15Compatible = pre15Compatible;
    this.pre16Compatible = pre16Compatible;
    this.pre17Compatible = pre17Compatible;
    this.with17Compatible = with17Compatible;
  }


  
  public boolean isPre14Compatible()
  {
    return pre14Compatible;
  }

  
  public boolean isPre15Compatible()
  {
    return pre15Compatible;
  }

  
  public boolean isPre16Compatible()
  {
    return pre16Compatible;
  }

  
  public boolean isPre17Compatible()
  {
    return pre17Compatible;
  }

  
  public boolean isWith17Compatible()
  {
    return with17Compatible;
  }

}
