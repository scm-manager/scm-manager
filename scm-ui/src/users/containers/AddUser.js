//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "./../components/UserForm";
import type { User } from "../types/User";
import type { History } from "history";
import { createUser, createUserReset } from "../modules/users";
import { Page } from "../../components/layout";
import { translate } from "react-i18next";

type Props = {
  t: string => string,
  addUser: (user: User, callback?: () => void) => void,
  loading?: boolean,
  error?: Error,
  history: History,
  resetForm: () => void
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
  if (state.users && state.users.create) {
    return state.users.create;
  }
  return {};
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(AddUser));
