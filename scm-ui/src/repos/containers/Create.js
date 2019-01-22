// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { Page } from "@scm-manager/ui-components";
import RepositoryForm from "../components/form";
import type { Repository, RepositoryType } from "@scm-manager/ui-types";
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

type Props = {
  repositoryTypes: RepositoryType[],
  typesLoading: boolean,
  createLoading: boolean,
  error: Error,
  repoLink: string,

  // dispatch functions
  fetchRepositoryTypesIfNeeded: () => void,
  createRepo: (link: string, Repository, callback: () => void) => void,
  resetForm: () => void,

  // context props
  t: string => string,
  history: History
};

class Create extends React.Component<Props> {
  componentDidMount() {
    this.props.resetForm();
    this.props.fetchRepositoryTypesIfNeeded();
  }

  repoCreated = (repo: Repository) => {
    const { history } = this.props;
    //TODO: Problem: repo name can be set in history, but repo namespace is not known without fetching anything
    history.push("/repos");
  };

  render() {
    const {
      typesLoading,
      createLoading,
      repositoryTypes,
      createRepo,
      error
    } = this.props;

    const { t, repoLink } = this.props;
    return (
      <Page
        title={t("create.title")}
        subtitle={t("create.subtitle")}
        loading={typesLoading}
        error={error}
        showContentOnError={true}
      >
        <RepositoryForm
          repositoryTypes={repositoryTypes}
          loading={createLoading}
          submitForm={repo => {
            createRepo(repoLink, repo, () => this.repoCreated(repo));
          }}
        />
      </Page>
    );
  }
}

const mapStateToProps = state => {
  const repositoryTypes = getRepositoryTypes(state);
  const typesLoading = isFetchRepositoryTypesPending(state);
  const createLoading = isCreateRepoPending(state);
  const error =
    getFetchRepositoryTypesFailure(state) || getCreateRepoFailure(state);
  const repoLink = getRepositoriesLink(state);
  return {
    repositoryTypes,
    typesLoading,
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
