/*
 Licensed to the Apache Software Foundation (ASF) under one or more
 contributor license agreements.  See the NOTICE file distributed with
 this work for additional information regarding copyright ownership.
 The ASF licenses this file to You under the Apache License, Version 2.0
 (the "License"); you may not use this file except in compliance with
 the License.  You may obtain a copy of the License at

     http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
*/

var set_healthcheck_status = function( status )
{
  var hc_button = $( '.healthcheck-status' )
  if ( status == 'enable' )
  {
    hc_button.parents( 'dd' )
      .removeClass( 'ico-0' )
      .addClass( 'ico-1' );
    hc_button
      .addClass( 'enabled' )
      .html( 'disable ping' );
  } else {
    hc_button.parents( 'dd' )
      .removeClass( 'ico-1')
      .addClass( 'ico-0' );
    hc_button
      .removeClass( 'enabled' )
      .html( 'enable ping' );
  }
};

sammy.bind(
  'stat_load_data',
  function(event, params) {
    var core_basepath = params.core_basepath;
    $.ajax(
    {
      url :config.url+ core_basepath + '/select/?wt=json&q=*:*',
      dataType : 'json',
      context : $( '#statistics', params.dashboard_element ),
      beforeSend : function( xhr, settings )
      {
        $( 'h2', this )
          .addClass( 'loader' );
                      
        $( '.message', this )
          .show()
          .html( 'Loading ...' );
                      
        $( '.content', this )
          .hide();
      },
      success : function( response, text_status, xhr )
      {
        $( '.message', this )
          .empty()
          .hide();
                      
        $( '.content', this )
          .show();
                          
        var data = {
          'index_num-docs' : response.data.numFound
        };
                      
        for( var key in data )
        {
          $( '.' + key, this )
            .show();
                          
          $( '.value.' + key, this )
            .html( data[key] );
        }
      },
      error : function( xhr, text_status, error_thrown )
      {
        this
          .addClass( 'disabled' );
                      
        $( '.message', this )
          .show()
          .html( 'Luke is not configured' );
      },
      complete : function( xhr, text_status )
      {
        $( 'h2', this )
          .removeClass( 'loader' );
      }
    });
  }
);

// #/:core
sammy.get
(
  new RegExp( app.core_regex_base + '$' ),
  function( context )
  {
    var core_basepath = this.active_core.attr( 'data-basepath' );
    var content_element = $( '#content' );
        
    content_element
      .removeClass( 'single' );
    
    if( !app.core_menu.data( 'admin-extra-loaded' ) )
    {
      app.core_menu.data( 'admin-extra-loaded', new Date() );
    }
        
    $.get
    (
      'tpl/dashboard.html',
      function( template )
      {
        content_element
          .html( template );
         var dashboard_element = $( '#dashboard', content_element );            
       // dashboard_element.append("<p>Some text and markup</p>")                             
        sammy.trigger(
          'stat_load_data', {
            core_basepath: core_basepath,
            dashboard_element: dashboard_element
          }
        );
                
        // clear button
        $( '#actions #clear', dashboard_element )
        .die( 'click' )
        .live
        (
          'click',
          function( event )
          {
            var ret = confirm( 'Do you really want to delete all data in "' + context.params.splat[0] + '"?' );
            if( !ret )
            {
              return false;
            }

            $.ajax
            (
              {
                url : config.url + core_basepath + '/delete?wt=json&q=*:*',
                dataType : 'json',
                context : $( this ),
                beforeSend : function( xhr, settings )
                {
                  $( 'span', this )
                    .addClass( 'loader' );
                },
                success : function( response, text_status, xhr )
                {
                  sammy.trigger(
                    'stat_load_data', {
                      core_basepath: core_basepath,
                      dashboard_element: dashboard_element
                    }
                  );
                },
                error : function( xhr, text_status, error_thrown )
                {
                },
                complete : function( xhr, text_status )
                {
                  $( 'span', this )
                    .removeClass( 'loader' );
                }
              }
            );
          }
        );

        // optimize button
        $( '#actions #optimize', dashboard_element )
        .die( 'click' )
        .live
        (
          'click',
          function( event )
          {
            var ret = confirm( 'Do you really want to optimize "' + context.params.splat[0] + '"? It is a time-consuming operator.' );
            if( !ret )
            {
              return false;
            }

            $.ajax
            (
              {
                url : config.url + core_basepath + '/optimize?wt=json',
                dataType : 'json',
                context : $( this ),
                beforeSend : function( xhr, settings )
                {
                  $( 'span', this )
                    .addClass( 'loader' );
                },
                success : function( response, text_status, xhr )
                {
                },
                error : function( xhr, text_status, error_thrown )
                {
                },
                complete : function( xhr, text_status )
                {
                  $( 'span', this )
                    .removeClass( 'loader' );
                }
              }
            );
          }
        );
                
      }
    );
  }
);
