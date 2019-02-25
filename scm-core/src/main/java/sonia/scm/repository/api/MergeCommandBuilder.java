package sonia.scm.repository.api;

import com.google.common.base.Preconditions;
import sonia.scm.repository.Person;
import sonia.scm.repository.spi.MergeCommand;
import sonia.scm.repository.spi.MergeCommandRequest;

/**
 * Use this {@link MergeCommandBuilder} to merge two branches of a repository ({@link #executeMerge()}) or to check if
 * the branches could be merged without conflicts ({@link #dryRun()}). To do so, you have to specify the name of
 * the target branch ({@link #setTargetBranch(String)}) and the name of the branch that should be merged
 * ({@link #setBranchToMerge(String)}). Additionally you can specify an author that should be used for the commit
 * ({@link #setAuthor(Person)}) and a message template ({@link #setMessageTemplate(String)}) if you are not doing a dry
 * run only. If no author is specified, the logged in user and a default message will be used instead.
 *
 * To actually merge <code>feature_branch</code> into <code>integration_branch</code> do this:
 * <pre><code>
 *     repositoryService.getMergeCommand()
 *       .setBranchToMerge("feature_branch")
 *       .setTargetBranch("integration_branch")
 *       .executeMerge();
 * </code></pre>
 *
 * If the merge is successful, the result will look like this:
 * <pre><code>
 *                            O    <- Merge result (new head of integration_branch)
 *                            |\
 *                            | \
 *  old integration_branch -> O  O <- feature_branch
 *                            |  |
 *                            O  O
 * </code></pre>
 *
 * To check whether they can be merged without conflicts beforehand do this:
 * <pre><code>
 *     repositoryService.getMergeCommand()
 *       .setBranchToMerge("feature_branch")
 *       .setTargetBranch("integration_branch")
 *       .dryRun()
 *       .isMergeable();
 * </code></pre>
 *
 * Keep in mind that you should <em>always</em> check the result of a merge even though you may have done a dry run
 * beforehand, because the branches can change between the dry run and the actual merge.
 *
 * @since 2.0.0
 */
public class MergeCommandBuilder {

  private final MergeCommand mergeCommand;
  private final MergeCommandRequest request = new MergeCommandRequest();

  MergeCommandBuilder(MergeCommand mergeCommand) {
    this.mergeCommand = mergeCommand;
  }

  /**
   * Use this to set the branch that should be merged into the target branch.
   *
   * <b>This is mandatory.</b>
   *
   * @return This builder instance.
   */
  public MergeCommandBuilder setBranchToMerge(String branchToMerge) {
    request.setBranchToMerge(branchToMerge);
    return this;
  }

  /**
   * Use this to set the target branch the other branch should be merged into.
   *
   * <b>This is mandatory.</b>
   *
   * @return This builder instance.
   */
  public MergeCommandBuilder setTargetBranch(String targetBranch) {
    request.setTargetBranch(targetBranch);
    return this;
  }

  /**
   * Use this to set the author of the merge commit manually. If this is omitted, the currently logged in user will be
   * used instead.
   *
   * This is optional and for {@link #executeMerge()} only.
   *
   * @return This builder instance.
   */
  public MergeCommandBuilder setAuthor(Person author) {
    request.setAuthor(author);
    return this;
  }

  /**
   * Use this to set a template for the commit message. If no message is set, a default message will be used.
   *
   * You can use the placeholder <code>{0}</code> for the branch to be merged and <code>{1}</code> for the target
   * branch, eg.:
   *
   * <pre><code>
   * ...setMessageTemplate("Merge of {0} into {1}")...
   * </code></pre>
   *
   * This is optional and for {@link #executeMerge()} only.
   *
   * @return This builder instance.
   */
  public MergeCommandBuilder setMessageTemplate(String messageTemplate) {
    request.setMessageTemplate(messageTemplate);
    return this;
  }

  /**
   * Use this to reset the command.
   * @return This builder instance.
   */
  public MergeCommandBuilder reset() {
    request.reset();
    return this;
  }

  /**
   * Use this to actually do the merge. If an automatic merge is not possible, {@link MergeCommandResult#isSuccess()}
   * will return <code>false</code>.
   *
   * @return The result of the merge.
   */
  public MergeCommandResult executeMerge() {
    Preconditions.checkArgument(request.isValid(), "revision to merge and target revision is required");
    return mergeCommand.merge(request);
  }

  /**
   * Use this to check whether the given branches can be merged autmatically. If this is possible,
   * {@link MergeDryRunCommandResult#isMergeable()} will return <code>true</code>.
   *
   * @return The result whether the given branches can be merged automatically.
   */
  public MergeDryRunCommandResult dryRun() {
    Preconditions.checkArgument(request.isValid(), "revision to merge and target revision is required");
    return mergeCommand.dryRun(request);
  }
}
