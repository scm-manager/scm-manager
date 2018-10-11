// @flow
import * as React from "react";
import type { Branch, Repository } from "@scm-manager/ui-types";
import { connect } from "react-redux";
import {
  fetchBranches,
  getBranch,
  getBranchNames,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../modules/branches";
import DropDown from "../components/DropDown";
import type { History } from "history";
import { withRouter } from "react-router-dom";
import { ErrorPage, Loading } from "@scm-manager/ui-components";

type Props = {
  repository: Repository,
  branches: Branch[],
  branchNames: string[],
  fetchBranches: Repository => void,
  history: History,
  match: any,
  selectedBranch?: Branch,
  label: string, //TODO: Should this be here?
  loading: boolean,
  branchSelected: string => void,
  error: Error,
  children: React.Node
};
type State = {};

class BranchChooser extends React.Component<Props, State> {
  componentDidMount() {
    this.props.fetchBranches(this.props.repository);
  }

  render() {
    const { selectedBranch, loading, error } = this.props;

    // TODO: i18n
    if (error) {
      return (
        <ErrorPage
          title={"Failed loading branches"}
          subtitle={"Somethin went wrong"}
          error={error}
        />
      );
    }

    if (loading) {
      return <Loading />;
    }

    const childrenWithBranch = React.Children.map(
      this.props.children,
      child => {
        return React.cloneElement(child, {
          branch: selectedBranch
        });
      }
    );

    return (
      <>
        {this.renderBranchChooser()}
        {childrenWithBranch}
      </>
    );
  }

  renderBranchChooser() {
    const { branchNames, label, branchSelected, match } = this.props;
    const selectedBranchName = match.params.branch;

    if (!branchNames || branchNames.length === 0) {
      return null;
    }

    return (
      <div className={"box"}>
        <label className="label">{label}</label>
        <DropDown
          options={branchNames}
          preselectedOption={selectedBranchName}
          optionSelected={branch => branchSelected(branch)}
        />
      </div>
    );
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchBranches: (repo: Repository) => {
      dispatch(fetchBranches(repo));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, match } = ownProps;
  const loading = isFetchBranchesPending(state, repository);
  const error = getFetchBranchesFailure(state, repository);
  const branchNames = getBranchNames(state, repository);
  const selectedBranch = getBranch(
    state,
    repository,
    decodeURIComponent(match.params.branch)
  );
  return {
    loading,
    branchNames,
    selectedBranch,
    error
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(BranchChooser)
);
