/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

/**
 *
 * @author Sebastian Sdorra
 */
public interface CommandResult
{

  /**
   * Method description
   *
   *
   * @return
   */
  public String getOutput();

  /**
   * Method description
   *
   *
   * @return
   */
  public int getReturnCode();

  /**
   * Method description
   *
   *
   * @return
   */
  public boolean isSuccessfull();
}
