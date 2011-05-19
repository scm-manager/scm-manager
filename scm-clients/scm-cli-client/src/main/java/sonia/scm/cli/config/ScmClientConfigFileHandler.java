/**
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * http://bitbucket.org/sdorra/scm-manager
 *
 */



package sonia.scm.cli.config;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.util.IOUtil;
import sonia.scm.util.Util;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

import java.util.UUID;
import java.util.prefs.Preferences;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.PBEParameterSpec;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;

/**
 *
 * @author Sebastian Sdorra
 */
public class ScmClientConfigFileHandler
{

  /** Field description */
  public static final String DEFAULT_CONFIG_NAME = ".scm-cli-config.enc.xml";

  /** Field description */
  public static final String ENV_CONFIG_FILE = "SCM_CLI_CONFIG";

  /** Field description */
  public static final String PREF_SECRET_KEY = "scm.client.key";

  /** Field description */
  public static final String SALT = "AE16347F";

  /** Field description */
  public static final int SPEC_ITERATION = 12;

  /** Field description */
  private static final String CIPHER_NAME = "PBEWithMD5AndDES";

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ScmClientConfigFileHandler()
  {
    prefs = Preferences.userNodeForPackage(ScmClientConfigFileHandler.class);
    key = prefs.get(PREF_SECRET_KEY, null);

    if (Util.isEmpty(key))
    {
      key = createNewKey();
      prefs.put(PREF_SECRET_KEY, key);
    }

    try
    {
      context = JAXBContext.newInstance(ScmClientConfig.class);
    }
    catch (JAXBException ex)
    {
      throw new ScmConfigException(
          "could not create JAXBContext for ScmClientConfig", ex);
    }
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   */
  public void delete()
  {
    File configFile = getConfigFile();

    if (configFile.exists() &&!configFile.delete())
    {
      throw new ScmConfigException("could not delete config file");
    }

    prefs.remove(PREF_SECRET_KEY);
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public ScmClientConfig read()
  {
    ScmClientConfig config = null;
    File configFile = getConfigFile();

    if (configFile.exists())
    {
      InputStream input = null;

      try
      {
        Cipher c = createCipher(Cipher.DECRYPT_MODE);

        input = new CipherInputStream(new FileInputStream(configFile), c);

        Unmarshaller um = context.createUnmarshaller();

        config = (ScmClientConfig) um.unmarshal(input);
      }
      catch (Exception ex)
      {
        throw new ScmConfigException("could not read config file", ex);
      }
      finally
      {
        IOUtil.close(input);
      }
    }

    return config;
  }

  /**
   * Method description
   *
   *
   * @param config
   */
  public void write(ScmClientConfig config)
  {
    File configFile = getConfigFile();
    OutputStream output = null;

    try
    {
      Cipher c = createCipher(Cipher.ENCRYPT_MODE);

      output = new CipherOutputStream(new FileOutputStream(configFile), c);

      Marshaller m = context.createMarshaller();

      m.marshal(config, output);
    }
    catch (Exception ex)
    {
      throw new ScmConfigException("could not write config file", ex);
    }
    finally
    {
      IOUtil.close(output);
    }
  }

  /**
   * Method description
   *
   *
   * @param mode
   *
   * @return
   *
   *
   * @throws InvalidAlgorithmParameterException
   * @throws InvalidKeyException
   * @throws InvalidKeySpecException
   * @throws NoSuchAlgorithmException
   * @throws NoSuchPaddingException
   */
  private Cipher createCipher(int mode)
          throws NoSuchAlgorithmException, NoSuchPaddingException,
                 InvalidKeySpecException, InvalidKeyException,
                 InvalidAlgorithmParameterException
  {
    SecretKey sk = createSecretKey();
    Cipher cipher = Cipher.getInstance(CIPHER_NAME);
    PBEParameterSpec spec = new PBEParameterSpec(SALT.getBytes(),
                              SPEC_ITERATION);

    cipher.init(mode, sk, spec);

    return cipher;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  private String createNewKey()
  {
    return UUID.randomUUID().toString();
  }

  /**
   * Method description
   *
   *
   * @return
   *
   * @throws InvalidKeySpecException
   * @throws NoSuchAlgorithmException
   */
  private SecretKey createSecretKey()
          throws NoSuchAlgorithmException, InvalidKeySpecException
  {
    PBEKeySpec keySpec = new PBEKeySpec(key.toCharArray());
    SecretKeyFactory factory = SecretKeyFactory.getInstance(CIPHER_NAME);

    return factory.generateSecret(keySpec);
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  private File getConfigFile()
  {
    File configFile = null;
    String configPath = System.getenv(ENV_CONFIG_FILE);

    if (Util.isEmpty(configPath))
    {
      configFile = new File(System.getProperty("user.home"),
                            DEFAULT_CONFIG_NAME);
    }
    else
    {
      configFile = new File(configPath);
    }

    return configFile;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private JAXBContext context;

  /** Field description */
  private String key;

  /** Field description */
  private Preferences prefs;
}
