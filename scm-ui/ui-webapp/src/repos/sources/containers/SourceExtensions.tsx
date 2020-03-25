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
    fetchSources: (repository: Repository, revision?: string, path?: string) => void;
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
