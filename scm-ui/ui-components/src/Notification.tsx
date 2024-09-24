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

import React, { FC, ReactNode } from "react";
import classNames from "classnames";

type NotificationType = "primary" | "info" | "success" | "warning" | "danger" | "inherit";

type Props = {
  type?: NotificationType;
  onClose?: () => void;
  className?: string;
  children?: ReactNode;
  role?: string;
};

/**
 * @deprecated Please import the identical module from "@scm-manager/ui-core"
 */

const Notification: FC<Props> = ({ type = "info", onClose, className, children, role }) => {
  const renderCloseButton = () => {
    if (onClose) {
      return <button className="delete" onClick={onClose} />;
    }
    return null;
  };

  const color = type !== "inherit" ? "is-" + type : "";

  return (
    <div className={classNames("notification", color, className)} role={role}>
      {renderCloseButton()}
      {children}
    </div>
  );
};

export default Notification;
