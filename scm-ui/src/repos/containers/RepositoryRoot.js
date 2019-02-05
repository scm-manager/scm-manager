//@flow
import React from "react";
import {deleteRepo, fetchRepoByName, getFetchRepoFailure, getRepository, isFetchRepoPending} from "../modules/repos";

import {connect} from "react-redux";
import {Route, Switch} from "react-router-dom";
import type {Repository} from "@scm-manager/ui-types";

import {ErrorPage, Loading, Navigation, NavLink, Page, Section} from "@scm-manager/ui-components";
import {translate} from "react-i18next";
import RepositoryDetails from "../components/RepositoryDetails";
import DeleteNavAction from "../components/DeleteNavAction";
import Edit from "../containers/Edit";
import Permissions from "../permissions/containers/Permissions";

import type {History} from "history";
import EditNavLink from "../components/EditNavLink";

import BranchRoot from "./ChangesetsRoot";
import ChangesetView from "./ChangesetView";
import PermissionsNavLink from "../components/PermissionsNavLink";
import Sources from "../sources/containers/Sources";
import RepositoryNavLink from "../components/RepositoryNavLink";
import {getLinks, getRepositoriesLink} from "../../modules/indexResource";
import {ExtensionPoint} from "@scm-manager/ui-extensions";

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
  deleteRepo: (repository: Repository, () => void) => void,

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
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  deleted = () => {
    this.props.history.push("/repos");
  };

  delete = (repository: Repository) => {
    this.props.deleteRepo(repository, this.deleted);
  };

  matches = (route: any) => {
    const url = this.matchedUrl();
    const regex = new RegExp(`${url}(/branches)?/?[^/]*/changesets?.*`);
    return route.location.pathname.match(regex);
  };

  render() {
    const { loading, error, indexLinks, repository, t } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("repository-root.error-title")}
          subtitle={t("repository-root.error-subtitle")}
          error={error}
        />
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

    return (
      <Page title={repository.namespace + "/" + repository.name}>
        <div className="columns">
          <div className="column is-three-quarters is-clipped">
            <Switch>
              <Route
                path={url}
                exact
                component={() => <RepositoryDetails repository={repository} />}
              />
              <Route
                path={`${url}/edit`}
                component={() => <Edit repository={repository} />}
              />
              <Route
                path={`${url}/permissions`}
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
              <Section label={t("repository-root.navigation-label")}>
                <NavLink to={url} icon="fas fa-info-circle" label={t("repository-root.information")} />
                <RepositoryNavLink
                  repository={repository}
                  linkName="changesets"
                  to={`${url}/changesets/`}
                  icon="fas fa-code-branch"
                  label={t("repository-root.history")}
                  activeWhenMatch={this.matches}
                  activeOnlyWhenExact={false}
                />
                <RepositoryNavLink
                  repository={repository}
                  linkName="sources"
                  to={`${url}/sources`}
                  icon="fas fa-code"
                  label={t("repository-root.sources")}
                  activeOnlyWhenExact={false}
                />
                <PermissionsNavLink
                  permissionUrl={`${url}/permissions`}
                  repository={repository}
                />
                <ExtensionPoint
                  name="repository.navigation"
                  props={extensionProps}
                  renderAll={true}
                />
              </Section>
              <Section label={t("repository-root.actions-label")}>
                <DeleteNavAction repository={repository} delete={this.delete} />
                <EditNavLink repository={repository} editUrl={`${url}/edit`} />
                <NavLink to="/repos" icon="fas fa-undo" label={t("repository-root.back-label")} />
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
    },
    deleteRepo: (repository: Repository, callback: () => void) => {
      dispatch(deleteRepo(repository, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(RepositoryRoot));
