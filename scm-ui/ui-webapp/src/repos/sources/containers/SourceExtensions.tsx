import React from "react";
import { File, Repository } from "@scm-manager/ui-types";
import { RouteComponentProps, withRouter } from "react-router-dom";

import { binder, ExtensionPoint } from "@scm-manager/ui-extensions";
import { fetchSources, getFetchSourcesFailure, getSources, isFetchSourcesPending } from "../modules/sources";
import { connect } from "react-redux";
import { ErrorNotification, Loading, Notification } from "@scm-manager/ui-components";
import { WithTranslation, withTranslation } from "react-i18next";

type Props = WithTranslation &
  RouteComponentProps & {
    repository: Repository;
    baseUrl: string;

    // url params
    extension: string;
    revision?: string;
    path?: string;

    // redux state
    loading: boolean;
    error?: Error | null;
    sources?: File | null;

    // dispatch props
    fetchSources: (repository: Repository, revision: string | undefined, path: string | undefined) => void;
  };

const extensionPointName = "repos.sources.extensions";

class SourceExtensions extends React.Component<Props> {
  componentDidMount() {
    const { fetchSources, repository, revision, path } = this.props;
    // TODO get typing right
    fetchSources(repository, revision, path);
  }

  render() {
    const { loading, error, repository, extension, revision, path, sources, baseUrl, t } = this.props;
    if (error) {
      return <ErrorNotification error={error} />;
    }
    if (loading) {
      return <Loading />;
    }

    const extprops = { extension, repository, revision, path, sources, baseUrl };
    if (!binder.hasExtension(extensionPointName, extprops)) {
      return <Notification type="warning">{t("sources.extension.notBound")}</Notification>;
    }

    return <ExtensionPoint name={extensionPointName} props={extprops} />;
  }
}

const mapStateToProps = (state: any, ownProps: Props): Partial<Props> => {
  const { repository, match } = ownProps;
  // @ts-ignore
  const revision: string = match.params.revision;
  // @ts-ignore
  const path: string = match.params.path;
  // @ts-ignore
  const extension: string = match.params.extension;
  const loading = isFetchSourcesPending(state, repository, revision, path);
  const error = getFetchSourcesFailure(state, repository, revision, path);
  const sources = getSources(state, repository, revision, path);

  return {
    repository,
    extension,
    revision,
    path,
    loading,
    error,
    sources
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchSources: (repository: Repository, revision: string, path: string) => {
      dispatch(fetchSources(repository, decodeURIComponent(revision), path));
    }
  };
};

export default withRouter(connect(mapStateToProps, mapDispatchToProps)(withTranslation("repos")(SourceExtensions)));
