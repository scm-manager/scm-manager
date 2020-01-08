import React from "react";
import { connect } from "react-redux";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { User } from "@scm-manager/ui-types";
import { Page } from "@scm-manager/ui-components";
import { getUsersLink } from "../../modules/indexResource";
import { createUser, createUserReset, isCreateUserPending, getCreateUserFailure } from "../modules/users";
import UserForm from "../components/UserForm";

type Props = WithTranslation & {
  loading?: boolean;
  error?: Error;
  usersLink: string;

  // dispatcher functions
  addUser: (link: string, user: User, callback?: () => void) => void;
  resetForm: () => void;

  // context objects
  history: History;
};

class CreateUser extends React.Component<Props> {
  componentDidMount() {
    this.props.resetForm();
  }

  userCreated = (user: User) => {
    const { history } = this.props;
    history.push("/user/" + user.name);
  };

  createUser = (user: User) => {
    this.props.addUser(this.props.usersLink, user, () => this.userCreated(user));
  };

  render() {
    const { t, loading, error } = this.props;

    return (
      <Page title={t("createUser.title")} subtitle={t("createUser.subtitle")} error={error} showContentOnError={true}>
        <UserForm submitForm={user => this.createUser(user)} loading={loading} />
      </Page>
    );
  }
}

const mapDispatchToProps = (dispatch: any) => {
  return {
    addUser: (link: string, user: User, callback?: () => void) => {
      dispatch(createUser(link, user, callback));
    },
    resetForm: () => {
      dispatch(createUserReset());
    }
  };
};

const mapStateToProps = (state: any) => {
  const loading = isCreateUserPending(state);
  const error = getCreateUserFailure(state);
  const usersLink = getUsersLink(state);
  return {
    usersLink,
    loading,
    error
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withTranslation("users"))(CreateUser);
