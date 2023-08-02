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

import * as RadixDialog from "@radix-ui/react-dialog";
import { DialogProps } from "@radix-ui/react-dialog";
import React, { ComponentProps, ReactComponentElement, ReactElement, ReactNode, useCallback } from "react";
import { Button } from "@scm-manager/ui-buttons";
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
} & Pick<DialogProps, "onOpenChange" | "open">;

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
