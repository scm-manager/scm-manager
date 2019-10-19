import React from 'react';
import { History } from 'history';
import { withRouter } from 'react-router-dom';
import classNames from 'classnames';
import { Button, urls } from './index';
import { FilterInput } from './forms';

type Props = {
  showCreateButton: boolean;
  link: string;
  label?: string;

  // context props
  history: History;
  location: any;
};

class OverviewPageActions extends React.Component<Props> {
  render() {
    const { history, location, link } = this.props;
    return (
      <>
        <FilterInput
          value={urls.getQueryStringFromLocation(location)}
          filter={filter => {
            history.push(`/${link}/?q=${filter}`);
          }}
        />
        {this.renderCreateButton()}
      </>
    );
  }

  renderCreateButton() {
    const { showCreateButton, link, label } = this.props;
    if (showCreateButton) {
      return (
        <div className={classNames('input-button', 'control')}>
          <Button label={label} link={`/${link}/create`} color="primary" />
        </div>
      );
    }
    return null;
  }
}

export default withRouter(OverviewPageActions);
