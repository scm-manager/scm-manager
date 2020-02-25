import React from "react";
import { storiesOf } from "@storybook/react";
import Footer from "./Footer";
import { Binder, BinderContext } from "@scm-manager/ui-extensions";
import { Me } from "@scm-manager/ui-types";
import { EXTENSION_POINT } from "../avatar/Avatar";
// @ts-ignore ignore unknown png
import hitchhiker from "../__resources__/hitchhiker.png";
// @ts-ignore ignore unknown jpg
import marvin from "../__resources__/marvin.jpg";
import NavLink from "../navigation/NavLink";
import ExternalLink from "../navigation/ExternalLink";

const trillian: Me = {
  name: "trillian",
  displayName: "Trillian McMillian",
  mail: "tricia@hitchhiker.com",
  groups: ["crew"],
  _links: {}
};

const bindAvatar = (binder: Binder, avatar: string) => {
  binder.bind(EXTENSION_POINT, () => {
    return avatar;
  });
};

const bindLinks = (binder: Binder) => {
  binder.bind("footer.information", () => <ExternalLink to="#" label="REST API" />);
  binder.bind("footer.information", () => <ExternalLink to="#" label="CLI" />);
  binder.bind("footer.support", () => <ExternalLink to="#" label="FAQ" />);
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
    bindAvatar(binder, hitchhiker);
    return withBinder(binder);
  })
  .add("With Plugin Links", () => {
    const binder = new Binder("link-story");
    bindLinks(binder);
    return withBinder(binder);
  })
  .add("Full", () => {
    const binder = new Binder("link-story");
    bindAvatar(binder, marvin);
    bindLinks(binder);
    return withBinder(binder);
  });
