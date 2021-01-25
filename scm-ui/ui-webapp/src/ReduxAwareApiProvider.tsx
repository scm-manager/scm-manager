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
import { ApiProvider } from "@scm-manager/ui-api";
import { IndexResources, Me } from "@scm-manager/ui-types";
import { ApiProviderProps } from "@scm-manager/ui-api/src/ApiProvider";
import { fetchIndexResourcesSuccess } from "./modules/indexResource";
import { fetchMeSuccess } from "./modules/auth";
import {connect} from "react-redux";

const ReduxAwareApiProvider: FC<ApiProviderProps> = ({ children, ...listeners }) => (
  <ApiProvider {...listeners}>{children}</ApiProvider>
);

const mapDispatchToProps = (dispatch: any) => {
  return {
    onIndexFetched: (index: IndexResources) => {
      dispatch(fetchIndexResourcesSuccess(index));
    },
    onMeFetched: (me: Me) => {
      dispatch(fetchMeSuccess(me));
    }
  };
};

// @ts-ignore no clue how to type it
export default connect(undefined, mapDispatchToProps)(ReduxAwareApiProvider);
