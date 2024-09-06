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

import { defineLazy, defineStatic } from "./define";

import * as React from "react";
import * as ReactDOM from "react-dom";
import * as ReactRouterDom from "react-router-dom";
import * as ReactQuery from "react-query";
import * as StyledComponents from "styled-components";
import * as ReactHookForm from "react-hook-form";
import * as ReactI18Next from "react-i18next";
import * as ClassNames from "classnames";
import * as QueryString from "query-string";
import * as UIExtensions from "@scm-manager/ui-extensions";
import * as UIComponents from "@scm-manager/ui-components";
import * as UIButtons from "@scm-manager/ui-buttons";
import * as UIForms from "@scm-manager/ui-forms";
import * as UIOverlays from "@scm-manager/ui-overlays";
import * as UILayout from "@scm-manager/ui-layout";
import * as UIApi from "@scm-manager/ui-api";
import * as UICore from "@scm-manager/ui-core";

// This module has side effects and is required to be imported unconditionally into the application at all times.

defineStatic("react", React);
defineStatic("react-dom", ReactDOM);
defineStatic("react-router-dom", ReactRouterDom);
defineStatic("styled-components", StyledComponents);
defineStatic("react-i18next", ReactI18Next);
defineStatic("react-hook-form", ReactHookForm);
defineStatic("react-query", ReactQuery);
defineStatic("classnames", ClassNames);
defineStatic("query-string", QueryString);
defineStatic("@scm-manager/ui-extensions", UIExtensions);
defineStatic("@scm-manager/ui-components", UIComponents);
defineStatic("@scm-manager/ui-buttons", UIButtons);
defineStatic("@scm-manager/ui-forms", UIForms);
defineStatic("@scm-manager/ui-overlays", UIOverlays);
defineStatic("@scm-manager/ui-layout", UILayout);
defineStatic("@scm-manager/ui-api", UIApi);
defineStatic("@scm-manager/ui-core", UICore);

// redux is deprecated in favor of ui-api
defineLazy("redux", () => import("@scm-manager/ui-legacy").then((legacy) => legacy.Redux));
defineLazy("react-redux", () => import("@scm-manager/ui-legacy").then((legacy) => legacy.ReactRedux));
