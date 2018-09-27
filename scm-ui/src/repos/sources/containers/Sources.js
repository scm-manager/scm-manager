// @flow
import React from "react";
import { connect } from "react-redux";
import type { Repository, SourcesCollection } from "@scm-manager/ui-types";
import FileTree from "../components/FileTree";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import {
  fetchSources,
  getFetchSourcesFailure,
  getSources,
  isFetchSourcesPending
} from "../modules/sources";

type Props = {
  repository: Repository,
  sources: SourcesCollection,
  loading: boolean,
  error: Error,

  // dispatch props
  fetchSources: (repository: Repository) => void
};

class Sources extends React.Component<Props> {
  componentDidMount() {
    const { fetchSources, repository } = this.props;

    fetchSources(repository);
  }

  render() {
    const { sources, loading, error } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (!sources || loading) {
      return <Loading />;
    }

    return <FileTree tree={sources} />;
  }
}

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const loading = isFetchSourcesPending(state, repository);
  const error = getFetchSourcesFailure(state, repository);
  const sources = getSources(state, repository);

  console.log(sources);

  return {
    loading,
    error,
    sources
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchSources: (repository: Repository) => {
      dispatch(fetchSources(repository));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Sources);
