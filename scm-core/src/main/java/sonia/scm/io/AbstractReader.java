/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author Sebastian Sdorra
 *
 * @param <T>
 */
public abstract class AbstractReader<T>
{

  /**
   * Method description
   *
   *
   * @param input
   *
   * @return
   *
   * @throws IOException
   */
  public abstract T read(InputStream input) throws IOException;

  /**
   * Method description
   *
   *
   * @param data
   *
   * @return
   *
   * @throws IOException
   */
  public T read(byte[] data) throws IOException
  {
    return read(new ByteArrayInputStream(data));
  }

  /**
   * Method description
   *
   *
   * @param path
   *
   * @return
   *
   * @throws IOException
   */
  public T read(String path) throws IOException
  {
    return read(new File(path));
  }

  /**
   * Method description
   *
   *
   * @param file
   *
   * @return
   *
   * @throws IOException
   */
  public T read(File file) throws IOException
  {
    return read(new FileInputStream(file));
  }
}
