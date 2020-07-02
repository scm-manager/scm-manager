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
import { RouteComponentProps, withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { Repository } from "@scm-manager/ui-types";
import { confirmAlert, DeleteButton, ErrorNotification, Level, ButtonGroup } from "@scm-manager/ui-components";
import { deleteRepo, getDeleteRepoFailure, isDeleteRepoPending } from "../modules/repos";

type Props = RouteComponentProps &
  WithTranslation & {
    loading: boolean;
    error: Error;
    repository: Repository;
    confirmDialog?: boolean;
    deleteRepo: (p1: Repository, p2: () => void) => void;
  };

class DeleteRepo extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleted = () => {
    this.props.history.push("/repos/");
  };

  deleteRepo = () => {
    this.props.deleteRepo(this.props.repository, this.deleted);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("deleteRepo.confirmAlert.title"),
      message: t("deleteRepo.confirmAlert.message"),
      buttons: [
        {
          className: "is-outlined",
          label: t("deleteRepo.confirmAlert.submit"),
          onClick: () => this.deleteRepo()
        },
        {
          label: t("deleteRepo.confirmAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.repository._links.delete;
  };

  render() {
    const { loading, error, confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteRepo;

    if (!this.isDeletable()) {
      return null;
    }

    return (
      <>
        <ErrorNotification error={error} />
        <Level
          left={
            <div>
              <strong>{t("deleteRepo.subtitle")}</strong>
              <p>{t("deleteRepo.description")}</p>
            </div>
          }
          right={<DeleteButton label={t("deleteRepo.button")} action={action} loading={loading} />}
        />
      </>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const loading = isDeleteRepoPending(state, namespace, name);
  const error = getDeleteRepoFailure(state, namespace, name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    deleteRepo: (repo: Repository, callback: () => void) => {
      dispatch(deleteRepo(repo, callback));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withRouter, withTranslation("repos"))(DeleteRepo);
