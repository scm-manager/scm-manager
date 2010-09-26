/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

//~--- JDK imports ------------------------------------------------------------

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sebastian Sdorra
 */
public abstract class AbstractResourceProcessor implements ResourceProcessor
{

  /**
   * Method description
   *
   *
   * @param variableMap
   * @param reader
   * @param writer
   *
   * @throws IOException
   */
  protected abstract void process(Map<String, String> variableMap,
                                  BufferedReader reader, BufferedWriter writer)
          throws IOException;

  /**
   * Method description
   *
   *
   * @param key
   * @param value
   */
  @Override
  public void addVariable(String key, String value)
  {
    variableMap.put(key, value);
  }

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   *
   * @throws IOException
   */
  @Override
  public void process(InputStream input, OutputStream output) throws IOException
  {
    process(new InputStreamReader(input), new OutputStreamWriter(output));
  }

  /**
   * Method description
   *
   *
   * @param input
   * @param output
   *
   * @throws IOException
   */
  @Override
  public void process(File input, File output) throws IOException
  {
    process(new FileReader(input), new FileWriter(input));
  }

  /**
   * Method description
   *
   *
   * @param reader
   * @param writer
   *
   * @throws IOException
   */
  @Override
  public void process(Reader reader, Writer writer) throws IOException
  {
    BufferedReader bufferedReader = null;
    BufferedWriter bufferedWriter = null;

    if (reader instanceof BufferedReader)
    {
      bufferedReader = (BufferedReader) reader;
    }
    else
    {
      bufferedReader = new BufferedReader(reader);
    }

    if (writer instanceof BufferedWriter)
    {
      bufferedWriter = (BufferedWriter) writer;
    }
    else
    {
      bufferedWriter = new BufferedWriter(writer);
    }

    process(variableMap, bufferedReader, bufferedWriter);
  }

  /**
   * Method description
   *
   *
   * @param key
   */
  @Override
  public void removeVariable(String key)
  {
    variableMap.remove(key);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private Map<String, String> variableMap = new HashMap<String, String>();
}
