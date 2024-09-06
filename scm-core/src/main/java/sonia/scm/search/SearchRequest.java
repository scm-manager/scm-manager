/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
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
