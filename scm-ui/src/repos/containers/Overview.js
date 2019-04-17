// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import type { History } from "history";
import queryString from "query-string";
import { withRouter } from "react-router-dom";
import type { RepositoryCollection } from "@scm-manager/ui-types";
import {
  fetchRepos,
  fetchReposByLink,
  fetchReposByPage,
  getFetchReposFailure,
  getRepositoryCollection,
  isAbleToCreateRepos,
  isFetchReposPending
} from "../modules/repos";
import {
  Page,
  PageActions,
  Button,
  CreateButton,
  Notification,
  LinkPaginator,
  getPageFromMatch
} from "@scm-manager/ui-components";
import RepositoryList from "../components/list";
import { getRepositoriesLink } from "../../modules/indexResource";

type Props = {
  page: number,
  collection: RepositoryCollection,
  loading: boolean,
  error: Error,
  showCreateButton: boolean,
  reposLink: string,

  // context props
  t: string => string,
  history: History,
  location: any,

  // dispatched functions
  fetchRepos: string => void,
  fetchReposByPage: (link: string, page: number, filter?: any) => void,
  fetchReposByLink: string => void
};

class Overview extends React.Component<Props> {
  componentDidMount() {
    const { fetchReposByPage, reposLink, page } = this.props;
    fetchReposByPage(reposLink, page, this.getQueryString());
  }

  componentDidUpdate = (prevProps: Props) => {
    const {
      collection,
      page,
      loading,
      location,
      fetchReposByPage,
      reposLink
    } = this.props;
    if (collection && page && !loading) {
      const statePage: number = collection.page + 1;
      if (
        page !== statePage ||
        prevProps.location.search !== location.search
      ) {
        fetchReposByPage(reposLink, page, this.getQueryString());
      }
    }
  };

  render() {
    const { error, loading, history, t } = this.props;
    return (
      <Page
        title={t("overview.title")}
        subtitle={t("overview.subtitle")}
        loading={loading}
        error={error}
        filter={filter => {
          history.push("/repos/?q=" + filter);
        }}
      >
        {this.renderOverview()}
        {this.renderPageActionCreateButton()}
      </Page>
    );
  }

  renderRepositoryList() {
    const { collection, page, t } = this.props;

    if (collection._embedded && collection._embedded.repositories.length > 0) {
      return (
        <>
          <RepositoryList repositories={collection._embedded.repositories} />
          <LinkPaginator
            collection={collection}
            page={page}
            filter={this.getQueryString()}
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

  renderPageActionCreateButton() {
    const { showCreateButton, t } = this.props;
    if (showCreateButton) {
      return (
        <PageActions>
          <Button
            label={t("overview.createButton")}
            link="/repos/create"
            color="primary"
          />
        </PageActions>
      );
    }
    return null;
  }

  getQueryString = () => {
    const { location } = this.props;
    return location.search ? queryString.parse(location.search).q : null;
  };
}

const mapStateToProps = (state, ownProps) => {
  const { match } = ownProps;
  const collection = getRepositoryCollection(state);
  const loading = isFetchReposPending(state);
  const error = getFetchReposFailure(state);
  const page = getPageFromMatch(match);
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
    fetchRepos: (link: string) => {
      dispatch(fetchRepos(link));
    },
    fetchReposByPage: (link: string, page: number, filter?: any) => {
      dispatch(fetchReposByPage(link, page, filter));
    },
    fetchReposByLink: (link: string) => {
      dispatch(fetchReposByLink(link));
    }
  };
};
export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(withRouter(Overview)));
