//@flow
import React from "react";
import injectSheet from "react-jss";
import classNames from "classnames";
import { translate } from "react-i18next";

type Props = {
  system?: boolean,

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

class SystemRoleTag extends React.Component<Props> {
  render() {
    const { system, classes, t } = this.props;

    if (system) {
      return (
        <span className={classNames("tag is-dark", classes.tag)}>
          {t("role.system")}
        </span>
      );
    }
    return null;
  }
}

export default injectSheet(styles)(translate("config")(SystemRoleTag));
