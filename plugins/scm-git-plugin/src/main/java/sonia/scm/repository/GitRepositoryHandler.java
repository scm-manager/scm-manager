/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.io.CommandResult;
import sonia.scm.io.ExtendedCommand;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;
import java.io.IOException;

/**
 *
 * @author Sebastian Sdorra
 */
public class GitRepositoryHandler
        extends AbstractSimpleRepositoryHandler<GitConfig>
{

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "Git";

  /** Field description */
  public static final String TYPE_NAME = "git";

  /** Field description */
  public static final RepositoryType TYPE = new RepositoryType(TYPE_NAME,
                                              TYPE_DISPLAYNAME);

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  public RepositoryType getType()
  {
    return TYPE;
  }

  //~--- methods --------------------------------------------------------------

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @return
   */
  @Override
  protected ExtendedCommand buildCreateCommand(Repository repository,
          File directory)
  {
    return new ExtendedCommand(config.getGitBinary(), "init", "--bare",
                               directory.getPath());
  }

  /**
   * Method description
   *
   *
   * @param repository
   * @param directory
   *
   * @throws IOException
   * @throws RepositoryException
   */
  @Override
  protected void postCreate(Repository repository, File directory)
          throws IOException, RepositoryException
  {
    ExtendedCommand command = new ExtendedCommand(config.getGitBinary(),
                                "update-server-info");

    command.setWorkDirectory(directory);

    CommandResult result = command.execute();

    if (!result.isSuccessfull())
    {
      StringBuilder msg = new StringBuilder("command exit with error ");

      msg.append(result.getReturnCode()).append(" and message: '");
      msg.append(result.getOutput()).append("'");

      throw new RepositoryException(msg.toString());
    }
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Class<GitConfig> getConfigClass()
  {
    return GitConfig.class;
  }
}
