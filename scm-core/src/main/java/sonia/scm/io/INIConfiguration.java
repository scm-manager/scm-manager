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
public class INIConfiguration
{

  /**
   * Constructs ...
   *
   */
  public INIConfiguration()
  {
    this.sectionMap = new LinkedHashMap<String, INISection>();
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param section
   */
  public void addSection(INISection section)
  {
    sectionMap.put(section.getName(), section);
  }

  /**
   * Method description
   *
   *
   * @param name
   */
  public void removeSection(String name)
  {
    sectionMap.remove(name);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param name
   *
   * @return
   */
  public INISection getSection(String name)
  {
    return sectionMap.get(name);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Collection<INISection> getSections()
  {
    return sectionMap.values();
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, INISection> sectionMap;
}
