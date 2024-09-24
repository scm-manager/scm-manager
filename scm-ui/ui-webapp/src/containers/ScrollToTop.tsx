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

import { FC, ReactElement, useEffect, useRef } from "react";

type Props = {
  location?: Location;
  children: ReactElement;
};

const ScrollToTop: FC<Props> = ({ location, children }) => {
  const previousLocation = useRef(location);

  useEffect(() => {
    if (previousLocation?.current?.pathname !== location?.pathname) {
      window.scrollTo(0, 0);
    }
  }, [location]);

  return children;
};

export default ScrollToTop;
