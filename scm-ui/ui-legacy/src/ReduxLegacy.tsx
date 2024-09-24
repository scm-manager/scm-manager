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

import React, { FC, useEffect } from "react";
import { BaseContext, useLegacyContext } from "@scm-manager/ui-api";
import { connect, Dispatch } from "react-redux";
import { ActionTypes, fetchIndexResourcesSuccess, fetchMeSuccess } from "./LegacyReduxProvider";
import { IndexResources, Me } from "@scm-manager/ui-types";

const ReduxLegacy: FC<BaseContext> = ({ children, onIndexFetched, onMeFetched }) => {
  const context = useLegacyContext();
  useEffect(() => {
    context.onIndexFetched = onIndexFetched;
    context.onMeFetched = onMeFetched;
    context.initialize();
  }, [context, onIndexFetched, onMeFetched]);
  return <>{children}</>;
};

const mapDispatchToProps = (dispatch: Dispatch<ActionTypes>) => {
  return {
    onIndexFetched: (index: IndexResources) => dispatch(fetchIndexResourcesSuccess(index)),
    onMeFetched: (me: Me) => dispatch(fetchMeSuccess(me))
  };
};

// eslint-disable-next-line @typescript-eslint/ban-types
const connector = connect<{}, BaseContext>(undefined, mapDispatchToProps);

export default connector(ReduxLegacy);
