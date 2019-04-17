//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";
import { translate } from "react-i18next";

type Props = {
  defaultBranch?: boolean,
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
        <span className={classNames("tag is-dark", classes.tag)}>
          {t("branch.defaultTag")}
        </span>
      );
    }
    return null;
  }
}

export default injectSheet(styles)(translate("repos")(DefaultBranchTag));
