/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.util;

//~--- JDK imports ------------------------------------------------------------

import java.util.Date;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 *
 * @author Sebastian Sdorra
 */
public class DateAdapter extends XmlAdapter<String, Date>
{

  /**
   * Method description
   *
   *
   * @param data
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public String marshal(Date data) throws Exception
  {
    return Util.formatDate(data);
  }

  /**
   * Method description
   *
   *
   * @param string
   *
   * @return
   *
   * @throws Exception
   */
  @Override
  public Date unmarshal(String string) throws Exception
  {
    return Util.parseDate(string);
  }
}
