// @flow

import React from "react";

import {apiClient, BranchSelector, ErrorPage, Loading, Subtitle, SubmitButton} from "@scm-manager/ui-components";
import type {Branch, Repository} from "@scm-manager/ui-types";
import {translate} from "react-i18next";

type Props = {
  repository: Repository,
  t: string => string
};

type State = {
  loadingBranches: boolean,
  loadingDefaultBranch: boolean,
  submitPending: boolean,
  error?: Error,
  branches: Branch[],
  selectedBranchName?: string,
  defaultBranchChanged: boolean,
  disabled: boolean
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
      defaultBranchChanged: false,
      disabled: true
    };
  }

  componentDidMount() {
    const { repository } = this.props;
    this.setState({ ...this.state, loadingBranches: true });
    apiClient
      .get(repository._links.branches.href)
      .then(response => response.json())
      .then(payload => payload._embedded.branches)
      .then(branches =>
        this.setState({ ...this.state, branches, loadingBranches: false })
      )
      .catch(error => this.setState({ ...this.state, error }));

    this.setState({ ...this.state, loadingDefaultBranch: true });
    apiClient
      .get(repository._links.configuration.href)
      .then(response => response.json())
      .then(payload =>
        this.setState({
          ...this.state,
          selectedBranchName: payload.defaultBranch,
          disabled: !payload._links.update,
          loadingDefaultBranch: false
        })
      )
      .catch(error => this.setState({ ...this.state, error }));
  }

  branchSelected = (branch: Branch) => {
    if (!branch) {
      this.setState({ ...this.state, selectedBranchName: undefined, defaultBranchChanged: false});
      return;
    }
    this.setState({ ...this.state, selectedBranchName: branch.name, defaultBranchChanged: false });
  };

  submit = (event: Event) => {
    event.preventDefault();

    const { repository } = this.props;
    const newConfig = {
      defaultBranch: this.state.selectedBranchName
    };
    this.setState({ ...this.state, submitPending: true });
    apiClient
      .put(
        repository._links.configuration.href,
        newConfig,
        GIT_CONFIG_CONTENT_TYPE
      )
      .then(() =>
        this.setState({
          ...this.state,
          submitPending: false,
          defaultBranchChanged: true
        })
      )
      .catch(error => this.setState({ ...this.state, error }));
  };

  render() {
    const { t } = this.props;
    const { loadingBranches, loadingDefaultBranch, submitPending, error, disabled } = this.state;

    if (error) {
      return (
        <ErrorPage
          title={t("scm-git-plugin.repo-config.error.title")}
          subtitle={t("scm-git-plugin.repo-config.error.subtitle")}
          error={error}
        />
      );
    }

    const submitButton = disabled? null: <SubmitButton
      label={t("scm-git-plugin.repo-config.submit")}
      loading={submitPending}
      disabled={!this.state.selectedBranchName}
    />;

    if (!(loadingBranches || loadingDefaultBranch)) {
      return (
        <>
          <hr />
          <Subtitle subtitle={t("scm-git-plugin.repo-config.title")}/>
          {this.renderBranchChangedNotification()}
          <form onSubmit={this.submit}>
            <BranchSelector
              label={t("scm-git-plugin.repo-config.default-branch")}
              branches={this.state.branches}
              selected={this.branchSelected}
              selectedBranch={this.state.selectedBranchName}
              disabled={disabled}
            />
            { submitButton }
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
              this.setState({ ...this.state, defaultBranchChanged: false })
            }
          />
          {this.props.t("scm-git-plugin.repo-config.success")}
        </div>
      );
    }
    return null;
  };
}

export default translate("plugins")(RepositoryConfig);
