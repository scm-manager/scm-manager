/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

/**
 *
 * @author Sebastian Sdorra
 */
public class BasicRepositoryConfig
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getBaseUrl()
  {
    return baseUrl;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param baseUrl
   */
  public void setBaseUrl(String baseUrl)
  {
    this.baseUrl = baseUrl;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String baseUrl;
}
