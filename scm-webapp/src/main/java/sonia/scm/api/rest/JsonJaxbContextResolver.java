/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.group.Group;
import sonia.scm.repository.Repository;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.json.JSONConfiguration;
import com.sun.jersey.api.json.JSONJAXBContext;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.ext.ContextResolver;
import javax.ws.rs.ext.Provider;

import javax.xml.bind.JAXBContext;

/**
 *
 * @author Sebastian Sdorra
 */
@Provider
public class JsonJaxbContextResolver implements ContextResolver<JAXBContext>
{

  /**
   * Constructs ...
   *
   *
   * @throws Exception
   */
  public JsonJaxbContextResolver() throws Exception
  {
    this.context = new JSONJAXBContext(
        JSONConfiguration.mapped().rootUnwrapping(true).arrays(
          "member", "groups", "permissions").nonStrings(
          "readable", "writeable", "groupPermission").build(), types.toArray(
          new Class[0]));
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param objectType
   *
   * @return
   */
  @Override
  public JAXBContext getContext(Class<?> objectType)
  {
    return (types.contains(objectType))
           ? context
           : null;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private List<Class> types = Arrays.asList(new Class[] { Group.class,
          Repository.class });
}
