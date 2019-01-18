// @flow
import React from "react";
import { translate } from "react-i18next";
import type { User } from "@scm-manager/ui-types";
import { DeleteButton, confirmAlert } from "@scm-manager/ui-components";
import {connect} from "react-redux";
import {
  deleteUser, fetchUserByName,
  getDeleteUserFailure,
  getUserByName,
  isDeleteUserPending,
} from "../modules/users";
import type {History} from "history";

type Props = {
  user: User,
  confirmDialog?: boolean,

  // dispatcher functions
  fetchUserByName: (string, string) => void,
  deleteUser: (user: User, callback?: () => void) => void,

  // context objects
  t: string => string,
  history: History
};

class DeleteUser extends React.Component<Props> {
  userDeleted = () => {
    this.props.history.push("/users");
  };

  deleteUser = (user: User) => {
    this.props.deleteUser(user, this.userDeleted);
  };

  static defaultProps = {
    confirmDialog: true
  };

  deleteUser = () => {
    this.props.deleteUser(this.props.user);
  };

  confirmDelete = () => {
    const { t } = this.props;
    confirmAlert({
      title: t("single-user.delete.confirm-alert.title"),
      message: t("single-user.delete.confirm-alert.message"),
      buttons: [
        {
          label: t("single-user.delete.confirm-alert.submit"),
          onClick: () => this.deleteUser()
        },
        {
          label: t("single-user.delete.confirm-alert.cancel"),
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
    return <DeleteButton label={t("single-user.delete.button")} action={action} />;
  }
}

/*
const mapStateToProps = (state, ownProps) => {
  const name = ownProps.match.params.name;
  const user = getUserByName(state, name);
  const loading = isDeleteUserPending(state, name);
  const error = getDeleteUserFailure(state, name);
  return {
    name,
    user,
    loading,
    error
  };
};

const mapDispatchToProps = dispatch => {
  return {
    fetchUserByName: (link: string, name: string) => {
      dispatch(fetchUserByName(link, name));
    },
    deleteUser: (user: User, callback?: () => void) => {
      dispatch(deleteUser(user, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(DeleteUser));
*/
export default translate("users")(DeleteUser);
