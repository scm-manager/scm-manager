// @flow
import * as React from "react";
import type { Branch, Repository } from "@scm-manager/ui-types";
import { connect } from "react-redux";
import {
  fetchBranches,
  getBranch,
  getBranches,
  getBranchNames,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../modules/branches";
import DropDown from "../components/DropDown";
import type { History } from "history";
import { withRouter } from "react-router-dom";
import { ErrorPage, Loading } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  branches: Branch[],
  fetchBranches: Repository => void,
  history: History,
  match: any,
  selectedBranch?: Branch,
  label: string, //TODO: Should this be here?
  loading: boolean,
  branchSelected: string => void,
  error: Error,
  children: React.Node,
  t: string => string
};
type State = {
  selectedBranch?: Branch
};

class BranchChooser extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {};
  }

  componentDidMount() {
    console.log("BC CDM");
    this.props.fetchBranches(this.props.repository);
  }

  render() {
    console.log("Branch chooser render");

    const { loading, error, t, repository } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("branch-chooser.error-title")}
          subtitle={t("branch-chooser.error-subtitle")}
          error={error}
        />
      );
    }

    if (!repository) {
      return null;
    }

    if (loading) {
      return <Loading />;
    }

    const { selectedBranch } = this.state;

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
    const { label, match, branches } = this.props;
    const selectedBranchName = match.params.branch;

    if (!branches || branches.length === 0) {
      return null;
    }

    return (
      <div className={"box"}>
        <label className="label">{label}</label>
        <DropDown
          options={branches.map(b => b.name)}
          preselectedOption={selectedBranchName}
          optionSelected={this.branchSelected}
        />
      </div>
    );
  }

  branchSelected = (branch: string) => {
    for (let b of this.props.branches) {
      if (b.name === branch) {
        this.setState({ selectedBranch: b });
        this.props.branchSelected(b.name);
        break;
      }
    }
  };
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
  const selectedBranch = getBranch(
    state,
    repository,
    decodeURIComponent(match.params.branch)
  );
  const branches = getBranches(state, repository);
  return {
    // loading,
    selectedBranch,
    // error,
    branches
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("repos")(BranchChooser))
);
