/*
 * MIT License
 *
 * Copyright (c) 2020-present Cloudogu GmbH and Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

import { storiesOf } from "@storybook/react";
import { MemoryRouter } from "react-router-dom";
import React, { FC, useRef, useState } from "react";
import Modal from "./Modal";
import Checkbox from "../forms/Checkbox";
import styled from "styled-components";
import ExternalLink from "../navigation/ExternalLink";
import { InputField, Radio, Textarea } from "../forms";
import { Button, ButtonGroup } from "../buttons";
import Notification from "../Notification";
import { ActiveModalCountContext, Autocomplete } from "../index";
import { SelectValue } from "@scm-manager/ui-types";

const TopAndBottomMargin = styled.div`
  margin: 0.75rem 0; // only for aesthetic reasons
`;

const RadioList = styled.div`
  display: flex;
  flex-direction: column;
  > label:not(:last-child) {
    margin-bottom: 0.6em;
  }
`;

const text = `Mind-paralyzing change needed improbability vortex machine sorts sought same theory upending job just allows
    hostess’s really oblong Infinite Improbability thing into the starship against which behavior accordance.with
    Kakrafoon humanoid undergarment ship powered by GPP-guided bowl of petunias nothing was frequently away incredibly
    ordinary mob.`;

const doNothing = () => {
  // Do nothing
};
const withFormElementsBody = (
  <>
    <RadioList>
      <Radio label="One" checked={true} helpText="The first one" />
      <Radio label="Two" checked={false} helpText="The second one" />
    </RadioList>
    <hr />
    <p>{text}</p>
    <hr />
    <Textarea label="Text" onChange={doNothing} />
  </>
);
const withFormElementsFooter = (
  <ButtonGroup>
    <Button label="One" />
    <Button label="Two" />
  </ButtonGroup>
);

const loadSuggestions: (p: string) => Promise<SelectValue[]> = () =>
  new Promise((resolve) => {
    setTimeout(() => {
      resolve([
        { value: { id: "trillian", displayName: "Tricia McMillan" }, label: "Tricia McMillan" },
        { value: { id: "zaphod", displayName: "Zaphod Beeblebrox" }, label: "Zaphod Beeblebrox" },
        { value: { id: "ford", displayName: "Ford Prefect" }, label: "Ford Prefect" },
        { value: { id: "dent", displayName: "Arthur Dent" }, label: "Arthur Dent" },
        { value: { id: "marvin", displayName: "Marvin" }, label: "Marvin the Paranoid Android " },
      ]);
    });
  });

storiesOf("Modal/Modal", module)
  .addDecorator((story) => <MemoryRouter initialEntries={["/"]}>{story()}</MemoryRouter>)
  .addDecorator((story) => (
    <ActiveModalCountContext.Provider value={{ value: 0, increment: doNothing, decrement: doNothing }}>
      {story()}
    </ActiveModalCountContext.Provider>
  ))
  .add("Default", () => (
    <NonCloseableModal>
      <p>{text}</p>
    </NonCloseableModal>
  ))
  .add("Closeable", () => (
    <CloseableModal>
      <p>{text}</p>
    </CloseableModal>
  ))
  .add("Nested", () => (
    <NestedModal>
      <p>{text}</p>
    </NestedModal>
  ))
  .add("With form elements", () => (
    <Modal
      body={withFormElementsBody}
      closeFunction={doNothing}
      active={true}
      title={"Hitchhiker Modal"}
      footer={withFormElementsFooter}
    />
  ))
  .add("With initial input field focus", () => {
    const ref = useRef<HTMLInputElement | null>(null);
    return (
      <Modal
        closeFunction={doNothing}
        active={true}
        title={"Hitchhiker Modal"}
        footer={withFormElementsFooter}
        initialFocusRef={ref}
      >
        <InputField ref={ref} />
      </Modal>
    );
  })
  .add("With initial button focus", () => <RefModal />)
  .add("With long tooltips", () => {
    return (
      <NonCloseableModal>
        <Notification type="info">
          This story exists because we had a problem, that long tooltips causes a horizontal scrollbar on the modal.
        </Notification>
        <hr />
        <p>The following elements will have a very long help text, which has triggered the scrollbar in the past.</p>
        <hr />
        <TopAndBottomMargin>
          <Checkbox label="Checkbox" checked={true} helpText={text} />
        </TopAndBottomMargin>
        <hr />
        <TopAndBottomMargin>
          <Radio label="Radio button" checked={false} helpText={text} />
        </TopAndBottomMargin>
        <hr />
        <TopAndBottomMargin>
          <InputField onChange={doNothing} label="Input" helpText={text} />
        </TopAndBottomMargin>
        <hr />
        <TopAndBottomMargin>
          <Textarea onChange={doNothing} label="Textarea" helpText={text} />
        </TopAndBottomMargin>
        <hr />
        <p>If this modal has no horizontal scrollbar the issue is fixed</p>
      </NonCloseableModal>
    );
  })
  .add("Long content", () => (
    <NonCloseableModal>
      <h1 className="title">Marvin</h1>
      <h2 className="subtitle">The Paranoid Android</h2>
      <hr />
      <Notification type="info">
        The following content comes from the awesome{" "}
        <ExternalLink to="https://hitchhikers.fandom.com/wiki/Main_Page">Hitchhikers Wiki</ExternalLink>
      </Notification>
      <hr />
      <div className="has-text-centered">
        <img
          alt="Marvin"
          src="https://vignette.wikia.nocookie.net/hitchhikers/images/a/a4/Marvin.jpg/revision/latest/scale-to-width-down/150?cb=20100530114055"
        />
      </div>
      <hr />
      <p className="content">
        Marvin, more fully known as Marvin the Paranoid Android, is an incredibly brilliant but overwhelmingly depressed
        robot manufactured by the Sirius Cybernetics Corporation and unwilling servant to the crew of the Heart of Gold.
      </p>
      <hr />
      <div className="content">
        <h4>Physical Appearance</h4>
        <p>
          In the novels, Marvin is described thusly: "...though it was beautifully constructed and polished it looked
          somehow as if the various parts of its more or less humanoid body didn't quite fit properly. In fact, they fit
          perfectly well, but something in its bearing suggested that they might have fitted better."
        </p>
        <p>
          On the radio show, there's no physical description of Marvin, though his voice is digitally altered to sound
          more robotic, and any scene that focuses on him is accompanied by sounds of mechanical clanking and hissing.
        </p>
        <p>
          In the TV series, Marvin is built in the style of a 1950's robot similar to Robbie the Robot from Forbidden
          Planet or Twiki from Buck Rogers. His body is blocky and angular, with a pair of clamp-claw hands, shuffling
          feet and a squarish head with a dour face.
        </p>
        <p>
          In the movie, Marvin is a short, stout robot built of smooth, white plastic. His arms are much longer than his
          legs, and his head is a massive sphere with only a pair of triangle eyes for a face. His large head and
          simian-like proportions give Marvin a perpetual slouch, adding to his melancholy personality. At the start of
          the film his eyes glow, but at the end he is shot but unharmed, leaving a hole in his head and dimming his
          eyes. This is probably the most depressing and unacceptable manifestation of Marvin ever conceived, and thus
          paradoxically the most accurate.
        </p>
      </div>
      <hr />
      <div className="content">
        <h4>Personality</h4>
        <p>
          Marvin the robot has a prototype version of the Genuine People Personality (GPP) software from SCC, allowing
          him sentience and the ability to feel emotions and develop a personality. He's also incredibly smart, having a
          "brain the size of a planet" capable of computing extremely complex mathematics, as well as solving difficult
          problems and operating high-tech devices.
        </p>
        <p>
          However, despite being so smart, Marvin is typically made to perform menial tasks and labour such as escorting
          people, opening doors, picking up pieces of paper, and other tasks well beneath his skills. Even extremely
          hard tasks, such as computing for the vast Krikkit robot army, are trivial for Marvin. All this leaves him
          extremely bored, frustrated, and overwhelmingly depressed. Because of this, all modern GPP-capable machines,
          such as Eddie the computer and the Heart of Gold's automatic doors, are programmed to be extremely cheerful
          and happy, much to Marvin's disgust.
        </p>
        <p>
          Marvin hates everyone and everything he comes into contact with, having no respect for anybody and will
          criticise and insult others at any opportunity, or otherwise rant and complain for hours on end about his own
          problems, such as the terrible pain he suffers in all the diodes down his left side. His contempt for everyone
          is often justified, as almost every person he comes across, even those who consider him a friend, (such as
          Arthur and Trillian, who treat him more kindly than Ford and Zaphod) treat Marvin as an expendable servant,
          even sending him to his death more than once (such as when Zaphod ordered Marvin to fight the gigantic,
          heavy-duty Frogstar Scout Robot Class D so he could escape). Being a robot, he still does what he's told (he
          won't enjoy it, nor will he let you forget it, but he'll do it anyway), though he'd much rather sulk in a
          corner by himself.
        </p>
        <p>
          Several times in the series Marvin ends up alone and isolated for extremely long periods of time, sometimes
          spanning millions of years, either by sheer bad luck (such as the explosion that propelled everyone but Marvin
          to Milliways in the far-off future) or because his unpleasantly depressing personality drives them away or, in
          more than one case, makes them commit suicide. In his spare time (which he has a lot of), Marvin will attempt
          to occupy himself by composing songs and writing poetry. Of course, none of them are particularly cheerful, or
          even that good.
        </p>
      </div>
    </NonCloseableModal>
  ))
  .add("With overflow", () => {
    return (
      <NonCloseableModal overflowVisible={true}>
        <h1 className="title">Please Select</h1>
        <Autocomplete
          valueSelected={() => {
            // nothing to do
          }}
          loadSuggestions={loadSuggestions}
        />
      </NonCloseableModal>
    );
  })
  .add("With overflow and footer", () => {
    return (
      <NonCloseableModal overflowVisible={true} footer={withFormElementsFooter}>
        <h1 className="title">Please Select</h1>
        <Autocomplete
          valueSelected={() => {
            // nothing to do
          }}
          loadSuggestions={loadSuggestions}
        />
      </NonCloseableModal>
    );
  });

type NonCloseableModalProps = { overflowVisible?: boolean; footer?: any };

const NonCloseableModal: FC<NonCloseableModalProps> = ({ overflowVisible, footer, children }) => {
  return (
    <Modal
      body={children}
      closeFunction={doNothing}
      active={true}
      title={"Hitchhiker Modal"}
      overflowVisible={overflowVisible}
      footer={footer}
    />
  );
};

const CloseableModal: FC = ({ children }) => {
  const [show, setShow] = useState(true);

  const toggleModal = () => {
    setShow(!show);
  };

  return <Modal body={children} closeFunction={toggleModal} active={show} title={"Hitchhiker Modal"} />;
};

const NestedModal: FC = ({ children }) => {
  const [showOuter, setShowOuter] = useState(true);
  const [showInner, setShowInner] = useState(false);
  const outerBody = (
    <>
      {showInner && (
        <Modal
          body={children}
          closeFunction={() => setShowInner(!showInner)}
          active={showInner}
          title="Inner Hitchhiker Modal"
        />
      )}

      <Button title="Open inner modal" className="button" action={() => setShowInner(true)}>
        Open inner modal
      </Button>
    </>
  );

  return (
    <>
      {showOuter && (
        <Modal
          body={outerBody}
          closeFunction={() => setShowOuter(!showOuter)}
          active={showOuter}
          title="Outer Hitchhiker Modal"
          size="M"
        />
      )}
    </>
  );
};

const RefModal = () => {
  const ref = useRef<HTMLButtonElement>(null);
  return (
    <Modal
      closeFunction={doNothing}
      active={true}
      title={"Hitchhiker Modal"}
      footer={withFormElementsFooter}
      initialFocusRef={ref}
    >
      <button ref={ref}>Hello</button>
    </Modal>
  );
};
