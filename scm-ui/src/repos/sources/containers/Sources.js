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
import { fetchBranches, getBranches } from "../../modules/branches";

type Props = {
  repository: Repository,
  sources: File,
  loading: boolean,
  error: Error,
  revision: string,
  path: string,
  baseUrl: string,
  branches: Branch[],

  // dispatch props
  fetchBranches: Repository => void,
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
    const {
      fetchSources,
      fetchBranches,
      repository,
      revision,
      path
    } = this.props;

    fetchBranches(this.props.repository);
    fetchSources(repository, revision, path);
  }

  branchSelected = (branch?: Branch) => {
    const { path, baseUrl, history } = this.props;
    let url;
    if (branch) {
      if (path) {
        url = `${baseUrl}/${branch.name}/${path}`;
      } else {
        url = `${baseUrl}/${branch.name}`;
      }
    } else {
      url = `${baseUrl}/`;
    }
    history.push(url);
  };

  findSelectedBranch = () => {
    const { revision, branches } = this.props;
    return branches.find((branch: Branch) => branch.name === revision);
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
  const { repository, match } = ownProps;
  const { revision, path } = match.params;

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
    },
    fetchBranches: (repository: Repository) => {
      dispatch(fetchBranches(repository));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Sources);
