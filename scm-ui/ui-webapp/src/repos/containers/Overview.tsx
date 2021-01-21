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
import React, {FC, useState} from "react";
import { connect } from "react-redux";
import {RouteComponentProps, useHistory, useLocation, useParams, withRouter} from "react-router-dom";
import { useTranslation, WithTranslation, withTranslation } from "react-i18next";
import { Link, NamespaceCollection, RepositoryCollection } from "@scm-manager/ui-types";
import {
  CreateButton,
  LinkPaginator,
  Loading,
  Notification,
  OverviewPageActions,
  Page,
  PageActions,
  urls
} from "@scm-manager/ui-components";
import { getNamespacesLink, getRepositoriesLink } from "../../modules/indexResource";
import {
  fetchNamespaces,
  fetchReposByPage,
  getFetchReposFailure,
  getNamespaceCollection,
  getRepositoryCollection,
  isAbleToCreateRepos,
  isFetchNamespacesPending,
  isFetchReposPending
} from "../modules/repos";
import RepositoryList from "../components/list";
import { useNamespaces, useRepositories } from "@scm-manager/ui-api";
import {getNamespaceAndPageFromMatch} from "@scm-manager/ui-components/src/urls";

type Props = WithTranslation &
  RouteComponentProps & {
    loading: boolean;
    error: Error;
    showCreateButton: boolean;
    collection: RepositoryCollection;
    namespaces: NamespaceCollection;
    page: number;
    namespace: string;
    reposLink: string;
    namespacesLink: string;

    // dispatched functions
    fetchReposByPage: (link: string, page: number, filter?: string) => void;
    fetchNamespaces: (link: string, callback?: () => void) => void;
  };

const useOverviewData = () => {
  const { namespace, page } = useUrlParams();
  const { isLoading: isLoadingNamespaces, error: errorNamespaces, data: namespaces } = useNamespaces();
  const ns = namespaces?._embedded.namespaces.find(n => n.namespace === namespace)
  const { isLoading: isLoadingRepositories, error: errorRepositories, data: repositories } = useRepositories(ns, page, !namespace || !!namespaces);
  return {
    isLoading: isLoadingNamespaces || isLoadingRepositories,
    error: errorNamespaces || errorRepositories || undefined,
    namespaces,
    namespace,
    repositories
  };
};

const useUrlParams = () => {
  const params = useParams();
  return urls.getNamespaceAndPageFromMatch({params});
}

const Overview: FC = () => {
  const { isLoading, error, namespace, namespaces, repositories } = useOverviewData();
  const location = useLocation();
  const history = useHistory();
  const [t] = useTranslation("repos");
  const showCreateButton = !!repositories?._links.create;

  console.log(location);

  const allNamespacesPlaceholder = t("overview.allNamespaces")

  let namespacesToRender: string[] = [];
  if (namespaces) {
    namespacesToRender = [
      allNamespacesPlaceholder, ...namespaces._embedded.namespaces.map(n => n.namespace).sort()
    ];
  }

  const namespaceSelected = (newNamespace: string) => {
    if (newNamespace === allNamespacesPlaceholder) {
      history.push("/repos/");
    } else {
      history.push(`/repos/${newNamespace}/`);
    }
  };

  return (
    <Page title={t("overview.title")} subtitle={t("overview.subtitle")} loading={isLoading} error={error}>
      {repositories && namespaces ? (
        <RepositoryList repositories={repositories._embedded.repositories} namespaces={namespaces} />
      ) : null}
      <PageActions>
        <OverviewPageActions
          showCreateButton={showCreateButton}
          currentGroup={namespace || ""}
          groups={namespacesToRender}
          groupSelected={namespaceSelected}
          link={namespace ? `repos/${namespace}` : "repos"}
          label={t("overview.createButton")}
          testId="repository-overview"
          searchPlaceholder={t("overview.searchRepository")}
        />
      </PageActions>
    </Page>
  );
};

class OverviewCmp extends React.Component<Props> {
  componentDidMount() {
    const { fetchNamespaces, namespacesLink } = this.props;
    fetchNamespaces(namespacesLink, () => this.fetchRepos());
  }

