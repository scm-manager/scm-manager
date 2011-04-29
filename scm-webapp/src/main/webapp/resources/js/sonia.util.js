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

 Ext.apply(Ext.util.Format, {

  formatTimestamp: function(value){
    var date = null;
    var result = '';
    if ( value != null && (value > 0 || value.length > 0)){
      var df = state.clientConfig.dateFormat;
      if ( df == null || df.length == 0 || ! Ext.isDefined(value) ){
        df = "Y-m-d H:i:s";
      }
      date = new Date(value);
      result = Ext.util.Format.date(date, df);
    }

    if (date != null && result.indexOf("{0}") >= 0){
      result = String.format( result, Ext.util.Format.timeAgo(date) );
    }

    return result;
  },

  // TODO i18n
  timeAgoJustNow: 'just now',
  timeAgoOneMinuteAgo: '1 minute ago',
  timeAgoOneMinuteFromNow: '1 minute from now',
  timeAgoMinutes: 'minutes',
  timeAgoOneHourAgo: '1 hour ago',
  timeAgoOneHourFromNow: '1 hour from now',
  timeAgoHours: 'hours',
  timeAgoYesterday: 'yesterday',
  timeAgoTomorrow: 'tomorrow',
  timeAgoDays: 'days',
  timeAgoLastWeek: 'last week',
  timeAgoNextWeek: 'next week',
  timeAgoWeeks: 'weeks',
  timeAgoLastMonth: 'last month',
  timeAgoNextMonth: 'next month',
  timeAgoMonths: 'months',
  timeAgoLastYear: 'last year',
  timeAgoNextYear: 'next year',
  timeAgoYears: 'years',
  timeAgoLastCentury: 'last century',
  timeAgoNextCentury: 'next century',
  timeAgoCenturies: 'centuries',


  timeAgo : function(value){

    var time_formats = [
      [60, this.timeAgoJustNow, 1], // 60
      [120, this.timeAgoOneMinuteAgo, this.timeAgoOneMinuteFromNow ], // 60*2
      [3600, this.timeAgoMinutes, 60], // 60*60, 60
      [7200, this.timeAgoOneHourAgo, this.timeAgoOneHourFromNow ], // 60*60*2
      [86400, this.timeAgoHours, 3600], // 60*60*24, 60*60
      [172800, this.timeAgoYesterday, this.timeAgoTomorrow ], // 60*60*24*2
      [604800, this.timeAgoDays, 86400], // 60*60*24*7, 60*60*24
      [1209600, this.timeAgoLastWeek, this.timeAgoNextWeek], // 60*60*24*7*4*2
      [2419200, this.timeAgoWeeks, 604800], // 60*60*24*7*4, 60*60*24*7
      [4838400, this.timeAgoLastMonth, this.timeAgoNextMonth], // 60*60*24*7*4*2
      [29030400, this.timeAgoMonths, 2419200], // 60*60*24*7*4*12, 60*60*24*7*4
      [58060800, this.timeAgoLastYear, this.timeAgoNextYear], // 60*60*24*7*4*12*2
      [2903040000, this.timeAgoYears, 29030400], // 60*60*24*7*4*12*100, 60*60*24*7*4*12
      [5806080000, this.timeAgoLastCentury, this.timeAgoNextCentury], // 60*60*24*7*4*12*100*2
      [58060800000, this.timeAgoCenturies, 2903040000] // 60*60*24*7*4*12*100*20, 60*60*24*7*4*12*100
    ];

    var date = value;

    if (!Ext.isDate(date)){
      date = parseInt(date);
      if ( date == 'NaN' ){
        date = Ext.parseDate(value)
      } else {
        date = new Date(date);
      }
    }

    var seconds = (new Date - date) / 1000;
    var token = 'ago';
    var list_choice = 1;
    if (seconds < 0){
      seconds = Math.abs(seconds);
      token = 'from now';
      list_choice = 2;
    }
    
    var i = 0;
    var format = null;
    while (format = time_formats[i++]){
      if (seconds < format[0]) {
        if (typeof format[2] == 'string'){
          return format[list_choice];
        } else {
          return Math.floor(seconds / format[2]) + ' ' + format[1] + ' ' + token;
        }
      }
    }

    if ( date == "Invalid Date" ){
      date = value;
    }
    
    return date;
  }
  
});

Ext.ns('Sonia.util');

// clone method

Sonia.util.clone = function(obj) {
  var newObj = (this instanceof Array) ? [] : {};
  for (i in obj) {
    if (i == 'clone') continue;
    if (obj[i] && typeof obj[i] == "object") {
      newObj[i] = Sonia.util.clone(obj[i]);
    } else newObj[i] = obj[i]
  } return newObj;
};

// link

Sonia.util.Link = Ext.extend(Ext.BoxComponent, {

  constructor: function(config) {
    config = config || {};
    config.xtype = 'box';
    config.autoEl = { tag: 'a', html: config.text, href: '#' };
    Sonia.util.Link.superclass.constructor.apply(this, arguments);
    this.addEvents({
      'click': true,
      'mouseover': true,
      'blur': true
    });
    this.text = config.text;
  },
    
  onRender: function() {
    theLnk = this;
    this.constructor.superclass.onRender.apply(this, arguments);
    if (!theLnk.disabled) {
      this.el.on('blur', function(e) { theLnk.fireEvent('blur'); });
      this.el.on('click', function(e) { theLnk.fireEvent('click'); });
      this.el.on('mouseover', function(e) { theLnk.fireEvent('mouseover'); });
    }
  }

});

// register xtype
Ext.reg('link', Sonia.util.Link);

Sonia.util.getStringFromArray = function(array){
  var value = '';
  if ( Ext.isArray(array) ){
    for ( var i=0; i<array.length; i++ ){
      value += array[i];
      if ( (i+1)<array.length ){
        value += ', ';
      }
    }
  }

  return value;
}