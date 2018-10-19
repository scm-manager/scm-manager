// @flow
import React from "react";
import { connect } from "react-redux";
import type { Repository, Branch, File } from "@scm-manager/ui-types";
import FileTree from "../components/FileTree";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import {
  fetchSources,
  getFetchSourcesFailure,
  getSources,
  isFetchSourcesPending
} from "../modules/sources";
import BranchSelector from "../../containers/BranchSelector";
import { getBranches } from "../../modules/branches";

type Props = {
  repository: Repository,
  sources: File,
  loading: boolean,
  error: Error,
  revision: string,
  path: string,
  baseUrl: string,
  branches: Branch[],
  selectedBranch: string,

  // dispatch props
  fetchSources: (
    repository: Repository,
    revision: string,
    path: string
  ) => void,

  // Context props
  history: any,
  match: any
};

class Sources extends React.Component<Props> {
  componentDidMount() {
    const { fetchSources, repository, revision, path } = this.props;

    fetchSources(repository, revision, path);
  }

  branchSelected = (branch?: Branch) => {
    let url;
    if (branch) {
      url = `${this.props.baseUrl}/${branch.revision}`;
    } else {
      url = `${this.props.baseUrl}/`;
    }
    this.props.history.push(url);
  };

  render() {
    const { sources, revision, path, baseUrl, loading, error } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (!sources || loading) {
      return <Loading />;
    }

    return (
      <>
        {this.renderBranchSelector()}
        <FileTree
          tree={sources}
          revision={revision}
          path={path}
          baseUrl={baseUrl}
        />
      </>
    );
  }

  renderBranchSelector = () => {
    const { repository, branches } = this.props;
    if (repository._links.branches) {
      return (
        <BranchSelector
          branches={branches}
          selected={(b: Branch) => {
            this.branchSelected(b);
          }}
        />
      );
    }
    return null;
  };
}

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const { revision, path } = ownProps.match.params;

  const loading = isFetchSourcesPending(state, repository, revision, path);
  const error = getFetchSourcesFailure(state, repository, revision, path);
  const branches = getBranches(state, repository);
  const sources = getSources(state, repository, revision, path);

  return {
    loading,
    error,
    sources,
    revision,
    path,
    branches
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchSources: (repository: Repository, revision: string, path: string) => {
      dispatch(fetchSources(repository, revision, path));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Sources);
