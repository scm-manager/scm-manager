/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.web.plugin;

//~--- non-JDK imports --------------------------------------------------------

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import sonia.scm.security.EncryptionHandler;
import sonia.scm.web.security.Authenticator;

//~--- JDK imports ------------------------------------------------------------

import javax.xml.bind.annotation.XmlElement;

/**
 *
 * @author Sebastian Sdorra
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class SecurityConfig
{

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<? extends Authenticator> getAuthenticator()
  {
    return authenticator;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  public Class<? extends EncryptionHandler> getEncryptionHandler()
  {
    return encryptionHandler;
  }

  //~--- set methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param authenticator
   */
  public void setAuthenticator(Class<? extends Authenticator> authenticator)
  {
    this.authenticator = authenticator;
  }

  /**
   * Method description
   *
   *
   * @param encryptionHandler
   */
  public void setEncryptionHandler(
          Class<? extends EncryptionHandler> encryptionHandler)
  {
    this.encryptionHandler = encryptionHandler;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  @XmlElement(name = "authenticator")
  private Class<? extends Authenticator> authenticator;

  /** Field description */
  @XmlElement(name = "encryption-handler")
  private Class<? extends EncryptionHandler> encryptionHandler;
}
