//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "./UserForm";
import type { User } from "../types/User";
import type { History } from "history";
import { createUser } from "../modules/users";
import Page from "../../components/Page";

type Props = {
  addUser: (user: User, callback?: () => void) => void,
  loading?: boolean,
  error?: Error,
  history: History
};

class AddUser extends React.Component<Props> {
  userCreated = () => {
    const { history } = this.props;
    history.push("/users");
  };

  createUser = (user: User) => {
    this.props.addUser(user, this.userCreated);
  };

  render() {
    const { loading, error } = this.props;

    // TODO i18n
    return (
      <Page
        title="Create User"
        subtitle="Create a new user"
        loading={loading}
        error={error}
      >
        <UserForm submitForm={user => this.createUser(user)} />
      </Page>
    );
  }
}

const mapDispatchToProps = dispatch => {
  return {
    addUser: (user: User, callback?: () => void) => {
      dispatch(createUser(user, callback));
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  if (state.users && state.users.users) {
    return {
      loading: state.users.users.loading
    };
  }
  return {};
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(AddUser);
