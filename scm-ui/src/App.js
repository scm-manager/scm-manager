import React, { Component } from 'react';
import Navigation from './Navigation';
import Main from './Main';
import {withRouter} from 'react-router-dom';
import 'ces-theme/dist/css/ces.css';



class App extends Component {
  render() {
    return (
      <div className="App">
        <Navigation />
        <Main />
      </div>
    );
  }
}

export default withRouter(App);
