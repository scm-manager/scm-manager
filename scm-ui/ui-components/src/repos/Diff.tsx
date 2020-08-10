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
import DiffFile from "./DiffFile";
import { DiffObjectProps, File } from "./DiffTypes";
import Notification from "../Notification";
import { WithTranslation, withTranslation } from "react-i18next";
import {Changeset} from "@scm-manager/ui-types";

type Props = WithTranslation &
  DiffObjectProps & {
    diff: File[];
    changeset: Changeset;
    baseUrl?: string;
  };

class Diff extends React.Component<Props> {
  static defaultProps: Partial<Props> = {
    sideBySide: false
  };

  render() {
    const { diff, t, ...fileProps } = this.props;
    return (
      <>
        {diff.length === 0 ? (
          <Notification type="info">{t("diff.noDiffFound")}</Notification>
        ) : (
          diff.map((file, index) => <DiffFile key={index} file={file} {...fileProps} {...this.props} />)
        )}
      </>
    );
  }
}

export default withTranslation("repos")(Diff);
