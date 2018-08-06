import React from "react";
import { mount, shallow } from "enzyme";
import "../../tests/enzyme";
import "../../tests/i18n";
import DeleteNavAction from "./DeleteNavAction";

import { confirmAlert } from "../../components/modals/ConfirmAlert";
jest.mock("../../components/modals/ConfirmAlert");

describe("DeleteNavAction", () => {
  it("should render nothing, if the delete link is missing", () => {
    const repository = {
      _links: {}
    };

    const navLink = shallow(
      <DeleteNavAction repository={repository} delete={() => {}} />
    );
    expect(navLink.text()).toBe("");
  });

  it("should render the navLink", () => {
    const repository = {
      _links: {
        delete: {
          href: "/repositories"
        }
      }
    };

    const navLink = mount(
      <DeleteNavAction repository={repository} delete={() => {}} />
    );
    expect(navLink.text()).not.toBe("");
  });

  it("should open the confirm dialog on navLink click", () => {
    const repository = {
      _links: {
        delete: {
          href: "/repositorys"
        }
      }
    };

    const navLink = mount(
      <DeleteNavAction repository={repository} delete={() => {}} />
    );
    navLink.find("a").simulate("click");

    expect(confirmAlert.mock.calls.length).toBe(1);
  });

  it("should call the delete repository function with delete url", () => {
    const repository = {
      _links: {
        delete: {
          href: "/repos"
        }
      }
    };

    let calledUrl = null;
    function capture(repository) {
      calledUrl = repository._links.delete.href;
    }

    const navLink = mount(
      <DeleteNavAction
        repository={repository}
        confirmDialog={false}
        delete={capture}
      />
    );
    navLink.find("a").simulate("click");

    expect(calledUrl).toBe("/repos");
  });
});
