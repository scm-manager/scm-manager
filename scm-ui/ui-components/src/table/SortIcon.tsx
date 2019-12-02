import React, { FC } from "react";
import styled from "styled-components";
import Icon from "../Icon";

type Props = {
  name: string;
  isVisible: boolean;
};

const IconWithMarginLeft = styled(Icon)`
  visibility: ${(props: Props) => (props.isVisible ? "visible" : "hidden")};
  margin-left: 0.25em;
`;

const SortIcon: FC<Props> = (props: Props) => {
  return <IconWithMarginLeft name={props.name} isVisible={props.isVisible} />;
};

export default SortIcon;
