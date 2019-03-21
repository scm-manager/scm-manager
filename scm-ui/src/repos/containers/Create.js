// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { Page } from "@scm-manager/ui-components";
import RepositoryForm from "../components/form";
import type { Repository, RepositoryType, NamespaceStrategies } from "@scm-manager/ui-types";
import {
  fetchRepositoryTypesIfNeeded,
  getFetchRepositoryTypesFailure,
  getRepositoryTypes,
  isFetchRepositoryTypesPending
} from "../modules/repositoryTypes";
import {
  createRepo,
  createRepoReset,
  getCreateRepoFailure,
  isCreateRepoPending
} from "../modules/repos";
import type { History } from "history";
import { getRepositoriesLink } from "../../modules/indexResource";
import {
  fetchNamespaceStrategiesIfNeeded,
  getFetchNamespaceStrategiesFailure, getNamespaceStrategies, isFetchNamespaceStrategiesPending
} from "../../config/modules/namespaceStrategies";

type Props = {
  repositoryTypes: RepositoryType[],
  namespaceStrategies: NamespaceStrategies,
  pageLoading: boolean,
  createLoading: boolean,
  error: Error,
  repoLink: string,

  // dispatch functions
  fetchNamespaceStrategiesIfNeeded: () => void,
  fetchRepositoryTypesIfNeeded: () => void,
  createRepo: (
    link: string,
    Repository,
    callback: (repo: Repository) => void
  ) => void,
  resetForm: () => void,

  // context props
  t: string => string,
  history: History
};

class Create extends React.Component<Props> {
  componentDidMount() {
    this.props.resetForm();
    this.props.fetchRepositoryTypesIfNeeded();
    this.props.fetchNamespaceStrategiesIfNeeded();
  }

  repoCreated = (repo: Repository) => {
    const { history } = this.props;

    history.push("/repo/" + repo.namespace + "/" + repo.name);
  };

  render() {
    const {
      pageLoading,
      createLoading,
      repositoryTypes,
      namespaceStrategies,
      createRepo,
      error
    } = this.props;

    const { t, repoLink } = this.props;
    return (
      <Page
        title={t("create.title")}
        subtitle={t("create.subtitle")}
        loading={pageLoading}
        error={error}
        showContentOnError={true}
      >
        <RepositoryForm
          repositoryTypes={repositoryTypes}
          loading={createLoading}
          namespaceStrategy={namespaceStrategies.current}
          submitForm={repo => {
            createRepo(repoLink, repo, (repo: Repository) =>
              this.repoCreated(repo)
            );
          }}
        />
      </Page>
    );
  }
}

const mapStateToProps = state => {
  const repositoryTypes = getRepositoryTypes(state);
  const namespaceStrategies = getNamespaceStrategies(state);
  const pageLoading = isFetchRepositoryTypesPending(state)
    || isFetchNamespaceStrategiesPending(state);
  const createLoading = isCreateRepoPending(state);
  const error = getFetchRepositoryTypesFailure(state) 
    || getCreateRepoFailure(state)
    || getFetchNamespaceStrategiesFailure(state);
  const repoLink = getRepositoriesLink(state);
  return {
    repositoryTypes,
    namespaceStrategies,
    pageLoading,
    createLoading,
    error,
    repoLink
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchRepositoryTypesIfNeeded: () => {
      dispatch(fetchRepositoryTypesIfNeeded());
    },
    fetchNamespaceStrategiesIfNeeded: () => {
      dispatch(fetchNamespaceStrategiesIfNeeded());
    },
    createRepo: (
      link: string,
      repository: Repository,
      callback: () => void
    ) => {
      dispatch(createRepo(link, repository, callback));
    },
    resetForm: () => {
      dispatch(createRepoReset());
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(Create));
