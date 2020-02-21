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
import Button from "@scm-manager/ui-components/src/buttons/Button";

type Hunk = {
  tree: File;
  loading: boolean;
  error: Error;
  updateSources: (hunk: number) => void;
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
      const { hunks } = this.props;
      hunks?.forEach((hunk, index) => {
        if (hunk.tree?._embedded?.children && hunk.tree._embedded.children.find(c => c.partialResult)) {
          const stoppableUpdateHandler = setTimeout(hunk.updateSources, 3000);
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
    this.props.fetchSources(this.props.repository, this.props.revision, this.props.path, this.props.hunks.length);
  };

  render() {
    const { hunks, t } = this.props;

    if (!hunks || hunks.length === 0) {
      return null;
    }

    if (hunks.some(hunk => hunk.error)) {
      return <ErrorNotification error={hunks.map(hunk => hunk.error)[0]} />;
    }

    const lastHunk = hunks[hunks.length - 1];

    return (
      <div className="panel-block">
        {this.renderSourcesTable()}
        {lastHunk.tree?.truncated && <Button label={t("sources.loadMore")} action={this.loadMore} />}
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
                <th>{t("sources.file-tree.name")}</th>
                <th className="is-hidden-mobile">{t("sources.file-tree.length")}</th>
                <th className="is-hidden-mobile">{t("sources.file-tree.commitDate")}</th>
                <th className="is-hidden-touch">{t("sources.file-tree.description")}</th>
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
        </>
      );
    }
    return <Notification type="info">{t("sources.noSources")}</Notification>;
  }
}

const mapDispatchToProps = (dispatch: any, ownProps: Props) => {
  const { repository, revision, path } = ownProps;

  return {
    updateSources: (hunk: number) => dispatch(fetchSources(repository, revision, path, false, hunk)),
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
    hunks.push({
      tree,
      loading
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
