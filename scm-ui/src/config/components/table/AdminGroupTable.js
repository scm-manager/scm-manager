//@flow
import React from "react";
import { translate } from "react-i18next";
import ArrayConfigTable from "./ArrayConfigTable";

type Props = {
  adminGroups: string[],
  onChange: (boolean, any, string) => void,
  disabled: boolean,

  // context props
  t: string => string
};

type State = {};

class AdminGroupTable extends React.Component<Props, State> {
  render() {
    const { t, disabled, adminGroups } = this.props;
    return (
      <ArrayConfigTable
        items={adminGroups}
        label={t("admin-settings.admin-groups")}
        removeLabel={t("admin-settings.remove-group-button")}
        onRemove={this.removeEntry}
        disabled={disabled}
      />
    );
  }

  removeEntry = (newGroups: string[]) => {
    this.props.onChange(true, newGroups, "adminGroups");
  };
}

export default translate("config")(AdminGroupTable);
