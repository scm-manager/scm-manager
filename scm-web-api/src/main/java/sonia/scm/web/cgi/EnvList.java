/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.cgi;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public class EnvList
{

  /**
   *    Constructs ...
   *
   */
  EnvList()
  {
    envMap = new HashMap<String, String>();
  }

  /**
   * Constructs ...
   *
   *
   * @param l
   */
  EnvList(EnvList l)
  {
    envMap = new HashMap<String, String>(l.envMap);
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String toString()
  {
    return envMap.toString();
  }

  public boolean containsKey( String key )
  {
    return envMap.containsKey(key);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Get representation suitable for passing to exec.
   *
   * @return
   */
  public String[] getEnvArray()
  {
    return envMap.values().toArray(new String[envMap.size()]);
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Set a name/value pair, null values will be treated as an empty String
   *
   * @param name
   * @param value
   */
  public void set(String name, String value)
  {
    envMap.put(name, name.concat("=").concat(Util.nonNull(value)));
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, String> envMap;
}
