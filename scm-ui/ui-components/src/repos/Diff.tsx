import React from "react";
import DiffFile from "./DiffFile";
import { DiffObjectProps, File } from "./DiffTypes";
import Notification from "../Notification";
import { WithTranslation, withTranslation } from "react-i18next";
import { DefaultCollapsed } from "./DefaultCollapsed";

type Props = WithTranslation &
  DiffObjectProps & {
    diff: File[];
    defaultCollapse?: DefaultCollapsed;
  };

class Diff extends React.Component<Props> {
  static defaultProps: Partial<Props> = {
    sideBySide: false
  };

  render() {
    const { diff, t, ...fileProps } = this.props;
    return (
      <>
        {diff.length === 0 ? (
          <Notification type="info">{t("diff.noDiffFound")}</Notification>
        ) : (
          diff.map((file, index) => <DiffFile key={index} file={file} {...fileProps} {...this.props} />)
        )}
      </>
    );
  }
}

export default withTranslation("repos")(Diff);
