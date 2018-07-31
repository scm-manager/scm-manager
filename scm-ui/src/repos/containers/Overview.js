// @flow
import React from "react";

import type { RepositoryCollection } from "../types/Repositories";

import { connect } from "react-redux";
import { fetchRepos, getRepositoryCollection } from "../modules/repos";
import { translate } from "react-i18next";
import { Page } from "../../components/layout";
import RepositoryList from "../components/RepositoryList";

type Props = {
  collection: RepositoryCollection,

  // dispatched functions
  fetchRepos: () => void,
  // context props
  t: string => string
};

class Overview extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchRepos();
  }
  render() {
    const { t } = this.props;
    return (
      <Page title={t("overview.title")} subtitle={t("overview.subtitle")}>
        {this.renderList()}
      </Page>
    );
  }

  renderList() {
    const { collection } = this.props;
    if (collection) {
      return (
        <RepositoryList repositories={collection._embedded.repositories} />
      );
    }
    return null;
  }
}

const mapStateToProps = (state, ownProps) => {
  const collection = getRepositoryCollection(state);
  return {
    collection
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchRepos: () => {
      dispatch(fetchRepos());
    }
  };
};
export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(Overview));
