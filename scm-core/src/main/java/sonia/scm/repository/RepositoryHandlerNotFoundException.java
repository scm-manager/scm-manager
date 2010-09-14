/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

/**
 *
 * @author Sebastian Sdorra
 */
public class RepositoryHandlerNotFoundException extends RepositoryException
{

  /** Field description */
  private static final long serialVersionUID = 5270463060802850944L;

  //~--- constructors ---------------------------------------------------------

  /**
   * Constructs ...
   *
   */
  public RepositoryHandlerNotFoundException() {}

  /**
   * Constructs ...
   *
   *
   * @param message
   */
  public RepositoryHandlerNotFoundException(String message)
  {
    super(message);
  }
}
