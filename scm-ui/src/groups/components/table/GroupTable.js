// @flow
import React from "react";
import { translate } from "react-i18next";
import GroupRow from "./GroupRow";
import type { Group } from "../../types/Group";

type Props = {
  t: string => string,
  groups: Group[]
};

class GroupTable extends React.Component<Props> {
  render() {
    const { groups, t } = this.props;
    return (
      <table className="table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th className="is-hidden-mobile">{t("group.name")}</th>
            <th>{t("group.description")}</th>
          </tr>
        </thead>
        <tbody>
          {groups.map((group, index) => {
            return <GroupRow key={index} group={group} />;
          })}
        </tbody>
      </table>
    );
  }
}

export default translate("groups")(GroupTable);
