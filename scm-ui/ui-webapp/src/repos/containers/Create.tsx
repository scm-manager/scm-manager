import React from "react";
import { connect } from "react-redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { NamespaceStrategies, Repository, RepositoryType } from "@scm-manager/ui-types";
import { Page } from "@scm-manager/ui-components";
import {
  fetchRepositoryTypesIfNeeded,
  getFetchRepositoryTypesFailure,
  getRepositoryTypes,
  isFetchRepositoryTypesPending
} from "../modules/repositoryTypes";
import RepositoryForm from "../components/form";
import { createRepo, createRepoReset, getCreateRepoFailure, isCreateRepoPending } from "../modules/repos";
import { getRepositoriesLink } from "../../modules/indexResource";
import {
  fetchNamespaceStrategiesIfNeeded,
  getFetchNamespaceStrategiesFailure,
  getNamespaceStrategies,
  isFetchNamespaceStrategiesPending
} from "../../admin/modules/namespaceStrategies";

type Props = WithTranslation & {
  repositoryTypes: RepositoryType[];
  namespaceStrategies: NamespaceStrategies;
  pageLoading: boolean;
  createLoading: boolean;
  error: Error;
  repoLink: string;

  // dispatch functions
  fetchNamespaceStrategiesIfNeeded: () => void;
  fetchRepositoryTypesIfNeeded: () => void;
  createRepo: (link: string, repository: Repository, initRepository: boolean, callback: (repo: Repository) => void) => void;
  resetForm: () => void;

  // context props
  history: History;
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
    const { pageLoading, createLoading, repositoryTypes, namespaceStrategies, createRepo, error } = this.props;

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
          submitForm={(repo, initRepository) => {
            createRepo(repoLink, repo, initRepository, (repo: Repository) => this.repoCreated(repo));
          }}
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
  return {
    repositoryTypes,
    namespaceStrategies,
    pageLoading,
    createLoading,
    error,
    repoLink
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
    createRepo: (link: string, repository: Repository, initRepository: boolean, callback: () => void) => {
      dispatch(createRepo(link, repository, initRepository, callback));
    },
    resetForm: () => {
      dispatch(createRepoReset());
    }
  };
};

export default connect(mapStateToProps, mapDispatchToProps)(withTranslation("repos")(Create));
