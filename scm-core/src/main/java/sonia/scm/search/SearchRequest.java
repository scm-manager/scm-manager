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


package sonia.scm.search;

/**
 *
 * @author Sebastian Sdorra
 */
public class SearchRequest
{

  /**
   * Constructs ...
   *
   */
  public SearchRequest() {}

  /**
   * Constructs ...
   *
   *
   * @param query
   */
  public SearchRequest(String query)
  {
    this.query = query;
  }

  /**
   * Constructs ...
   *
   *
   * @param query
   * @param ignoreCase
   */
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

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public int getMaxResults()
  {
    return maxResults;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getQuery()
  {
    return query;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public int getStartWith()
  {
    return startWith;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isIgnoreCase()
  {
    return ignoreCase;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param ignoreCase
   */
  public void setIgnoreCase(boolean ignoreCase)
  {
    this.ignoreCase = ignoreCase;
  }

  /**
   * Method description
   *
   *
   * @param maxResults
   */
  public void setMaxResults(int maxResults)
  {
    this.maxResults = maxResults;
  }

  /**
   * Method description
   *
   *
   * @param query
   */
  public void setQuery(String query)
  {
    this.query = query;
  }

  /**
   * Method description
   *
   *
   * @param startWith
   */
  public void setStartWith(int startWith)
  {
    this.startWith = startWith;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private boolean ignoreCase = false;

  /** Field description */
  private int maxResults = -1;

  /** Field description */
  private String query;

  /** Field description */
  private int startWith = 0;
}
