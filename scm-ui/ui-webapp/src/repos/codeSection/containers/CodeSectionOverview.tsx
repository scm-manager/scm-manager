import React from "react";
import styled from "styled-components";
import { Route, withRouter, RouteComponentProps } from "react-router-dom";
import ChangesetView from "../../containers/ChangesetView";
import Sources from "../../sources/containers/Sources";
import SourceExtensions from "../../sources/containers/SourceExtensions";
import ChangesetsRoot from "../../containers/ChangesetsRoot";
import { Repository, Branch } from "@scm-manager/ui-types";
import { BranchSelector, Level } from "@scm-manager/ui-components";
import CodeViewSwitcher from "../components/CodeViewSwitcher";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { connect } from "react-redux";
import {
  fetchBranches,
  getBranches,
  getFetchBranchesFailure,
  isFetchBranchesPending
} from "../../branches/modules/branches";

type Props = RouteComponentProps &
  WithTranslation & {
    repository: Repository;
    baseUrl: string;

    // State props
    branches: Branch[];
    loading: boolean;
    error: Error;
    selectedView: string;
    selectedBranch: string;

    // Dispatch props
    fetchBranches: (p: Repository) => void;
  };

const CodeSectionActionBar = styled.div.attrs(() => ({}))`
  background-color: whitesmoke;
  border: 1px solid #dbdbdb;
  border-radius: 4px;
  color: #363636;
  font-size: 1.25em;
  font-weight: 300;
  line-height: 1.25;
  padding: 0.5em 0.75em;
  margin-bottom: 1em;
`;

class CodeSectionOverview extends React.Component<Props> {
  componentDidMount() {
    const { repository } = this.props;
    this.props.fetchBranches(repository);
  }

  findSelectedBranch = () => {
    const { selectedBranch, branches } = this.props;
    return branches?.find((branch: Branch) => branch.name === selectedBranch);
  };

  branchSelected = (branch?: Branch) => {
    let url;
    if (branch) {
      url = `${this.props.baseUrl}/${this.props.selectedView}/${encodeURIComponent(branch.name)}`;
    } else {
      url = `${this.props.baseUrl}/${this.props.selectedView}/`;
    }
    this.props.history.push(url);
  };

  render() {
    const { repository, baseUrl, branches, t } = this.props;
    const url = baseUrl;

    return (
      <div>
        <CodeSectionActionBar>
          <Level
            left={
              branches?.length > 0 && (
                <BranchSelector
                  label={t("code.branchSelector")}
                  branches={branches}
                  selectedBranch={this.props.selectedBranch}
                  selected={(b: Branch) => {
                    this.branchSelected(b);
                  }}
                />
              )
            }
            right={<CodeViewSwitcher url={this.props.location.pathname} />}
          />
        </CodeSectionActionBar>
        <Route exact path={`${url}/changeset/:id`} render={() => <ChangesetView repository={repository} />} />
        <Route
          path={`${url}/sources`}
          exact={true}
          render={() => <Sources repository={repository} baseUrl={`${url}/sources`} />}
        />
        <Route
          path={`${url}/sources/:revision/:path*`}
          render={() => <Sources repository={repository} baseUrl={`${url}/sources`} />}
        />
        <Route
          path={`${url}/sourceext/:extension`}
          exact={true}
          render={() => <SourceExtensions repository={repository} />}
        />
        <Route
          path={`${url}/sourceext/:extension/:revision/:path*`}
          render={() => <SourceExtensions repository={repository} />}
        />
        <Route
          path={`${url}/changesets`}
          exact={true}
          render={() => <ChangesetsRoot repository={repository} baseUrl={`${url}/changesets`} />}
        />
        <Route
          path={`${url}/changesets/:branch/`}
          render={() => <ChangesetsRoot repository={repository} baseUrl={`${url}/changesets`} />}
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
  const { repository, location } = ownProps;
  const loading = isFetchBranchesPending(state, repository);
  const error = getFetchBranchesFailure(state, repository);
  const branches = getBranches(state, repository);
  const selectedView = decodeURIComponent(location.pathname.split("/")[5]);
  const selectedBranch = decodeURIComponent(location.pathname.split("/")[6]);

  return {
    loading,
    error,
    branches,
    selectedView,
    selectedBranch
  };
};

export default compose(
  withRouter,
  withTranslation("repos"),
  connect(mapStateToProps, mapDispatchToProps)
)(CodeSectionOverview);
