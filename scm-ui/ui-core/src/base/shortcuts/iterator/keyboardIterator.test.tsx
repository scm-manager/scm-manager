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
import {
  KeyboardIteratorContextProvider,
  KeyboardSubIterator,
  KeyboardSubIteratorContextProvider,
  useKeyboardIteratorItem,
} from "./keyboardIterator";
import { render } from "@testing-library/react";
import { ShortcutDocsContextProvider } from "../useShortcutDocs";
import Mousetrap from "mousetrap";
import "jest-extended";

jest.mock("react-i18next", () => ({
  useTranslation: () => [jest.fn()],
}));

const Wrapper: FC<{ initialIndex?: number }> = ({ children, initialIndex }) => {
  return (
    <ShortcutDocsContextProvider>
      <KeyboardIteratorContextProvider initialIndex={initialIndex}>{children}</KeyboardIteratorContextProvider>
    </ShortcutDocsContextProvider>
  );
};

const DocsWrapper: FC = ({ children }) => <ShortcutDocsContextProvider>{children}</ShortcutDocsContextProvider>;

const createWrapper =
  (initialIndex?: number): FC =>
  ({ children }) =>
    <Wrapper initialIndex={initialIndex}>{children}</Wrapper>;

const Item: FC<{ callback: () => void }> = ({ callback }) => {
  useKeyboardIteratorItem(callback);
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

    renderHook(() => useKeyboardIteratorItem(callback), {
      wrapper: Wrapper,
    });

    expect(callback).not.toHaveBeenCalled();
  });

  it("should not throw if not inside keyboard iterator context", () => {
    const callback = jest.fn();

    const { result, unmount } = renderHook(() => useKeyboardIteratorItem(callback), {
      wrapper: DocsWrapper,
    });

    unmount();

    expect(result.error).toBeUndefined();
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

    Mousetrap.trigger("j");

    expect(callback).toHaveBeenCalledTimes(1);
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();
  });

  it("should call last callback once upon pressing backward in initial state", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    render(
      <Wrapper>
        <List callbacks={[callback, callback2, callback3]} />
      </Wrapper>
    );

    Mousetrap.trigger("k");

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).toHaveBeenCalledTimes(1);
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

    Mousetrap.trigger("j");
    Mousetrap.trigger("j");

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

  it("should move to existing index when active index in the middle is deregistered", async () => {
    const callback = jest.fn();
    const callback2 = jest.fn();
    const callback3 = jest.fn();

    const { rerender } = render(<List callbacks={[callback, callback2, callback3]} />, {
      wrapper: createWrapper(1),
    });

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();

    rerender(<List callbacks={[callback, callback3]} />);

    expect(callback).toHaveBeenCalledTimes(1);
    expect(callback2).not.toHaveBeenCalled();
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

  describe("With Subiterator", () => {
    it("should call in correct order", () => {
      const callback = jest.fn();
      const callback2 = jest.fn();
      const callback3 = jest.fn();

      render(
        <Wrapper>
          <KeyboardSubIterator>
            <List callbacks={[callback, callback2]} />
          </KeyboardSubIterator>
          <List callbacks={[callback3]} />
        </Wrapper>
      );

      Mousetrap.trigger("j");
      Mousetrap.trigger("j");
      Mousetrap.trigger("j");

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback2).toHaveBeenCalledTimes(1);
      expect(callback3).toHaveBeenCalledTimes(1);
    });

    it("should call first target that is not an empty subiterator", () => {
      const callback = jest.fn();
      const callback2 = jest.fn();
      const callback3 = jest.fn();

      render(
        <Wrapper>
          <KeyboardSubIterator>
            <List callbacks={[]} />
          </KeyboardSubIterator>
          <KeyboardSubIterator>
            <List callbacks={[]} />
          </KeyboardSubIterator>
          <List callbacks={[callback, callback2, callback3]} />
        </Wrapper>
      );

      Mousetrap.trigger("j");

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback2).not.toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });

    it("should skip empty sub-iterators during navigation", () => {
      const callback = jest.fn();
      const callback2 = jest.fn();
      const callback3 = jest.fn();

      render(
        <Wrapper>
          <List callbacks={[callback]} />
          <KeyboardSubIterator>
            <List callbacks={[]} />
          </KeyboardSubIterator>
          <KeyboardSubIterator>
            <List callbacks={[]} />
          </KeyboardSubIterator>
          <List callbacks={[callback2, callback3]} />
        </Wrapper>
      );

      Mousetrap.trigger("j");
      Mousetrap.trigger("j");
      Mousetrap.trigger("j");

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback2).toHaveBeenCalledTimes(1);
      expect(callback3).toHaveBeenCalledTimes(1);
    });

    it("should not enter subiterator if its empty", () => {
      const callback = jest.fn();
      const callback2 = jest.fn();
      const callback3 = jest.fn();

      render(
        <Wrapper initialIndex={1}>
          <KeyboardSubIterator>
            <List callbacks={[]} />
          </KeyboardSubIterator>
          <List callbacks={[callback, callback2, callback3]} />
        </Wrapper>
      );

      Mousetrap.trigger("k");

      expect(callback).not.toHaveBeenCalled();
      expect(callback2).not.toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });

    it("should not loop", () => {
      const callback = jest.fn();
      const callback2 = jest.fn();
      const callback3 = jest.fn();

      render(
        <Wrapper initialIndex={1}>
          <KeyboardSubIterator>
            <List callbacks={[callback, callback2]} />
          </KeyboardSubIterator>
          <List callbacks={[callback3]} />
        </Wrapper>
      );

      Mousetrap.trigger("k");
      Mousetrap.trigger("k");
      Mousetrap.trigger("k");
      Mousetrap.trigger("k");
      Mousetrap.trigger("k");
      Mousetrap.trigger("k");

      expect(callback3).not.toHaveBeenCalled();
      expect(callback2).toHaveBeenCalledTimes(1);
      expect(callback).toHaveBeenCalledTimes(1);
    });

    it("should move subiterator if its active callback is de-registered", () => {
      const callback = jest.fn();
      const callback2 = jest.fn();
      const callback3 = jest.fn();
      const callback4 = jest.fn();

      const { rerender } = render(
        <>
          <KeyboardSubIteratorContextProvider initialIndex={1}>
            <List callbacks={[callback, callback2, callback3]} />
          </KeyboardSubIteratorContextProvider>
          <List callbacks={[callback4]} />
        </>,
        {
          wrapper: createWrapper(0),
        }
      );

      expect(callback).not.toHaveBeenCalled();
      expect(callback2).not.toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
      expect(callback4).not.toHaveBeenCalled();

      rerender(
        <>
          <KeyboardSubIteratorContextProvider initialIndex={1}>
            <List callbacks={[callback, callback3]} />
          </KeyboardSubIteratorContextProvider>
          <List callbacks={[callback4]} />
        </>
      );

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback2).not.toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
      expect(callback4).not.toHaveBeenCalled();
    });
  });
});
