// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import RepositoryForm from "../components/form";
import type { Repository } from "@scm-manager/ui-types";
import {
  modifyRepo,
  isModifyRepoPending,
  getModifyRepoFailure
} from "../modules/repos";
import { withRouter } from "react-router-dom";
import type { History } from "history";
import { ErrorNotification } from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  modifyRepo: (Repository, () => void) => void,
  loading: boolean,
  error: Error,

  // context props
  t: string => string,
  history: History
};

class Edit extends React.Component<Props> {
  repoModified = () => {
    const { history, repository } = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}`);
  };

  render() {
    const { loading, error } = this.props;
    return (
      <div>
        <ErrorNotification error={error} />
        <RepositoryForm
          repository={this.props.repository}
          loading={loading}
          submitForm={repo => {
            this.props.modifyRepo(repo, this.repoModified);
          }}
        />
      </div>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const { namespace, name } = ownProps.repository;
  const loading = isModifyRepoPending(state, namespace, name);
  const error = getModifyRepoFailure(state, namespace, name);
  return {
    loading,
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
