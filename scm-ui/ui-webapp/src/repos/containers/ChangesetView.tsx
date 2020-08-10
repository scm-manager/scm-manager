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
import {connect} from "react-redux";
import {compose} from "redux";
import {withRouter} from "react-router-dom";
import {WithTranslation, withTranslation} from "react-i18next";
import {Changeset, Repository} from "@scm-manager/ui-types";
import {ErrorPage, Loading} from "@scm-manager/ui-components";
import {
  fetchChangesetIfNeeded,
  getChangeset,
  getFetchChangesetFailure,
  isFetchChangesetPending
} from "../modules/changesets";
import ChangesetDetails from "../components/changesets/ChangesetDetails";
import {FileControlFactory} from "@scm-manager/ui-components";

type Props = WithTranslation & {
  id: string;
  changeset: Changeset;
  repository: Repository;
  fileControlFactoryFactory?: (changeset: Changeset) => FileControlFactory;
  loading: boolean;
  error: Error;
  fetchChangesetIfNeeded: (repository: Repository, id: string) => void;
  match: any;
};

class ChangesetView extends React.Component<Props> {
  componentDidMount() {
    const {fetchChangesetIfNeeded, repository, id} = this.props;
    fetchChangesetIfNeeded(repository, id);
  }

  componentDidUpdate(prevProps: Props) {
    const {fetchChangesetIfNeeded, repository, id} = this.props;
    if (prevProps.id !== id) {
      fetchChangesetIfNeeded(repository, id);
    }
  }

  render() {
    const {changeset, loading, error, t, repository, fileControlFactoryFactory} = this.props;

    if (error) {
      return <ErrorPage title={t("changesets.errorTitle")} subtitle={t("changesets.errorSubtitle")} error={error}/>;
    }

    if (!changeset || loading) return <Loading/>;

    return <ChangesetDetails changeset={changeset} repository={repository}
                             fileControlFactory={fileControlFactoryFactory && fileControlFactoryFactory(changeset)}/>;
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const repository = ownProps.repository;
  const id = ownProps.match.params.id;
  const changeset = getChangeset(state, repository, id);
  const loading = isFetchChangesetPending(state, repository, id);
  const error = getFetchChangesetFailure(state, repository, id);
  return {
    id,
    changeset,
    error,
    loading
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchChangesetIfNeeded: (repository: Repository, id: string) => {
      dispatch(fetchChangesetIfNeeded(repository, id));
    }
  };
};

export default compose(
  withRouter,
  connect(mapStateToProps, mapDispatchToProps),
  withTranslation("repos")
)(ChangesetView);
