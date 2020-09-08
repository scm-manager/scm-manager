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
import React from "react";
import { withRouter, RouteComponentProps } from "react-router-dom";
import classNames from "classnames";
import { Button, DropDown, urls } from "./index";
import { FilterInput } from "./forms";

type Props = RouteComponentProps & {
  showCreateButton: boolean;
  currentGroup: string;
  groups: string[];
  link: string;
  groupSelected: (namespace: string) => void;
  label?: string;
  testId?: string;
};

class OverviewPageActions extends React.Component<Props> {
  render() {
    const { history, currentGroup, groups, location, link, testId, groupSelected } = this.props;
    const groupSelector = groups && (
      <div className={"column is-flex"}>
        <DropDown
          className={"is-fullwidth"}
          options={groups}
          preselectedOption={currentGroup}
          optionSelected={groupSelected}
        />
      </div>
    );

    return (
      <div className={"columns is-tablet"}>
        {groupSelector}
        <div className={"column"}>
          <FilterInput
            value={urls.getQueryStringFromLocation(location)}
            filter={filter => {
              history.push(`/${link}/?q=${filter}`);
            }}
            testId={testId + "-filter"}
          />
        </div>
        {this.renderCreateButton()}
      </div>
    );
  }

  renderCreateButton() {
    const { showCreateButton, link, label } = this.props;
    if (showCreateButton) {
      return (
        <div className={classNames("input-button", "control", "column")}>
          <Button label={label} link={`/${link}/create`} color="primary" />
        </div>
      );
    }
    return null;
  }
}

export default withRouter(OverviewPageActions);
