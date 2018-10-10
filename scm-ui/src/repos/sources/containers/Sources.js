// @flow
import React from "react";
import { connect } from "react-redux";
import type { Repository, File } from "@scm-manager/ui-types";
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
  sources: File,
  loading: boolean,
  error: Error,
  revision: string,
  path: string,
  baseUrl: string,

  // dispatch props
  fetchSources: (
    repository: Repository,
    revision: string,
    path: string
  ) => void,
  match: any
};

class Sources extends React.Component<Props> {
  componentDidMount() {
    const { fetchSources, repository, revision, path } = this.props;

    fetchSources(repository, revision, path);
  }

  render() {
    const { sources, revision, path, baseUrl, loading, error } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (!sources || loading) {
      return <Loading />;
    }

    return (
      <FileTree
        tree={sources}
        revision={revision}
        path={path}
        baseUrl={baseUrl}
      />
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const { repository } = ownProps;
  const { revision, path } = ownProps.match.params;

  const loading = isFetchSourcesPending(state, repository, revision, path);
  const error = getFetchSourcesFailure(state, repository, revision, path);
  const sources = getSources(state, repository, revision, path);

  return {
    loading,
    error,
    sources,
    revision,
    path
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchSources: (repository: Repository, revision: string, path: string) => {
      dispatch(fetchSources(repository, revision, path));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(Sources);
