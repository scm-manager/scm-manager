/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.server;

/**
 *
 * @author Sebastian Sdorra
 */
public class ServerException extends Exception
{

  /** Field description */
  private static final long serialVersionUID = 2936673332739265774L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public ServerException() {}

  /**
   * Constructs ...
   *
   *
   * @param message
   */
  public ServerException(String message)
  {
    super(message);
  }

  /**
   * Constructs ...
   *
   *
   * @param cause
   */
  public ServerException(Throwable cause)
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
  public ServerException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
