import React from "react";
import classNames from "classnames";
import styled from "styled-components";
import Tooltip from "./Tooltip";
import HelpIcon from "./HelpIcon";

type Props = {
  message: string;
  className?: string;
};

const HelpTooltip = styled(Tooltip)`
  position: absolute;
  padding-left: 3px;
`;

export default class Help extends React.Component<Props> {
  render() {
    const { message, className } = this.props;
    return (
      <HelpTooltip
        className={classNames("is-inline-block", className)}
        message={message}
      >
        <HelpIcon />
      </HelpTooltip>
    );
  }
}
