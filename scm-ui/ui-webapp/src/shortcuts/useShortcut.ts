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

import { useEffect } from "react";
import Mousetrap from "mousetrap";
import useShortcutDocs from "./useShortcutDocs";

export type UseShortcutOptions = {
  /**
   * Whether the shortcut is currently active
   *
   * @default true
   */
  active?: boolean;
};

/**
 * ## Summary
 *
 * Binds a global keyboard shortcut to a given callback.
 *
 * The callback is automatically cleaned up upon unmount.
 *
 * ## Supported keys
 *
 * For modifier keys you can use shift, ctrl, alt, or meta.
 *
 * You can substitute option for alt and command for meta.
 *
 * Other special keys are backspace, tab, enter, return, capslock, esc, escape, space, pageup, pagedown, end, home, left, up, right, down, ins, del, and plus.
 *
 * Any other key you should be able to reference by name like a, /, $, *, or =.
 *
 * A "mod" helper exists which maps to either "command" on Mac and "ctrl" on Windows and Linux.
 *
 * ## Combinations
 *
 * Keys can be combined by separating them with a whitespace.
 * For using modifiers, prefix the key with the modifier and concat them with a "+".
 *
 * Please also refer to the examples.
 *
 * @param key The keycode combination that triggers the callback
 * @param callback The function that is executed when the key combination is pressed
 * @param description The translated description used for the shortcut documentation
 * @param options Whether the shortcut is currently active, defaults to true
 * @example useShortcut("a b", ...)
 * @example useShortcut("ctrl+shift+k", ...)
 * @see https://github.com/ccampbell/mousetrap
 * @see https://craig.is/killing/mice
 */
export default function useShortcut(
  key: string,
  callback: (e: KeyboardEvent) => void,
  description: string,
  options?: UseShortcutOptions
) {
  const { add, remove } = useShortcutDocs();
  useEffect(() => {
    const active = !options || options.active === undefined || options.active;
    if (active) {
      add(key, description);
      Mousetrap.bind(key, (e) => {
        callback(e);
        /*
         * Returning false disables default event behaviour and stops event bubbling.
         * Otherwise, a shortcut that moves focus to an input field would cause the key to be entered into the input at the same time.
         * We could move the decision to the callback, but this behaviour is an implementation detail of Mousetrap which we would like to hide.
         */
        return false;
      });
    }

    return () => {
      remove(key);
      Mousetrap.unbind(key);
    };
  }, [key, callback, add, remove, options, description]);
}
