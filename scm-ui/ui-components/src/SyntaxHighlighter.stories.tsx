import React from 'react';
import { storiesOf } from '@storybook/react';
import styled from 'styled-components';
import SyntaxHighlighter from './SyntaxHighlighter';

import JavaHttpServer from './__resources__/HttpServer.java';
import GoHttpServer from './__resources__/HttpServer.go';
//import JsHttpServer from './__resources__/HttpServer.js';
import PyHttpServer from './__resources__/HttpServer.py';

const Spacing = styled.div`
  padding: 1em;
`;

storiesOf('SyntaxHighlighter', module)
  .add('Java', () => (
    <Spacing>
      <SyntaxHighlighter language="java" value={JavaHttpServer} />
    </Spacing>
  ))
  .add('Go', () => (
    <Spacing>
      <SyntaxHighlighter language="go" value={GoHttpServer} />
    </Spacing>
  ))
  /*.add('Javascript', () => (
    <Spacing>
      <SyntaxHighlighter language="javascript" value={JsHttpServer} />
    </Spacing>
  ))*/
  .add('Python', () => (
    <Spacing>
      <SyntaxHighlighter language="python" value={PyHttpServer} />
    </Spacing>
  ));
