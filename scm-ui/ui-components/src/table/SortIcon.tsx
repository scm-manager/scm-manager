import React, { FC } from "react";
import styled from "styled-components";
import Icon from "../Icon";

type Props = {
  name: string;
  isHidden: boolean;
};

const IconWithMarginLeft = styled(Icon)`
  visibility: ${(props: Props) => (props.isHidden ? "hidden" : "visible")};
  margin-left: 0.25em;
`;

const SortIcon: FC<Props> = (props: Props) => {
  return <IconWithMarginLeft name={props.name} isHidden={props.isHidden} />;
};

export default SortIcon;
