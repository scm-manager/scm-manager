import React from "react";
import { compose } from "redux";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";
import { binder } from "@scm-manager/ui-extensions";
import { Repository, File } from "@scm-manager/ui-types";
import { ErrorNotification, Loading, Notification } from "@scm-manager/ui-components";
import { getFetchSourcesFailure, isFetchSourcesPending, getSources } from "../modules/sources";
import FileTreeLeaf from "./FileTreeLeaf";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  tree: File;
  repository: Repository;
  revision: string;
  path: string;
  baseUrl: string;

  // context props
  match: any;
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

class FileTree extends React.Component<Props> {
  render() {
    const { error, loading, tree } = this.props;

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }
    if (!tree) {
      return null;
    }

    return <div className="panel-block">{this.renderSourcesTable()}</div>;
  }

  renderSourcesTable() {
    const { tree, revision, path, baseUrl, t } = this.props;

    const files = [];

    if (path) {
      files.push({
        name: "..",
        path: findParent(path),
        directory: true
      });
    }

    const compareFiles = function(f1: File, f2: File): number {
      if (f1.directory) {
        if (f2.directory) {
          return f1.name.localeCompare(f2.name);
        } else {
          return -1;
        }
      } else {
        if (f2.directory) {
          return 1;
        } else {
          return f1.name.localeCompare(f2.name);
        }
      }
    };

    if (tree._embedded && tree._embedded.children) {
      const children = [...tree._embedded.children].sort(compareFiles);
      files.push(...children);
    }

    if (files && files.length > 0) {
      let baseUrlWithRevision = baseUrl;
      if (revision) {
        baseUrlWithRevision += "/" + encodeURIComponent(revision);
      } else {
        baseUrlWithRevision += "/" + encodeURIComponent(tree.revision);
      }

      return (
        <table className="table table-hover table-sm is-fullwidth">
          <thead>
            <tr>
              <FixedWidthTh />
              <th>{t("sources.file-tree.name")}</th>
              <th className="is-hidden-mobile">{t("sources.file-tree.length")}</th>
              <th className="is-hidden-mobile">{t("sources.file-tree.lastModified")}</th>
              <th className="is-hidden-touch">{t("sources.file-tree.description")}</th>
              {binder.hasExtension("repos.sources.tree.row.right") && <th className="is-hidden-mobile" />}
            </tr>
          </thead>
          <tbody>
            {files.map(file => (
              <FileTreeLeaf key={file.name} file={file} baseUrl={baseUrlWithRevision} />
            ))}
          </tbody>
        </table>
      );
    }
    return <Notification type="info">{t("sources.noSources")}</Notification>;
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, revision, path } = ownProps;

  const loading = isFetchSourcesPending(state, repository, revision, path);
  const error = getFetchSourcesFailure(state, repository, revision, path);
  const tree = getSources(state, repository, revision, path);

  return {
    revision,
    path,
    loading,
    error,
    tree
  };
};

export default compose(
  withRouter,
  connect(mapStateToProps)
)(withTranslation("repos")(FileTree));
