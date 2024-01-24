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
