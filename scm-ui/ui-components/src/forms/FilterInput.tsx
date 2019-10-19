import React from 'react';
import { translate } from 'react-i18next';
import styled from 'styled-components';

type Props = {
  filter: (p: string) => void;
  value?: string;

  // context props
  t: (p: string) => string;
};

type State = {
  value: string;
};

const FixedHeightInput = styled.input`
  height: 2.5rem;
`;

class FilterInput extends React.Component<Props, State> {
  constructor(props) {
    super(props);
    this.state = {
      value: this.props.value ? this.props.value : '',
    };
  }

  handleChange = event => {
    this.setState({
      value: event.target.value,
    });
  };

  handleSubmit = event => {
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
            placeholder={t('filterEntries')}
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

export default translate('commons')(FilterInput);
