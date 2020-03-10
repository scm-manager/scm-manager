import React from "react";
import { connect } from "react-redux";
import { Redirect, Route, Switch, RouteComponentProps } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { Repository } from "@scm-manager/ui-types";
import {
  ErrorPage,
  Loading,
  NavLink,
  Page,
  Section,
  SubNavigation,
  MenuContext,
  storeMenuCollapsed,
  isMenuCollapsed
} from "@scm-manager/ui-components";
import { fetchRepoByName, getFetchRepoFailure, getRepository, isFetchRepoPending } from "../modules/repos";
import RepositoryDetails from "../components/RepositoryDetails";
import EditRepo from "./EditRepo";
import BranchesOverview from "../branches/containers/BranchesOverview";
import CreateBranch from "../branches/containers/CreateBranch";
import Permissions from "../permissions/containers/Permissions";
import EditRepoNavLink from "../components/EditRepoNavLink";
import BranchRoot from "../branches/containers/BranchRoot";
import PermissionsNavLink from "../components/PermissionsNavLink";
import RepositoryNavLink from "../components/RepositoryNavLink";
import { getLinks, getRepositoriesLink } from "../../modules/indexResource";
import CodeOverview from "../codeSection/containers/CodeOverview";
import ChangesetView from "./ChangesetView";
import SourceExtensions from "../sources/containers/SourceExtensions";

type Props = RouteComponentProps &
  WithTranslation & {
    namespace: string;
    name: string;
    repository: Repository;
    loading: boolean;
    error: Error;
    repoLink: string;
    indexLinks: object;

    // dispatch functions
    fetchRepoByName: (link: string, namespace: string, name: string) => void;
  };

type State = {
  menuCollapsed: boolean;
};

