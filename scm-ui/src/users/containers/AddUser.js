//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "./../components/UserForm";
import type { User } from "../types/User";
import type { History } from "history";
import {
  createUser,
  createUserReset,
  isCreateUserPending,
  getCreateUserFailure
} from "../modules/users";
import { Page } from "../../components/layout";
import { translate } from "react-i18next";

type Props = {
  loading?: boolean,
  error?: Error,

  // dispatcher functions
  addUser: (user: User, callback?: () => void) => void,
  resetForm: () => void,

  // context objects
  t: string => string,
  history: History
};

class AddUser extends React.Component<Props> {
  componentDidMount() {
    this.props.resetForm();
  }

  userCreated = () => {
    const { history } = this.props;
    history.push("/users");
  };

  createUser = (user: User) => {
    this.props.addUser(user, this.userCreated);
  };

  render() {
    const { t, loading, error } = this.props;

    return (
      <Page
        title={t("add-user.title")}
        subtitle={t("add-user.subtitle")}
        error={error}
        showContentOnError={true}
      >
        <UserForm
          submitForm={user => this.createUser(user)}
          loading={loading}
        />
      </Page>
    );
  }
}

const mapDispatchToProps = dispatch => {
  return {
    addUser: (user: User, callback?: () => void) => {
      dispatch(createUser(user, callback));
    },
    resetForm: () => {
      dispatch(createUserReset());
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  const loading = isCreateUserPending(state);
  const error = getCreateUserFailure(state);
  return {
    loading,
    error
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(AddUser));
