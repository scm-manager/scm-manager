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

import { useContext, useEffect, useRef } from "react";
import ActiveModalCount from "./activeModalCountContext";

/**
 * Should not yet be part of the public API, as it is exclusively used by the {@link Modal} component.
 *
 * @param active Whether the modal is currently open
 * @param initialValue DO NOT USE - Used only for testing purposes
 */
export default function useRegisterModal(active: boolean, initialValue: boolean | null = null) {
  const { increment, decrement } = useContext(ActiveModalCount);
  const previousActiveState = useRef<boolean | null>(initialValue);
  useEffect(() => {
    if (active) {
      previousActiveState.current = true;
      if (increment) {
        increment();
      }
    } else {
      if (previousActiveState.current !== null) {
        if (decrement) {
          decrement();
        }
      }
      previousActiveState.current = false;
    }
    return () => {
      if (previousActiveState.current) {
        if (decrement) {
          decrement();
        }
        previousActiveState.current = null;
      }
    };
  }, [active, decrement, increment]);
}
