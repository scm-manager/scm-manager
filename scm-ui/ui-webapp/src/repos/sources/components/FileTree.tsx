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
import { compose } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import { binder } from "@scm-manager/ui-extensions";
import { File, Repository } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Notification } from "@scm-manager/ui-components";
import {
  fetchSources,
  getFetchSourcesFailure,
  getHunkCount,
  getSources,
  isFetchSourcesPending
} from "../modules/sources";
import FileTreeLeaf from "./FileTreeLeaf";
import { Button } from "@scm-manager/ui-components";

type Hunk = {
  tree: File;
  loading: boolean;
  error: Error;
};

type Props = WithTranslation & {
  repository: Repository;
  revision: string;
  path: string;
  baseUrl: string;
  location: any;
  hunks: Hunk[];

  // dispatch props
  fetchSources: (repository: Repository, revision: string, path: string, hunk: number) => void;
  updateSources: (hunk: number) => () => void;

  // context props
  match: any;
};

type State = {
  stoppableUpdateHandler: number[];
};

const FixedWidthTh = styled.th`
  width: 16px;
`;

export function findParent(path: string) {
  if (path.endsWith("/")) {
    path = path.substring(0, path.length - 1);
  }

  const index = path.lastIndexOf("/");
  if (index > 0) {
    return path.substring(0, index);
  }
  return "";
}

class FileTree extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = { stoppableUpdateHandler: [] };
  }

  componentDidUpdate(prevProps: Readonly<Props>, prevState: Readonly<State>): void {
    if (prevState.stoppableUpdateHandler === this.state.stoppableUpdateHandler) {
      const { hunks, updateSources } = this.props;
      hunks?.forEach((hunk, index) => {
        if (hunk.tree?._embedded?.children && hunk.tree._embedded.children.find(c => c.partialResult)) {
          const stoppableUpdateHandler = setTimeout(updateSources(index), 3000);
          this.setState(prevState => {
            return {
              stoppableUpdateHandler: [...prevState.stoppableUpdateHandler, stoppableUpdateHandler]
            };
          });
        }
      });
    }
  }

  componentWillUnmount(): void {
    this.state.stoppableUpdateHandler.forEach(handler => clearTimeout(handler));
  }

  loadMore = () => {
    this.props.fetchSources(
      this.props.repository,
      decodeURIComponent(this.props.revision),
      this.props.path,
      this.props.hunks.length
    );
  };

  renderTruncatedInfo = () => {
    const { hunks, t } = this.props;
    const lastHunk = hunks[hunks.length - 1];
    const fileCount = hunks
      .filter(hunk => hunk?.tree?._embedded?.children)
      .map(hunk => hunk.tree._embedded.children.filter(c => !c.directory).length)
      .reduce((a, b) => a + b, 0);
    if (lastHunk.tree?.truncated) {
      return (
        <Notification type={"info"}>
          <div className={"columns is-centered"}>
            <div className={"column"}>{t("sources.moreFilesAvailable", { count: fileCount })}</div>
            <Button label={t("sources.loadMore")} action={this.loadMore} />
          </div>
        </Notification>
      );
    }
  };

  render() {
    const { hunks } = this.props;

    if (!hunks || hunks.length === 0) {
      return null;
    }

    if (hunks[0]?.error) {
      return <ErrorNotification error={hunks[0].error} />;
    }

    return (
      <div className="panel-block">
        {this.renderSourcesTable()}
        {this.renderTruncatedInfo()}
      </div>
    );
  }

  renderSourcesTable() {
    const { hunks, revision, path, baseUrl, t } = this.props;

    const files = [];

    if (path) {
      files.push({
        name: "..",
        path: findParent(path),
        directory: true
      });
    }

    if (hunks.every(hunk => hunk.loading)) {
      return <Loading />;
    }

    hunks
      .filter(hunk => !hunk.loading)
      .forEach(hunk => {
        if (hunk.tree?._embedded && hunk.tree._embedded.children) {
          const children = [...hunk.tree._embedded.children];
          files.push(...children);
        }
      });

    const loading = hunks.filter(hunk => hunk.loading).length > 0;

    if (loading || (files && files.length > 0)) {
      let baseUrlWithRevision = baseUrl;
      if (revision) {
        baseUrlWithRevision += "/" + encodeURIComponent(revision);
      } else {
        baseUrlWithRevision += "/" + encodeURIComponent(hunks[0].tree.revision);
      }

      return (
        <>
          <table className="table table-hover table-sm is-fullwidth">
            <thead>
              <tr>
                <FixedWidthTh />
                <th>{t("sources.fileTree.name")}</th>
                <th className="is-hidden-mobile">{t("sources.fileTree.length")}</th>
                <th className="is-hidden-mobile">{t("sources.fileTree.commitDate")}</th>
                <th className="is-hidden-touch">{t("sources.fileTree.description")}</th>
                {binder.hasExtension("repos.sources.tree.row.right") && <th className="is-hidden-mobile" />}
              </tr>
            </thead>
            <tbody>
              {files.map((file: any) => (
                <FileTreeLeaf key={file.name} file={file} baseUrl={baseUrlWithRevision} />
              ))}
            </tbody>
          </table>
          {hunks[hunks.length - 1].loading && <Loading />}
          {hunks[hunks.length - 1].error && <ErrorNotification error={hunks[hunks.length - 1].error} />}
        </>
      );
    }
    return <Notification type="info">{t("sources.noSources")}</Notification>;
  }
}

const mapDispatchToProps = (dispatch: any, ownProps: Props) => {
  const { repository, revision, path } = ownProps;

  return {
    updateSources: (hunk: number) => () => dispatch(fetchSources(repository, revision, path, false, hunk)),
    fetchSources: (repository: Repository, revision: string, path: string, hunk: number) => {
      dispatch(fetchSources(repository, revision, path, true, hunk));
    }
  };
};

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, revision, path } = ownProps;

  const error = getFetchSourcesFailure(state, repository, revision, path, 0);
  const hunkCount = getHunkCount(state, repository, revision, path);
  const hunks = [];
  for (let i = 0; i < hunkCount; ++i) {
    const tree = getSources(state, repository, revision, path, i);
    const loading = isFetchSourcesPending(state, repository, revision, path, i);
    const error = getFetchSourcesFailure(state, repository, revision, path, i);
    hunks.push({
      tree,
      loading,
      error
    });
  }

  return {
    revision,
    path,
    error,
    hunks
  };
};

export default compose(withRouter, connect(mapStateToProps, mapDispatchToProps))(withTranslation("repos")(FileTree));
