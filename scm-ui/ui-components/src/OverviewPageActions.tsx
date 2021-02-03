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
import React, { FC } from "react";
import { useHistory, useLocation } from "react-router-dom";
import classNames from "classnames";
import { Button, DropDown, urls } from "./index";
import { FilterInput } from "./forms";

type Props = {
  showCreateButton: boolean;
  link: string;
  label?: string;
  testId?: string;
  searchPlaceholder?: string;
};

const OverviewPageActions: FC<Props> = ({
  showCreateButton,
  link,
  label,
  testId,
  searchPlaceholder
}) => {
  const history = useHistory();
  const location = useLocation();

  const renderCreateButton = () => {
    if (showCreateButton) {
      return (
        <div className={classNames("input-button", "control", "column")}>
          <Button label={label} link={`/${link}/create`} color="primary" />
        </div>
      );
    }
    return null;
  };

  return (
    <div className={"columns is-tablet"}>
      <div className={"column"}>
        <FilterInput
          placeholder={searchPlaceholder}
          value={urls.getQueryStringFromLocation(location)}
          filter={filter => {
            history.push(`/${link}/?q=${filter}`);
          }}
          testId={testId + "-filter"}
        />
      </div>
      {renderCreateButton()}
    </div>
  );
};

export default OverviewPageActions;
