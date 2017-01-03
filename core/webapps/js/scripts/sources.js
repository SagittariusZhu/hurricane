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

var template_xml = "<?xml version='1.0' encoding='UTF-8' ?>\n" +
                   "<schema name='schema1'  version='1.5' desc='表描述信息' shards='3' factor='2' maxShardsPerNode='2'>\n" +
                   "  <fields>\n" + 
                   "     <field name='messageid' type='string' indexed='true' stored='true'  multiValued='false'  required='true' unique='true'></field>\n" +
                   "  </fields>\n" +
                   "</schema>";
sammy.bind
(
  'sources_load_data',
  function( event, params )
  {
    $.ajax
    (
      {
        url : config.url+config.solr_path + app.config.core_admin_path + '?wt=json',
        dataType : 'json',
        beforeSend : function( xhr, settings )
        {
        },
        success : function( response, text_status, xhr )
        {
          if( params.only_failures )
          {
            app.check_for_init_failures( response );
            return true;
          }

          var has_sources = true;
          /*
          for( source in response.data.sources )
          {
            has_sources = true; 
            break;
          }*/

          app.set_sources_data( response );
          
          if( has_sources )
          {
            params.success( app.sources_data );
          }
          else
          {
            params.error();
          }
        },
        error : function( xhr, text_status, error_thrown)
        {
        },
        complete : function( xhr, text_status )
        {
        }
      }
    );
  }
);

sammy.bind
(
  'sources_build_navigation',
  function( event, params )
  {
    var navigation_content = ['<ul>'];

    for( var i=0;i<params.sources.length;i++ )
    {
      source=params.sources[i];
      var source_name = source.name;
      if( !source_name )
      {
        source_name = '<em>(empty)</em>';
      }
      navigation_content.push( '<li><a href="' + params.basepath + source.name + '">' + source_name + '</a></li>' );
    }

    params.navigation_element
      .html( navigation_content.join( "\n" ) );
        
    $( 'a[href="' + params.basepath + params.current_source + '"]', params.navigation_element ).parent()
      .addClass( 'current' );
  }
);

sammy.bind
(
  'sources_load_template',
  function( event, params )
  {
    if( app.sources_template )
    {
      params.callback();
      return true;
    }

    $.get
    (
      'tpl/sources.html',
      function( template )
      {
        params.content_element
          .html( template );
             
        app.sources_template = template;   
        params.callback();
      }
    );
  }
);

sammy.bind
(
  'sources_load_property',
  function( event, params )
  {
    $.ajax
    (
      {
        //url : config.url+config.solr_path + app.config.core_admin_path + '?wt=json&action=property&name=' + params.current_source,
        url: app.config.solr_path + '/admin/properties?wt=json',
        dataType : 'json',
        beforeSend : function( xhr, settings )
        {
        },
        success : function( response, text_status, xhr )
        {
          /*
          var has_sources = false;
          for( source in response.data.sources )
          {
            has_sources = true; 
            break;
          }
          
          if( has_sources )
          {
            params.success( response.data.sources );
          }
          */

          params.success( response );

        },
        error : function( xhr, text_status, error_thrown)
        {
        },
        complete : function( xhr, text_status )
        {
        }
      }
    );
  }
);

