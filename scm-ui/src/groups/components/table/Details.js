//@flow
import React from "react";
import type { Group } from "@scm-manager/ui-types";
import { translate } from "react-i18next";
import GroupMember from "./GroupMember";
import { DateFromNow } from "@scm-manager/ui-components";

type Props = {
  group: Group,
  t: string => string
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
    if (this.props.group.members.length > 0) {
      return (
        <tr>
          <td>
            {this.props.t("group.members")}
            <ul>
               {this.props.group._embedded.members.map((member, index) => {
                return <GroupMember key={index} member={member} />;
              })}
            </ul>
          </td>
        </tr>
      );
    } else {
      return;
    }
  }
}

export default translate("groups")(Details);
