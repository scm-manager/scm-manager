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
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  selectListAsCollection,
  fetchChangesetsByLink,
  getChangesetsFromState,
  fetchChangesetsByPage,
  fetchChangesetsByBranchAndPage,
  fetchChangesets
} from "../modules/changesets";
import type { History } from "history";
import type { PagedCollection, Repository } from "@scm-manager/ui-types";
import ChangesetList from "../components/changesets/ChangesetList";
import DropDown from "../components/DropDown";
import { withRouter } from "react-router-dom";
import { fetchBranches, getBranchNames } from "../modules/branches";

type Props = {
  repository: Repository,
  branchName: string,
  history: History,
  fetchChangesetsByNamespaceNameAndBranch: (
    namespace: string,
    name: string,
    branch: string
  ) => void,
  list: PagedCollection,
  fetchChangesetsByLink: string => void,
  page: number,
  t: string => string
};

type State = {
  branch: string
};

class Changesets extends React.PureComponent<State, Props> {
  constructor(props) {
    super(props);
    this.state = {};
  }

  onPageChange = (link: string) => {
    const { repository } = this.props;
    const branch = this.props.match.params.branch;
    this.props.fetchChangesetsByLink(repository, link, branch);
  };

  componentDidMount() {
    this.updateContent();
  }

  updateContent() {
    const { repository } = this.props;

    const branchName = this.props.match.params.branch;
    const {
      fetchChangesetsByPage,
      fetchChangesetsByBranchAndPage,
      fetchBranches
    } = this.props;
    if (branchName) {
      fetchChangesetsByBranchAndPage(repository, branchName, this.props.page);
    } else {
      fetchChangesetsByPage(repository, this.props.page);
    }
    fetchBranches(repository);
  }

  componentDidUpdate(prevProps: Props, prevState: State, snapshot: any) {
    const { page, list, repository, match } = this.props;
    const { namespace, name } = repository;
    const branch = match.params.branch;

    if (branch !== prevState.branch) {
      this.updateContent();
      this.setState({ branch });
    }

    if (list && (list.page || list.page === 0)) {
      // backend starts paging at 0
      const statePage: number = list.page + 1;
      if (page !== statePage) {
        if (branch) {
          this.props.history.push(
            `/repo/${namespace}/${name}/${branch}/history/${statePage}`
          );
        } else {
          this.props.history.push(
            `/repo/${namespace}/${name}/history/${statePage}`
          );
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
        {this.renderTable()}
        {this.renderPaginator()}
      </div>
    );
  }

  renderTable = () => {
    const branch = this.props.match.params.branch;
    const { repository, changesets, branchNames, t } = this.props;

    if (branchNames && branchNames.length > 0) {
      return (
        <div>
          <label className="label">
            {t("changesets.branchselector-label")}
          </label>
          <DropDown
            options={branchNames}
            preselectedOption={branch}
            optionSelected={branch => this.branchChanged(branch)}
          />
          <ChangesetList repository={repository} changesets={changesets} />
        </div>
      );
    }

    return <ChangesetList repository={repository} changesets={changesets} />;
  };

  renderPaginator() {
    const { list } = this.props;
    if (list) {
      return <Paginator collection={list} onPageChange={this.onPageChange} />;
    }
    return null;
  }

  branchChanged = (branchName: string): void => {
    const { history, repository } = this.props;
    if (branchName === undefined || branchName === "") {
      history.push(`/repo/${repository.namespace}/${repository.name}/history`);
    } else {
      history.push(
        `/repo/${repository.namespace}/${repository.name}/${branchName}/history`
      );
    }
  };
}

const createKey = (
  namespace: string,
  name: string,
  branch?: string
): string => {
  let key = `${namespace}/${name}`;
  if (branch) {
    key = key + `/${branch}`;
  }
  return key;
};

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
  const { namespace, name } = ownProps.repository;
  const { branch } = ownProps.match.params;
  const key = createKey(namespace, name, branch);
  const loading = isFetchChangesetsPending(state, repository, branch);
  const changesets = getChangesetsFromState(state, key);
  const branchNames = getBranchNames(state, repository);
  const error = getFetchChangesetsFailure(state, repository, branch);
  const list = selectListAsCollection(state, key);
  const page = getPageFromProps(ownProps);

  return {
    loading,
    changesets,
    branchNames,
    error,
    list,
    page
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
      branch: string,
      page: number
    ) => {
      dispatch(fetchChangesetsByBranchAndPage(repository, branch, page));
    },
    fetchChangesetsByLink: (
      repository: Repository,
      link: string,
      branch?: string
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
