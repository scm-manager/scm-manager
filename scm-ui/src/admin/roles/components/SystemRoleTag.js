//@flow
import React from "react";
import injectSheet from "react-jss";
import { translate } from "react-i18next";
import { Tag } from "@scm-manager/ui-components";

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
        <Tag
          className={classes.tag}
          color="dark"
          label={t("repositoryRole.system")}
        />
      );
    }
    return null;
  }
}

export default injectSheet(styles)(translate("admin")(SystemRoleTag));
