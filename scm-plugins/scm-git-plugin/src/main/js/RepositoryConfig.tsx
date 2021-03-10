/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React, { FormEvent } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, Repository, Link } from "@scm-manager/ui-types";
import {
  apiClient,
  BranchSelector,
  Checkbox,
  ErrorPage,
  Loading,
  Subtitle,
  Level,
  SubmitButton
} from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
};

type State = {
  loadingBranches: boolean;
  loadingDefaultBranch: boolean;
  submitPending: boolean;
  error?: Error;
  branches: Branch[];
  selectedBranchName: string;
  nonFastForwardDisallowed: boolean;
  defaultBranchChanged: boolean;
  disabled: boolean;
};

const GIT_CONFIG_CONTENT_TYPE = "application/vnd.scmm-gitConfig+json";

class RepositoryConfig extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      loadingBranches: true,
      loadingDefaultBranch: true,
      submitPending: false,
      branches: [],
      selectedBranchName: "",
      nonFastForwardDisallowed: false,
      defaultBranchChanged: false,
      disabled: true
    };
  }

  componentDidMount() {
    const { repository } = this.props;
    this.setState({
      ...this.state,
      loadingBranches: true
    });
    const branchesLink = repository._links.branches as Link;
    apiClient
      .get(branchesLink.href)
      .then(response => response.json())
      .then(payload => payload._embedded.branches)
      .then(branches =>
        this.setState({
          ...this.state,
          branches,
          loadingBranches: false
        })
      )
      .catch(error =>
        this.setState({
          ...this.state,
          error
        })
      );

    const configurationLink = repository._links.configuration as Link;
    this.setState({
      ...this.state,
      loadingDefaultBranch: true
    });
    apiClient
      .get(configurationLink.href)
      .then(response => response.json())
      .then(payload =>
        this.setState({
          ...this.state,
          selectedBranchName: payload.defaultBranch,
          nonFastForwardDisallowed: payload.nonFastForwardDisallowed,
          disabled: !payload._links.update,
          loadingDefaultBranch: false
        })
      )
      .catch(error =>
        this.setState({
          ...this.state,
          error
        })
      );
  }

  branchSelected = (branch?: Branch) => {
    if (!branch) {
      this.setState({
        ...this.state,
        selectedBranchName: "",
        defaultBranchChanged: false
      });
    } else {
      this.setState({
        ...this.state,
        selectedBranchName: branch.name,
        defaultBranchChanged: false
      });
    }
  };

  onNonFastForwardDisallowed = (value: boolean) => {
    this.setState({
        nonFastForwardDisallowed: value
      });
  };

  submit = (event: FormEvent) => {
    event.preventDefault();

    const { repository } = this.props;
    const { selectedBranchName, nonFastForwardDisallowed } = this.state;
    const newConfig = {
      defaultBranch: selectedBranchName,
      nonFastForwardDisallowed
    };
    this.setState({
      ...this.state,
      submitPending: true
    });
    const configurationLink = repository._links.configuration as Link;
    apiClient
      .put(configurationLink.href, newConfig, GIT_CONFIG_CONTENT_TYPE)
      .then(() =>
        this.setState({
          ...this.state,
          submitPending: false,
          defaultBranchChanged: true
        })
      )
      .catch(error =>
        this.setState({
          ...this.state,
          error
        })
      );
  };

  render() {
    const { t } = this.props;
    const { loadingBranches, loadingDefaultBranch, submitPending, error, disabled } = this.state;

    if (error) {
      return (
        <ErrorPage
          title={t("scm-git-plugin.repoConfig.error.title")}
          subtitle={t("scm-git-plugin.repoConfig.error.subtitle")}
          error={error}
        />
      );
    }

    const submitButton = disabled ? null : (
      <Level
        right={
          <SubmitButton
            label={t("scm-git-plugin.repoConfig.submit")}
            loading={submitPending}
            disabled={!this.state.selectedBranchName}
          />
        }
      />
    );

    if (!(loadingBranches || loadingDefaultBranch)) {
      const { branches, selectedBranchName, nonFastForwardDisallowed } = this.state;
      return (
        <>
          <hr />
          <Subtitle subtitle={t("scm-git-plugin.repoConfig.title")} />
          {this.renderBranchChangedNotification()}
          <form onSubmit={this.submit}>
            <BranchSelector
              label={t("scm-git-plugin.repoConfig.defaultBranch")}
              branches={branches}
              onSelectBranch={this.branchSelected}
              selectedBranch={selectedBranchName}
              disabled={disabled}
            />
            <Checkbox
              name="nonFastForwardDisallowed"
              label={t("scm-git-plugin.repoConfig.nonFastForwardDisallowed")}
              helpText={t("scm-git-plugin.repoConfig.nonFastForwardDisallowedHelpText")}
              checked={nonFastForwardDisallowed}
              onChange={this.onNonFastForwardDisallowed}
              disabled={disabled}
            />
            {submitButton}
          </form>
        </>
      );
    } else {
      return <Loading />;
    }
  }

  renderBranchChangedNotification = () => {
    if (this.state.defaultBranchChanged) {
      return (
        <div className="notification is-primary">
          <button
            className="delete"
            onClick={() =>
              this.setState({
                ...this.state,
                defaultBranchChanged: false
              })
            }
          />
          {this.props.t("scm-git-plugin.repoConfig.success")}
        </div>
      );
    }
    return null;
  };
}

export default withTranslation("plugins")(RepositoryConfig);
