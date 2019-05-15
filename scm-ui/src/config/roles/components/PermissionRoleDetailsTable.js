//@flow
import React from "react";
import type { Role } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import { compose } from "redux";
import injectSheet from "react-jss";

type Props = {
  role: Role,
  // context props
  t: string => string
};

const styles = {
  spacing: {
    padding: "0 !important"
  }
};

class PermissionRoleDetailsTable extends React.Component<Props> {
  render() {
    const { role, t } = this.props;
    return (
      <table className="table content">
        <tbody>
          <tr>
            <th>{t("repositoryRole.name")}</th>
            <td>{role.name}</td>
          </tr>
          <tr>
            <th>{t("repositoryRole.type")}</th>
            <td>{role.type}</td>
          </tr>
          {this.renderVerbs()}
        </tbody>
      </table>
    );
  }

  renderVerbs() {
    const { role, t, classes } = this.props;

    let verbs = null;
    if (role.verbs.length > 0) {
      verbs = (
        <tr>
          <th>{t("repositoryRole.verbs")}</th>
          <td className={classes.spacing}>
            <ul>
              {role.verbs.map(verb => {
                return <li>{verb}</li>;
              })}
            </ul>
          </td>
        </tr>
      );
    }
    return verbs;
  }
}

export default compose(
  injectSheet(styles),
  translate("config")
)(PermissionRoleDetailsTable);
