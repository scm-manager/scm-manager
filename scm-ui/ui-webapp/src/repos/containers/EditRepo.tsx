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
import { RouteComponentProps, withRouter } from "react-router-dom";
import RepositoryForm from "../components/form";
import { Repository, Links } from "@scm-manager/ui-types";
import { getModifyRepoFailure, isModifyRepoPending, modifyRepo, modifyRepoReset } from "../modules/repos";
import { History } from "history";
import { ErrorNotification, Subtitle } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { compose } from "redux";
import RepositoryDangerZone from "./RepositoryDangerZone";
import { getLinks } from "../../modules/indexResource";
import { urls } from "@scm-manager/ui-components";
import { TranslationProps, withTranslation } from "react-i18next";

type Props = TranslationProps &
  RouteComponentProps & {
    loading: boolean;
    error: Error;
    indexLinks: Links;

    modifyRepo: (p1: Repository, p2: () => void) => void;
    modifyRepoReset: (p: Repository) => void;

    // context props
    repository: Repository;
    history: History;
  };

class EditRepo extends React.Component<Props> {
  componentDidMount() {
    const { modifyRepoReset, repository } = this.props;
    modifyRepoReset(repository);
  }

  repoModified = () => {
    const { history, repository } = this.props;
    history.push(`/repo/${repository.namespace}/${repository.name}`);
  };

  render() {
    const { loading, error, repository, indexLinks, t } = this.props;

    const url = urls.matchedUrl(this.props);

    const extensionProps = {
      repository,
      url
    };

    return (
      <>
        <Subtitle subtitle={t("repositoryForm.subtitle")} />
        <ErrorNotification error={error} />
        <RepositoryForm
          repository={this.props.repository}
          loading={loading}
          modifyRepository={repo => {
            this.props.modifyRepo(repo, this.repoModified);
          }}
        />
        <ExtensionPoint name="repo-config.route" props={extensionProps} renderAll={true} />
        <RepositoryDangerZone repository={repository} indexLinks={indexLinks} />
      </>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const loading = isModifyRepoPending(state, namespace, name);
  const error = getModifyRepoFailure(state, namespace, name);
  const indexLinks = getLinks(state);

  return {
    loading,
    error,
    indexLinks
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    modifyRepo: (repo: Repository, callback: () => void) => {
      dispatch(modifyRepo(repo, callback));
    },
    modifyRepoReset: (repo: Repository) => {
      dispatch(modifyRepoReset(repo));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withRouter)(withTranslation("repos")(EditRepo));
