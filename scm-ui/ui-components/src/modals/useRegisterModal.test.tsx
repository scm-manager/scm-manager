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

import { renderHook } from "@testing-library/react-hooks";
import React, { FC } from "react";
import ActiveModalCountContext, { ModalStateContextType } from "./activeModalCountContext";
import useRegisterModal from "./useRegisterModal";

const createWrapper =
  (context: ModalStateContextType): FC =>
  ({ children }) => {
    return <ActiveModalCountContext.Provider value={context}>{children}</ActiveModalCountContext.Provider>;
  };

describe("useRegisterModal", () => {
  it("should increment and not decrement on active registration", () => {
    const increment = jest.fn();
    const decrement = jest.fn();

    renderHook(() => useRegisterModal(true), {
      wrapper: createWrapper({ value: 0, increment, decrement }),
    });

    expect(decrement).not.toHaveBeenCalled();
    expect(increment).toHaveBeenCalled();
  });

  it("should not decrement on inactive registration", () => {
    const increment = jest.fn();
    const decrement = jest.fn();

    renderHook(() => useRegisterModal(false), {
      wrapper: createWrapper({ value: 0, increment, decrement }),
    });

    expect(decrement).not.toHaveBeenCalled();
    expect(increment).not.toHaveBeenCalled();
  });

  it("should not decrement on inactive de-registration", () => {
    const increment = jest.fn();
    const decrement = jest.fn();

    const result = renderHook(() => useRegisterModal(false), {
      wrapper: createWrapper({ value: 0, increment, decrement }),
    });

    result.unmount();

    expect(decrement).not.toHaveBeenCalled();
    expect(increment).not.toHaveBeenCalled();
  });

  it("should decrement once on active de-registration", () => {
    const increment = jest.fn();
    const decrement = jest.fn();

    const result = renderHook(() => useRegisterModal(false, true), {
      wrapper: createWrapper({ value: 0, increment, decrement }),
    });

    result.unmount();

    expect(decrement).toHaveBeenCalledTimes(1);
    expect(increment).not.toHaveBeenCalled();
  });

  it("should decrement once when switching from active to inactive", () => {
    const increment = jest.fn();
    const decrement = jest.fn();

    const result = renderHook(({ active }: { active: boolean }) => useRegisterModal(active), {
      wrapper: createWrapper({ value: 0, increment, decrement }),
      initialProps: { active: true },
    });

    result.rerender({ active: false });

    expect(decrement).toHaveBeenCalledTimes(1);
    expect(increment).toHaveBeenCalledTimes(1);
  });
});