  componentDidUpdate = (prevProps: Props) => {
    const { loading, collection, namespace, namespaces, page, location } = this.props;
    if (namespaces !== prevProps.namespaces && namespace) {
      this.fetchRepos();
    } else if (collection && (page || namespace) && !loading) {
      const statePage: number = collection.page + 1;
      if (page !== statePage || prevProps.location.search !== location.search || prevProps.namespace !== namespace) {
        this.fetchRepos();
      }
    }
  };

  fetchRepos = () => {
    const { page, location, fetchReposByPage } = this.props;
    const link = this.getReposLink();
    if (link) {
      fetchReposByPage(link, page, urls.getQueryStringFromLocation(location));
    }
  };

  getReposLink = () => {
    const { namespace, namespaces, reposLink } = this.props;
    if (namespace) {
      return (namespaces?._embedded.namespaces.find(n => n.namespace === namespace)?._links?.repositories as Link)
        ?.href;
    } else {
      return reposLink;
    }
  };

  getNamespaceFilterPlaceholder = () => {
    return this.props.t("overview.allNamespaces");
  };

  namespaceSelected = (newNamespace: string) => {
    if (newNamespace === this.getNamespaceFilterPlaceholder()) {
      this.props.history.push("/repos/");
    } else {
      this.props.history.push(`/repos/${newNamespace}/`);
    }
  };

  render() {
    const { error, loading, showCreateButton, namespace, namespaces, t } = this.props;
    const namespaceFilterPlaceholder = this.getNamespaceFilterPlaceholder();
    const namespacesToRender = namespaces
      ? [namespaceFilterPlaceholder, ...namespaces._embedded.namespaces.map(n => n.namespace).sort()]
      : [];

    return (
      <Page title={t("overview.title")} subtitle={t("overview.subtitle")} loading={loading} error={error}>
        {this.renderOverview()}
        <PageActions>
          <OverviewPageActions
            showCreateButton={showCreateButton}
            currentGroup={namespace}
            groups={namespacesToRender}
            groupSelected={this.namespaceSelected}
            link={namespace ? `repos/${namespace}` : "repos"}
            label={t("overview.createButton")}
            testId="repository-overview"
            searchPlaceholder={t("overview.searchRepository")}
          />
        </PageActions>
      </Page>
    );
  }

  renderRepositoryList() {
    const { collection, page, location, namespaces, t } = this.props;

    if (collection._embedded && collection._embedded.repositories.length > 0) {
      return (
        <>
          <RepositoryList repositories={collection._embedded.repositories} namespaces={namespaces} />
          <LinkPaginator collection={collection} page={page} filter={urls.getQueryStringFromLocation(location)} />
        </>
      );
    }
    return <Notification type="info">{t("overview.noRepositories")}</Notification>;
  }

  renderOverview() {
    const { collection } = this.props;
    if (collection) {
      return (
        <>
          {this.renderRepositoryList()}
          {this.renderCreateButton()}
        </>
      );
    }
    return null;
  }

  renderCreateButton() {
    const { showCreateButton, t } = this.props;
    if (showCreateButton) {
      return <CreateButton label={t("overview.createButton")} link="/repos/create" />;
    }
    return null;
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { match } = ownProps;
  const collection = getRepositoryCollection(state);
  const namespaces = getNamespaceCollection(state);
  const loading = isFetchReposPending(state) || isFetchNamespacesPending(state);
  const error = getFetchReposFailure(state);
  const { namespace, page } = urls.getNamespaceAndPageFromMatch(match);
  const showCreateButton = isAbleToCreateRepos(state);
  const reposLink = getRepositoriesLink(state);
  const namespacesLink = getNamespacesLink(state);
  return {
    collection,
    namespaces,
    loading,
    error,
    page,
    namespace,
    showCreateButton,
    reposLink,
    namespacesLink
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchReposByPage: (link: string, page: number, filter?: string) => {
      dispatch(fetchReposByPage(link, page, filter));
    },
    fetchNamespaces: (link: string, callback?: () => void) => {
      dispatch(fetchNamespaces(link, callback));
    }
  };
};

// export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("repos")(withRouter(OverviewCmp)));
export default Overview;
