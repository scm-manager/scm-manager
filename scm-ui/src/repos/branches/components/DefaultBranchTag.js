//@flow
import React from "react";
import injectSheet from "react-jss";
import { translate } from "react-i18next";
import { Tag } from "@scm-manager/ui-components";

type Props = {
  defaultBranch?: boolean,

  // context props
  classes: any,
  t: string => string
};

const styles = {
  tag: {
    marginLeft: "0.75rem",
    verticalAlign: "inherit"
  }
};

class DefaultBranchTag extends React.Component<Props> {
  render() {
    const { defaultBranch, classes, t } = this.props;

    if (defaultBranch) {
      return (
        <Tag
          className={classes.tag}
          color="dark"
          label={t("branch.defaultTag")}
        />
      );
    }
    return null;
  }
}

export default injectSheet(styles)(translate("repos")(DefaultBranchTag));
