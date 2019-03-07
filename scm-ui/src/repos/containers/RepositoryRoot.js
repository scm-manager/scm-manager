//@flow
import React from "react";
import {
  fetchRepoByName,
  getFetchRepoFailure,
  getRepository,
  isFetchRepoPending
} from "../modules/repos";

import { connect } from "react-redux";
import {Redirect, Route, Switch} from "react-router-dom";
import type { Repository } from "@scm-manager/ui-types";

import {
  CollapsibleErrorPage,
  Loading,
  Navigation,
  SubNavigation,
  NavLink,
  Page,
  Section, ErrorPage
} from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import RepositoryDetails from "../components/RepositoryDetails";
import EditRepo from "./EditRepo";
import Permissions from "../permissions/containers/Permissions";

import type { History } from "history";
import EditRepoNavLink from "../components/EditRepoNavLink";

import BranchRoot from "./ChangesetsRoot";
import ChangesetView from "./ChangesetView";
import PermissionsNavLink from "../components/PermissionsNavLink";
import Sources from "../sources/containers/Sources";
import RepositoryNavLink from "../components/RepositoryNavLink";
import {getLinks, getRepositoriesLink} from "../../modules/indexResource";
import {binder, ExtensionPoint} from "@scm-manager/ui-extensions";

type Props = {
  namespace: string,
  name: string,
  repository: Repository,
  loading: boolean,
  error: Error,
  repoLink: string,
  indexLinks: Object,

  // dispatch functions
  fetchRepoByName: (link: string, namespace: string, name: string) => void,

  // context props
  t: string => string,
  history: History,
  match: any
};

class RepositoryRoot extends React.Component<Props> {
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

  matches = (route: any) => {
    const url = this.matchedUrl();
    const regex = new RegExp(`${url}(/branches)?/?[^/]*/changesets?.*`);
    return route.location.pathname.match(regex);
  };

  render() {
    const { loading, error, indexLinks, repository, t } = this.props;

    if (error) {
      return <ErrorPage
        title={t("repositoryRoot.errorTitle")}
        subtitle={t("repositoryRoot.errorSubtitle")}
        error={error}
      />
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
    if (redirectUrlFactory){
      redirectedUrl = url + redirectUrlFactory(this.props);
    }else{
      redirectedUrl = url + "/info";
    }

    return (
      <Page title={repository.namespace + "/" + repository.name}>
        <div className="columns">
          <div className="column is-three-quarters is-clipped">
            <Switch>
              <Redirect exact from={this.props.match.url} to={redirectedUrl}/>
              <Route
                path={`${url}/info`}
                exact
                component={() => <RepositoryDetails repository={repository} />}
              />
              <Route
                path={`${url}/settings/general`}
                component={() => <EditRepo repository={repository} />}
              />
              <Route
                path={`${url}/settings/permissions`}
                render={() => (
                  <Permissions
                    namespace={this.props.repository.namespace}
                    repoName={this.props.repository.name}
                  />
                )}
              />
              <Route
                exact
                path={`${url}/changeset/:id`}
                render={() => <ChangesetView repository={repository} />}
              />
              <Route
                path={`${url}/sources`}
                exact={true}
                render={() => (
                  <Sources repository={repository} baseUrl={`${url}/sources`} />
                )}
              />
              <Route
                path={`${url}/sources/:revision/:path*`}
                render={() => (
                  <Sources repository={repository} baseUrl={`${url}/sources`} />
                )}
              />
              <Route
                path={`${url}/changesets`}
                render={() => (
                  <BranchRoot
                    repository={repository}
                    baseUrlWithBranch={`${url}/branches`}
                    baseUrlWithoutBranch={`${url}/changesets`}
                  />
                )}
              />
              <Route
                path={`${url}/branches/:branch/changesets`}
                render={() => (
                  <BranchRoot
                    repository={repository}
                    baseUrlWithBranch={`${url}/branches`}
                    baseUrlWithoutBranch={`${url}/changesets`}
                  />
                )}
              />
              <ExtensionPoint
                name="repository.route"
                props={extensionProps}
                renderAll={true}
              />
            </Switch>
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("repositoryRoot.menu.navigationLabel")}>
                <ExtensionPoint
                  name="repository.navigation.topLevel"
                  props={extensionProps}
                  renderAll={true}
                />
                <NavLink
                  to={`${url}/info`}
                  icon="fas fa-info-circle"
                  label={t("repositoryRoot.menu.informationNavLink")}
                />
                <RepositoryNavLink
                  repository={repository}
                  linkName="changesets"
                  to={`${url}/changesets/`}
                  icon="fas fa-code-branch"
                  label={t("repositoryRoot.menu.historyNavLink")}
                  activeWhenMatch={this.matches}
                  activeOnlyWhenExact={false}
                />
                <RepositoryNavLink
                  repository={repository}
                  linkName="sources"
                  to={`${url}/sources`}
                  icon="fas fa-code"
                  label={t("repositoryRoot.menu.sourcesNavLink")}
                  activeOnlyWhenExact={false}
                />
                <ExtensionPoint
                  name="repository.navigation"
                  props={extensionProps}
                  renderAll={true}
                />
                <SubNavigation
                  to={`${url}/settings/general`}
                  label={t("repositoryRoot.menu.settingsNavLink")}
                >
                  <EditRepoNavLink
                    repository={repository}
                    editUrl={`${url}/settings/general`}
                  />
                  <PermissionsNavLink
                    permissionUrl={`${url}/settings/permissions`}
                    repository={repository}
                  />
                  <ExtensionPoint
                    name="repository.setting"
                    props={extensionProps}
                    renderAll={true}
                  />
                </SubNavigation>
              </Section>
            </Navigation>
          </div>
        </div>
      </Page>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
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

const mapDispatchToProps = dispatch => {
  return {
    fetchRepoByName: (link: string, namespace: string, name: string) => {
      dispatch(fetchRepoByName(link, namespace, name));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(RepositoryRoot));
