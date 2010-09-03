/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.api.rest;

//~--- JDK imports ------------------------------------------------------------

import com.sun.jersey.api.core.PackagesResourceConfig;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.core.MediaType;

/**
 *
 * @author Sebastian Sdorra
 */
public class UriExtensionsConfig extends PackagesResourceConfig
{

  /**
   * Constructs ...
   *
   */
  public UriExtensionsConfig()
  {
    super();
  }

  /**
   * Constructs ...
   *
   *
   * @param props
   */
  public UriExtensionsConfig(Map<String, Object> props)
  {
    super(props);
  }

  /**
   * Constructs ...
   *
   *
   * @param paths
   */
  public UriExtensionsConfig(String[] paths)
  {
    super(paths);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public Map<String, MediaType> getMediaTypeMappings()
  {
    if (mediaTypeMap == null)
    {
      mediaTypeMap = new HashMap<String, MediaType>();
      mediaTypeMap.put("json", MediaType.APPLICATION_JSON_TYPE);
      mediaTypeMap.put("xml", MediaType.APPLICATION_XML_TYPE);
    }

    return mediaTypeMap;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, MediaType> mediaTypeMap;
}
