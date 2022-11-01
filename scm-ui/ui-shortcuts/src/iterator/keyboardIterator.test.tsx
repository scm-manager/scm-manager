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

import {renderHook} from "@testing-library/react-hooks";
import React, {FC} from "react";
import {KeyboardIteratorContextProvider, useKeyboardIteratorCallback,} from "./keyboardIterator";
import {render} from "@testing-library/react";
import {ShortcutDocsContextProvider} from "../useShortcutDocs";
import Mousetrap from "mousetrap";

jest.mock("react-i18next", () => ({
  useTranslation: () => [jest.fn()],
}));

const Wrapper: FC<{ initialIndex?: number }> = ({children, initialIndex}) => {
  return (
    <ShortcutDocsContextProvider>
      <KeyboardIteratorContextProvider initialIndex={initialIndex}>{children}</KeyboardIteratorContextProvider>
    </ShortcutDocsContextProvider>
  );
};

const createWrapper =
  (initialIndex?: number): FC =>
  ({ children }) =>
    <Wrapper initialIndex={initialIndex}>{children}</Wrapper>;

const Item: FC<{ callback: () => void }> = ({ callback }) => {
  useKeyboardIteratorCallback(callback);
  return <li>example</li>;
};

const List: FC<{ callbacks: Array<() => void> }> = ({ callbacks }) => {
  return (
    <ul data-testid="list">
      {callbacks.map((cb, idx) => (
        <Item key={idx} callback={cb} />
      ))}
    </ul>
  );
};

describe("shortcutIterator", () => {
  beforeEach(() => Mousetrap.reset());

  it("should not call callback upon registration", () => {
    const callback = jest.fn();

    renderHook(() => useKeyboardIteratorCallback(callback), {
      wrapper: Wrapper,
    });

    expect(callback).not.toHaveBeenCalled();
  });

  it("should call first callback upon pressing forward in initial state", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    render(
      <Wrapper>
        <List callbacks={[callback, callback2, callback3]} />
      </Wrapper>
    );

    Mousetrap.trigger("k");

    expect(callback).toHaveBeenCalledTimes(1);
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();
  });

  it("should call first callback once upon pressing backward in initial state", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    render(
      <Wrapper>
        <List callbacks={[callback, callback2, callback3]} />
      </Wrapper>
    );

    Mousetrap.trigger("j");
    Mousetrap.trigger("j");

    expect(callback).toHaveBeenCalledTimes(1);
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();
  });

  it("should not allow moving past the end of the callback array", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    render(
      <Wrapper initialIndex={1}>
        <List callbacks={[callback, callback2, callback3]} />
      </Wrapper>
    );

    Mousetrap.trigger("k");
    Mousetrap.trigger("k");

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).toHaveBeenCalledTimes(1);
  });

  it("should move to existing index when active index is at the end and last callback is deregistered", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    const { rerender } = render(<List callbacks={[callback, callback2, callback3]} />, {
      wrapper: createWrapper(2),
    });

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();

    rerender(<List callbacks={[callback, callback2]} />);

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).toHaveBeenCalledTimes(1);
    expect(callback3).not.toHaveBeenCalled();
  });

  it("should move to existing index when active index is at the beginning and first callback is deregistered", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    const { rerender } = render(<List callbacks={[callback, callback2, callback3]} />, {
      wrapper: createWrapper(0),
    });

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();

    rerender(<List callbacks={[callback2, callback3]} />);

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).toHaveBeenCalledTimes(1);
    expect(callback3).not.toHaveBeenCalled();
  });

  it("should move to existing index when active index is at the end and first callback is deregistered", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    const { rerender } = render(<List callbacks={[callback, callback2, callback3]} />, {
      wrapper: createWrapper(2),
    });

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();

    rerender(<List callbacks={[callback, callback2]} />);

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).toHaveBeenCalledTimes(1);
    expect(callback3).not.toHaveBeenCalled();
  });

  it("should not move on deregistration if iterator is not active", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    const { rerender } = render(<List callbacks={[callback, callback2, callback3]} />, {
      wrapper: createWrapper(),
    });

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();

    rerender(<List callbacks={[callback, callback2]} />);

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();
  });

  it("should not explode if the last item in the list is removed", async () => {
    const callback = jest.fn();

    const { rerender } = render(<List callbacks={[callback]} />, {
      wrapper: createWrapper(),
    });

    expect(callback).not.toHaveBeenCalled();

    rerender(<List callbacks={[]} />);

    expect(callback).not.toHaveBeenCalled();
  });
});