// #/~sources
sammy.get
(
  /^#\/(~sources)$/,
  function( context )
  {
    delete app.sources_template;
    var content_element = $( '#content' );

    sammy.trigger
    (
      'sources_load_data',
      {
        success : function( sources )
        {
          var first_source = null;
          for( var i=0;i<sources.length;i++ )
          {
            key=sources[i];
            if( !first_source )
            {
              first_source = key;
            }
            continue;
          }
          context.redirect( context.path + '/' + first_source.name );
        },
        error : function()
        {
          sammy.trigger
          (
            'sources_load_template',
            {
              content_element : content_element,
              callback : function()
              {
                var sources_element = $( '#sources', content_element );
                var navigation_element = $( '#navigation', sources_element );
                var data_element = $( '#data', sources_element );
                var core_data_element = $( '#core-data', data_element );
                var index_data_element = $( '#index-data', data_element );

                // layout

                var ui_block = $( '#ui-block' );
                var actions_element = $( '.actions', sources_element );
                var div_action = $( 'div.action', actions_element );
                var textarea_element = $( 'form #add_schema', div_action );

                textarea_element.val(template_xml);

                ui_block
                  .css( 'opacity', 0.7 )
                  .width( sources_element.width() + 10 )
                  .height( sources_element.height() );


                $( 'button.action', actions_element )
                  .die( 'click' )
                  .live
                  (
                    'click',
                    function( event )
                    {
                      var self = $( this );

                      self
                        .toggleClass( 'open' );

                      $( '.action.' + self.attr( 'id' ), actions_element )
                        .trigger( 'open' );

                      return false;
                    }
                  );

                div_action
                  .die( 'close' )
                  .live
                  (
                    'close',
                    function( event )
                    {
                      div_action.hide();
                      ui_block.hide();
                    }
                  )
                  .die( 'open' )
                  .live
                  (
                    'open',
                    function( event )
                    {
                      var self = $( this );
                      var rel = $( '#' + self.data( 'rel' ) );

                      self
                        .trigger( 'close' )
                        .show()
                        .css( 'left', rel.position().left );
                      
                      ui_block
                        .show();
                    }
                  );

                $( 'form button.reset', actions_element )
                  .die( 'click' )
                  .live
                  (
                    'click',
                    function( event )
                    {
                      $( this ).closest( 'div.action' )
                        .trigger( 'close' );
                    }
                  );

                $( 'form', div_action )
                  .ajaxForm
                  (
                    {
                      url : app.config.solr_path + app.config.core_admin_path + '?wt=json&indexInfo=false',
                      dataType : 'json',
                      beforeSubmit : function( array, form, options )
                      {
                        $( 'button[type="submit"] span', form )
                          .addClass( 'loader' );
                      },
                      success : function( response, status_text, xhr, form )
                      {
                        delete app.sources_data;
                        sammy.refresh();

                        $( 'button.reset', form )
                          .trigger( 'click' );
                      },
                      error : function( xhr, text_status, error_thrown )
                      {
                        var response = null;
                        eval( 'response = ' + xhr.responseText + ';' );

                        var error_elem = $( '.error', div_action.filter( ':visible' ) );
                        error_elem.show();
                        $( 'span', error_elem ).text( response.error.msg );
                      },
                      complete : function()
                      {
                        $( 'button span.loader', actions_element )
                          .removeClass( 'loader' );
                      }
                    }
                  );

                // --

                $( '#add', content_element )
                  .trigger( 'click' );

                $( '[data-rel="add"] input[type="text"]:first', content_element )
                  .focus();
              }
            }
          );
        }
      }
    );
  }
);

