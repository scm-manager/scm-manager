// @flow
import * as React from "react";
import type { Branch, Repository } from "@scm-manager/ui-types";
import { connect } from "react-redux";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../modules/branches";
import DropDown from "../components/DropDown";
import type { History } from "history";
import { ErrorPage, Loading } from "@scm-manager/ui-components";
import { translate } from "react-i18next";

type Props = {
  repository: Repository,
  label: string, //TODO: Should this be here?
  onChange: string => void,
  children: React.Node,
  selected?: string,

  // State props
  branches: Branch[],
  error: Error,
  loading: boolean,

  // Dispatch props
  fetchBranches: Repository => void,

  // Context props
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
    this.props.fetchBranches(this.props.repository);
  }

  render() {

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
    const { label, branches, selected } = this.props;

    if (!branches || branches.length === 0) {
      return null;
    }

    return (
      <div className={"box"}>
        <label className="label">{label}</label>
        <DropDown
          options={branches.map(b => b.name)}
          preselectedOption={
            this.state.selectedBranch
              ? this.state.selectedBranch.name
              : selected
          }
          optionSelected={(branchName: string) => {
            this.branchSelected(branchName, true);
          }}
        />
      </div>
    );
  }

  branchSelected = (branch: string, changed: boolean) => {
    for (let b of this.props.branches) {
      if (b.name === branch) {
        this.updateBranch(branch, b, changed);
        break;
      }
    }
  };

  updateBranch = (branchName: string, branch: Branch, changed: boolean) => {
    this.setState(
      prevState => {
        if (
          !prevState.selectedBranch ||
          branchName !== prevState.selectedBranch.name
        ) {
          return { selectedBranch: branch };
        }
      },
      () => {
        if (changed) {
          this.props.onChange(branch.name);
        }
      }
    );
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
  const { repository } = ownProps;
  const loading = isFetchBranchesPending(state, repository);
  const error = getFetchBranchesFailure(state, repository);

  const branches = getBranches(state, repository);
  return {
    loading,
    error,
    branches
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(BranchChooser));
