import React from "react"
import {connect} from "react-redux";
import {Loading} from "@scm-manager/ui-components";

import {
  fetchChangesetsByNamespaceAndName, fetchChangesetsByNamespaceNameAndBranch,
  getChangesets, isFetchChangesetsPending,
} from "../modules/changesets";
import type { History } from "history";
import {fetchBranchesByNamespaceAndName, getBranchNames} from "../../repos/modules/branches";
import type {Repository} from "@scm-manager/ui-types";
import ChangesetTable from "../components/ChangesetTable";
import DropDown from "../components/DropDown";
import {withRouter} from "react-router-dom";

type Props = {
  repository: Repository,
  branchName: string,
  history: History,
  fetchChangesetsByNamespaceNameAndBranch: (namespace: string, name: string, branch: string) => void
}


class Changesets extends React.Component<State, Props> {
  constructor(props) {
    super(props);
    this.state = {
    };
  }

  componentDidMount() {
    const {namespace, name} = this.props.repository;
    const branchName = this.props.match.params.branch;
    if (branchName) {
      this.props.fetchChangesetsByNamespaceNameAndBranch(namespace, name, branchName);
    } else {
      this.props.fetchChangesetsByNamespaceAndName(namespace, name);
    }
    this.props.fetchBranchesByNamespaceAndName(namespace, name);
  }

  render() {
    const {changesets, branchNames, loading} = this.props;
    const branch = this.props.match.params.branch;
    if (loading || !changesets) {
      return <Loading />
    }
    if (branchNames) {
      return <div>
        <DropDown options={branchNames} preselectedOption={branch} optionSelected={branch => this.branchChanged(branch)}/>
        <ChangesetTable changesets={changesets}/>
      </div>;
    } else {
      return <ChangesetTable changesets={changesets}/>
    }

  }

  branchChanged = (branchName: string) => {
    const { history, repository } = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}/history/${branchName}`);
  };
}


const mapStateToProps = (state, ownProps: Props) => {
  const {namespace, name} = ownProps.repository;
  return {
    loading: isFetchChangesetsPending(namespace, name, state),
    changesets: getChangesets(state, namespace, name, ownProps.match.params.branch),
    branchNames: getBranchNames(namespace, name, state)
  }
};

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetsByNamespaceAndName: (namespace: string, name: string) => {
      dispatch(fetchChangesetsByNamespaceAndName(namespace, name));
    },
    fetchChangesetsByNamespaceNameAndBranch: (namespace: string, name: string, branch: string) => {
      dispatch(fetchChangesetsByNamespaceNameAndBranch(namespace, name, branch));
    },
    fetchBranchesByNamespaceAndName: (namespace: string, name: string) => {
      dispatch(fetchBranchesByNamespaceAndName(namespace, name));
    }
  }
};

export default withRouter(connect(
  mapStateToProps,
  mapDispatchToProps
)(Changesets));
