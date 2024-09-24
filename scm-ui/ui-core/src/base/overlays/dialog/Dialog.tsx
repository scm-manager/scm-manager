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

import * as RadixDialog from "@radix-ui/react-dialog";
import { DialogProps } from "@radix-ui/react-dialog";
import React, { ComponentProps, ReactComponentElement, ReactElement, ReactNode, useCallback } from "react";
import { Button } from "../../buttons";
import { useTranslation } from "react-i18next";

export const CloseButton = React.forwardRef<HTMLButtonElement, ComponentProps<typeof Button>>(
  ({ children, ...props }, ref) => (
    <RadixDialog.Close asChild>
      <Button {...props} ref={ref}>
        {children}
      </Button>
    </RadixDialog.Close>
  )
);

type Props = {
  trigger: ReactComponentElement<typeof Button>;
  title: ReactNode;
  description?: ReactNode;
  footer?: ReactElement[] | ((close: () => void) => ReactElement[]);
} & Pick<DialogProps, "onOpenChange" | "open" | "children">;

/**
 * @beta
 * @since 2.46.0
 */
const Dialog = React.forwardRef<HTMLDivElement, Props>(
  ({ children, onOpenChange, open, footer, title, trigger, description }, ref) => {
    const contentProps = description ? {} : { "aria-describedby": undefined };
    const close = useCallback(() => onOpenChange && onOpenChange(false), [onOpenChange]);
    const [t] = useTranslation("commons");
    return (
      <RadixDialog.Root open={open} onOpenChange={onOpenChange}>
        <RadixDialog.Trigger asChild>{trigger}</RadixDialog.Trigger>
        <RadixDialog.Portal>
          <RadixDialog.Overlay className="modal-background" />
          <RadixDialog.Content className="modal is-active" ref={ref} {...contentProps}>
            <div className="modal-card">
              <div className="modal-card-head">
                <RadixDialog.Title className="modal-card-title">{title}</RadixDialog.Title>
                <RadixDialog.Close asChild>
                  <button className="delete" aria-label={t("dialog.closeButton.ariaLabel")} />
                </RadixDialog.Close>
              </div>
              <div className="modal-card-body">
                {description ? <RadixDialog.Description>{description}</RadixDialog.Description> : null}
                {children}
              </div>
              {footer ? (
                <div className="modal-card-foot">{typeof footer === "function" ? footer(close) : footer}</div>
              ) : null}
            </div>
          </RadixDialog.Content>
        </RadixDialog.Portal>
      </RadixDialog.Root>
    );
  }
);

export default Dialog;
