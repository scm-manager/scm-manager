// @flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "@scm-manager/ui-types";
import {
  Subtitle,
  DeleteButton,
  confirmAlert
} from "@scm-manager/ui-components";
import { getDeleteUserFailure, isDeleteUserPending } from "../modules/users";
import { connect } from "react-redux";
import { ErrorNotification } from "@scm-manager/ui-components";

type Props = {
  loading: boolean,
  error: Error,
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
      title: t("deleteUser.confirmAlert.title"),
      message: t("deleteUser.confirmAlert.message"),
      buttons: [
        {
          label: t("deleteUser.confirmAlert.submit"),
          onClick: () => this.deleteUser()
        },
        {
          label: t("deleteUser.confirmAlert.cancel"),
          onClick: () => null
        }
      ]
    });
  };

  isDeletable = () => {
    return this.props.user._links.delete;
  };

  render() {
    const { loading, error, confirmDialog, t } = this.props;
    const action = confirmDialog ? this.confirmDelete : this.deleteUser;

    if (!this.isDeletable()) {
      return null;
    }

    return (
      <>
        <Subtitle subtitle={t("deleteUser.subtitle")} />
        <ErrorNotification error={error} />
        <div className="columns">
          <div className="column">
            <DeleteButton
              label={t("deleteUser.button")}
              action={action}
              loading={loading}
            />
          </div>
        </div>
      </>
    );
  }
}

const mapStateToProps = (state, ownProps) => {
  const loading = isDeleteUserPending(state, ownProps.user.name);
  const error = getDeleteUserFailure(state, ownProps.user.name);
  return {
    loading,
    error
  };
};

export default connect(mapStateToProps)(translate("users")(DeleteUser));
