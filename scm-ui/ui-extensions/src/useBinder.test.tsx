import useBinder, { BinderContext } from "./useBinder";
import { Binder } from "./binder";
import { mount } from "enzyme";
import "@scm-manager/ui-tests/enzyme";
import React from "react";

describe("useBinder tests", () => {
  const BinderName = () => {
    const binder = useBinder();
    return <>{binder.name}</>;
  };

  it("should return default binder", () => {
    const rendered = mount(<BinderName />);
    expect(rendered.text()).toBe("default");
  });

  it("should return binder from context", () => {
    const binder = new Binder("from-context");
    const app = (
      <BinderContext.Provider value={binder}>
        <BinderName />
      </BinderContext.Provider>
    );

    const rendered = mount(app);
    expect(rendered.text()).toBe("from-context");
  });
});
