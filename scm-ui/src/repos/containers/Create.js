// @flow
import React from "react";
import { connect } from "react-redux";
import { translate } from "react-i18next";
import { Page } from "../../components/layout";
import RepositoryForm from "../components/RepositoryForm";
import type { RepositoryType } from "../types/RepositoryTypes";
import {
  fetchRepositoryTypesIfNeeded,
  getFetchRepositoryTypesFailure,
  getRepositoryTypes,
  isFetchRepositoryTypesPending
} from "../modules/repository-types";

type Props = {
  repositoryTypes: RepositoryType[],
  typesLoading: boolean,
  error: Error,

  // dispatch functions
  fetchRepositoryTypesIfNeeded: () => void,

  // context props
  t: string => string
};

class Create extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchRepositoryTypesIfNeeded();
  }

  render() {
    const { typesLoading, repositoryTypes, error } = this.props;

    const { t } = this.props;
    return (
      <Page
        title={t("create.title")}
        subtitle={t("create.subtitle")}
        loading={typesLoading}
        error={error}
      >
        <RepositoryForm repositoryTypes={repositoryTypes} />
      </Page>
    );
  }
}

const mapStateToProps = state => {
  const repositoryTypes = getRepositoryTypes(state);
  const typesLoading = isFetchRepositoryTypesPending(state);
  const error = getFetchRepositoryTypesFailure(state);
  return {
    repositoryTypes,
    typesLoading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchRepositoryTypesIfNeeded: () => {
      dispatch(fetchRepositoryTypesIfNeeded());
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(Create));
