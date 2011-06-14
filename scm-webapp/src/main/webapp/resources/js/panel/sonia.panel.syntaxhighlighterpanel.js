/* *
 * Copyright (c) 2010, Sebastian Sdorra
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 * 
 * 1. Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 * 3. Neither the name of SCM-Manager; nor the names of its
 *    contributors may be used to endorse or promote products derived from this
 *    software without specific prior written permission.
 * 
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED.  IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY
 * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 * 
 * http://bitbucket.org/sdorra/scm-manager
 * 
 */


Sonia.panel.SyntaxHighlighterPanel = Ext.extend(Ext.Panel, {
  
  syntaxes: [{
    name: 'ActionScript3',
    aliases: ['as3', 'actionscript3'],
    fileName: 'shBrushAS3.js'
  },{
    name: 'Bash/shell',
    aliases: ['bash', 'shell'],
    fileName: 'shBrushBash.js'
  },{
    name: 'ColdFusion',
    aliases: ['cf', 'coldfusion'],
    fileName: 'shBrushColdFusion.js'
  },{
    name: 'C#',
    aliases: ['c-sharp', 'csharp'],
    fileName: 'shBrushCSharp.js'
  },{
    name: 'C++',
    aliases: ['cpp', 'c'],
    fileName: 'shBrushCpp.js'
  },{
    name: 'CSS',
    aliases: ['css'],
    fileName: 'shBrushCss.js'
  },{
    name: 'Delphi',
    aliases: ['delphi', 'pas', 'pascal'],
    fileName: 'shBrushDelphi.js'
  },{
    name: 'Diff',
    aliases: ['diff', 'patch'],
    fileName: 'shBrushDiff.js'
  },{
    name: 'Erlang',
    aliases: ['erl', 'erlang'],
    fileName: 'shBrushErlang.js'
  },{
    name: 'Groovy',
    aliases: ['groovy'],
    fileName: 'shBrushGroovy.js'
  },{
    name: 'JavaScript',
    aliases: ['js', 'jscript', 'javascript' ],
    fileName: 'shBrushJScript.js'
  },{
    name: 'Java',
    aliases: ['java'],
    fileName: 'shBrushJava.js'
  },{
    name: 'JavaFX',
    aliases: ['jfx', 'javafx'],
    fileName: 'shBrushJavaFX.js'
  },{
    name: 'Perl',
    aliases: ['perl', 'pl'],
    fileName: 'shBrushPerl.js'
  },{
    name: 'PHP',
    aliases: ['php'],
    fileName: 'shBrushPhp.js'
  },{
    name: 'Plain Text',
    aliases: ['plain', 'text', 'txt'],
    fileName: 'shBrushPlain.js'
  },{
    name: 'PowerShell',
    aliases: ['ps', 'powershell'],
    fileName: 'shBrushPowerShell.js'
  },{
    name: 'Python',
    aliases: ['py', 'python'],
    fileName: 'shBrushPython.js'
  },{
    name: 'Ruby',
    aliases: ['rails', 'ror', 'ruby', 'rb'],
    fileName: 'shBrushRuby.js'
  },{
    name: 'Scala',
    aliases: ['scala'],
    fileName: 'shBrushScala.js'
  },{
    name: 'SQL',
    aliases: ['sql'],
    fileName: 'shBrushScala.js'
  },{
    name: 'Visual Basic',
    aliases: ['vb', 'vbnet'],
    fileName: 'shBrushVb.js'
  },{
    name: 'Python',
    aliases: ['py', 'python'],
    fileName: 'shBrushPython.js'
  },{
    name: 'XML',
    aliases: ['xml', 'xhtml', 'xslt', 'html', 'xhtml'],
    fileName: 'shBrushXml.js'
  }],
  
  syntax: 'plain',
  brushUrl: 'shBrushPlain.js',
  theme: 'Default',
  shPath: 'resources/syntaxhighlighter',
  contentUrl: null,
  
  contentLoaded: false,
  scriptsLoaded: false,
  
  initComponent: function(){
    
    if (debug){
      console.debug( 'try to find brush for ' + this.syntax );
    }
    
    if ( this.syntax != 'plain' ){
      var s = null;
      var found = false;
      for (var i=0; i<this.syntaxes.length; i++){
        s = this.syntaxes[i];
        for ( var j=0;j<s.aliases.length; j++ ){
          if ( this.syntax == s.aliases[j] ){
            found = true;
            this.syntax = s.name;
            this.brushUrl = s.fileName;
            if (debug){
              console.debug( "found brush " + this.syntax + " at " + this.brushUrl );
            }
            break;
          }
        }
        if ( found ){
          break;
        }
      }
      
      if (! found){
        if ( debug ){
          console.debug( 'could not find syntax for ' + this.syntax );
        }
        this.syntax = 'plain';
      }
    }
    
    // load core stylesheet
    main.loadStylesheet( this.shPath + '/styles/shCore.css');
    // load theme stylesheet
    if ( debug ){
      console.debug( 'load theme ' + this.theme );
    }
    main.loadStylesheet(this.shPath + '/styles/shCore' + this.theme + '.css');
    
    var config = {
      autoScroll: true,
      listeners: {
        afterrender: {
          fn: this.loadContent,
          scope: this
        }
      }
    };
    
    this.loadContent();
    
    Ext.apply(this, Ext.apply(this.initialConfig, config));
    
    if (debug){
      console.debug(config);
    }
    
    Sonia.panel.SyntaxHighlighterPanel.superclass.initComponent.apply(this, arguments);
  },
  
  loadContent: function(){
    main.loadScript(this.shPath + '/scripts/shCore.js', this.loadBrush, this);
    Ext.Ajax.request({
      url: this.contentUrl,
      scope: this,
      success: function(response){
        console.debug( this.syntax );
        this.update('<pre class="brush: ' + this.syntax + '">' + Ext.util.Format.htmlEncode(response.responseText) + '</pre>');
        this.contentLoaded = true;
        this.highlight();
      },
      failure: function(){
        // TODO
      }
    });
  },
  
  loadBrush: function(){
    main.loadScript(this.shPath + '/scripts/' + this.brushUrl, function(){
      this.scriptsLoaded = true;
      this.highlight();
    }, this);
  },
  
  highlight: function(){
    if (debug){
      console.debug('loaded, script: ' + this.scriptsLoaded + ", content: " + this.contentLoaded );
    }
    if ( this.scriptsLoaded && this.contentLoaded ){
      if (debug){
        console.debug('call SyntaxHighlighter.highlight()');
      }
      SyntaxHighlighter.highlight({}, this.body.el);
    }
  }
  
});

Ext.reg('syntaxHighlighterPanel', Sonia.panel.SyntaxHighlighterPanel);