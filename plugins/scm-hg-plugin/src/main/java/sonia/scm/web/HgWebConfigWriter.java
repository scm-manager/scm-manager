/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.io.INIConfiguration;
import sonia.scm.io.INIConfigurationWriter;
import sonia.scm.io.INISection;
import sonia.scm.io.RegexResourceProcessor;
import sonia.scm.io.ResourceProcessor;
import sonia.scm.repository.HgConfig;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.servlet.ServletContext;

/**
 *
 * @author Sebastian Sdorra
 */
public class HgWebConfigWriter
{

  /** Field description */
  public static final String CGI_TEMPLATE = "/sonia/scm/hgweb.cgi";

  /** Field description */
  public static final String CONFIG_NAME = "hgweb.config";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   *
   * @param config
   */
  public HgWebConfigWriter(HgConfig config)
  {
    this.config = config;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param context
   *
   * @throws IOException
   */
  public void write(ServletContext context) throws IOException
  {
    File webConfigFile = new File(config.getRepositoryDirectory(), CONFIG_NAME);

    writeWebConfigFile(webConfigFile);

    File cgiFile = HgUtil.getCGI();

    writeCGIFile(cgiFile, webConfigFile);
  }

  /**
   * Method description
   *
   *
   * @param cgiFile
   * @param webConfigFile
   *
   * @throws IOException
   */
  private void writeCGIFile(File cgiFile, File webConfigFile) throws IOException
  {
    InputStream input = null;
    OutputStream output = null;

    try
    {
      input = HgWebConfigWriter.class.getResourceAsStream(CGI_TEMPLATE);
      output = new FileOutputStream(cgiFile);

      ResourceProcessor rp = new RegexResourceProcessor();

      rp.addVariable("python", config.getPythonBinary());
      rp.addVariable("config", webConfigFile.getAbsolutePath());
      rp.process(input, output);
      cgiFile.setExecutable(true);
    }
    finally
    {
      Util.close(input);
      Util.close(output);
    }
  }

  /**
   * Method description
   *
   *
   * @param webConfigFile
   *
   * @throws IOException
   */
  private void writeWebConfigFile(File webConfigFile) throws IOException
  {
    INIConfiguration webConfig = new INIConfiguration();
    INISection pathsSection = new INISection("paths");
    String path = config.getRepositoryDirectory().getAbsolutePath();

    if (!path.endsWith(File.separator))
    {
      path = path.concat(File.separator);
    }

    pathsSection.setParameter("/", path.concat("*"));
    webConfig.addSection(pathsSection);
    new INIConfigurationWriter().write(webConfig, webConfigFile);
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private HgConfig config;
}
