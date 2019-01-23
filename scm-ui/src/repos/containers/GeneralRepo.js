// @flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import RepositoryForm from "../components/form";
import DeleteRepo from "../components/DeleteRepo";
import type { Repository } from "@scm-manager/ui-types";
import {
  modifyRepo,
  deleteRepo,
  isModifyRepoPending,
  getModifyRepoFailure,
  modifyRepoReset
} from "../modules/repos";
import type { History } from "history";
import { ErrorNotification } from "@scm-manager/ui-components";

type Props = {
  loading: boolean,
  error: Error,

  modifyRepo: (Repository, () => void) => void,
  modifyRepoReset: Repository => void,
  deleteRepo: (Repository, () => void) => void,

  // context props
  repository: Repository,
  history: History
};

class GeneralRepo extends React.Component<Props> {
  componentDidMount() {
    const { modifyRepoReset, repository } = this.props;
    modifyRepoReset(repository);
  }

  repoModified = () => {
    const { history, repository } = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}`);
  };

  deleted = () => {
    this.props.history.push("/repos");
  };

  delete = (repository: Repository) => {
    this.props.deleteRepo(repository, this.deleted);
  };

  render() {
    const { loading, error, repository } = this.props;
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
        <hr />
        <DeleteRepo repository={repository} delete={this.delete} />
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
    },
    modifyRepoReset: (repo: Repository) => {
      dispatch(modifyRepoReset(repo));
    },
    deleteRepo: (repo: Repository, callback: () => void) => {
      dispatch(deleteRepo(repo, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(GeneralRepo));
