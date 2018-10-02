// @flow
import React from "react";
import { translate } from "react-i18next";
import { Subtitle, AddEntryToTableField } from "@scm-manager/ui-components";
import AdminGroupTable from "../table/AdminGroupTable";
import AdminUserTable from "../table/AdminUserTable";

type Props = {
  adminGroups: string[],
  adminUsers: string[],
  t: string => string,
  onChange: (boolean, any, string) => void,
  hasUpdatePermission: boolean
};

class AdminSettings extends React.Component<Props> {
  render() {
    const { t, adminGroups, adminUsers, hasUpdatePermission } = this.props;

    return (
      <div>
        <Subtitle subtitle={t("admin-settings.name")} />
        <AdminGroupTable
          adminGroups={adminGroups}
          onChange={(isValid, changedValue, name) =>
            this.props.onChange(isValid, changedValue, name)
          }
          disabled={!hasUpdatePermission}
        />
        <AddEntryToTableField
          addEntry={this.addGroup}
          disabled={!hasUpdatePermission}
          buttonLabel={t("admin-settings.add-group-button")}
          fieldLabel={t("admin-settings.add-group-textfield")}
          errorMessage={t("admin-settings.add-group-error")}
          helpText={t("help.adminGroupsHelpText")}
        />
        <AdminUserTable
          adminUsers={adminUsers}
          onChange={(isValid, changedValue, name) =>
            this.props.onChange(isValid, changedValue, name)
          }
          disabled={!hasUpdatePermission}
        />
        <AddEntryToTableField
          addEntry={this.addUser}
          disabled={!hasUpdatePermission}
          buttonLabel={t("admin-settings.add-user-button")}
          fieldLabel={t("admin-settings.add-user-textfield")}
          errorMessage={t("admin-settings.add-user-error")}
          helpText={t("help.adminUsersHelpText")}
        />
      </div>
    );
  }

  addGroup = (groupname: string) => {
    if (this.isAdminGroupMember(groupname)) {
      return;
    }
    this.props.onChange(
      true,
      [...this.props.adminGroups, groupname],
      "adminGroups"
    );
  };

  isAdminGroupMember = (groupname: string) => {
    return this.props.adminGroups.includes(groupname);
  };

  addUser = (username: string) => {
    if (this.isAdminUserMember(username)) {
      return;
    }
    this.props.onChange(
      true,
      [...this.props.adminUsers, username],
      "adminUsers"
    );
  };

  isAdminUserMember = (username: string) => {
    return this.props.adminUsers.includes(username);
  };
}

export default translate("config")(AdminSettings);