// #/~sources
sammy.get
(
  /^#\/(~sources)\//,
  function( context )
  {
    var content_element = $( '#content' );

    var path_parts = this.path.match( /^(.+\/~sources\/)(.*)$/ );
    var current_source = path_parts[2];

    sammy.trigger
    (
      'sources_load_data',
      {
        error : function()
        {
          context.redirect( '#/' + context.params.splat[0] );
        },
        success : function( sources )
        {
          sammy.trigger
          (
            'sources_load_template',
            {
              content_element : content_element,
              callback : function()
              {
                var sources_element = $( '#sources', content_element );

                var navigation_element = $( '#navigation', sources_element );

                var property_element = $( '#source-properties', sources_element );

                var form_add_element = $('div.add', sources_element);
                var form_config_element = $('div.config', sources_element);

                sources_element
                  .removeClass( 'empty' );

                // build source list in left
                sammy.trigger
                (
                  'sources_build_navigation',
                  {
                    sources : sources,
                    basepath : path_parts[1],
                    current_source : current_source,
                    navigation_element : navigation_element
                  }
                );

                var source_data = sources[current_source];
                var source_basepath = $( '#' + current_source, app.menu_element ).attr( 'data-basepath' );
                
                // load current source property in right
                sammy.trigger
                (
                  'sources_load_property',
                  {
                    basepath : path_parts[1],
                    current_source : current_source,
                    property_element : property_element,
                    success : function( response )
                    {
                      var system_properties = response['data']['system.properties'];
                      var properties_data = {};
                      var properties_content = [];
                      var properties_order = [];

                      /*
                      var workaround = xhr.responseText.match( /"(line\.separator)"\s*:\s*"(.+?)"/ );
                      if( workaround && workaround[2] )
                      {
                        system_properties[workaround[1]] = workaround[2];
                      }
                      */

                      for( var key in system_properties )
                      {
                        var displayed_key = key.replace( /\./g, '.&#8203;' );
                        var displayed_value = [ system_properties[key] ];
                        var item_class = 'clearfix';

                        if( -1 !== key.indexOf( '.path' ) || -1 !== key.indexOf( '.dirs' ) )
                        {
                          displayed_value = system_properties[key].split( system_properties['path.separator'] );
                          if( 1 < displayed_value.length )
                          {
                            item_class += ' multi';
                          }
                        }

                        var item_content = '<li><dl class="' + item_class + '">' + "\n"
                                         + '<dt>' + displayed_key.esc() + '</dt>' + "\n";

                        for( var i in displayed_value )
                        {
                          item_content += '<dd>' + displayed_value[i].esc() + '</dd>' + "\n";
                        }

                        item_content += '</dl></li>';

                        properties_data[key] = item_content;
                        properties_order.push( key );
                      }

                      properties_order.sort();
                      for( var i in properties_order )
                      {
                        properties_content.push( properties_data[properties_order[i]] );
                      }

                      property_element
                        .html( '<ul>' + properties_content.join( "\n" ) + '</ul>' );
                                
                      $( '.timeago', property_element )
                        .timeago();

                      $( 'li:odd', property_element )
                        .addClass( 'odd' );
                                
                      $( '.multi dd:odd', property_element )
                        .addClass( 'odd' );
                    }
                  }
                );


                /*

                var core_names = [];
                var core_selects = $( '#actions select', sources_element );

                for( var i=0;i<sources.length;i++)
                {
                  key=sources[i];
                  core_names.push( '<option value="' + key.name + '">' + key.name + '</option>' )
                }

                core_selects
                  .html( core_names.join( "\n") );

                $( 'option[value="' + current_core + '"]', core_selects.filter( '.other' ) )
                  .remove();
                
                $( 'input[data-core="current"]', sources_element )
                  .val( current_core );
                */


                // layout

                var ui_block = $( '#ui-block' );
                var actions_element = $( '.actions', sources_element );
                var div_action = $( 'div.action', actions_element );

                ui_block
                  .css( 'opacity', 0.7 )
                  .width( sources_element.width() + 10 )
                  .height( sources_element.height() );

                //var textarea_element = $( 'form #add_schema', div_action );
                //textarea_element.val(template_xml);


                $( 'button.action', actions_element )
                  .die( 'click' )
                  .live
                  (
                    'click',
                    function( event )
                    {
                      var self = $( this );

                      self
                        .toggleClass( 'open' );

                      $( '.action.' + self.attr( 'id' ), actions_element )
                        .trigger( 'open' );

                      return false;
                    }
                  );

                div_action
                  .die( 'close' )
                  .live
                  (
                    'close',
                    function( event )
                    {
                      div_action.hide();
                      ui_block.hide();
                    }
                  )
                  .die( 'open' )
                  .live
                  (
                    'open',
                    function( event )
                    {
                      var self = $( this );
                      var rel = $( '#' + self.data( 'rel' ) );

                      self
                        .trigger( 'close' )
                        .show()
                        .css( 'left', rel.position().left );
                      
                      ui_block
                        .show();
                    }
                  );

                $( 'form button.reset', actions_element )
                  .die( 'click' )
                  .live
                  (
                    'click',
                    function( event )
                    {
                      $( this ).closest( 'div.action' )
                        .trigger( 'close' );
                    }
                  );

                var form_callback = {

                  rename : function( form, response )
                  {
                    var url = path_parts[1] + $( 'input[name="other"]', form ).val();
                    context.redirect( url );
                  }

                };

                $( 'form', div_action )
                  .ajaxForm
                  (
                    {
                      url : app.config.url+app.config.solr_path + app.config.core_admin_path ,
                      dataType : 'json',
                      beforeSubmit : function( array, form, options )
                      {
                        $( 'button[type="submit"] span', form )
                          .addClass( 'loader' );
                      },
                      success : function( response, status_text, xhr, form )
                      {
                        var action = $( 'input[name="action"]', form ).val().toLowerCase();

                        delete app.sources_data;

                        if( form_callback[action] )
                        {
                         form_callback[action]( form, response ); 
                        }
                        else
                        {
                          sammy.refresh();
                        }

                        $( 'button.reset', form )
                          .trigger( 'click' );
                      },
                      error : function( xhr, text_status, error_thrown )
                      {
                        var response = null;
                        eval( 'response = ' + xhr.responseText + ';' );

                        var error_elem = $( '.error', div_action.filter( ':visible' ) );
                        error_elem.show();
                        $( 'span', error_elem ).text( response.error.msg );
                      },
                      complete : function()
                      {
                        $( 'button span.loader', actions_element )
                          .removeClass( 'loader' );
                      }
                    }
                  );
                           
                //delete     
                $( '#actions #delete', sources_element )
                  .die( 'click' )
                  .live
                  (
                    'click',
                    function( event )
                    {
                      var ret = confirm( 'Do you really want to delete source "' + current_core + '"?' );
                      if( !ret )
                      {
                        return false;
                      }

                      $.ajax
                      (
                        {
                          url : app.config.url+app.config.solr_path + app.config.core_admin_path + '?wt=json&action=DELETE&name=' + current_core,
                          dataType : 'json',
                          context : $( this ),
                          beforeSend : function( xhr, settings )
                          {
                            $( 'span', this )
                              .addClass( 'loader' );
                          },
                          success : function( response, text_status, xhr )
                          {
                            delete app.sources_data;
                            context.redirect( path_parts[1].substr( 0, path_parts[1].length - 1 ) );
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
            }
          );
        }
      }
    );
  }
);