/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.server;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.cli.Argument;

/**
 *
 * @author Sebastian Sdorra
 */
public class ServerConfig
{

  /**
   * Constructs ...
   *
   */
  public ServerConfig() {}

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public String getContextPath()
  {
    return contextPath;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Integer getPort()
  {
    return port;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public String getResourcePath()
  {
    return resourcePath;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Boolean getShowHelp()
  {
    return showHelp;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param contextPath
   */
  public void setContextPath(String contextPath)
  {
    this.contextPath = contextPath;
  }

  /**
   * Method description
   *
   *
   * @param port
   */
  public void setPort(Integer port)
  {
    this.port = port;
  }

  /**
   * Method description
   *
   *
   * @param resourcePath
   */
  public void setResourcePath(String resourcePath)
  {
    this.resourcePath = resourcePath;
  }

  /**
   * Method description
   *
   *
   * @param showHelp
   */
  public void setShowHelp(Boolean showHelp)
  {
    this.showHelp = showHelp;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @Argument(
    value = "c",
    longName = "context-path",
    description = "The Context-Path of the Jab-WebApp"
  )
  private String contextPath = "/";

  /** Field description */
  @Argument(
    value = "p",
    longName = "port",
    description = "The port for the listener"
  )
  private Integer port = Integer.valueOf(8080);

  /** Field description */
  @Argument(
    value = "r",
    longName = "resource-path",
    description = "Path to the server resource directory"
  )
  private String resourcePath;

  /** Field description */
  @Argument(
    value = "h",
    longName = "help",
    description = "Shows this help"
  )
  private Boolean showHelp = Boolean.FALSE;
}
