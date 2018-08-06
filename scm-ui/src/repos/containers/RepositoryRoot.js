//@flow
import React from "react";
import {
  deleteRepo,
  fetchRepo,
  getFetchRepoFailure,
  getRepository,
  isFetchRepoPending
} from "../modules/repos";
import { connect } from "react-redux";
import { Route } from "react-router-dom";
import type { Repository } from "../types/Repositories";
import { Page } from "../../components/layout";
import Loading from "../../components/Loading";
import ErrorPage from "../../components/ErrorPage";
import { translate } from "react-i18next";
import { Navigation, NavLink, Section } from "../../components/navigation";
import RepositoryDetails from "../components/RepositoryDetails";
import DeleteNavAction from "../components/DeleteNavAction";

import type { History } from "history";

type Props = {
  namespace: string,
  name: string,
  repository: Repository,
  loading: boolean,
  error: Error,

  // dispatch functions
  fetchRepo: (namespace: string, name: string) => void,
  deleteRepo: (repository: Repository, () => void) => void,

  // context props
  t: string => string,
  history: History,
  match: any
};

class RepositoryRoot extends React.Component<Props> {
  componentDidMount() {
    const { fetchRepo, namespace, name } = this.props;

    fetchRepo(namespace, name);
  }

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  deleted = () => {
    this.props.history.push("/repos");
  };

  delete = (repository: Repository) => {
    this.props.deleteRepo(repository, this.deleted);
  };

  render() {
    const { loading, error, repository, t } = this.props;

    if (error) {
      return (
        <ErrorPage
          title={t("repository-root.error-title")}
          subtitle={t("repository-root.error-subtitle")}
          error={error}
        />
      );
    }

    if (!repository || loading) {
      return <Loading />;
    }

    const url = this.matchedUrl();

    return (
      <Page title={repository.namespace + "/" + repository.name}>
        <div className="columns">
          <div className="column is-three-quarters">
            <Route
              path={url}
              exact
              component={() => <RepositoryDetails repository={repository} />}
            />
          </div>
          <div className="column">
            <Navigation>
              <Section label={t("repository-root.actions-label")}>
                <DeleteNavAction repository={repository} delete={this.delete} />
                <NavLink to="/repos" label={t("repository-root.back-label")} />
              </Section>
            </Navigation>
          </div>
        </div>
      </Page>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const { namespace, name } = ownProps.match.params;
  const repository = getRepository(state, namespace, name);
  const loading = isFetchRepoPending(state, namespace, name);
  const error = getFetchRepoFailure(state, namespace, name);
  return {
    namespace,
    name,
    repository,
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchRepo: (namespace: string, name: string) => {
      dispatch(fetchRepo(namespace, name));
    },
    deleteRepo: (repository: Repository, callback: () => void) => {
      dispatch(deleteRepo(repository, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("repos")(RepositoryRoot));
