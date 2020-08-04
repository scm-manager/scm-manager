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
/// <reference types="cypress" />

context('Viewport', () => {
  beforeEach(() => {
    cy.visit('https://example.cypress.io/commands/viewport')
  })

  it('cy.viewport() - set the viewport size and dimension', () => {
    // https://on.cypress.io/viewport

    cy.get('#navbar').should('be.visible')
    cy.viewport(320, 480)

    // the navbar should have collapse since our screen is smaller
    cy.get('#navbar').should('not.be.visible')
    cy.get('.navbar-toggle').should('be.visible').click()
    cy.get('.nav').find('a').should('be.visible')

    // lets see what our app looks like on a super large screen
    cy.viewport(2999, 2999)

    // cy.viewport() accepts a set of preset sizes
    // to easily set the screen to a device's width and height

    // We added a cy.wait() between each viewport change so you can see
    // the change otherwise it is a little too fast to see :)

    cy.viewport('macbook-15')
    cy.wait(200)
    cy.viewport('macbook-13')
    cy.wait(200)
    cy.viewport('macbook-11')
    cy.wait(200)
    cy.viewport('ipad-2')
    cy.wait(200)
    cy.viewport('ipad-mini')
    cy.wait(200)
    cy.viewport('iphone-6+')
    cy.wait(200)
    cy.viewport('iphone-6')
    cy.wait(200)
    cy.viewport('iphone-5')
    cy.wait(200)
    cy.viewport('iphone-4')
    cy.wait(200)
    cy.viewport('iphone-3')
    cy.wait(200)

    // cy.viewport() accepts an orientation for all presets
    // the default orientation is 'portrait'
    cy.viewport('ipad-2', 'portrait')
    cy.wait(200)
    cy.viewport('iphone-4', 'landscape')
    cy.wait(200)

    // The viewport will be reset back to the default dimensions
    // in between tests (the  default can be set in cypress.json)
  })
})
