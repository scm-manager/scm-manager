/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Reader;
import java.io.Writer;

/**
 *
 * @author Sebastian Sdorra
 */
public interface ResourceProcessor
{

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  public void addVariable(String key, String value);

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   *
   * @throws IOException
   */
  public void process(InputStream input, OutputStream output)
          throws IOException;

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   *
   * @throws IOException
   */
  public void process(File input, File output) throws IOException;

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   *
   * @throws IOException
   */
  public void process(Reader reader, Writer writer) throws IOException;

  /**
   * Method description
   *
   *
   * @param key
   */
  public void removeVariable(String key);
}
