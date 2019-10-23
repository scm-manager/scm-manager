import React from "react";
import { connect } from "react-redux";
import { withRouter } from "react-router-dom";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { User } from "@scm-manager/ui-types";
import { Subtitle, DeleteButton, confirmAlert, ErrorNotification } from "@scm-manager/ui-components";
import { deleteUser, getDeleteUserFailure, isDeleteUserPending } from "../modules/users";

type Props = WithTranslation & {
  loading: boolean;
  error: Error;
  user: User;
  confirmDialog?: boolean;
  deleteUser: (user: User, callback?: () => void) => void;

  // context props
  history: History;
};

class DeleteUser extends React.Component<Props> {
  static defaultProps = {
    confirmDialog: true
  };

  userDeleted = () => {
    this.props.history.push("/users/");
  };

  deleteUser = () => {
    this.props.deleteUser(this.props.user, this.userDeleted);
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
            <DeleteButton label={t("deleteUser.button")} action={action} loading={loading} />
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

const mapDispatchToProps = dispatch => {
  return {
    deleteUser: (user: User, callback?: () => void) => {
      dispatch(deleteUser(user, callback));
    }
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(withRouter(withTranslation("users")(DeleteUser)));
