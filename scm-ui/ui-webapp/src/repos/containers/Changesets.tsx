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
import { compose } from "redux";
import { withRouter, RouteComponentProps } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { Branch, Changeset, PagedCollection, Repository } from "@scm-manager/ui-types";
import {
  ChangesetList,
  ErrorNotification,
  getPageFromMatch,
  LinkPaginator,
  Loading,
  Notification
} from "@scm-manager/ui-components";
import {
  fetchChangesets,
  getChangesets,
  getFetchChangesetsFailure,
  isFetchChangesetsPending,
  selectListAsCollection
} from "../modules/changesets";

type Props = RouteComponentProps &
  WithTranslation & {
    repository: Repository;
    branch: Branch;
    page: number;

    // State props
    changesets: Changeset[];
    list: PagedCollection;
    loading: boolean;
    error: Error;

    // Dispatch props
    fetchChangesets: (p1: Repository, p2: Branch, p3: number) => void;
  };

class Changesets extends React.Component<Props> {
  componentDidMount() {
    const { fetchChangesets, repository, branch, page } = this.props;
    fetchChangesets(repository, branch, page);
  }

  shouldComponentUpdate(nextProps: Readonly<Props>): boolean {
    return this.props.changesets !== nextProps.changesets ||
      this.props.loading !== nextProps.loading ||
      this.props.error !== nextProps.error;
  }

  render() {
    const { changesets, loading, error, t } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }

    if (!changesets || changesets.length === 0) {
      return (
        <div className="panel-block">
          <Notification type="info">{t("changesets.noChangesets")}</Notification>
        </div>
      );
    }
    return (
      <>
        {this.renderList()}
        {this.renderPaginator()}
      </>
    );
  }

  renderList = () => {
    const { repository, changesets } = this.props;
    return (
      <div className="panel-block">
        <ChangesetList repository={repository} changesets={changesets} />
      </div>
    );
  };

  renderPaginator = () => {
    const { page, list } = this.props;
    if (list) {
      return (
        <div className="panel-footer">
          <LinkPaginator page={page} collection={list} />
        </div>
      );
    }
    return null;
  };
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    fetchChangesets: (repo: Repository, branch: Branch, page: number) => {
      dispatch(fetchChangesets(repo, branch, page));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, branch, match } = ownProps;
  const changesets = getChangesets(state, repository, branch);
  const loading = isFetchChangesetsPending(state, repository, branch);
  const error = getFetchChangesetsFailure(state, repository, branch);
  const list = selectListAsCollection(state, repository, branch);
  const page = getPageFromMatch(match);

  return {
    changesets,
    list,
    page,
    loading,
    error
  };
};

export default compose(withTranslation("repos"), withRouter, connect(mapStateToProps, mapDispatchToProps))(Changesets);
