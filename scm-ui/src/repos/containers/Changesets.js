// @flow
import React from "react";
import { connect } from "react-redux";
import {
  ErrorNotification,
  Loading,
  Paginator
} from "@scm-manager/ui-components";

import {
  fetchChangesets,
  fetchChangesetsByNamespaceNameAndBranch,
  getChangesets,
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  selectListAsCollection
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
  list: PagedCollection
};

class Changesets extends React.Component<State, Props> {
  constructor(props) {
    super(props);
    this.state = {};
  }

  onPageChange = (link: string) => {};
  componentDidMount() {
    const { namespace, name } = this.props.repository;
    const branchName = this.props.match.params.branch;
    const {
      fetchChangesetsByNamespaceNameAndBranch,
      fetchChangesetsByNamespaceAndName,
      fetchBranchesByNamespaceAndName
    } = this.props;
    if (branchName) {
      fetchChangesetsByNamespaceNameAndBranch(namespace, name, branchName);
    } else {
      fetchChangesetsByNamespaceAndName(namespace, name);
    }
    fetchBranchesByNamespaceAndName(namespace, name);
  }

  render() {
    const { changesets, loading, error } = this.props;
    if (loading || !changesets) {
      return <Loading />;
    }
    return (
      <div>
        <ErrorNotification error={error} />
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
    history.push(
      `/repo/${repository.namespace}/${repository.name}/history/${branchName}`
    );
  };
}

const mapStateToProps = (state, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const loading = isFetchChangesetsPending(
    state,
    namespace,
    name,
    ownProps.match.params.branch
  );
  const changesets = getChangesets(
    state,
    namespace,
    name,
    ownProps.match.params.branch
  );
  const branchNames = getBranchNames(namespace, name, state);
  const error = getFetchChangesetsFailure(
    state,
    namespace,
    name,
    ownProps.match.params.branch
  );
  const list = selectListAsCollection(state);

  return {
    loading,
    changesets,
    branchNames,
    error,
    list
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetsByNamespaceAndName: (namespace: string, name: string) => {
      dispatch(fetchChangesets(namespace, name));
    },
    fetchChangesetsByNamespaceNameAndBranch: (
      namespace: string,
      name: string,
      branch: string
    ) => {
      dispatch(
        fetchChangesetsByNamespaceNameAndBranch(namespace, name, branch)
      );
    },
    fetchBranchesByNamespaceAndName: (namespace: string, name: string) => {
      dispatch(fetchBranchesByNamespaceAndName(namespace, name));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(Changesets)
);
