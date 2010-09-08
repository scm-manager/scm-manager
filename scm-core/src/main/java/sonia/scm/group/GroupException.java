/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.group;

/**
 *
 * @author Sebastian Sdorra
 */
public class GroupException extends Exception
{

  /** Field description */
  private static final long serialVersionUID = 5191341492098994225L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public GroupException()
  {
    super();
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   */
  public GroupException(String message)
  {
    super(message);
  }

  /**
   * Constructs ...
   *
   *
   * @param cause
   */
  public GroupException(Throwable cause)
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
  public GroupException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
