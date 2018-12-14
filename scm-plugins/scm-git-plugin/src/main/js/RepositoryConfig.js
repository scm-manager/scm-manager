// @flow

import React from "react";

import {apiClient, BranchSelector, ErrorPage, Loading, SubmitButton} from "@scm-manager/ui-components";
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
  error: Error,
  branches: Branch[],
  selectedBranchName: string
};

const GIT_CONFIG_CONTENT_TYPE = "application/vnd.scmm-gitConfig+json";

class RepositoryConfig extends React.Component<Props, State> {
  state = {
    branches: []
  };

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
      .then(payload => payload.defaultBranch)
      .then(selectedBranchName =>
        this.setState({
          ...this.state,
          selectedBranchName,
          loadingDefaultBranch: false
        })
      )
      .catch(error => this.setState({ ...this.state, error }));
  }

  branchSelected = (branch: Branch) => {
    if (!branch) {
      this.setState({ ...this.state, selectedBranchName: null });
    }
    this.setState({ ...this.state, selectedBranchName: branch.name });
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
      .then(() => this.setState({ ...this.state, submitPending: false }))
      .catch(error => this.setState({ ...this.state, error }));
  };

  render() {
    const { t, error } = this.props;
    const { loadingBranches, loadingDefaultBranch, submitPending } = this.state;

    if (error) {
      return (
        <ErrorPage
          title={t("scm-git-plugin.repo-config.error.title")}
          subtitle={t("scm-git-plugin.repo-config.error.subtitle")}
          error={error}
        />
      );
    }
    if (!(loadingBranches || loadingDefaultBranch)) {

      return (
        <form onSubmit={this.submit}>
          <BranchSelector
            label={t("scm-git-plugin.repo-config.default-branch")}
            branches={this.state.branches}
            selected={this.branchSelected}
            selectedBranch={this.state.selectedBranchName}
          />
          <SubmitButton
            label={t("scm-git-plugin.repo-config.submit")}
            loading={submitPending}
            disabled={
              !this.state.selectedBranchName
            }
          />
        </form>
      );
    } else {
      return <Loading />;
    }
  }
}

export default translate("plugins")(RepositoryConfig);
