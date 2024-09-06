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

import React, { MouseEvent, KeyboardEvent } from "react";
import Button, { ButtonProps } from "./Button";

type SubmitButtonProps = ButtonProps & {
  scrollToTop: boolean;
  testId?: string;
};

/**
 * @deprecated Use {@link ui-buttons/src/Button.tsx} instead
 */
class SubmitButton extends React.Component<SubmitButtonProps> {
  static defaultProps = {
    scrollToTop: true,
  };

  render() {
    const { action, scrollToTop, testId } = this.props;
    return (
      <Button
        type="submit"
        color="primary"
        {...this.props}
        action={(event: MouseEvent | KeyboardEvent) => {
          if (action) {
            action(event);
          }
          if (scrollToTop) {
            window.scrollTo(0, 0);
          }
        }}
        testId={testId ? testId : "submit-button"}
      />
    );
  }
}

export default SubmitButton;
