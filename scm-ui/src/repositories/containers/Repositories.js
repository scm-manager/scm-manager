// @flow
import React from 'react';
import { connect } from 'react-redux';

import { fetchRepositoriesIfNeeded } from '../modules/repositories';
import { Link } from 'react-router-dom';


type Props = {
  login: boolean,
  error: Error,
  repositories: any,
  fetchRepositoriesIfNeeded: () => void
}

class Repositories extends React.Component<Props> {

  componentDidMount() {
    this.props.fetchRepositoriesIfNeeded();
  }

  render() {
    const { login, error, repositories } = this.props;


   return (
        <div>
          <h1>SCM</h1>
          <h2>Startpage</h2>
          <Link to='/users'>Users hier!</Link>
        </div>
      )


  }

}

const mapStateToProps = (state) => {
  return null;
};

const mapDispatchToProps = (dispatch) => {
  return {
    fetchRepositoriesIfNeeded: () => {
      dispatch(fetchRepositoriesIfNeeded())
    }
  }
};

export default connect(mapStateToProps, mapDispatchToProps)(Repositories);
