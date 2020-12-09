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
import { NamespaceStrategies, Repository, RepositoryCreation, RepositoryType } from "@scm-manager/ui-types";
import { Page } from "@scm-manager/ui-components";
import {
  fetchRepositoryTypesIfNeeded,
  getFetchRepositoryTypesFailure,
  getRepositoryTypes,
  isFetchRepositoryTypesPending
} from "../modules/repositoryTypes";
import RepositoryForm from "../components/form";
import RepositoryFormSwitcher from "../components/form/RepositoryFormSwitcher";
import { createRepo, createRepoReset, getCreateRepoFailure, isCreateRepoPending } from "../modules/repos";
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
    resetForm: () => void;

    // context props
    history: History;
  };

class CreateRepository extends React.Component<Props> {
  componentDidMount() {
    this.props.resetForm();
    this.props.fetchRepositoryTypesIfNeeded();
    this.props.fetchNamespaceStrategiesIfNeeded();
  }

  repoCreated = (repo: Repository) => {
    this.props.history.push("/repo/" + repo.namespace + "/" + repo.name);
  };

  render() {
    const {
      pageLoading,
      createLoading,
      repositoryTypes,
      namespaceStrategies,
      createRepo,
      error,
      indexResources,
      repoLink,
      t
    } = this.props;

    return (
      <Page
        title={t("create.title")}
        subtitle={t("create.subtitle")}
        afterTitle={<RepositoryFormSwitcher creationMode={"CREATE"} />}
        loading={pageLoading}
        error={error}
        showContentOnError={true}
      >
        <RepositoryForm
          repositoryTypes={repositoryTypes}
          loading={createLoading}
          namespaceStrategy={namespaceStrategies.current}
          createRepository={(repo, initRepository) => {
            createRepo(repoLink, repo, initRepository, (repo: Repository) => this.repoCreated(repo));
          }}
          indexResources={indexResources}
        />
      </Page>
    );
  }
}

const mapStateToProps = (state: any) => {
  const repositoryTypes = getRepositoryTypes(state);
  const namespaceStrategies = getNamespaceStrategies(state);
  const pageLoading = isFetchRepositoryTypesPending(state) || isFetchNamespaceStrategiesPending(state);
  const createLoading = isCreateRepoPending(state);
  const error =
    getFetchRepositoryTypesFailure(state) || getCreateRepoFailure(state) || getFetchNamespaceStrategiesFailure(state);
  const repoLink = getRepositoriesLink(state);
  const indexResources = state?.indexResources;

  return {
    repositoryTypes,
    namespaceStrategies,
    pageLoading,
    createLoading,
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
    resetForm: () => {
      dispatch(createRepoReset());
    }
  };
};

export default compose(
  withRouter,
  withTranslation("repos"),
  connect(mapStateToProps, mapDispatchToProps)
)(CreateRepository);
