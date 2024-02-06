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
    
package sonia.scm.search;


public class SearchRequest
{
  private boolean ignoreCase = false;

  private int maxResults = -1;

  private String query;

  private int startWith = 0;

  public SearchRequest() {}

  public SearchRequest(String query)
  {
    this.query = query;
  }

  public SearchRequest(String query, boolean ignoreCase)
  {
    this.query = query;
    this.ignoreCase = ignoreCase;
  }

  public SearchRequest(String query, boolean ignoreCase, int maxResults) {
    this.query = query;
    this.ignoreCase = ignoreCase;
    this.maxResults = maxResults;
  }


  
  public int getMaxResults()
  {
    return maxResults;
  }

  
  public String getQuery()
  {
    return query;
  }

  
  public int getStartWith()
  {
    return startWith;
  }

  
  public boolean isIgnoreCase()
  {
    return ignoreCase;
  }

  public void setIgnoreCase(boolean ignoreCase)
  {
    this.ignoreCase = ignoreCase;
  }

  public void setMaxResults(int maxResults)
  {
    this.maxResults = maxResults;
  }

  public void setQuery(String query)
  {
    this.query = query;
  }

  public void setStartWith(int startWith)
  {
    this.startWith = startWith;
  }

}
