import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, Repository } from "@scm-manager/ui-types";
import { BranchSelector, Breadcrumb, ErrorNotification, Loading } from "@scm-manager/ui-components";
import FileTree from "../components/FileTree";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../../branches/modules/branches";
import { compose } from "redux";
import Content from "./Content";
import {fetchSources, getSources, isDirectory} from "../modules/sources";

type Props = WithTranslation & {
  repository: Repository;
  loading: boolean;
  error: Error;
  baseUrl: string;
  branches: Branch[];
  revision: string;
  path: string;
  currentFileIsDirectory: boolean;
  sources: File;

  // dispatch props
  fetchBranches: (p: Repository) => void;
  fetchSources: (p1: Repository, p2: string, p3: string) => void;

  // Context props
  history: any;
  match: any;
  location: any;
};

type State = {
  selectedBranch: any;
};

class Sources extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      selectedBranch: null
    };
  }

  componentDidMount() {
    const { fetchBranches, repository, revision, path, fetchSources } = this.props;

    fetchBranches(repository);
    fetchSources(repository, this.decodeRevision(revision), path);

    this.redirectToDefaultBranch();
  }

  componentDidUpdate(prevProps) {
    const { fetchSources, repository, revision, path } = this.props;
    if (prevProps.revision !== revision || prevProps.path !== path) {
      fetchSources(repository, this.decodeRevision(revision), path);
    }

    this.redirectToDefaultBranch();
  }

  decodeRevision = (revision: string) => {
    return revision ? decodeURIComponent(revision) : revision;
  };

  redirectToDefaultBranch = () => {
    const { branches } = this.props;
    if (this.shouldRedirectToDefaultBranch()) {
      const defaultBranches = branches.filter(b => b.defaultBranch);

      if (defaultBranches.length > 0) {
        this.branchSelected(defaultBranches[0]);
      }
    }
  };

  shouldRedirectToDefaultBranch = () => {
    const { branches, revision } = this.props;
    return branches && !revision;
  };

  branchSelected = (branch?: Branch) => {
    const { baseUrl, history, path } = this.props;
    let url;
    if (branch) {
      this.setState({
        selectedBranch: branch
      });
      if (path) {
        url = `${baseUrl}/${encodeURIComponent(branch.name)}/${path}`;
      } else {
        url = `${baseUrl}/${encodeURIComponent(branch.name)}/`;
      }
    } else {
      this.setState({
        selectedBranch: null
      });
      url = `${baseUrl}/`;
    }
    history.push(url);
  };

  render() {
    const { repository, baseUrl, loading, error, revision, path, currentFileIsDirectory } = this.props;

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
          {this.renderBreadcrumb()}
          <FileTree repository={repository} revision={revision} path={path} baseUrl={baseUrl} />
        </div>
      );
    } else {
      return <Content repository={repository} revision={revision} path={path} />;
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

  renderBreadcrumb = () => {
    const { revision, path, baseUrl, branches, sources, repository } = this.props;
    const { selectedBranch } = this.state;

    return (
      <Breadcrumb
        repository={repository}
        revision={revision}
        path={path}
        baseUrl={baseUrl}
        branch={selectedBranch}
        defaultBranch={
          branches && branches.filter(b => b.defaultBranch === true)[0]
        }
        sources={sources}
      />
    );
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
  const sources = getSources(state, repository, decodedRevision, path);

  return {
    repository,
    revision,
    path,
    loading,
    error,
    branches,
    currentFileIsDirectory,
    sources
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
  withTranslation("repos"),
  withRouter,
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(Sources);
