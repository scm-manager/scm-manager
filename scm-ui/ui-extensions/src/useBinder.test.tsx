/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import useBinder, { BinderContext } from "./useBinder";
import { Binder } from "./binder";
import { mount } from "enzyme";
import "@scm-manager/ui-tests";
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
