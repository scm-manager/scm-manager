// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import {
  ErrorNotification,
  Loading,
  Paginator
} from "@scm-manager/ui-components";

import {
  fetchChangesets,
  fetchChangesetsByBranchAndPage,
  fetchChangesetsByLink,
  fetchChangesetsByPage,
  getChangesetsFromState,
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  selectListAsCollection
} from "../modules/changesets";
import type { History } from "history";
import type {
  Changeset,
  PagedCollection,
  Repository,
  Branch
} from "@scm-manager/ui-types";
import ChangesetList from "../components/changesets/ChangesetList";
import { withRouter } from "react-router-dom";
import { fetchBranches, getBranch, getBranchNames } from "../modules/branches";
import BranchChooser from "./BranchChooser";

type Props = {
  repository: Repository,
  branchName: string,
  branchNames: string[],
  history: History,
  fetchChangesetsByNamespaceNameAndBranch: (
    namespace: string,
    name: string,
    branch: string
  ) => void,
  list: PagedCollection,
  fetchChangesetsByLink: (Repository, string, Branch) => void,
  fetchChangesetsByPage: (Repository, number) => void,
  fetchChangesetsByBranchAndPage: (Repository, Branch, number) => void,
  fetchBranches: Repository => void,
  page: number,
  t: string => string,
  match: any,
  changesets: Changeset[],
  loading: boolean,
  error: boolean,
  branch: Branch
};

type State = {
  branch: string
};

class Changesets extends React.PureComponent<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      branch: ""
    };
  }

  onPageChange = (link: string) => {
    const { repository, branch } = this.props;
    this.props.fetchChangesetsByLink(repository, link, branch);
  };

  componentDidMount() {
    if (!this.props.loading) {
      this.updateContent();
    }
  }

  updateContent() {
    const {
      repository,
      branch,
      page,
      fetchChangesetsByPage,
      fetchChangesetsByBranchAndPage
    } = this.props;
    if (branch) {
      fetchChangesetsByBranchAndPage(repository, branch, page);
    } else {
      fetchChangesetsByPage(repository, page);
    }
  }

  componentDidUpdate(prevProps: Props) {
    const { page, list, repository, match } = this.props;
    const { namespace, name } = repository;
    const branch = decodeURIComponent(match.params.branch);
    if (!this.props.loading) {
      if (prevProps.branch !== this.props.branch) {
        this.setState({ branch });
        this.updateContent();
      }

      if (list && (list.page || list.page === 0)) {
        // backend starts paging at 0
        const statePage: number = list.page + 1;
        if (page !== statePage) {
          if (branch) {
            this.props.history.push(
              `/repo/${namespace}/${name}/${branch}/changesets/${statePage}`
            );
          } else {
            this.props.history.push(
              `/repo/${namespace}/${name}/changesets/${statePage}`
            );
          }
        }
      }
    }
  }

  render() {
    const { changesets, loading, error } = this.props;
    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading || !changesets) {
      return <Loading />;
    }

    return (
      <div>
        {this.renderList()}
        {this.renderPaginator()}
      </div>
    );
  }

  renderList = () => {
    const branch = decodeURIComponent(this.props.match.params.branch);
    const { repository, changesets, t } = this.props;

    return (
      <>
        <div className={"box"}>
          <label className="label">
            {t("changesets.branchselector-label")}
          </label>
          <BranchChooser
            repository={repository}
            selectedBranchName={branch}
            callback={branch => this.branchChanged(branch)}
          />
        </div>
        <ChangesetList repository={repository} changesets={changesets} />
      </>
    );
  };

  renderPaginator() {
    const { list } = this.props;
    if (list) {
      return <Paginator collection={list} onPageChange={this.onPageChange} />;
    }
    return null;
  }

  branchChanged = (branch: Branch): void => {
    const { history, repository } = this.props;
    if (branch === undefined) {
      history.push(
        `/repo/${repository.namespace}/${repository.name}/changesets`
      );
    } else {
      const branchName = encodeURIComponent(branch.name);
      this.setState({ branch: branchName });
      history.push(
        `/repo/${repository.namespace}/${
          repository.name
        }/${branchName}/changesets`
      );
    }
  };
}

const getPageFromProps = props => {
  let page = props.match.params.page;
  if (page) {
    page = parseInt(page, 10);
  } else {
    page = 1;
  }
  return page;
};

const mapStateToProps = (state, ownProps: Props) => {
  const { repository } = ownProps;
  const branchName = ownProps.match.params.branch;
  const branch = getBranch(state, repository, branchName);
  const loading = isFetchChangesetsPending(state, repository, branch);
  const changesets = getChangesetsFromState(state, repository);
  const branchNames = getBranchNames(state, repository);
  const error = getFetchChangesetsFailure(state, repository, branch);
  const list = selectListAsCollection(state, repository);
  const page = getPageFromProps(ownProps);

  return {
    loading,
    changesets,
    branchNames,
    error,
    list,
    page,
    branch
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchBranches: (repository: Repository) => {
      dispatch(fetchBranches(repository));
    },
    fetchChangesets: (repository: Repository) => {
      dispatch(fetchChangesets(repository));
    },
    fetchChangesetsByPage: (repository, page: number) => {
      dispatch(fetchChangesetsByPage(repository, page));
    },
    fetchChangesetsByBranchAndPage: (
      repository,
      branch: Branch,
      page: number
    ) => {
      dispatch(fetchChangesetsByBranchAndPage(repository, branch, page));
    },
    fetchChangesetsByLink: (
      repository: Repository,
      link: string,
      branch?: Branch
    ) => {
      dispatch(fetchChangesetsByLink(repository, link, branch));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("repos")(Changesets))
);