class RepositoryRoot extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);

    this.state = {
      menuCollapsed: isMenuCollapsed()
    };
  }

  componentDidMount() {
    const { fetchRepoByName, namespace, name, repoLink } = this.props;
    fetchRepoByName(repoLink, namespace, name);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 1);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  matchesBranches = (route: any) => {
    const url = this.matchedUrl();
    const regex = new RegExp(`${url}/branch/.+/info`);
    return route.location.pathname.match(regex);
  };

  matchesCode = (route: any) => {
    const url = this.matchedUrl();
    const regex = new RegExp(`${url}(/code)/.*`);
    return route.location.pathname.match(regex);
  };

  getCodeLinkname = () => {
    const { repository } = this.props;
    if (repository?._links?.sources) {
      return "sources";
    }
    if (repository?._links?.changesets) {
      return "changesets";
    }
    return "";
  };

  evaluateDestinationForCodeLink = () => {
    const { repository } = this.props;
    let url = `${this.matchedUrl()}/code`;
    if (repository?._links?.sources) {
      return `${url}/sources/`;
    }
    return `${url}/changesets`;
  };

  onCollapseRepositoryMenu = (collapsed: boolean) => {
    this.setState({ menuCollapsed: collapsed }, () => storeMenuCollapsed(collapsed));
  };

  render() {
    const { loading, error, indexLinks, repository, t } = this.props;
    const { menuCollapsed } = this.state;

    if (error) {
      return (
        <ErrorPage title={t("repositoryRoot.errorTitle")} subtitle={t("repositoryRoot.errorSubtitle")} error={error} />
      );
    }

    if (!repository || loading) {
      return <Loading />;
    }

    const url = this.matchedUrl();

    const extensionProps = {
      repository,
      url,
      indexLinks
    };

    const redirectUrlFactory = binder.getExtension("repository.redirect", this.props);
    let redirectedUrl;
    if (redirectUrlFactory) {
      redirectedUrl = url + redirectUrlFactory(this.props);
    } else {
      redirectedUrl = url + "/info";
    }

    return (
      <MenuContext.Provider
        value={{
          menuCollapsed,
          setMenuCollapsed: (collapsed: boolean) => this.setState({ menuCollapsed: collapsed })
        }}
      >
        <Page title={repository.namespace + "/" + repository.name}>
          <div className="columns">
            <div className="column">
              <Switch>
                <Redirect exact from={this.props.match.url} to={redirectedUrl} />

                {/* redirect pre 2.0.0-rc2 links */}
                <Redirect from={`${url}/changeset/:id`} to={`${url}/code/changeset/:id`} />
                <Redirect exact from={`${url}/sources`} to={`${url}/code/sources`} />
                <Redirect from={`${url}/sources/:revision/:path*`} to={`${url}/code/sources/:revision/:path*`} />
                <Redirect exact from={`${url}/changesets`} to={`${url}/code/changesets`} />
                <Redirect from={`${url}/branch/:branch/changesets`} to={`${url}/code/branch/:branch/changesets/`} />

                <Route path={`${url}/info`} exact component={() => <RepositoryDetails repository={repository} />} />
                <Route path={`${url}/settings/general`} component={() => <EditRepo repository={repository} />} />
                <Route
                  path={`${url}/settings/permissions`}
                  render={() => (
                    <Permissions namespace={this.props.repository.namespace} repoName={this.props.repository.name} />
                  )}
                />
                <Route
                  exact
                  path={`${url}/code/changeset/:id`}
                  render={() => <ChangesetView repository={repository} />}
                />
                <Route
                  path={`${url}/code/sourceext/:extension`}
                  exact={true}
                  render={() => <SourceExtensions repository={repository} />}
                />
                <Route
                  path={`${url}/code/sourceext/:extension/:revision/:path*`}
                  render={() => <SourceExtensions repository={repository} baseUrl={`${url}/code/sources`} />}
                />
                <Route
                  path={`${url}/code`}
                  render={() => <CodeOverview baseUrl={`${url}/code`} repository={repository} />}
                />
                <Route
                  path={`${url}/branch/:branch`}
                  render={() => <BranchRoot repository={repository} baseUrl={`${url}/branch`} />}
                />
                <Route
                  path={`${url}/branches`}
                  exact={true}
                  render={() => <BranchesOverview repository={repository} baseUrl={`${url}/branch`} />}
                />
                <Route path={`${url}/branches/create`} render={() => <CreateBranch repository={repository} />} />
                <ExtensionPoint name="repository.route" props={extensionProps} renderAll={true} />
              </Switch>
            </div>
            <div className={menuCollapsed ? "column is-1" : "column is-3"}>
              <Section
                label={t("repositoryRoot.menu.navigationLabel")}
                onCollapse={() => this.onCollapseRepositoryMenu(!menuCollapsed)}
                collapsed={menuCollapsed}
              >
                <ExtensionPoint name="repository.navigation.topLevel" props={extensionProps} renderAll={true} />
                <NavLink
                  to={`${url}/info`}
                  icon="fas fa-info-circle"
                  label={t("repositoryRoot.menu.informationNavLink")}
                  title={t("repositoryRoot.menu.informationNavLink")}
                />
                <RepositoryNavLink
                  repository={repository}
                  linkName="branches"
                  to={`${url}/branches/`}
                  icon="fas fa-code-branch"
                  label={t("repositoryRoot.menu.branchesNavLink")}
                  activeWhenMatch={this.matchesBranches}
                  activeOnlyWhenExact={false}
                  title={t("repositoryRoot.menu.branchesNavLink")}
                />
                <RepositoryNavLink
                  repository={repository}
                  linkName={this.getCodeLinkname()}
                  to={this.evaluateDestinationForCodeLink()}
                  icon="fas fa-code"
                  label={t("repositoryRoot.menu.sourcesNavLink")}
                  activeWhenMatch={this.matchesCode}
                  activeOnlyWhenExact={false}
                  title={t("repositoryRoot.menu.sourcesNavLink")}
                />
                <ExtensionPoint name="repository.navigation" props={extensionProps} renderAll={true} />
                <SubNavigation
                  to={`${url}/settings/general`}
                  label={t("repositoryRoot.menu.settingsNavLink")}
                  title={t("repositoryRoot.menu.settingsNavLink")}
                >
                  <EditRepoNavLink repository={repository} editUrl={`${url}/settings/general`} />
                  <PermissionsNavLink permissionUrl={`${url}/settings/permissions`} repository={repository} />
                  <ExtensionPoint name="repository.setting" props={extensionProps} renderAll={true} />
                </SubNavigation>
              </Section>
            </div>
          </div>
        </Page>
      </MenuContext.Provider>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespace, name } = ownProps.match.params;
  const repository = getRepository(state, namespace, name);
  const loading = isFetchRepoPending(state, namespace, name);
  const error = getFetchRepoFailure(state, namespace, name);
  const repoLink = getRepositoriesLink(state);
  const indexLinks = getLinks(state);
  return {
    namespace,
    name,
    repository,
    loading,
    error,
    repoLink,
    indexLinks
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchRepoByName: (link: string, namespace: string, name: string) => {
      dispatch(fetchRepoByName(link, namespace, name));
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("repos")(RepositoryRoot));
