// @flow
import React from "react";
import { translate } from "react-i18next";
import { Subtitle, DeleteButton, confirmAlert } from "@scm-manager/ui-components";
import type { User } from "@scm-manager/ui-types";
import type { History } from "history";

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
  static defaultProps = {
    confirmDialog: true
  };

  userDeleted = () => {
    this.props.history.push("/users");
  };

  deleteUser = (user: User) => {
    this.props.deleteUser(user, this.userDeleted);
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
