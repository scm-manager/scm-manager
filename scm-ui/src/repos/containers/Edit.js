// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { Page } from "../../components/layout";
import RepositoryForm from "../components/form";
import type { Repository } from "../types/Repositories";
import {
  modifyRepo,
  isModifyRepoPending,
  getModifyRepoFailure
} from "../modules/repos";
import { withRouter } from "react-router-dom";
import type { History } from "history";

type Props = {
  repository: Repository,
  modifyRepo: (Repository, () => void) => void,
  modifyLoading: boolean,
  error: Error,

  // context props
  t: string => string,
  history: History
};

class Edit extends React.Component<Props> {
  componentDidMount() {}

  repoModified = () => {
    const { history, repository } = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}`);
  };

  render() {
    const { t, modifyLoading, error } = this.props;
    return (
      <Page
        title={t("edit.title")}
        subtitle={t("edit.subtitle")}
        error={error}
        showContentOnError={true}
      >
        <RepositoryForm
          repository={this.props.repository}
          loading={modifyLoading}
          submitForm={repo => {
            this.props.modifyRepo(repo, this.repoModified);
          }}
        />
      </Page>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const { namespace, name } = ownProps.repository;

  const modifyLoading = isModifyRepoPending(state, namespace, name);

  const error = getModifyRepoFailure(state, namespace, name);

  return {
    modifyLoading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    modifyRepo: (repo: Repository, callback: () => void) => {
      dispatch(modifyRepo(repo, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(withRouter(Edit)));
