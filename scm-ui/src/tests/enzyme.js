import "raf/polyfill";

// Temporary hack to suppress error
// https://github.com/facebook/create-react-app/issues/3199#issuecomment-345024029
window.requestAnimationFrame = function(callback) {
  setTimeout(callback, 0);
  return 0;
};

import { configure } from "enzyme";
import Adapter from "enzyme-adapter-react-16";

configure({ adapter: new Adapter() });
