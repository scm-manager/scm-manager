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

import ActiveModalCountContext from "./activeModalCountContext";
import React, { FC, useCallback, useState } from "react";

/**
 * A simple counter that allows developers to keep track of how many modals are currently open.
 *
 * A default provider instance wraps the SCM-Manager so there is no need for plugins to use this component.
 *
 * Required by {@link useActiveModals}.
 */
const ActiveModalCountContextProvider: FC = ({ children }) => {
  const [activeModalCount, setActiveModalCount] = useState(0);
  const incrementModalCount = useCallback(() => setActiveModalCount((prev) => prev + 1), []);
  const decrementModalCount = useCallback(() => setActiveModalCount((prev) => prev - 1), []);

  return (
    <ActiveModalCountContext.Provider
      value={{ value: activeModalCount, increment: incrementModalCount, decrement: decrementModalCount }}
    >
      {children}
    </ActiveModalCountContext.Provider>
  );
};

export default ActiveModalCountContextProvider;
