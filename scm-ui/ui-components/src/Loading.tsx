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
import { WithTranslation, withTranslation } from "react-i18next";
import classNames from "classnames";
import styled from "styled-components";
import Image from "./Image";

type Props = WithTranslation & {
  message?: string;
};

const Wrapper = styled.div`
  min-height: 256px;
`;

const FixedSizedImage = styled(Image)`
  width: 128px;
  height: 128px;
`;

/**
 * @deprecated Please import the identical module from "@scm-manager/ui-core"
 */

class Loading extends React.Component<Props> {
  render() {
    const { message, t } = this.props;
    return (
      <Wrapper
        className={classNames(
          "is-flex",
          "is-flex-direction-column",
          "is-justify-content-center",
          "is-align-items-center"
        )}
      >
        <FixedSizedImage className="mb-3" src="/images/loading.svg" alt={t("loading.alt")} />
        <p className="has-text-centered">{message}</p>
      </Wrapper>
    );
  }
}

export default withTranslation("commons")(Loading);
