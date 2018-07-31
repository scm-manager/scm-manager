//@flow
import React from "react";
import type { Group } from "../../types/Group";
import { translate } from "react-i18next";
import GroupMember from "./GroupMember";

type Props = {
  group: Group,
  t: string => string
};

class Details extends React.Component<Props> {

  render() {
    const { group, t } = this.props;
    return (
      <table className="table">
        <tbody>
          <tr>
            <td>{t("group.name")}</td>
            <td>{group.name}</td>
          </tr>
          <tr>
            <td>{t("group.description")}</td>
            <td>{group.description}</td>
          </tr>
          <tr>
            <td>{t("group.creationDate")}</td>
            <td>{group.creationDate}</td>
          </tr>
          <tr>
            <td>{t("group.lastModified")}</td>
            <td>{group.lastModified}</td>
          </tr>
          <tr>
            <td>{t("group.type")}</td>
            <td>{group.type}</td>
          </tr>
          <tr>
            <td>{t("group.members")}</td>
            <td>
              <table><tbody>
                {group.members.map((member, index) => {
                  return <GroupMember key={index} member={member} />;
                })}
                </tbody></table>
            </td>
          </tr>
        </tbody>
      </table>
    );
  }
}

export default translate("groups")(Details);
