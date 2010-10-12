/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.plugin;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.repository.RepositoryHandler;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlRootElement(name = "plugin-config")
@XmlAccessorType(XmlAccessType.FIELD)
public class SCMPlugin
{

  /**
   * Constructs ...
   *
   */
  public SCMPlugin()
  {
    handlers = new HashSet<Class<? extends RepositoryHandler>>();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param handlerClass
   *
   * @return
   */
  public boolean addHandler(Class<? extends RepositoryHandler> handlerClass)
  {
    return handlers.add(handlerClass);
  }

  /**
   * Method description
   *
   *
   * @param handlerClass
   *
   * @return
   */
  public boolean removeHandler(Class<? extends RepositoryHandler> handlerClass)
  {
    return handlers.remove(handlerClass);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<Class<? extends RepositoryHandler>> getHandlers()
  {
    return handlers;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElementWrapper(name = "repository-handlers")
  @XmlElement(name = "handler")
  private Set<Class<? extends RepositoryHandler>> handlers;
}
