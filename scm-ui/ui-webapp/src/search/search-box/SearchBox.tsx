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

import React, {
  FocusEventHandler,
  HTMLAttributes,
  KeyboardEvent,
  MouseEventHandler,
  RefObject,
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from "react";
import { useGeneratedId } from "@scm-manager/ui-components";
import { SearchBoxContext } from "./SearchBoxContext";
import { SearchBoxInput } from "./SearchBoxInput";
import { SearchBoxOption } from "./SearchBoxOption";
import { SearchBoxOptions } from "./SearchBoxOptions";

const SearchBox = React.forwardRef<
  HTMLDivElement,
  HTMLAttributes<HTMLDivElement> & {
    onQueryChange: (newQuery: string) => void;
    shouldClear?: boolean;
    query: string;
  }
>(({ children, query, onQueryChange, shouldClear, ...props }, ref) => {
  const [open, setOpen] = useState(false);
  const [activeId, setActiveId] = useState<string | undefined>();
  const [options, setOptions] = useState<RefObject<HTMLAnchorElement>[]>([]);
  const popupId = useGeneratedId();
  const inputRef = useRef<HTMLInputElement>(null);
  const registerOption = useCallback(
    (ref: RefObject<HTMLAnchorElement>) =>
      setOptions((prev) => {
        let indexToInsert = -1;
        for (let i = prev.length - 1; i >= 0; i--) {
          const loopTabStop = prev[i];
          if (loopTabStop.current?.id === ref.current?.id) {
            return prev;
          }
          if (
            indexToInsert === -1 &&
            loopTabStop.current &&
            ref.current &&
            !!(loopTabStop.current.compareDocumentPosition(ref.current) & Node.DOCUMENT_POSITION_FOLLOWING)
          ) {
            indexToInsert = i + 1;
            break;
          }
        }
        if (indexToInsert === -1) {
          indexToInsert = 0;
        }
        const result = prev.slice();
        result.splice(indexToInsert, 0, ref);
        return result;
      }),
    []
  );
  const deregisterOption = useCallback(
    (ref: RefObject<HTMLAnchorElement>) => setOptions((prev) => prev.filter((it) => it !== ref)),
    []
  );
  const handleInputBlur: FocusEventHandler<HTMLInputElement> = useCallback(
    (e) => {
      const close = () => {
        if (open && shouldClear) {
          onQueryChange?.("");
        }
        setOpen(false);
        setActiveId(undefined);
      };

      if (activeId && e.relatedTarget?.id === activeId) {
        setTimeout(close, 100);
      } else {
        close();
      }
    },
    [activeId, onQueryChange, open, shouldClear]
  );
  const handleInputKeyDown = useCallback(
    (e: KeyboardEvent<HTMLInputElement>) => {
      switch (e.key) {
        case "ArrowDown":
          if (activeId === undefined) {
            if (options.length > 0) {
              setActiveId(options[0].current?.id);
              setOpen(true);
            }
          } else {
            const nextId = options.findIndex((ref) => ref.current?.id === activeId) + 1;
            if (options.length > nextId) {
              setActiveId(options[nextId].current?.id);
            }
          }
          e.preventDefault();
          break;
        case "ArrowUp":
          if (activeId) {
            const nextId = options.findIndex((ref) => ref.current?.id === activeId) - 1;
            if (nextId >= 0) {
              setActiveId(options[nextId].current?.id);
            }
          }
          e.preventDefault();
          break;
        case "Escape":
          if (open) {
            setActiveId(undefined);
            setOpen(false);
          } else {
            onQueryChange?.("");
          }
          break;
        case "Enter":
          if (activeId) {
            const currentElement = options.find((ref) => ref.current?.id === activeId)?.current;
            currentElement?.click();
            setActiveId(undefined);
            setOpen(false);
            if (shouldClear) {
              onQueryChange?.("");
            }
          }
          break;
        case "ArrowLeft":
        case "ArrowRight":
          break;
        default:
          setOpen(true);
      }
    },
    [activeId, onQueryChange, open, options, shouldClear]
  );
  const handleInputFocus = useCallback(() => {
    if (query) {
      setOpen(true);
    }
  }, [query]);
  const handleOptionMouseEnter: MouseEventHandler<HTMLAnchorElement> = useCallback((e) => {
    setActiveId(e.currentTarget.id);
  }, []);
  useEffect(() => onQueryChange?.(query), [onQueryChange, query]);
  useEffect(() => {
    if (open && !activeId && options.length) {
      setActiveId(options[0].current?.id);
    }
  }, [activeId, open, options]);
  useEffect(() => {
    const activeOption = options.find((opt) => opt.current?.id === activeId);
    if (activeOption) {
      activeOption.current?.scrollIntoView({
        block: "nearest",
      });
    }
  }, [activeId, options]);

  return (
    <SearchBoxContext.Provider
      value={useMemo(
        () => ({
          query,
          onQueryChange,
          popupId,
          open,
          handleInputBlur,
          handleInputKeyDown,
          handleInputFocus,
          handleOptionMouseEnter,
          activeId,
          registerOption,
          deregisterOption,
          inputRef,
        }),
        [
          activeId,
          deregisterOption,
          handleInputBlur,
          handleInputFocus,
          handleInputKeyDown,
          handleOptionMouseEnter,
          onQueryChange,
          open,
          popupId,
          query,
          registerOption,
        ]
      )}
    >
      <div {...props} ref={ref}>
        {children}
      </div>
    </SearchBoxContext.Provider>
  );
});

export default Object.assign(SearchBox, {
  Input: SearchBoxInput,
  Options: Object.assign(SearchBoxOptions, {
    Option: SearchBoxOption,
  }),
});
