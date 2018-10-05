//@flow
import React from "react";
import { translate } from "react-i18next";
import ArrayConfigTable from "./ArrayConfigTable";

type Props = {
  adminUsers: string[],
  onChange: (boolean, any, string) => void,
  disabled: boolean,

  // context props
  t: string => string
};

class AdminUserTable extends React.Component<Props> {
  render() {
    const { adminUsers, t, disabled } = this.props;
    return (
      <ArrayConfigTable
        items={adminUsers}
        label={t("admin-settings.admin-users")}
        removeLabel={t("admin-settings.remove-user-button")}
        onRemove={this.removeEntry}
        disabled={disabled}
        helpText={t("help.adminUsersHelpText")}
      />
    );
  }

  removeEntry = (newUsers: string[]) => {
    this.props.onChange(true, newUsers, "adminUsers");
  };
}

export default translate("config")(AdminUserTable);
