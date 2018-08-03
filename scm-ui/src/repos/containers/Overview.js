// @flow
import React from "react";

import type { RepositoryCollection } from "../types/Repositories";

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
import { Page } from "../../components/layout";
import RepositoryList from "../components/RepositoryList";
import Paginator from "../../components/Paginator";
import { withRouter } from "react-router-dom";
import type { History } from "history";
import CreateButton from "../../components/buttons/CreateButton";

type Props = {
  page: number,
  collection: RepositoryCollection,
  loading: boolean,
  error: Error,
  showCreateButton: boolean,

  // dispatched functions
  fetchRepos: () => void,
  fetchReposByPage: number => void,
  fetchReposByLink: string => void,

  // context props
  t: string => string,
  history: History
};

class Overview extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchReposByPage(this.props.page);
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
        {this.renderList()}
      </Page>
    );
  }

  renderList() {
    const { collection, fetchReposByLink } = this.props;
    if (collection) {
      return (
        <div>
          <RepositoryList repositories={collection._embedded.repositories} />
          <Paginator collection={collection} onPageChange={fetchReposByLink} />
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
        <CreateButton
          label={t("overview.create-button")}
          link="/repos/create"
        />
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
  return {
    page,
    collection,
    loading,
    error,
    showCreateButton
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchRepos: () => {
      dispatch(fetchRepos());
    },
    fetchReposByPage: (page: number) => {
      dispatch(fetchReposByPage(page));
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
