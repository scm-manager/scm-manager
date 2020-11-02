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
import { withRouter } from "react-router-dom";
import UserForm from "../components/UserForm";
import DeleteUser from "./DeleteUser";
import { User } from "@scm-manager/ui-types";
import {
  fetchUserByLink,
  getModifyUserFailure,
  isModifyUserPending,
  modifyUser,
  modifyUserReset
} from "../modules/users";
import { History } from "history";
import { ErrorNotification } from "@scm-manager/ui-components";
import { compose } from "redux";
import UserConverter from "../components/UserConverter";

type Props = {
  loading: boolean;
  error: Error;

  // dispatch functions
  modifyUser: (user: User, callback?: () => void) => void;
  modifyUserReset: (p: User) => void;
  fetchUser: (user: User) => void;

  // context objects
  user: User;
  history: History;
};

class EditUser extends React.Component<Props> {
  componentDidMount() {
    const { modifyUserReset, user } = this.props;
    modifyUserReset(user);
  }

  userModified = (user: User) => () => {
    this.props.history.push(`/user/${user.name}`);
  };

  modifyUser = (user: User) => {
    this.props.modifyUser(user, this.userModified(user));
  };

  render() {
    const { user, loading, error } = this.props;
    return (
      <div>
        <ErrorNotification error={error} />
        <UserForm submitForm={this.modifyUser} user={user} loading={loading} />
        <hr />
        <UserConverter user={user} fetchUser={this.props.fetchUser} />
        <DeleteUser user={user} />
      </div>
    );
  }
}

const mapStateToProps = (state: any, ownProps: Props) => {
  const loading = isModifyUserPending(state, ownProps.user.name);
  const error = getModifyUserFailure(state, ownProps.user.name);
  return {
    loading,
    error
  };
};

const mapDispatchToProps = (dispatch: any) => {
  return {
    modifyUser: (user: User, callback?: () => void) => {
      dispatch(modifyUser(user, callback));
    },
    modifyUserReset: (user: User) => {
      dispatch(modifyUserReset(user));
    },
    fetchUser: (user: User) => {
      dispatch(fetchUserByLink(user));
    }
  };
};

export default compose(connect(mapStateToProps, mapDispatchToProps), withRouter)(EditUser);
