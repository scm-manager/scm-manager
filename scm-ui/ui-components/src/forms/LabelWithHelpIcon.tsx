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
import Help from "../Help";
import { RequiredMarker } from "@scm-manager/ui-core";

type Props = {
  label?: string;
  helpText?: string;
  id?: string;
  helpId?: string;
  required?: boolean;
};

/**
 * @deprecated
 */
class LabelWithHelpIcon extends React.Component<Props> {
  renderHelp() {
    const { helpText, helpId } = this.props;
    if (helpText) {
      return <Help message={helpText} id={helpId} />;
    }
  }

  render() {
    const { label, id, required } = this.props;

    if (label) {
      const help = this.renderHelp();
      return (
        <label className="label">
          <span id={id}>{label}</span>
          {required ? <RequiredMarker /> : null}
          {help}
        </label>
      );
    }

    return "";
  }
}

export default LabelWithHelpIcon;
