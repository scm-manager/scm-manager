// @flow
import React from 'react';
import { connect } from 'react-redux';

import { fetchUsersIfNeeded } from '../modules/users';
import Login from '../Login';

type Props = {
  login: boolean,
  error: any,
  users: any,
  fetchUsersIfNeeded: () => void
}

class Users extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchUsersIfNeeded();
  }

  render() {
    const { login, error, users } = this.props;



    if(login) {
      return (
        <div>
          <h1>SCM</h1>
          <Login/>
        </div>
      );
    }
    else if(!login){
      return (
        <div>
          <h1>SCM</h1>
          <h2>Users</h2>
        </div>
      );
    }
  }

}

const mapStateToProps = (state) => {
  return null;
};

const mapDispatchToProps = (dispatch) => {
  return {
    fetchUsersIfNeeded: () => {
      dispatch(fetchUsersIfNeeded())
    }
  }
};

export default connect(mapStateToProps, mapDispatchToProps)(Users);
