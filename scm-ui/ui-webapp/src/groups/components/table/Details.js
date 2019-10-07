//@flow
import React from "react";
import type { Group } from "@scm-manager/ui-types";
import GroupMember from "./GroupMember";
import { DateFromNow, Checkbox } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import injectSheet from "react-jss";

type Props = {
  group: Group,

  // Context props
  classes: any,
  t: string => string
};

const styles = {
  spacing: {
    padding: "0 !important"
  }
};

class Details extends React.Component<Props> {
  render() {
    const { group, t } = this.props;
    return (
      <table className="table content">
        <tbody>
        <tr>
          <th>{t("group.name")}</th>
          <td>{group.name}</td>
        </tr>
        <tr>
          <th>{t("group.description")}</th>
          <td>{group.description}</td>
        </tr>
        <tr>
          <th>{t("group.external")}</th>
          <td>
            <Checkbox checked={group.external} />
          </td>
        </tr>
        <tr>
          <th>{t("group.type")}</th>
          <td>{group.type}</td>
        </tr>
        <tr>
          <th>{t("group.creationDate")}</th>
          <td>
            <DateFromNow date={group.creationDate} />
          </td>
        </tr>
        <tr>
          <th>{t("group.lastModified")}</th>
          <td>
            <DateFromNow date={group.lastModified} />
          </td>
        </tr>
        {this.renderMembers()}
        </tbody>
      </table>
    );
  }

  renderMembers() {
    const { group, t, classes } = this.props;

    let member = null;
    if (group.members.length > 0) {
      member = (
        <tr>
          <th>{t("group.members")}</th>
          <td className={classes.spacing}>
            <ul>
              {group._embedded.members.map((member, index) => {
                return <GroupMember key={index} member={member}/>;
              })}
            </ul>
          </td>
        </tr>
      );
    }
    return member;
  }
}

export default injectSheet(styles)(translate("groups")(Details));
