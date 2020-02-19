import React from "react";
import { storiesOf } from "@storybook/react";
import Footer from "./Footer";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
import { Me } from "@scm-manager/ui-types";
import { EXTENSION_POINT } from "../avatar/Avatar";
// @ts-ignore ignore unknown png
import avatar from "../__resources__/avatar.png";
import NavLink from "../navigation/NavLink";

const trillian: Me = {
  name: "trillian",
  displayName: "Trillian McMillian",
  mail: "tricia@hitchhiker.com",
  groups: ["crew"],
  _links: {}
};

const bindAvatar = (binder: Binder) => {
  binder.bind(EXTENSION_POINT, () => {
    return avatar;
  });
};

const bindLinks = (binder: Binder) => {
  binder.bind("footer.links", () => <a href="#">REST API</a>);
  binder.bind("footer.links", () => <a href="#">CLI</a>);
  binder.bind("profile.setting", () => <NavLink label="Authorized Keys" to="#" />);
};

const withBinder = (binder: Binder) => {
  return (
    <BinderContext.Provider value={binder}>
      <Footer me={trillian} version="2.0.0" links={{}} />
    </BinderContext.Provider>
  );
};

storiesOf("Layout|Footer", module)
  .add("Default", () => {
    return <Footer me={trillian} version="2.0.0" links={{}} />;
  })
  .add("With Avatar", () => {
    const binder = new Binder("avatar-story");
    bindAvatar(binder);
    return withBinder(binder);
  })
  .add("With Plugin Links", () => {
    const binder = new Binder("link-story");
    bindLinks(binder);
    return withBinder(binder);
  })
  .add("Full", () => {
    const binder = new Binder("link-story");
    bindAvatar(binder);
    bindLinks(binder);
    return withBinder(binder);
  });
