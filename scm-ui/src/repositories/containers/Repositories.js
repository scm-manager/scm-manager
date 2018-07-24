// @flow
import React from 'react';

import { Link } from 'react-router-dom';


type Props = {
}

class Repositories extends React.Component<Props> {

  render() {

   return (
        <div>
          <h1>SCM</h1>
          <h2>Repositories will be shown here.</h2>
          <Link to='/users'>Users hier!</Link>
        </div>
      )


  }

}

export default (Repositories);
