// @flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "../types/User";
import type { UserEntry } from "../types/UserEntry";
import { confirmAlert } from "../../components/ConfirmAlert";
import DeleteButton from "../../components/DeleteButton";

type Props = {
  entry: UserEntry,
  confirmDialog?: boolean,
  t: string => string,
  deleteUser: (user: User) => void
};

class DeleteUserButton extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleteUser = () => {
    this.props.deleteUser(this.props.entry.entry);
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
    return this.props.entry.entry._links.delete;
  };

  render() {
    const { confirmDialog, entry, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteUser;

    if (!this.isDeletable()) {
      return null;
    }
    return (
      <DeleteButton
        label={t("delete-user-button.label")}
        action={action}
        loading={entry.loading}
      />
    );
  }
}

export default translate("users")(DeleteUserButton);
