/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

package sonia.scm.repository.api;

import com.google.common.base.Preconditions;
import jakarta.annotation.Nullable;
import sonia.scm.repository.Person;
import sonia.scm.repository.spi.MergeCommand;
import sonia.scm.repository.spi.MergeCommandRequest;
import sonia.scm.repository.spi.MergeConflictResult;
import sonia.scm.repository.util.AuthorUtil;
import sonia.scm.user.DisplayUser;
import sonia.scm.user.EMail;

import java.util.Set;

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
 *                           O    &lt;- Merge result (new head of integration_branch)
 *                           |\
 *                           | \
 * old integration_branch -&gt; O  O &lt;- feature_branch
 *                           |  |
 *                           O  O
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

  @Nullable
  private final EMail eMail;

  MergeCommandBuilder(MergeCommand mergeCommand, @Nullable EMail eMail) {
    this.mergeCommand = mergeCommand;
    this.eMail = eMail;
  }

  /**
   * Use this to check if merge-strategy is supported by mergeCommand.
   *
   * @return boolean.
   */
  public boolean isSupported(MergeStrategy strategy) {
    return mergeCommand.isSupported(strategy);
  }

  /**
   * Use this to get a Set of all supported merge strategies by merge command.
   *
   * @return boolean.
   */
  public Set<MergeStrategy> getSupportedMergeStrategies() {
    return mergeCommand.getSupportedMergeStrategies();
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
   * Use this to set the author of the merge commit manually. If this is omitted, the currently logged-in user will be
   * used instead.
   *
   * This is optional and for {@link #executeMerge()} only.
   *
   * @return This builder instance.
   * @deprecated Use {@link #setAuthor(DisplayUser)} instead to enable fallback email computation.
   */
  @Deprecated
  public MergeCommandBuilder setAuthor(Person author) {
    request.setAuthor(author);
    return this;
  }

  /**
   * Use this to set the author of the merge commit manually. If this is omitted, the currently logged-in user will be
   * used instead. If the given user object does not have an email address, we will use {@link EMail} to compute a
   * fallback address.
   *
   * This is optional and for {@link #executeMerge()} only.
   *
   * @return This builder instance.
   */
  public MergeCommandBuilder setAuthor(DisplayUser author) {
    String mailAddress = eMail == null ? author.getMail() : eMail.getMailOrFallback(author);
    Person person = new Person(author.getDisplayName(), mailAddress);
    request.setAuthor(person);
    return this;
  }

  /**
   * Disables adding a verifiable signature to the merge commit.
   * @return This builder instance.
   * @since 2.4.0
   */
  public MergeCommandBuilder disableSigning() {
    request.setSign(false);
    return this;
  }

  /**
   * Use this to set the strategy of the merge commit manually.
   *
   * This is optional and for {@link #executeMerge()} only.
   *
   * @return This builder instance.
   */
  public MergeCommandBuilder setMergeStrategy(MergeStrategy strategy) {
    if (!mergeCommand.isSupported(strategy)) {
      throw new IllegalArgumentException("merge strategy not supported: " + strategy);
    }
    request.setMergeStrategy(strategy);
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
   * Use this to set the commit message. If no message is set, the message template will be used.
   *
   * This is optional and for {@link #executeMerge()} only.
   *
   * @return This builder instance.
   */
  public MergeCommandBuilder setMessage(String message) {
    request.setMessage(message);
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
    AuthorUtil.setAuthorIfNotAvailable(request, eMail);
    Preconditions.checkArgument(request.isValid(), "revision to merge and target revision is required");
    return mergeCommand.merge(request);
  }

  /**
   * Use this to check whether the given branches can be merged automatically. If this is possible,
   * {@link MergeDryRunCommandResult#isMergeable()} will return <code>true</code>.
   *
   * @return The result whether the given branches can be merged automatically.
   */
  public MergeDryRunCommandResult dryRun() {
    Preconditions.checkArgument(request.isValid(), "revision to merge and target revision is required");
    return mergeCommand.dryRun(request);
  }

  /**
   * Use this to compute concrete conflicts for a merge.
   *
   * @return A result containing all conflicts for the merge.
   */
  public MergeConflictResult conflicts() {
    Preconditions.checkArgument(request.isValid(), "revision to merge and target revision is required");
    return mergeCommand.computeConflicts(request);
  }
}
