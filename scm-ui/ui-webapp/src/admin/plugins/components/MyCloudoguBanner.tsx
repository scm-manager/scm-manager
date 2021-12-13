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

import { Button } from "@scm-manager/ui-components";
import * as React from "react";
import { FC } from "react";
import styled from "styled-components";
import { Trans, useTranslation } from "react-i18next";

const MyCloudoguBannerWrapper = styled.div`
  border: 1px solid;
`;

type Props = {
  loginLink?: string;
};

const MyCloudoguBanner: FC<Props> = ({ loginLink }) => {
  const [t] = useTranslation("admin");
  return loginLink ? (
    <MyCloudoguBannerWrapper className="has-rounded-border is-flex is-flex-direction-column is-align-items-center p-5 mb-4 has-border-success">
      <Button className="mb-5 has-text-weight-normal has-border-info" reducedMobile={true} link={loginLink}>
        <Trans
          t={t}
          i18nKey="plugins.myCloudogu.login.button.label"
          components={[<span className="mx-1 has-text-info">myCloudogu</span>]}
        />
      </Button>
      <p className="is-align-self-flex-start is-size-7">
        <Trans
          t={t}
          i18nKey="plugins.myCloudogu.login.description"
          components={[<a href="https://my.cloudogu.com/">myCloudogu</a>]}
        />
      </p>
    </MyCloudoguBannerWrapper>
  ) : null;
};

export default MyCloudoguBanner;
