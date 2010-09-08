/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.io;

/**
 *
 * @author Sebastian Sdorra
 */
public class SimpleCommandResult implements CommandResult
{

  /**
   * Constructs ...
   *
   *
   * @param output
   * @param returnCode
   */
  public SimpleCommandResult(String output, int returnCode)
  {
    this.output = output;
    this.returnCode = returnCode;
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public String getOutput()
  {
    return output;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public int getReturnCode()
  {
    return returnCode;
  }

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public boolean isSuccessfull()
  {
    return returnCode == 0;
  }

  //~--- fields ---------------------------------------------------------------

  /** Field description */
  private String output;

  /** Field description */
  private int returnCode;
}
