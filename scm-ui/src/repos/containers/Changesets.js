// @flow
import React from "react";
import { connect } from "react-redux";
import {
  ErrorNotification,
  Loading,
  Page,
  Paginator
} from "@scm-manager/ui-components";

import {
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  selectListAsCollection,
  fetchChangesetsByLink,
  getChangesetsFromState,
  fetchChangesetsByPage,
  fetchChangesetsByBranchAndPage
} from "../modules/changesets";
import type { History } from "history";
import {
  fetchBranchesByNamespaceAndName,
  getBranchNames
} from "../../repos/modules/branches";
import type { PagedCollection, Repository } from "@scm-manager/ui-types";
import ChangesetList from "../components/ChangesetList";
import DropDown from "../components/DropDown";
import { withRouter } from "react-router-dom";

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
  page: number
};

class Changesets extends React.PureComponent<State, Props> {
  constructor(props) {
    super(props);
    this.state = {};
  }

  onPageChange = (link: string) => {
    const { namespace, name } = this.props.repository;
    const branch = this.props.match.params.branch;
    this.props.fetchChangesetsByLink(namespace, name, link, branch);
  };

  componentDidMount() {
    const { namespace, name } = this.props.repository;
    const branchName = this.props.match.params.branch;
    const {
      fetchBranchesByNamespaceAndName,
      fetchChangesetsByPage,
      fetchChangesetsByBranchAndPage
    } = this.props;
    if (branchName) {
      fetchChangesetsByBranchAndPage(
        namespace,
        name,
        branchName,
        this.props.page
      );
    } else {
      fetchChangesetsByPage(namespace, name, this.props.page);
    }
    fetchBranchesByNamespaceAndName(namespace, name);
  }

  componentDidUpdate() {
    const { page, list, repository, match } = this.props;
    const { namespace, name } = repository;
    const branch = match.params.branch;

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

    if (loading || !changesets) {
      return <Loading />;
    }

    if (error) {
      return <ErrorNotification error={error} />;
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
    const { repository, changesets, branchNames } = this.props;

    if (branchNames && branchNames.length > 0) {
      return (
        <div>
          <label className="label">Branch: </label>
          <DropDown
            options={branchNames}
            preselectedOption={branch}
            optionSelected={branch => this.branchChanged(branch)}
          />
          <ChangesetList repository={repository} changesets={changesets} />
        </div>
      );
    }

    return <ChangesetList changesets={changesets} />;
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
    history.push(
      `/repo/${repository.namespace}/${repository.name}/${branchName}/history`
    );
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
  const { namespace, name } = ownProps.repository;
  const { branch } = ownProps.match.params;
  const key = createKey(namespace, name, branch);
  const loading = isFetchChangesetsPending(state, namespace, name, branch);
  const changesets = getChangesetsFromState(state, key);
  const branchNames = getBranchNames(namespace, name, state);
  const error = getFetchChangesetsFailure(state, namespace, name, branch);
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
    fetchBranchesByNamespaceAndName: (namespace: string, name: string) => {
      dispatch(fetchBranchesByNamespaceAndName(namespace, name));
    },
    fetchChangesetsByPage: (namespace: string, name: string, page: number) => {
      dispatch(fetchChangesetsByPage(namespace, name, page));
    },
    fetchChangesetsByBranchAndPage: (
      namespace: string,
      name: string,
      branch: string,
      page: number
    ) => {
      dispatch(fetchChangesetsByBranchAndPage(namespace, name, branch, page));
    },
    fetchChangesetsByLink: (
      namespace: string,
      name: string,
      link: string,
      branch?: string
    ) => {
      dispatch(fetchChangesetsByLink(namespace, name, link, branch));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(Changesets)
);
