import React, { FormEvent } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, Repository, Link } from "@scm-manager/ui-types";
import { apiClient, BranchSelector, ErrorPage, Loading, Subtitle, SubmitButton } from "@scm-manager/ui-components";

type Props = WithTranslation & {
  repository: Repository;
};

type State = {
  loadingBranches: boolean;
  loadingDefaultBranch: boolean;
  submitPending: boolean;
  error?: Error;
  branches: Branch[];
  selectedBranchName?: string;
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
        selectedBranchName: undefined,
        defaultBranchChanged: false
      });
      return;
    }
    this.setState({
      ...this.state,
      selectedBranchName: branch.name,
      defaultBranchChanged: false
    });
  };

  submit = (event: FormEvent) => {
    event.preventDefault();

    const { repository } = this.props;
    const newConfig = {
      defaultBranch: this.state.selectedBranchName
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
          title={t("scm-git-plugin.repo-config.error.title")}
          subtitle={t("scm-git-plugin.repo-config.error.subtitle")}
          error={error}
        />
      );
    }

    const submitButton = disabled ? null : (
      <SubmitButton
        label={t("scm-git-plugin.repo-config.submit")}
        loading={submitPending}
        disabled={!this.state.selectedBranchName}
      />
    );

    if (!(loadingBranches || loadingDefaultBranch)) {
      return (
        <>
          <hr />
          <Subtitle subtitle={t("scm-git-plugin.repo-config.title")} />
          {this.renderBranchChangedNotification()}
          <form onSubmit={this.submit}>
            <BranchSelector
              label={t("scm-git-plugin.repo-config.default-branch")}
              branches={this.state.branches}
              selected={this.branchSelected}
              selectedBranch={this.state.selectedBranchName}
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
          {this.props.t("scm-git-plugin.repo-config.success")}
        </div>
      );
    }
    return null;
  };
}

export default withTranslation("plugins")(RepositoryConfig);
