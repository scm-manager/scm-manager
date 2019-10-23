import React from "react";
import { compose } from "redux";
import { connect } from "react-redux";
import { Route, withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, Repository } from "@scm-manager/ui-types";
import { BranchSelector, ErrorNotification, Loading } from "@scm-manager/ui-components";
import Changesets from "./Changesets";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../branches/modules/branches";

type Props = WithTranslation & {
  repository: Repository;
  baseUrl: string;
  selected: string;
  baseUrlWithBranch: string;
  baseUrlWithoutBranch: string;

  // State props
  branches: Branch[];
  loading: boolean;
  error: Error;

  // Dispatch props
  fetchBranches: (p: Repository) => void;

  // Context props
  history: any; // TODO flow type
  match: any;
};

class ChangesetsRoot extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchBranches(this.props.repository);
    this.redirectToDefaultBranch();
  }

  redirectToDefaultBranch = () => {
    if (this.shouldRedirectToDefaultBranch()) {
      const defaultBranches = this.props.branches.filter(b => b.defaultBranch === true);
      if (defaultBranches.length > 0) {
        this.branchSelected(defaultBranches[0]);
      }
    }
  };

  shouldRedirectToDefaultBranch = () => {
    return (
      this.props.branches &&
      this.props.branches.length > 0 &&
      this.props.selected !== this.props.branches.filter(b => b.defaultBranch === true)[0]
    );
  };

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  branchSelected = (branch?: Branch) => {
    let url;
    if (branch) {
      url = `${this.props.baseUrlWithBranch}/${encodeURIComponent(branch.name)}/changesets/`;
    } else {
      url = `${this.props.baseUrlWithoutBranch}/`;
    }
    this.props.history.push(url);
  };

  findSelectedBranch = () => {
    const { selected, branches } = this.props;
    return branches.find((branch: Branch) => branch.name === selected);
  };

  render() {
    const { repository, error, loading, match, branches } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }

    if (!repository) {
      return null;
    }

    const url = this.stripEndingSlash(match.url);
    const branch = branches ? this.findSelectedBranch() : null;
    const changesets = <Changesets repository={repository} branch={branch} />;

    return (
      <div className="panel">
        {this.renderBranchSelector()}
        <Route path={`${url}/:page?`} component={() => changesets} />
      </div>
    );
  }

  renderBranchSelector = () => {
    const { repository, branches, selected, t } = this.props;
    if (repository._links.branches) {
      return (
        <div className="panel-heading">
          <BranchSelector
            label={t("changesets.branchSelectorLabel")}
            branches={branches}
            selectedBranch={selected}
            selected={(b: Branch) => {
              this.branchSelected(b);
            }}
          />
        </div>
      );
    }
    return null;
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
  const branches = getBranches(state, repository);
  const selected = decodeURIComponent(match.params.branch);

  return {
    loading,
    error,
    branches,
    selected
  };
};

export default compose(
  withRouter,
  withTranslation("repos"),
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(ChangesetsRoot);
