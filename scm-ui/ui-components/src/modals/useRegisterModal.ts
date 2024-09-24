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
