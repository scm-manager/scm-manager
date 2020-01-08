import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import RepositoryForm from "../components/form";
import DeleteRepo from "./DeleteRepo";
import { Repository } from "@scm-manager/ui-types";
import { modifyRepo, isModifyRepoPending, getModifyRepoFailure, modifyRepoReset } from "../modules/repos";
import { History } from "history";
import { ErrorNotification } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { compose } from "redux";

type Props = {
  loading: boolean;
  error: Error;

  modifyRepo: (p1: Repository, p2: () => void) => void;
  modifyRepoReset: (p: Repository) => void;

  // context props
  repository: Repository;
  history: History;
  match: any;
};

class EditRepo extends React.Component<Props> {
  componentDidMount() {
    const { modifyRepoReset, repository } = this.props;
    modifyRepoReset(repository);
  }

  repoModified = () => {
    const { history, repository } = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}`);
  };

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { loading, error, repository } = this.props;

    const url = this.matchedUrl();

    const extensionProps = {
      repository,
      url
    };

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
        <ExtensionPoint name="repo-config.route" props={extensionProps} renderAll={true} />
        <DeleteRepo repository={repository} />
      </div>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const loading = isModifyRepoPending(state, namespace, name);
  const error = getModifyRepoFailure(state, namespace, name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    modifyRepo: (repo: Repository, callback: () => void) => {
      dispatch(modifyRepo(repo, callback));
    },
    modifyRepoReset: (repo: Repository) => {
      dispatch(modifyRepoReset(repo));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withRouter)(EditRepo);
