/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { connect } from "react-redux";
import { Link, Redirect, Route, RouteComponentProps, Switch } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { Changeset, Repository } from "@scm-manager/ui-types";
import {
  CustomQueryFlexWrappedColumns,
  ErrorPage,
  Loading,
  NavLink,
  Page,
  PrimaryContentColumn,
  SecondaryNavigation,
  SecondaryNavigationColumn,
  StateMenuContextProvider,
  SubNavigation
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
import { FileControlFactory, JumpToFileButton } from "@scm-manager/ui-components";

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

class RepositoryRoot extends React.Component<Props> {
  componentDidMount() {
    const { fetchRepoByName, namespace, name, repoLink } = this.props;
    fetchRepoByName(repoLink, namespace, name);
  }

  componentDidUpdate(prevProps: Props) {
    const { fetchRepoByName, namespace, name, repoLink } = this.props;
    if (namespace !== prevProps.namespace || name !== prevProps.name) {
      fetchRepoByName(repoLink, namespace, name);
    }
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
    const url = `${this.matchedUrl()}/code`;
    if (repository?._links?.sources) {
      return `${url}/sources/`;
    }
    return `${url}/changesets`;
  };

  render() {
    const { loading, error, indexLinks, repository, t } = this.props;

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

    const fileControlFactoryFactory: (changeset: Changeset) => FileControlFactory = changeset => file => {
      const baseUrl = `${url}/code/sources`;
      const sourceLink = {
        url: `${baseUrl}/${changeset.id}/${file.newPath}/`,
        label: t("diff.jumpToSource")
      };
      const targetLink = changeset._embedded?.parents?.length === 1 && {
        url: `${baseUrl}/${changeset._embedded.parents[0].id}/${file.oldPath}`,
        label: t("diff.jumpToTarget")
      };

      const links = [];
      switch (file.type) {
        case "add":
          links.push(sourceLink);
          break;
        case "delete":
          if (targetLink) {
            links.push(targetLink);
          }
          break;
        default:
          if (targetLink) {
            links.push(targetLink, sourceLink); // Target link first because its the previous file
          } else {
            links.push(sourceLink);
          }
      }

      return links.map(({ url, label }) => <JumpToFileButton tooltip={label} link={url} />);
    };

    const titleComponent = <><Link to={`/repos/${repository.namespace}/`} className={"has-text-dark"}>{repository.namespace}</Link>/{repository.name}</>;

    return (
      <StateMenuContextProvider>
        <Page
          titleComponent={titleComponent}
          title={`${repository.namespace}/${repository.name}`}
          afterTitle={<ExtensionPoint name={"repository.afterTitle"} props={{ repository }} />}
        >
          <CustomQueryFlexWrappedColumns>
            <PrimaryContentColumn>
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
                  render={() => (
                    <ChangesetView repository={repository} fileControlFactoryFactory={fileControlFactoryFactory} />
                  )}
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
            </PrimaryContentColumn>
            <SecondaryNavigationColumn>
              <SecondaryNavigation label={t("repositoryRoot.menu.navigationLabel")}>
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
              </SecondaryNavigation>
            </SecondaryNavigationColumn>
          </CustomQueryFlexWrappedColumns>
        </Page>
      </StateMenuContextProvider>
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
