import React from 'react';
import ReactDOM from 'react-dom';
import App from './App';
import registerServiceWorker from './registerServiceWorker';

import { Provider } from 'react-redux';
import createHistory from 'history/createBrowserHistory';
import createReduxStore from './createReduxStore';
import { ConnectedRouter } from 'react-router-redux';


// Create a history of your choosing (we're using a browser history in this case)
const history = createHistory({
  basename: process.env.PUBLIC_URL
});

window.appHistory = history;
// Add the reducer to your store on the `router` key
// Also apply our middleware for navigating
const store = createReduxStore(history);

ReactDOM.render(
  <Provider store={store}>
      { /* ConnectedRouter will use the store from Provider automatically */}
      <ConnectedRouter history={history}>
        <App />
      </ConnectedRouter>
  </Provider>,
  document.getElementById('root')
);

registerServiceWorker();
