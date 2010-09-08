/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class INISection
{

  /**
   * Constructs ...
   *
   *
   * @param name
   */
  public INISection(String name)
  {
    this.name = name;
    this.parameters = new LinkedHashMap<String, String>();
  }

  /**
   * Constructs ...
   *
   *
   * @param name
   * @param parameters
   */
  public INISection(String name, Map<String, String> parameters)
  {
    this.name = name;
    this.parameters = parameters;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   */
  public void removeParameter(String key)
  {
    parameters.put(key, name);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    String s = System.getProperty("line.separator");
    StringBuilder out = new StringBuilder();

    out.append("[").append(name).append("]").append(s);

    for (Map.Entry<String, String> entry : parameters.entrySet())
    {
      out.append(entry.getKey()).append(" = ").append(entry.getValue());
      out.append(s);
    }

    return out.toString();
  }

  //~--- get methods ----------------------------------------------------------

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
   * @param key
   *
   * @return
   */
  public String getParameter(String key)
  {
    return parameters.get(key);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<String> getParameterKeys()
  {
    return parameters.keySet();
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  public void setParameter(String key, String value)
  {
    parameters.put(key, value);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String name;

  /** Field description */
  private Map<String, String> parameters;
}
