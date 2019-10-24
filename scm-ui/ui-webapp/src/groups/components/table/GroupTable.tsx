import React from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import GroupRow from "./GroupRow";
import { Group } from "@scm-manager/ui-types";

type Props = WithTranslation & {
  groups: Group[];
};

class GroupTable extends React.Component<Props> {
  render() {
    const { groups, t } = this.props;
    return (
      <table className="card-table table is-hoverable is-fullwidth">
        <thead>
          <tr>
            <th>{t("group.name")}</th>
            <th className="is-hidden-mobile">{t("group.description")}</th>
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

export default withTranslation("groups")(GroupTable);
