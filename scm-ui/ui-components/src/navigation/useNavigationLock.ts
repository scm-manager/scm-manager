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

import { useEffect } from "react";

// This hook can be used to warn the user on reloading or closing the current page if the navigation lock is enabled.
const useNavigationLock = (enabled: boolean) => {
  useEffect(() => {
    if (enabled) {
      window.onbeforeunload = () => true;
    } else {
      // @ts-ignore We need to reset this listener if the lock was disabled
      window.onbeforeunload = undefined;
    }
    return () => {
      // @ts-ignore Remove this listener when the hook will be unmounted
      window.onbeforeunload = undefined;
    };
  }, [enabled]);
};

export default useNavigationLock;
