/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryException extends Exception
{

  /** Field description */
  private static final long serialVersionUID = -4939196278070910058L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public RepositoryException()
  {
    super();
  }

  /**
   * Constructs ...
   *
   *
   * @param message
   */
  public RepositoryException(String message)
  {
    super(message);
  }

  /**
   * Constructs ...
   *
   *
   * @param cause
   */
  public RepositoryException(Throwable cause)
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
  public RepositoryException(String message, Throwable cause)
  {
    super(message, cause);
  }
}
