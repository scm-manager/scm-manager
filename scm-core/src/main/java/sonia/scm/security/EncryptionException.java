/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.security;

/**
 *
 * @author Sebastian Sdorra
 */
public class EncryptionException extends RuntimeException
{

  /** Field description */
  private static final long serialVersionUID = -3733681356044140444L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public EncryptionException()
  {
    super();
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   */
  public EncryptionException(String message)
  {
    super(message);
  }

  /**
   * Constructs ...
   *
   *
   * @param cause
   */
  public EncryptionException(Throwable cause)
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
  public EncryptionException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
