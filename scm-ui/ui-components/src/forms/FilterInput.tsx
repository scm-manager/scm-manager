import React, { ChangeEvent, FormEvent } from "react";
import { WithTranslation, withTranslation } from "react-i18next";
import styled from "styled-components";

type Props = WithTranslation & {
  filter: (p: string) => void;
  value?: string;
};

type State = {
  value: string;
};

const FixedHeightInput = styled.input`
  height: 2.5rem;
`;

class FilterInput extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      value: this.props.value ? this.props.value : ""
    };
  }

  handleChange = (event: ChangeEvent<HTMLInputElement>) => {
    this.setState({
      value: event.target.value
    });
  };

  handleSubmit = (event: FormEvent) => {
    this.props.filter(this.state.value);
    event.preventDefault();
  };

  render() {
    const { t } = this.props;
    return (
      <form className="input-field" onSubmit={this.handleSubmit}>
        <div className="control has-icons-left">
          <FixedHeightInput
            className="input"
            type="search"
            placeholder={t("filterEntries")}
            value={this.state.value}
            onChange={this.handleChange}
          />
          <span className="icon is-small is-left">
            <i className="fas fa-search" />
          </span>
        </div>
      </form>
    );
  }
}

export default withTranslation("commons")(FilterInput);
