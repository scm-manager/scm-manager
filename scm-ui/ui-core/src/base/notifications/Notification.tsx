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

import React, { HTMLAttributes } from "react";
import classNames from "classnames";

type NotificationType = "primary" | "info" | "success" | "warning" | "danger" | "inherit";

type Props = {
  type?: NotificationType;
  onClose?: () => void;
  role?: string;
};

const Notification = React.forwardRef<HTMLDivElement, HTMLAttributes<HTMLDivElement> & Props>(
  ({ type = "info", onClose, className, children, role, ...props }, ref) => {
    const color = type !== "inherit" ? "is-" + type : "";

    return (
      <div className={classNames("notification", color, className)} role={role} ref={ref}>
        {onClose ? <button className="delete" onClick={onClose} /> : null}
        {children}
      </div>
    );
  }
);

export default Notification;
