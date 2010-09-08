/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm;

/**
 *
 * @author Sebastian Sdorra
 */
public class ConfigurationException extends RuntimeException
{

  /** Field description */
  private static final long serialVersionUID = 3462977946341972841L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ConfigurationException()
  {
    super();
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   */
  public ConfigurationException(String message)
  {
    super(message);
  }

  /**
   * Constructs ...
   *
   *
   * @param cause
   */
  public ConfigurationException(Throwable cause)
  {
    super(cause);
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   * @param cause
   */
  public ConfigurationException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
