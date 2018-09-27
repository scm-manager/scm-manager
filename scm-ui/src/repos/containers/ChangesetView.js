//@flow
import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import type { Changeset, Repository } from "@scm-manager/ui-types";
import {
  fetchChangesetIfNeeded,
  fetchChangesetReset,
  getChangeset,
  getFetchChangesetFailure,
  isFetchChangesetPending
} from "../modules/changesets";
import ChangesetDetails from "../components/ChangesetDetails";
import { translate } from "react-i18next";
import { Loading, ErrorPage } from "@scm-manager/ui-components";

type Props = {
  id: string,
  changeset: Changeset,
  repository: Repository,
  loading: boolean,
  error: Error,
  fetchChangesetIfNeeded: (
    namespace: string,
    repoName: string,
    id: string
  ) => void,
  resetForm: (namespace: string, repoName: string, id: string) => void,
  match: any,
  t: string => string
};

class ChangesetView extends React.Component<Props> {
  componentDidMount() {
    const { fetchChangesetIfNeeded, repository } = this.props;
    const id = this.props.match.params.id;
    fetchChangesetIfNeeded(repository.namespace, repository.name, id);
    this.props.resetForm(repository.namespace, repository.name, id);
  }

  render() {
    const { changeset, loading, error, t, repository } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("changeset-error.title")}
          subtitle={t("changeset-error.subtitle")}
          error={error}
        />
      );
    }

    if (!changeset || loading) return <Loading />;

    return <ChangesetDetails changeset={changeset} repository={repository}/>;
  }
}

const mapStateToProps = (state, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const id = ownProps.match.params.id;
  const changeset = getChangeset(state, namespace, name, id);
  const loading = isFetchChangesetPending(state, namespace, name, id);
  const error = getFetchChangesetFailure(state, namespace, name, id);
  return { changeset, error, loading };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchChangesetIfNeeded: (
      namespace: string,
      repoName: string,
      id: string
    ) => {
      dispatch(fetchChangesetIfNeeded(namespace, repoName, id));
    },
    resetForm: (namespace: string, repoName: string, id: string) => {
      dispatch(fetchChangesetReset(namespace, repoName, id));
    }
  };
};

export default withRouter(
  connect(
    mapStateToProps,
    mapDispatchToProps
  )(translate("changesets")(ChangesetView))
);
