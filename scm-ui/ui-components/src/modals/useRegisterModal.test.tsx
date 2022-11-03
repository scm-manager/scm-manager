/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
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
