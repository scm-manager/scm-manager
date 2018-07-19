//@flow
import React from "react";
import { connect } from "react-redux";
import UserForm from "./UserForm";
import type { User } from "../types/User";
import Loading from "../../components/Loading";

import { updateUser, fetchUser } from "../modules/users";

type Props = {
  name: string,
  fetchUser: string => void,
  userEntry?: UserEntry,
  updateUser: User => void,
  loading: boolean
};

class EditUser extends React.Component<Props> {
  componentDidMount() {
    this.props.fetchUser(this.props.name);
  }

  render() {
    const submitUser = this.props.updateUser;

    const { userEntry } = this.props;

    if (!userEntry || userEntry.loading) {
      return <Loading />;
    } else {
      return (
        <div>
          <UserForm
            submitForm={user => submitUser(user)}
            user={userEntry.entry}
            loading={userEntry.loading}
          />
        </div>
      );
    }
  }
}

const mapDispatchToProps = dispatch => {
  return {
    fetchUser: (name: string) => {
      dispatch(fetchUser(name));
    },
    updateUser: (user: User) => {
      dispatch(updateUser(user));
    }
  };
};

const mapStateToProps = (state, ownProps) => {
  const name = ownProps.match.params.name;
  let userEntry;
  if (state.users && state.users.usersByNames) {
    userEntry = state.users.usersByNames[name];
  }

  return {
    name,
    userEntry
  };
};

export default connect(
  mapStateToProps,
  mapDispatchToProps
)(EditUser);
