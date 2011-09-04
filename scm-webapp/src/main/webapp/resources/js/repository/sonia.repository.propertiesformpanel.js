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



Sonia.repository.PropertiesFormPanel = Ext.extend(Sonia.repository.FormPanel, {

  properties: [],

  initComponent: function(){
    var config = {
      listeners: {
        preUpdate: {
          fn: this.storeProperties,
          scope: this
        }
      }
    };

    Ext.apply(this, Ext.apply(this.initialConfig, config));
    Sonia.repository.PropertiesFormPanel.superclass.initComponent.apply(this, arguments);
    
    // load properties from fields
    this.loadProperties();
  },
  
  loadProperties: function(){
    this.items.each(function(field){
      if (field.property){
        this.properties.push({
          'name': field.name,
          'property': field.property
        });
        field.submitValue = false;
        
        for ( var j in this.item.properties ){
          var property = this.item.properties[j];
          if ( property.key == field.property ){
            field.setValue(property.value);
          }
        }
      }
    }, this);
  },
  
  storeProperties: function(item){
    // create properties if they are empty
    if (!item.properties){
      item.properties = [];
    }
    
    // copy fields to properties
    for ( var i in this.properties ){
      var property = this.properties[i];
      item.properties.push({
        key: property.property,
        value: item[property.name]
      });
      delete item[property.name];
    }
  }

});
