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
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import {
  NamespaceStrategies,
  Repository,
  RepositoryCreation,
  RepositoryImport,
  RepositoryType
} from "@scm-manager/ui-types";
import { Loading, Notification, Page } from "@scm-manager/ui-components";
import {
  fetchRepositoryTypesIfNeeded,
  getFetchRepositoryTypesFailure,
  getRepositoryTypes,
  isFetchRepositoryTypesPending
} from "../modules/repositoryTypes";
import RepositoryForm from "../components/form";
import RepositoryFormSwitcher from "../components/form/RepositoryFormSwitcher";
import {
  createRepo,
  createRepoReset,
  getCreateRepoFailure,
  getImportRepoFailure,
  importRepoFromUrl,
  importRepoReset,
  isCreateRepoPending,
  isImportRepoPending
} from "../modules/repos";
import { getRepositoriesLink } from "../../modules/indexResource";
import {
  fetchNamespaceStrategiesIfNeeded,
  getFetchNamespaceStrategiesFailure,
  getNamespaceStrategies,
  isFetchNamespaceStrategiesPending
} from "../../admin/modules/namespaceStrategies";
import { RouteComponentProps, withRouter } from "react-router-dom";
import { compose } from "redux";

type Props = WithTranslation &
  RouteComponentProps & {
    repositoryTypes: RepositoryType[];
    namespaceStrategies: NamespaceStrategies;
    pageLoading: boolean;
    createLoading: boolean;
    importLoading: boolean;
    error: Error;
    repoLink: string;
    indexResources: any;

    // dispatch functions
    fetchNamespaceStrategiesIfNeeded: () => void;
    fetchRepositoryTypesIfNeeded: () => void;
    createRepo: (
      link: string,
      repository: RepositoryCreation,
      initRepository: boolean,
      callback: (repo: Repository) => void
    ) => void;
    importRepoFromUrl: (link: string, repository: RepositoryImport, callback: (repo: Repository) => void) => void;
    resetForm: () => void;

    // context props
    history: History;
  };

class AddRepository extends React.Component<Props> {
  componentDidMount() {
    this.props.resetForm();
    this.props.fetchRepositoryTypesIfNeeded();
    this.props.fetchNamespaceStrategiesIfNeeded();
  }

  repoCreated = (repo: Repository) => {
    this.props.history.push("/repo/" + repo.namespace + "/" + repo.name);
  };

  resolveLocation = () => {
    const currentUrl = this.props.location.pathname;
    if (currentUrl.includes("/repos/create")) {
      return "create";
    }
    if (currentUrl.includes("/repos/import")) {
      return "import";
    }
    return "";
  };

  isImportPage = () => this.resolveLocation() === "import";
  isCreatePage = () => this.resolveLocation() === "create";

  getSubtitle = () => {
    const { importLoading, t } = this.props;
    let subtitle;
    if (this.isCreatePage()) {
      subtitle = t("create.subtitle");
    } else if (!importLoading) {
      subtitle = t("import.subtitle");
    } else {
      subtitle = t("import.pending.subtitle");
    }

    return subtitle;
  };

  render() {
    const {
      pageLoading,
      createLoading,
      importLoading,
      repositoryTypes,
      namespaceStrategies,
      createRepo,
      importRepoFromUrl,
      error,
      indexResources,
      repoLink,
      t
    } = this.props;

    return (
      <Page
        title={t("create.title")}
        subtitle={this.getSubtitle()}
        loading={pageLoading}
        error={error}
        showContentOnError={true}
      >
        {importLoading ? (
          <>
            <Notification type="info">{t("import.pending.infoText")}</Notification>
            <Loading />
          </>
        ) : (
          <>
            {!error && <RepositoryFormSwitcher creationMode={this.isImportPage() ? "IMPORT" : "CREATE"} />}
            <RepositoryForm
              repositoryTypes={repositoryTypes}
              loading={createLoading}
              namespaceStrategy={namespaceStrategies.current}
              createRepository={(repo, initRepository) => {
                createRepo(repoLink, repo, initRepository, (repo: Repository) => this.repoCreated(repo));
              }}
              importRepository={repo => {
                importRepoFromUrl(repoLink, repo, (repo: Repository) => this.repoCreated(repo));
              }}
              indexResources={indexResources}
              creationMode={this.isImportPage() ? "IMPORT" : "CREATE"}
            />
          </>
        )}
      </Page>
    );
  }
}

const mapStateToProps = (state: any) => {
  const repositoryTypes = getRepositoryTypes(state);
  const namespaceStrategies = getNamespaceStrategies(state);
  const pageLoading = isFetchRepositoryTypesPending(state) || isFetchNamespaceStrategiesPending(state);
  const createLoading = isCreateRepoPending(state);
  const importLoading = isImportRepoPending(state);
  const error =
    getFetchRepositoryTypesFailure(state) ||
    getCreateRepoFailure(state) ||
    getFetchNamespaceStrategiesFailure(state) ||
    getImportRepoFailure(state);
  const repoLink = getRepositoriesLink(state);
  const indexResources = state?.indexResources;

  return {
    repositoryTypes,
    namespaceStrategies,
    pageLoading,
    createLoading,
    importLoading,
    error,
    repoLink,
    indexResources
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchRepositoryTypesIfNeeded: () => {
      dispatch(fetchRepositoryTypesIfNeeded());
    },
    fetchNamespaceStrategiesIfNeeded: () => {
      dispatch(fetchNamespaceStrategiesIfNeeded());
    },
    createRepo: (link: string, repository: RepositoryCreation, initRepository: boolean, callback: () => void) => {
      dispatch(createRepo(link, repository, initRepository, callback));
    },
    importRepoFromUrl: (link: string, repository: RepositoryImport, callback: () => void) => {
      dispatch(importRepoFromUrl(link, repository, callback));
    },
    resetForm: () => {
      dispatch(createRepoReset());
      dispatch(importRepoReset());
    }
  };
};

export default compose(
  withRouter,
  withTranslation("repos"),
  connect(mapStateToProps, mapDispatchToProps)
)(AddRepository);
