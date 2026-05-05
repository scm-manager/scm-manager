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

import React, { FC, ReactNode } from "react";
import { render } from "@testing-library/react";
import Mousetrap from "mousetrap";
import { renderTestHook } from "../../helpers/devbuild";
import { ShortcutDocsContextProvider } from "../useShortcutDocs";
import {
  KeyboardIteratorContextProvider,
  KeyboardSubIterator,
  KeyboardSubIteratorContextProvider,
  useKeyboardIteratorItem,
} from "./keyboardIterator";

vi.mock("react-i18next", () => ({
  useTranslation: () => [vi.fn()],
}));

const Wrapper: FC<{ initialIndex?: number; children?: ReactNode }> = ({ children, initialIndex }) => {
  return (
    <ShortcutDocsContextProvider>
      <KeyboardIteratorContextProvider initialIndex={initialIndex}>{children}</KeyboardIteratorContextProvider>
    </ShortcutDocsContextProvider>
  );
};

const DocsWrapper: FC<{ children?: ReactNode }> = ({ children }) => (
  <ShortcutDocsContextProvider>{children}</ShortcutDocsContextProvider>
);

const createWrapper = (initialIndex?: number): FC<{ children?: ReactNode }> => {
  const Component: FC<{ children?: ReactNode }> = ({ children }) => (
    <Wrapper initialIndex={initialIndex}>{children}</Wrapper>
  );
  Component.displayName = "CreateWrapper";
  return Component;
};

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
    const callback = vi.fn();

    renderTestHook(() => useKeyboardIteratorItem(callback), Wrapper);

    expect(callback).not.toHaveBeenCalled();
  });

  it("should not throw if not inside keyboard iterator context", () => {
    const callback = vi.fn();

    const callHook = () => {
      const { unmount } = renderTestHook(() => useKeyboardIteratorItem(callback), DocsWrapper);
      unmount();
    };

    expect(callHook).not.toThrow();
  });

  it("should call first callback upon pressing forward in initial state", async () => {
    const callback = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

    render(
      <Wrapper>
        <List callbacks={[callback, callback2, callback3]} />
      </Wrapper>,
    );

    Mousetrap.trigger("j");

    expect(callback).toHaveBeenCalledTimes(1);
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).not.toHaveBeenCalled();
  });

  it("should call last callback once upon pressing backward in initial state", async () => {
    const callback = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

    render(
      <Wrapper>
        <List callbacks={[callback, callback2, callback3]} />
      </Wrapper>,
    );

    Mousetrap.trigger("k");

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).toHaveBeenCalledTimes(1);
  });

  it("should not allow moving past the end of the callback array", async () => {
    const callback = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

    render(
      <Wrapper initialIndex={1}>
        <List callbacks={[callback, callback2, callback3]} />
      </Wrapper>,
    );

    Mousetrap.trigger("j");
    Mousetrap.trigger("j");

    expect(callback).not.toHaveBeenCalled();
    expect(callback2).not.toHaveBeenCalled();
    expect(callback3).toHaveBeenCalledTimes(1);
  });

  it("should move to existing index when active index is at the end and last callback is deregistered", async () => {
    const callback = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

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
    const callback = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

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
    const callback = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

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
    const callback = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

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
    const callback = vi.fn();
    const callback2 = vi.fn();
    const callback3 = vi.fn();

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
    const callback = vi.fn();

    const { rerender } = render(<List callbacks={[callback]} />, {
      wrapper: createWrapper(),
    });

    expect(callback).not.toHaveBeenCalled();

    rerender(<List callbacks={[]} />);

    expect(callback).not.toHaveBeenCalled();
  });

  describe("With Subiterator", () => {
    it("should call in correct order", () => {
      const callback = vi.fn();
      const callback2 = vi.fn();
      const callback3 = vi.fn();

      render(
        <Wrapper>
          <KeyboardSubIterator>
            <List callbacks={[callback, callback2]} />
          </KeyboardSubIterator>
          <List callbacks={[callback3]} />
        </Wrapper>,
      );

      Mousetrap.trigger("j");
      Mousetrap.trigger("j");
      Mousetrap.trigger("j");

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback2).toHaveBeenCalledTimes(1);
      expect(callback3).toHaveBeenCalledTimes(1);
    });

    it("should call first target that is not an empty subiterator", () => {
      const callback = vi.fn();
      const callback2 = vi.fn();
      const callback3 = vi.fn();

      render(
        <Wrapper>
          <KeyboardSubIterator>
            <List callbacks={[]} />
          </KeyboardSubIterator>
          <KeyboardSubIterator>
            <List callbacks={[]} />
          </KeyboardSubIterator>
          <List callbacks={[callback, callback2, callback3]} />
        </Wrapper>,
      );

      Mousetrap.trigger("j");

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback2).not.toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });

    it("should skip empty sub-iterators during navigation", () => {
      const callback = vi.fn();
      const callback2 = vi.fn();
      const callback3 = vi.fn();

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
        </Wrapper>,
      );

      Mousetrap.trigger("j");
      Mousetrap.trigger("j");
      Mousetrap.trigger("j");

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback2).toHaveBeenCalledTimes(1);
      expect(callback3).toHaveBeenCalledTimes(1);
    });

    it("should not enter subiterator if its empty", () => {
      const callback = vi.fn();
      const callback2 = vi.fn();
      const callback3 = vi.fn();

      render(
        <Wrapper initialIndex={1}>
          <KeyboardSubIterator>
            <List callbacks={[]} />
          </KeyboardSubIterator>
          <List callbacks={[callback, callback2, callback3]} />
        </Wrapper>,
      );

      Mousetrap.trigger("k");

      expect(callback).not.toHaveBeenCalled();
      expect(callback2).not.toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
    });

    it("should not loop", () => {
      const callback = vi.fn();
      const callback2 = vi.fn();
      const callback3 = vi.fn();

      render(
        <Wrapper initialIndex={1}>
          <KeyboardSubIterator>
            <List callbacks={[callback, callback2]} />
          </KeyboardSubIterator>
          <List callbacks={[callback3]} />
        </Wrapper>,
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
      const callback = vi.fn();
      const callback2 = vi.fn();
      const callback3 = vi.fn();
      const callback4 = vi.fn();

      const { rerender } = render(
        <>
          <KeyboardSubIteratorContextProvider initialIndex={1}>
            <List callbacks={[callback, callback2, callback3]} />
          </KeyboardSubIteratorContextProvider>
          <List callbacks={[callback4]} />
        </>,
        {
          wrapper: createWrapper(0),
        },
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
        </>,
      );

      expect(callback).toHaveBeenCalledTimes(1);
      expect(callback2).not.toHaveBeenCalled();
      expect(callback3).not.toHaveBeenCalled();
      expect(callback4).not.toHaveBeenCalled();
    });
  });
});
