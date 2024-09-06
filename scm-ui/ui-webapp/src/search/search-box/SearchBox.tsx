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
import { useLocation } from "react-router-dom";

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
  const location = useLocation();
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
  const close = useCallback(() => {
    if (open && shouldClear) {
      onQueryChange?.("");
    }
    setOpen(false);
    setActiveId(undefined);
  }, [onQueryChange, open, shouldClear]);
  const handleInputBlur: FocusEventHandler<HTMLInputElement> = useCallback(
    (e) => {
      if (!activeId || e.relatedTarget?.id !== activeId) {
        close();
      }
    },
    [activeId, close]
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
  useEffect(close, [location]);

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
