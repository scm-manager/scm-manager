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

import React from "react";
import { Navigate, Outlet, useLocation } from "react-router-dom";

type Props = {
  authenticated?: boolean;
};

const ProtectedRoute: React.FC<Props> = ({ authenticated }) => {
  const location = useLocation();

  if (authenticated === undefined) {
    // Optional: Render a loading indicator while checking auth status
    return null; // or <Loading />
  }

  return authenticated ? <Outlet /> : <Navigate to="/login" replace state={{ from: location }} />;
};

export default ProtectedRoute;
