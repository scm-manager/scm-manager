//@flow
import React from "react";
import { translate } from "react-i18next";
import { connect } from "react-redux";
import injectSheet from "react-jss";
import FileTreeLeaf from "./FileTreeLeaf";
import type { Repository, File } from "@scm-manager/ui-types";
import { ErrorNotification, Loading } from "@scm-manager/ui-components";
import {
  fetchSources,
  getFetchSourcesFailure,
  isFetchSourcesPending,
  getSources
} from "../modules/sources";
import { withRouter } from "react-router-dom";
import { compose } from "redux";

const styles = {
  iconColumn: {
    width: "16px"
  }
};

type Props = {
  loading: boolean,
  error: Error,
  tree: File,
  repository: Repository,
  revision: string,
  path: string,
  baseUrl: string,
  fetchSources: (Repository, string, string) => void,
  // context props
  classes: any,
  t: string => string,
  match: any
};

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
  componentDidMount() {
    const { fetchSources, repository, revision, path } = this.props;

    fetchSources(repository, revision, path);
  }

  render() {
    const {
      error,
      loading,
      tree,
      revision,
      path,
      baseUrl,
      classes,
      t
    } = this.props;

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

    if (error) {
      return <ErrorNotification error={error} />;
    }

    if (loading) {
      return <Loading />;
    }
    if (!tree) {
      return null;
    }

    const files = [];
    if (path) {
      files.push({
        name: "..",
        path: findParent(path),
        directory: true
      });
    }

    files.push(...tree._embedded.children.sort(compareFiles));

    let baseUrlWithRevision = baseUrl;
    if (revision) {
      baseUrlWithRevision += "/" + revision;
    } else {
      baseUrlWithRevision += "/" + tree.revision;
    }

    return (
      <table className="table table-hover table-sm is-fullwidth">
        <thead>
          <tr>
            <th className={classes.iconColumn} />
            <th>{t("sources.file-tree.name")}</th>
            <th>{t("sources.file-tree.length")}</th>
            <th>{t("sources.file-tree.lastModified")}</th>
            <th>{t("sources.file-tree.description")}</th>
          </tr>
        </thead>
        <tbody>
          {files.map(file => (
            <FileTreeLeaf
              key={file.name}
              file={file}
              baseUrl={baseUrlWithRevision}
            />
          ))}
        </tbody>
      </table>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const { repository, match } = ownProps;
  const { revision, path } = match.params;

  const loading = isFetchSourcesPending(state, repository, revision, path);
  const error = getFetchSourcesFailure(state, repository, revision, path);
  const tree = getSources(state, repository, revision, path);

  return {
    loading,
    error,
    revision,
    path,
    tree
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchSources: (repository: Repository, revision: string, path: string) => {
      dispatch(fetchSources(repository, revision, path));
    }
  };
};

export default compose(
  withRouter,
  connect(
    mapStateToProps,
    mapDispatchToProps
  )
)(injectSheet(styles)(translate("repos")(FileTree)));
