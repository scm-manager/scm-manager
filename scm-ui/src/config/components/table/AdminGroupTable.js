//@flow
import React from "react";
import { translate } from "react-i18next";
import { RemoveEntryOfTableButton } from "../../../components/buttons";

type Props = {
  adminGroups: string[],
  t: string => string,
  onChange: (boolean, any, string) => void,
  disabled: boolean
};

type State = {};

class AdminGroupTable extends React.Component<Props, State> {
  render() {
    const { t, disabled } = this.props;
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
                    <RemoveEntryOfTableButton
                      entryname={group}
                      removeEntry={this.removeEntry}
                      disabled={disabled}
                      label={t("admin-settings.remove-group-button")}
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

  removeEntry = (groupname: string) => {
    const newGroups = this.props.adminGroups.filter(name => name !== groupname);
    this.props.onChange(true, newGroups, "adminGroups");
  };
}

export default translate("config")(AdminGroupTable);
