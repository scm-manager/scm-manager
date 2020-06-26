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
import { withRouter } from "react-router-dom";
import RepositoryForm from "../components/form";
import { Repository } from "@scm-manager/ui-types";
import { getModifyRepoFailure, isModifyRepoPending, modifyRepo, modifyRepoReset } from "../modules/repos";
import { History } from "history";
import { ErrorNotification } from "@scm-manager/ui-components";
import { ExtensionPoint } from "@scm-manager/ui-extensions";
import { compose } from "redux";
import DangerZone from "./DangerZone";

type Props = {
  loading: boolean;
  error: Error;

  modifyRepo: (p1: Repository, p2: () => void) => void;
  modifyRepoReset: (p: Repository) => void;

  // context props
  repository: Repository;
  history: History;
  match: any;
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

  stripEndingSlash = (url: string) => {
    if (url.endsWith("/")) {
      return url.substring(0, url.length - 2);
    }
    return url;
  };

  matchedUrl = () => {
    return this.stripEndingSlash(this.props.match.url);
  };

  render() {
    const { loading, error, repository } = this.props;

    const url = this.matchedUrl();

    const extensionProps = {
      repository,
      url
    };

    return (
      <>
        <ErrorNotification error={error} />
        <RepositoryForm
          repository={this.props.repository}
          loading={loading}
          submitForm={repo => {
            this.props.modifyRepo(repo, this.repoModified);
          }}
        />
        <ExtensionPoint name="repo-config.route" props={extensionProps} renderAll={true} />
        <DangerZone repository={repository} />
      </>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { namespace, name } = ownProps.repository;
  const loading = isModifyRepoPending(state, namespace, name);
  const error = getModifyRepoFailure(state, namespace, name);
  return {
    loading,
    error
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

export default compose(connect(mapStateToProps, mapDispatchToProps), withRouter)(EditRepo);
