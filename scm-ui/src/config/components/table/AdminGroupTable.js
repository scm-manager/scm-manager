//@flow
import React from "react";
import { translate } from "react-i18next";
import RemoveAdminGroupButton from "../buttons/RemoveAdminGroupButton";

type Props = {
  adminGroups: string[],
  t: string => string,
  onChange: (boolean, any, string) => void
};

type State = {};

class AdminGroupTable extends React.Component<Props, State> {
  render() {
    const { t } = this.props;
    return (
      <div>
        <label className="label">{t("admin-settings.admin-groups")}</label>
        <table className="table is-hoverable is-fullwidth">
          <tbody>
            {this.props.adminGroups.map(group => {
              return (
                <tr key={group}>
                  <td key={group}>{group}</td>
                  <td>
                    <RemoveAdminGroupButton
                      groupname={group}
                      removeGroup={this.removeGroup}
                    />
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </div>
    );
  }

  removeGroup = (groupname: string) => {
    const newGroups = this.props.adminGroups.filter(name => name !== groupname);
    this.props.onChange(true, newGroups, "adminGroups");
  };
}

export default translate("config")(AdminGroupTable);
