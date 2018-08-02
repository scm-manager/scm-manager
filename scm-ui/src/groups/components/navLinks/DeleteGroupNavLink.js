// @flow
import React from "react";
import { translate } from "react-i18next";
import type { Group } from "../../types/Group";
import { confirmAlert } from "../../../components/modals/ConfirmAlert";
import { NavAction } from "../../../components/navigation";

type Props = {
  group: Group,
  confirmDialog?: boolean,
  t: string => string,
  deleteGroup: (group: Group) => void
};

export class DeleteGroupNavLink extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleteGroup = () => {
    this.props.deleteGroup(this.props.group);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("delete-group-button.confirm-alert.title"),
      message: t("delete-group-button.confirm-alert.message"),
      buttons: [
        {
          label: t("delete-group-button.confirm-alert.submit"),
          onClick: () => this.deleteGroup()
        },
        {
          label: t("delete-group-button.confirm-alert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.group._links.delete;
  };

  render() {
    const { confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteGroup;

    if (!this.isDeletable()) {
      return null;
    }
    return <NavAction label={t("delete-group-button.label")} action={action} />;
  }
}

export default translate("groups")(DeleteGroupNavLink);
