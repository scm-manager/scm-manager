// @flow
import React from "react";
import {connect} from "react-redux";
import {withRouter} from "react-router-dom";
import type {Branch, Repository} from "@scm-manager/ui-types";
import FileTree from "../components/FileTree";
import {BranchSelector, Breadcrumb, ErrorNotification, Loading} from "@scm-manager/ui-components";
import {translate} from "react-i18next";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../../branches/modules/branches";
import {compose} from "redux";
import Content from "./Content";
import {fetchSources, isDirectory} from "../modules/sources";

type Props = {
  repository: Repository,
  loading: boolean,
  error: Error,
  baseUrl: string,
  branches: Branch[],
  revision: string,
  path: string,
  currentFileIsDirectory: boolean,

  // dispatch props
  fetchBranches: Repository => void,
  fetchSources: (Repository, string, string) => void,

  // Context props
  history: any,
  match: any,
  t: string => string
};

class Sources extends React.Component<Props> {
  componentDidMount() {
    const {
      fetchBranches,
      repository,
      revision,
      path,
      fetchSources
    } = this.props;

    fetchBranches(repository);
    fetchSources(repository, revision, path);
  }
  componentDidUpdate(prevProps) {
    const { fetchSources, repository, revision, path } = this.props;
    if (prevProps.revision !== revision || prevProps.path !== path) {
      fetchSources(repository, revision, path);
    }
  }

  branchSelected = (branch?: Branch) => {
    const { baseUrl, history, path } = this.props;
    let url;
    if (branch) {
      if (path) {
        url = `${baseUrl}/${encodeURIComponent(branch.name)}/${path}`;
      } else {
        url = `${baseUrl}/${encodeURIComponent(branch.name)}/`;
      }
    } else {
      url = `${baseUrl}/`;
    }
    history.push(url);
  };

  render() {
    const {
      repository,
      baseUrl,
      loading,
      error,
      revision,
      path,
      currentFileIsDirectory
    } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }

    if (currentFileIsDirectory) {
      return (
        <div className="panel">
          {this.renderBranchSelector()}
          <Breadcrumb revision={encodeURIComponent(revision)} path={path} baseUrl={baseUrl} />
          <FileTree
            repository={repository}
            revision={revision}
            path={path}
            baseUrl={baseUrl}
          />
        </div>
      );
    } else {
      return (
        <Content repository={repository} revision={revision} path={path} />
      );
    }
  }

  renderBranchSelector = () => {
    const { branches, revision, t } = this.props;

    if (branches) {
      return (
        <div className="panel-heading">
          <BranchSelector
            branches={branches}
            selectedBranch={revision}
            label={t("changesets.branchSelectorLabel")}
            selected={(b: Branch) => {
              this.branchSelected(b);
            }}
          />
        </div>
      );
    }
    return null;
  };
}

const mapStateToProps = (state, ownProps) => {
  const { repository, match } = ownProps;
  const { revision, path } = match.params;
  const decodedRevision = revision ? decodeURIComponent(revision) : undefined;
  const loading = isFetchBranchesPending(state, repository);
  const error = getFetchBranchesFailure(state, repository);
  const branches = getBranches(state, repository);
  const currentFileIsDirectory = decodedRevision
    ? isDirectory(state, repository, decodedRevision, path)
    : isDirectory(state, repository, revision, path);

  return {
    repository,
    revision: decodedRevision,
    path,
    loading,
    error,
    branches,
    currentFileIsDirectory
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchBranches: (repository: Repository) => {
      dispatch(fetchBranches(repository));
    },
    fetchSources: (repository: Repository, revision: string, path: string) => {
      dispatch(fetchSources(repository, revision, path));
    }
  };
};

export default compose(
  translate("repos"),
  withRouter,
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(Sources);
