import "./enzyme";

import { createMount, createShallow } from "enzyme-context";
import { routerContext } from "enzyme-context-react-router-4";

const plugins = {
  history: routerContext()
};

export const mount = createMount(plugins);
export const shallow = createShallow(plugins);
