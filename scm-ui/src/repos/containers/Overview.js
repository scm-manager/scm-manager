// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { History } from "history";
import { withRouter } from "react-router-dom";
import type { RepositoryCollection } from "@scm-manager/ui-types";
import {
  fetchReposByPage,
  getFetchReposFailure,
  getRepositoryCollection,
  isAbleToCreateRepos,
  isFetchReposPending
} from "../modules/repos";
import {
  Page,
  PageActions,
  OverviewPageActions,
  CreateButton,
  Notification,
  LinkPaginator,
  urls
} from "@scm-manager/ui-components";
import RepositoryList from "../components/list";
import { getRepositoriesLink } from "../../modules/indexResource";

type Props = {
  loading: boolean,
  error: Error,
  showCreateButton: boolean,
  collection: RepositoryCollection,
  page: number,
  reposLink: string,

  // context props
  t: string => string,
  history: History,
  location: any,

  // dispatched functions
  fetchReposByPage: (link: string, page: number, filter?: string) => void
};

class Overview extends React.Component<Props> {
  componentDidMount() {
    const { fetchReposByPage, reposLink, page, location } = this.props;
    fetchReposByPage(
      reposLink,
      page,
      urls.getQueryStringFromLocation(location)
    );
  }

  componentDidUpdate = (prevProps: Props) => {
    const {
      loading,
      collection,
      page,
      reposLink,
      location,
      fetchReposByPage
    } = this.props;
    if (collection && page && !loading) {
      const statePage: number = collection.page + 1;
      if (page !== statePage || prevProps.location.search !== location.search) {
        fetchReposByPage(
          reposLink,
          page,
          urls.getQueryStringFromLocation(location)
        );
      }
    }
  };

  render() {
    const { error, loading, showCreateButton, t } = this.props;
    return (
      <Page
        title={t("overview.title")}
        subtitle={t("overview.subtitle")}
        loading={loading}
        error={error}
      >
        {this.renderOverview()}
        <PageActions>
          <OverviewPageActions
            showCreateButton={showCreateButton}
            link="repos/create"
            label={t("overview.createButton")}
          />
        </PageActions>
      </Page>
    );
  }

  renderRepositoryList() {
    const { collection, page, location, t } = this.props;

    if (collection._embedded && collection._embedded.repositories.length > 0) {
      return (
        <>
          <RepositoryList repositories={collection._embedded.repositories} />
          <LinkPaginator
            collection={collection}
            page={page}
            filter={urls.getQueryStringFromLocation(location)}
          />
        </>
      );
    }
    return (
      <Notification type="info">{t("overview.noRepositories")}</Notification>
    );
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
      return (
        <CreateButton label={t("overview.createButton")} link="/repos/create" />
      );
    }
    return null;
  }
}

const mapStateToProps = (state, ownProps) => {
  const { match } = ownProps;
  const collection = getRepositoryCollection(state);
  const loading = isFetchReposPending(state);
  const error = getFetchReposFailure(state);
  const page = urls.getPageFromMatch(match);
  const showCreateButton = isAbleToCreateRepos(state);
  const reposLink = getRepositoriesLink(state);
  return {
    collection,
    loading,
    error,
    page,
    showCreateButton,
    reposLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchReposByPage: (link: string, page: number, filter?: string) => {
      dispatch(fetchReposByPage(link, page, filter));
    }
  };
};
export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(withRouter(Overview)));
