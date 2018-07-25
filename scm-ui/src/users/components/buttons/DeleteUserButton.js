// @flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "../../types/User";
import { confirmAlert } from "../../../components/modals/ConfirmAlert";
import { NavAction } from "../../../components/navigation";

type Props = {
  user: User,
  confirmDialog?: boolean,
  t: string => string,
  deleteUser: (user: User) => void
};

class DeleteUserButton extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleteUser = () => {
    this.props.deleteUser(this.props.user);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("delete-user-button.confirm-alert.title"),
      message: t("delete-user-button.confirm-alert.message"),
      buttons: [
        {
          label: t("delete-user-button.confirm-alert.submit"),
          onClick: () => this.deleteUser()
        },
        {
          label: t("delete-user-button.confirm-alert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.user._links.delete;
  };

  render() {
    const { confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteUser;

    if (!this.isDeletable()) {
      return null;
    }
    return <NavAction label={t("delete-user-button.label")} action={action} />;
  }
}

export default translate("users")(DeleteUserButton);
