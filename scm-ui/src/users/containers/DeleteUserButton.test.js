import React from 'react';
import {configure, shallow} from 'enzyme';
import DeleteUserButton from "./DeleteUserButton";
import Adapter from 'enzyme-adapter-react-16';

import 'raf/polyfill';

configure({ adapter: new Adapter() });

it('should render nothing, if the delete link is missing', () => {

  const user = {
    _links: {}
  };

  const button = shallow(<DeleteUserButton user={ user } />);
  expect(button.text()).toBe("");
});

it('should render the button', () => {

  const user = {
    _links: {
      "delete": {
        "href": "/users"
      }
    }
  };

  const button = shallow(<DeleteUserButton user={ user } />);
  expect(button.text()).not.toBe("");
});

it('should call the delete user function with delete url', () => {

  const user = {
    _links: {
      "delete": {
        "href": "/users"
      }
    }
  };

  let calledUrl = null;

  function capture(url) {
    calledUrl = url;
  }

  const button = shallow(<DeleteUserButton user={ user } deleteUser={ capture } />);
  button.simulate("click");

  expect(calledUrl).toBe("/users");
});
