/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
import React from "react";
import { connect } from "react-redux";
import { compose } from "redux";
import { WithTranslation, withTranslation } from "react-i18next";
import { History } from "history";
import { User } from "@scm-manager/ui-types";
import { Page } from "@scm-manager/ui-components";
import { mustGetUsersLink } from "../../modules/indexResource";
import { createUser, createUserReset, getCreateUserFailure, isCreateUserPending } from "../modules/users";
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
  const usersLink = mustGetUsersLink(state);
  return {
    usersLink,
    loading,
    error
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withTranslation("users"))(CreateUser);
