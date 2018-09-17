import React from "react"
import {connect} from "react-redux";

import {
  fetchChangesetsByNamespaceAndName, fetchChangesetsByNamespaceNameAndBranch,
  getChangesets,
} from "../modules/changesets";
import {translate} from "react-i18next";
import {fetchBranchesByNamespaceAndName, getBranchNames} from "../../repos/modules/branches";
import type {Repository} from "@scm-manager/ui-types";
import ChangesetTable from "../components/ChangesetTable";
import DropDown from "../components/DropDown";

type Props = {
  repository: Repository,
  branchName: string,
  history: History,
  fetchChangesetsByNamespaceNameAndBranch: (namespace: string, name: string, branch: string) => void
}

type State = {
  branchName: string
}

class Changesets extends React.Component<State, Props> {
  constructor(props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    const {namespace, name} = this.props.repository;
    this.props.fetchChangesetsByNamespaceNameAndBranch(namespace, name, this.props.branchName);
    this.props.fetchBranchesByNamespaceAndName(namespace, name);
  }

  render() {
    const {changesets, branchNames} = this.props;
    if (changesets === null) {
      return null
    }
    if (branchNames) {
      return <div>
        <DropDown options={branchNames} optionSelected={this.branchChanged}/>
        <ChangesetTable changesets={changesets}/>
      </div>;
    } else {
      return <ChangesetTable changesets={changesets}/>
    }

  }

  branchChanged = (branchName: string) => {
    this.props.history.push(branchName)
  };
}


const mapStateToProps = (state, ownProps: Props) => {
  console.log("mapStateToProps ownProps: ", ownProps);
  const {namespace, name} = ownProps.repository;
  return {
    changesets: getChangesets(namespace, name, ownProps.branchName, state),
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

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Changesets);
