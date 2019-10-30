import React from "react";
import { Repository, File } from "@scm-manager/ui-types";
import { withRouter, RouteComponentProps } from "react-router-dom";

import { ExtensionPoint, binder } from "@scm-manager/ui-extensions";
import {
  fetchSources,
  getFetchSourcesFailure,
  getSources,
  isFetchSourcesPending
} from "../modules/sources";
import { connect} from "react-redux";
import { Loading, ErrorNotification } from "@scm-manager/ui-components";
import Notification from "@scm-manager/ui-components/src/Notification";

type Props = RouteComponentProps & {
  repository: Repository;

  // url params
  extension: string;
  revision?: string;
  path?: string;

  // redux state
  loading: boolean;
  error?: Error | null;
  sources?: File | null;

  // dispatch props
  fetchSources: (repository: Repository, revision: string, path: string) => void;
};

const extensionPointName = "repos.sources.extensions";

class SourceExtensions extends React.Component<Props> {
  componentDidMount() {
    const { fetchSources, repository, revision, path } = this.props;
    // TODO get typing right
    fetchSources(repository, revision || "", path || "");
  }

  render() {
    const { loading, error, repository, extension, revision, path, sources } = this.props;
    if (error) {
      return <ErrorNotification error={error} />;
    }
    if (loading) {
      return <Loading />;
    }

    const extprops = { extension, repository, revision, path, sources };
    if (!binder.hasExtension(extensionPointName, extprops)) {
      // TODO i18n
      return <Notification type="warning">No extension bound</Notification>;
    }

    return <ExtensionPoint name={extensionPointName} props={extprops} />;
  }
}

const mapStateToProps = (state: any, ownProps: Props): Partial<Props> => {
  const { repository, match } = ownProps;
  // TODO add query-string v6
  // see : https://www.pluralsight.com/guides/react-router-typescript
  // @ts-ignore
  const revision: string = match.params.revision;
  // @ts-ignore
  const path: string = match.params.path;
  // @ts-ignore
  const extension: string = match.params.extension;
  const decodedRevision = revision ? decodeURIComponent(revision) : undefined;
  const loading = isFetchSourcesPending(state, repository, revision, path);
  const error = getFetchSourcesFailure(state, repository, revision, path);
  const sources = getSources(state, repository, revision, path);

  return {
    repository,
    extension,
    revision: decodedRevision,
    path,
    loading,
    error,
    sources
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchSources: (repository: Repository, revision: string, path: string) => {
      dispatch(fetchSources(repository, revision, path));
    }
  };
};

export default withRouter(connect(
  mapStateToProps,
  mapDispatchToProps
)(SourceExtensions));
