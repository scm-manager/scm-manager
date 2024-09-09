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
  SubmitButton,
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
  changesSubmitted: boolean;
  disabled: boolean;
  changed: boolean;
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
      changesSubmitted: false,
      disabled: true,
      changed: false,
    };
  }

  componentDidMount() {
    const { repository } = this.props;
    this.setState({
      loadingBranches: true,
    });
    const branchesLink = repository._links.branches as Link;
    apiClient
      .get(branchesLink.href)
      .then((response) => response.json())
      .then((payload) => payload._embedded.branches)
      .then((branches) =>
        this.setState({
          branches,
          loadingBranches: false,
        })
      )
      .catch((error) =>
        this.setState({
          error,
        })
      );

    const configurationLink = repository._links.configuration as Link;
    this.setState({
      loadingDefaultBranch: true,
    });
    apiClient
      .get(configurationLink.href)
      .then((response) => response.json())
      .then((payload) => {
        const defaultBranch =
          payload.defaultBranch || this.state.branches?.filter((b: Branch) => b.defaultBranch)[0]?.name;
        this.setState({
          selectedBranchName: defaultBranch,
          nonFastForwardDisallowed: payload.nonFastForwardDisallowed,
          disabled: !payload._links.update,
          loadingDefaultBranch: false,
          changed: false,
        });
      })
      .catch((error) =>
        this.setState({
          error,
        })
      );
  }

  branchSelected = (branch?: Branch) => {
    if (!branch) {
      this.setState({
        selectedBranchName: "",
        changesSubmitted: false,
        changed: true,
      });
    } else {
      this.setState({
        selectedBranchName: branch.name,
        changesSubmitted: false,
        changed: true,
      });
    }
  };

  onNonFastForwardDisallowed = (value: boolean) => {
    this.setState({
      nonFastForwardDisallowed: value,
      changed: true,
    });
  };

  submit = (event: FormEvent) => {
    event.preventDefault();

    const { repository } = this.props;
    const { selectedBranchName, nonFastForwardDisallowed } = this.state;
    const newConfig = {
      defaultBranch: selectedBranchName,
      nonFastForwardDisallowed,
    };
    this.setState({
      submitPending: true,
    });
    const configurationLink = repository._links.configuration as Link;
    apiClient
      .put(configurationLink.href, newConfig, GIT_CONFIG_CONTENT_TYPE)
      .then(() =>
        this.setState({
          submitPending: false,
          changesSubmitted: true,
          changed: false,
        })
      )
      .catch((error) =>
        this.setState({
          error,
        })
      );
  };

  render() {
    const { t } = this.props;
    const { loadingBranches, loadingDefaultBranch, submitPending, error, disabled, changed } = this.state;

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
          <SubmitButton label={t("scm-git-plugin.repoConfig.submit")} loading={submitPending} disabled={!changed} />
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
    if (this.state.changesSubmitted) {
      return (
        <div className="notification is-primary">
          <button
            className="delete"
            onClick={() =>
              this.setState({
                changesSubmitted: false,
                changed: false,
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
