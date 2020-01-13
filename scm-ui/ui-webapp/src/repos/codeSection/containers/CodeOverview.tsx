import React from "react";
import styled from "styled-components";
import { Route, RouteComponentProps, withRouter } from "react-router-dom";
import Sources from "../../sources/containers/Sources";
import ChangesetsRoot from "../../containers/ChangesetsRoot";
import { Branch, Repository } from "@scm-manager/ui-types";
import { BranchSelector, ErrorPage, Level, Loading } from "@scm-manager/ui-components";
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
    error: Error;
    loading: boolean;
    selectedBranch: string;

    // Dispatch props
    fetchBranches: (p: Repository) => void;
  };

const CodeActionBar = styled.div.attrs(() => ({}))`
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

class CodeOverview extends React.Component<Props> {
  componentDidMount() {
    const { repository, branches } = this.props;
    new Promise(() => {
      this.props.fetchBranches(repository);
    }).then(() => {
      if (this.props.branches?.length > 0) {
        const defaultBranch = branches.filter((branch: Branch) => branch.defaultBranch === true)[0];
        this.branchSelected(defaultBranch);
      }
    });
  }

  findSelectedBranch = () => {
    const { selectedBranch, branches } = this.props;
    return branches?.find((branch: Branch) => branch.name === selectedBranch);
  };

  branchSelected = (branch?: Branch) => {
    let splittedUrl = this.props.location.pathname.split("/");
    if (
      this.props.location.pathname.includes("/code/sources") ||
      this.props.location.pathname.includes("/code/branch")
    ) {
      if (branch) {
        splittedUrl[6] = encodeURIComponent(branch.name);
      }
      this.props.history.push(splittedUrl.join("/"));
    }
    if (this.props.location.pathname.includes("/code/changesets")) {
      this.props.history.push(
        `${splittedUrl[0]}/${splittedUrl[1]}/${splittedUrl[2]}/${splittedUrl[3]}/${
          splittedUrl[4]
        }/branch/${encodeURIComponent(branch.name)}/${splittedUrl[5]}/`
      );
    }
  };

  render() {
    const { repository, baseUrl, branches, selectedBranch, error, loading, t } = this.props;
    const url = baseUrl;

    if (loading) {
      return <Loading />;
    }

    if (error) {
      return (
        <ErrorPage title={t("repositoryRoot.errorTitle")} subtitle={t("repositoryRoot.errorSubtitle")} error={error} />
      );
    }

    return (
      <div>
        <CodeActionBar>
          <Level
            left={
              <BranchSelector
                label={t("code.branchSelector")}
                branches={branches}
                selectedBranch={selectedBranch}
                onSelectBranch={this.branchSelected}
              />
            }
            right={<CodeViewSwitcher baseUrl={url} currentUrl={this.props.location.pathname} branches={branches} selectedBranch={selectedBranch}/>}
          />
        </CodeActionBar>
        <Route
          path={`${url}/sources`}
          exact={true}
          render={() => <Sources repository={repository} baseUrl={`${url}/sources`} branches={branches} />}
        />
        <Route
          path={`${url}/sources/:revision/:path*`}
          render={() => <Sources repository={repository} baseUrl={`${url}/sources`} branches={branches} />}
        />
        <Route
          path={`${url}/changesets`}
          render={() => <ChangesetsRoot repository={repository} baseUrl={`${url}/changesets`} />}
        />
        <Route
          path={`${url}/branch/:branch/changesets/`}
          render={() => (
            <ChangesetsRoot
              repository={repository}
              baseUrl={`${url}/changesets`}
              selectedBranch={branches && branches.filter(b => b.name === this.props.selectedBranch)[0]}
            />
          )}
        />
      </div>
    );
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchBranches: (repo: Repository) => {
      dispatch(fetchBranches(repo));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, location } = ownProps;
  const error = getFetchBranchesFailure(state, repository);
  const loading = isFetchBranchesPending(state, repository);
  const branches = getBranches(state, repository);
  const branchFromURL =
    !location.pathname.includes("/code/changesets/") && decodeURIComponent(location.pathname.split("/")[6]);
  const selectedBranch = branchFromURL && branchFromURL !== "undefined" ? branchFromURL : "";
  return {
    error,
    loading,
    branches,
    selectedBranch
  };
};

export default compose(
  withRouter,
  withTranslation("repos"),
  connect(mapStateToProps, mapDispatchToProps)
)(CodeOverview);
