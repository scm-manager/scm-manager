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

import { useLocalStorage } from "@scm-manager/ui-api";
import { useCallback, useState } from "react";

const LOCAL_STORAGE_KEY = "scm.accessibility";

export type AccessibilityConfig = {
  deactivateShortcuts: boolean;
};

export const useAccessibilityConfig = () => {
  const [value, updateConfig] = useLocalStorage<AccessibilityConfig>(LOCAL_STORAGE_KEY, { deactivateShortcuts: false });
  const [isLoading, setLoading] = useState(false);
  const setValue = useCallback(
    (newConfig: AccessibilityConfig) => {
      setLoading(true);
      updateConfig(newConfig);
      window.location.reload();
    },
    [updateConfig]
  );
  return { value, setValue, isLoading } as const;
};
