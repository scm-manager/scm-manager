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

  // context props
  classes: any,
  t: string => string
};

class FileTree extends React.Component<Props> {
  render() {
    const { tree, classes, t } = this.props;

    const files = tree._embedded.files;

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
            <FileTreeLeaf key={file.name} file={file} />
          ))}
        </tbody>
      </table>
    );
  }
}
export default injectSheet(styles)(translate("repos")(FileTree));
