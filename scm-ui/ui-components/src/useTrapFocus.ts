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

import { MutableRefObject, RefObject, useCallback, useEffect, useMemo, useRef, useState } from "react";
import { FocusableElement, tabbable } from "tabbable";

type Node = HTMLDivElement | null;

interface UseTrapFocus {
  includeContainer?: boolean;
  initialFocus?: "container" | Node;
  returnFocus?: boolean;
  updateNodes?: boolean;
}

// Based on https://tobbelindstrom.com/blog/useTrapFocus/

export const useTrapFocus = (options: UseTrapFocus): MutableRefObject<Node> => {
  const node = useRef<Node>(null);
  const { includeContainer, initialFocus, returnFocus, updateNodes } = useMemo<UseTrapFocus>(() => {
    return {
      includeContainer: false,
      initialFocus: null,
      returnFocus: true,
      updateNodes: false,
      ...options
    };
  }, [options.initialFocus]);
  const [tabbableNodes, setTabbableNodes] = useState<FocusableElement[]>([]);
  const previousFocusedNode = useRef<Node>(document.activeElement as Node);

  const setInitialFocus = useCallback(() => {
    if (initialFocus === "container") {
      node.current?.focus();
    } else {
      initialFocus?.focus();
    }
  }, [initialFocus]);

  const updateTabbableNodes = useCallback(() => {
    const { current } = node;

    if (current) {
      const getTabbableNodes = tabbable(current, { includeContainer });
      setTabbableNodes(getTabbableNodes);
      return getTabbableNodes;
    }

    return [];
  }, [includeContainer]);

  useEffect(() => {
    updateTabbableNodes();
    if (node.current) setInitialFocus();
  }, [setInitialFocus, updateTabbableNodes]);

  useEffect(() => {
    return () => {
      const { current } = previousFocusedNode;
      if (current && returnFocus) current.focus();
    };
  }, [returnFocus]);

  const handleKeydown = useCallback(
    event => {
      const { key, keyCode, shiftKey } = event;

      let getTabbableNodes = tabbableNodes;
      if (updateNodes) getTabbableNodes = updateTabbableNodes();

      if ((key === "Tab" || keyCode === 9) && getTabbableNodes.length) {
        const firstNode = getTabbableNodes[0];
        const lastNode = getTabbableNodes[getTabbableNodes.length - 1];
        const { activeElement } = document;

        if (!getTabbableNodes.includes(activeElement as FocusableElement)) {
          event.preventDefault();
          shiftKey ? lastNode.focus() : firstNode.focus();
        }

        if (shiftKey && activeElement === firstNode) {
          event.preventDefault();
          lastNode.focus();
        }

        if (!shiftKey && activeElement === lastNode) {
          event.preventDefault();
          firstNode.focus();
        }
      }
    },
    [tabbableNodes, updateNodes, updateTabbableNodes]
  );

  useEventListener({
    type: "keydown",
    listener: handleKeydown
  });

  return node;
};

interface UseEventListener {
  type: keyof WindowEventMap;
  listener: EventListener;
  element?: RefObject<Element> | Document | Window | null;
  options?: AddEventListenerOptions;
}

export const useEventListener = ({
  type,
  listener,
  element = isSSR ? undefined : window,
  options
}: UseEventListener): void => {
  const savedListener = useRef<EventListener>();

  useEffect(() => {
    savedListener.current = listener;
  }, [listener]);

  const handleEventListener = useCallback((event: Event) => {
    savedListener.current?.(event);
  }, []);

  useEffect(() => {
    const target = getRefElement(element);
    target?.addEventListener(type, handleEventListener, options);
    return () => target?.removeEventListener(type, handleEventListener);
  }, [type, element, options, handleEventListener]);
};

const isSSR = !(typeof window !== "undefined" && window.document?.createElement);

const getRefElement = <T>(element?: RefObject<Element> | T): Element | T | undefined | null => {
  if (element && "current" in element) {
    return element.current;
  }

  return element;
};
