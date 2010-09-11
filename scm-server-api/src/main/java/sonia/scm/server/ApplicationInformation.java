/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.server;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "app-info")
public class ApplicationInformation
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getAppName()
  {
    return appName;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getName()
  {
    return name;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getVersion()
  {
    return version;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param appName
   */
  public void setAppName(String appName)
  {
    this.appName = appName;
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  public void setName(String name)
  {
    this.name = name;
  }

  /**
   * Method description
   *
   *
   * @param version
   */
  public void setVersion(String version)
  {
    this.version = version;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String appName;

  /** Field description */
  private String name;

  /** Field description */
  private String version;
}
