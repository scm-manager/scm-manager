//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "../components/UserForm";
import type { User } from "@scm-manager/ui-types";
import type { History } from "history";
import {
  createUser,
  createUserReset,
  isCreateUserPending,
  getCreateUserFailure
} from "../modules/users";
import { Page } from "@scm-manager/ui-components";
import { translate } from "react-i18next";
import {getUsersLink} from "../../modules/indexResource";

type Props = {
  loading?: boolean,
  error?: Error,
  usersLink: string,

  // dispatcher functions
  addUser: (link: string, user: User, callback?: () => void) => void,
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
    this.props.addUser(this.props.usersLink, user, this.userCreated);
  };

  render() {
    const { t, loading, error } = this.props;

    return (
      <Page
        title={t("addUser.title")}
        subtitle={t("addUser.subtitle")}
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
    addUser: (link: string, user: User, callback?: () => void) => {
      dispatch(createUser(link, user, callback));
    },
    resetForm: () => {
      dispatch(createUserReset());
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  const loading = isCreateUserPending(state);
  const error = getCreateUserFailure(state);
  const usersLink = getUsersLink(state);
  return {
    usersLink,
    loading,
    error
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(translate("users")(AddUser));
