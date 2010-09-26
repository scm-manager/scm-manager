/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractWriter<T>
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
  public abstract void write(T object, OutputStream output) throws IOException;

  /**
   * Method description
   *
   *
   * @param object
   * @param file
   *
   * @throws IOException
   */
  public void write(T object, File file) throws IOException
  {
    write(object, new FileOutputStream(file));
  }

  /**
   * Method description
   *
   *
   * @param object
   * @param path
   *
   * @throws IOException
   */
  public void write(T object, String path) throws IOException
  {
    write(object, new File(path));
  }
}
