//@flow
import React from "react";
import type { Group } from "@scm-manager/ui-types";
import GroupMember from "./GroupMember";
import { DateFromNow } from "@scm-manager/ui-components";
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
          <td className="has-text-weight-semibold">{t("group.name")}</td>
          <td>{group.name}</td>
        </tr>
        <tr>
          <td className="has-text-weight-semibold">{t("group.description")}</td>
          <td>{group.description}</td>
        </tr>
        <tr>
          <td className="has-text-weight-semibold">{t("group.type")}</td>
          <td>{group.type}</td>
        </tr>
        <tr>
          <td className="has-text-weight-semibold">{t("group.creationDate")}</td>
          <td>
            <DateFromNow date={group.creationDate} />
          </td>
        </tr>
        <tr>
          <td className="has-text-weight-semibold">{t("group.lastModified")}</td>
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
          <td className="has-text-weight-semibold">{t("group.members")}</td>
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
