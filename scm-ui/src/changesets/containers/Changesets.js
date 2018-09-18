import React from "react"
import {connect} from "react-redux";
import {ErrorNotification, Loading} from "@scm-manager/ui-components";

import {
  fetchChangesetsByNamespaceAndName, fetchChangesetsByNamespaceNameAndBranch,
  getChangesets, getFetchChangesetsFailure, isFetchChangesetsPending,
} from "../modules/changesets";
import type {History} from "history";
import {fetchBranchesByNamespaceAndName, getBranchNames} from "../../repos/modules/branches";
import type {Repository} from "@scm-manager/ui-types";
import ChangesetTable from "../components/ChangesetTable";
import DropDown from "../components/DropDown";
import {Route, withRouter} from "react-router-dom";
import ChangesetView from "../../repos/changesets/containers/ChangesetView";

type Props = {
  repository: Repository,
  branchName: string,
  history: History,
  fetchChangesetsByNamespaceNameAndBranch: (namespace: string, name: string, branch: string) => void
}


class Changesets extends React.Component<State, Props> {
  constructor(props) {
    super(props);
    this.state = {};
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

  matchedUrl = () => {
    return this.props.match.url;
  };
  render() {
    const url = this.matchedUrl();

    const {changesets, loading, error, repository} = this.props;
    if (loading || !changesets) {
      return <Loading/>
    }
    return(
      <div className="column is-three-quarters">
          <Route
            exact
            path={url}
            render={this.renderChangesets}
          />
          <Route
            exact
            path={`${url}/:branch`}
            render={this.renderChangesets}
          />
          <Route
            exact
            path={`${url}/:branch/commit/:id`}
            component={() => <ChangesetView repository={repository}/>}
          />
          <Route
            exact
            path={`${url}/commit/:id`}
            component={() => <ChangesetView repository={repository}/>}
          />
        </div>
      )


  }

  renderChangesets = () => {
    return(
      <div>
      <ErrorNotification error={this.props.error}/>
      {this.renderContent()}
    </div>
    )
  }
  renderContent = () => {
    const branch = this.props.match.params.branch;
    const {changesets, branchNames} = this.props;

    if (branchNames) {
      return (<div>
        <DropDown options={branchNames} preselectedOption={branch}
                  optionSelected={branch => this.branchChanged(branch)}/>
        <ChangesetTable changesets={changesets}/>
      </div>);
    }

    return <ChangesetTable changesets={changesets}/>
  };


  branchChanged = (branchName: string) => {
    const {history, repository} = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}/history/${branchName}`);
  };
}


const mapStateToProps = (state, ownProps: Props) => {
  const {namespace, name} = ownProps.repository;
  return {
    loading: isFetchChangesetsPending(namespace, name, state),
    changesets: getChangesets(state, namespace, name, ownProps.match.params.branch),
    branchNames: getBranchNames(namespace, name, state),
    error: getFetchChangesetsFailure(state, namespace, name, ownProps.match.params.branch)
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
