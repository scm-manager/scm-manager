// @flow
import React from "react";

import type { RepositoryCollection } from "@scm-manager/ui-types";

import { connect } from "react-redux";
import {
  fetchRepos,
  fetchReposByLink,
  fetchReposByPage,
  getFetchReposFailure,
  getRepositoryCollection,
  isAbleToCreateRepos,
  isFetchReposPending
} from "../modules/repos";
import { translate } from "react-i18next";
import {
  Page,
  PageActions,
  Button,
  CreateButton,
  Notification,
  Paginator
} from "@scm-manager/ui-components";
import RepositoryList from "../components/list";
import { withRouter } from "react-router-dom";
import type { History } from "history";
import { getRepositoriesLink } from "../../modules/indexResource";

type Props = {
  page: number,
  collection: RepositoryCollection,
  loading: boolean,
  error: Error,
  showCreateButton: boolean,
  reposLink: string,

  // dispatched functions
  fetchRepos: string => void,
  fetchReposByPage: (string, number) => void,
  fetchReposByLink: string => void,

  // context props
  t: string => string,
  history: History
};

class Overview extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchReposByPage(this.props.reposLink, this.props.page);
  }

  /**
   * reflect page transitions in the uri
   */
  componentDidUpdate() {
    const { page, collection } = this.props;
    if (collection) {
      // backend starts paging by 0
      const statePage: number = collection.page + 1;
      if (page !== statePage) {
        this.props.history.push(`/repos/${statePage}`);
      }
    }
  }

  render() {
    const { error, loading, t } = this.props;
    return (
      <Page
        title={t("overview.title")}
        subtitle={t("overview.subtitle")}
        loading={loading}
        error={error}
      >
        {this.renderOverview()}
        {this.renderPageActionCreateButton()}
      </Page>
    );
  }

  renderRepositoryList() {
    const { collection, fetchReposByLink, t } = this.props;

    if (collection._embedded && collection._embedded.repositories.length > 0) {
      return (
        <>
          <RepositoryList repositories={collection._embedded.repositories} />
          <Paginator collection={collection} onPageChange={fetchReposByLink} />
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
        <div>
          {this.renderRepositoryList()}
          {this.renderCreateButton()}
        </div>
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
}

const getPageFromProps = props => {
  let page = props.match.params.page;
  if (page) {
    page = parseInt(page, 10);
  } else {
    page = 1;
  }
  return page;
};

const mapStateToProps = (state, ownProps) => {
  const page = getPageFromProps(ownProps);
  const collection = getRepositoryCollection(state);
  const loading = isFetchReposPending(state);
  const error = getFetchReposFailure(state);
  const showCreateButton = isAbleToCreateRepos(state);
  const reposLink = getRepositoriesLink(state);
  return {
    reposLink,
    page,
    collection,
    loading,
    error,
    showCreateButton
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchRepos: (link: string) => {
      dispatch(fetchRepos(link));
    },
    fetchReposByPage: (link: string, page: number) => {
      dispatch(fetchReposByPage(link, page));
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
