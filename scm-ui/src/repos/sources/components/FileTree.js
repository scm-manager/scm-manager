//@flow
import React from "react";
import { translate } from "react-i18next";
import injectSheet from "react-jss";
import FileTreeLeaf from "./FileTreeLeaf";
import type { SourcesCollection } from "@scm-manager/ui-types";

const styles = {
  iconColumn: {
    width: "16px"
  }
};

type Props = {
  tree: SourcesCollection,
  path: string,
  baseUrl: string,

  // context props
  classes: any,
  t: string => string
};

export function findParent(path: string) {
  if (path.endsWith("/")) {
    path = path.substring(path, path.length - 1);
  }

  const index = path.lastIndexOf("/");
  if (index > 0) {
    return path.substring(0, index);
  }
  return "";
}

class FileTree extends React.Component<Props> {
  render() {
    const { tree, path, baseUrl, classes, t } = this.props;
    const baseUrlWithRevision = baseUrl + "/" + tree.revision;

    const files = [];
    if (path) {
      files.push({
        name: "..",
        path: findParent(path),
        directory: true
      });
    }
    files.push(...tree._embedded.files);

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
export default injectSheet(styles)(translate("repos")(FileTree));
