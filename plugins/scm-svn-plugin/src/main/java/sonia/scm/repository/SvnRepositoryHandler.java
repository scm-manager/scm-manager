/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



package sonia.scm.repository;

//~--- non-JDK imports --------------------------------------------------------

import sonia.scm.io.ExtendedCommand;

//~--- JDK imports ------------------------------------------------------------

import java.io.File;

/**
 *
 * @author Sebastian Sdorra
 */
public class SvnRepositoryHandler
        extends AbstractSimpleRepositoryHandler<SvnConfig>
{

  /** Field description */
  public static final String TYPE_DISPLAYNAME = "Subversion";

  /** Field description */
  public static final String TYPE_NAME = "svn";

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
    return new ExtendedCommand(config.getSvnAdminBinary(), "create",
                               directory.getPath());
  }

  //~--- get methods ----------------------------------------------------------

  /**
   * Method description
   *
   *
   * @return
   */
  @Override
  protected Class<SvnConfig> getConfigClass()
  {
    return SvnConfig.class;
  }
}
