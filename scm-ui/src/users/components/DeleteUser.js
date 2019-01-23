// @flow
import React from "react";
import { translate } from "react-i18next";
import { Subtitle, DeleteButton, confirmAlert } from "@scm-manager/ui-components";
import type { User } from "@scm-manager/ui-types";

type Props = {
  user: User,
  confirmDialog?: boolean,

  // dispatcher functions
  deleteUser: (user: User) => void,

  // context objects
  t: string => string
};

class DeleteUser extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  deleteUser = () => {
    this.props.deleteUser(this.props.user);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("delete.confirm-alert.title"),
      message: t("delete.confirm-alert.message"),
      buttons: [
        {
          label: t("delete.confirm-alert.submit"),
          onClick: () => this.deleteUser()
        },
        {
          label: t("delete.confirm-alert.cancel"),
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
    return (
      <>
        <Subtitle subtitle={t("delete.subtitle")} />
        <div className="columns">
          <div className="column">
            <DeleteButton
              label={t("delete.button")}
              action={action}
            />
          </div>
        </div>
      </>
    );
  }
}

export default translate("users")(DeleteUser);
