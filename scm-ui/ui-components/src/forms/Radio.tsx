import React, { ChangeEvent } from "react";
import { Help } from "../index";
import styled from "styled-components";


const StyledRadio = styled.label`
  margin-right: 0.5em;
`;

type Props = {
  label?: string;
  name?: string;
  value?: string;
  checked: boolean;
  onChange?: (value: boolean, name?: string) => void;
  disabled?: boolean;
  helpText?: string;
};

class Radio extends React.Component<Props> {
  renderHelp = () => {
    const helpText = this.props.helpText;
    if (helpText) {
      return <Help message={helpText} />;
    }
  };

  onValueChange = (event: ChangeEvent<HTMLInputElement>) => {
    if (this.props.onChange) {
      this.props.onChange(event.target.checked, this.props.name);
    }
  };

  render() {
    return (
      <>
        {/*
        we have to ignore the next line, 
        because jsx label does not the custom disabled attribute
        but bulma does.
        // @ts-ignore */}
        <StyledRadio className="radio" disabled={this.props.disabled}>
          <input
            type="radio"
            name={this.props.name}
            value={this.props.value}
            checked={this.props.checked}
            onChange={this.onValueChange}
            disabled={this.props.disabled}
          />{" "}
          {this.props.label}
          {this.renderHelp()}
        </StyledRadio>
      </>
    );
  }
}

export default Radio;
