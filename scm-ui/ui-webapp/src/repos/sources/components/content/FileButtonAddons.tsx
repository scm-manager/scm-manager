import React from 'react';
import { translate } from 'react-i18next';
import { ButtonAddons, Button } from '@scm-manager/ui-components';

type Props = {
  className?: string;
  t: (p: string) => string;
  historyIsSelected: boolean;
  showHistory: (p: boolean) => void;
};

class FileButtonAddons extends React.Component<Props> {
  showHistory = () => {
    this.props.showHistory(true);
  };

  showSources = () => {
    this.props.showHistory(false);
  };

  color = (selected: boolean) => {
    return selected ? 'link is-selected' : null;
  };

  render() {
    const { className, t, historyIsSelected } = this.props;

    return (
      <ButtonAddons className={className}>
        <div title={t('sources.content.sourcesButton')}>
          <Button
            action={this.showSources}
            className="reduced"
            color={this.color(!historyIsSelected)}
          >
            <span className="icon">
              <i className="fas fa-code" />
            </span>
          </Button>
        </div>
        <div title={t('sources.content.historyButton')}>
          <Button
            action={this.showHistory}
            className="reduced"
            color={this.color(historyIsSelected)}
          >
            <span className="icon">
              <i className="fas fa-history" />
            </span>
          </Button>
        </div>
      </ButtonAddons>
    );
  }
}

export default translate('repos')(FileButtonAddons);
