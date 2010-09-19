/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;

/**
 *
 * @author Sebastian Sdorra
 */
public class INIConfigurationWriter extends AbstractWriter<INIConfiguration>
{

  /**
   * Method description
   *
   *
   * @param object
   * @param output
   *
   * @throws IOException
   */
  @Override
  public void write(INIConfiguration object, OutputStream output)
          throws IOException
  {
    PrintWriter writer = null;

    try
    {
      writer = new PrintWriter(output);

      for (INISection section : object.getSections())
      {
        writer.println(section.toString());
      }

      writer.flush();
    }
    finally
    {
      Util.close(writer);
    }
  }
}
