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

var current_core = null;
var cookie_schema_browser_autoload = 'schema-browser_autoload';

sammy.bind
(
  'schema_update_load_template',
  function( event, params )
  {
    if( app.schema_update_template )
    {
      params.callback();
      return true;
    }

    $.get
    (
      'tpl/schema-update.html',
      function( template )
      {

        params.content_element
          .html( template );

        var dialog_form = $('#dialog-form', params.content_element).dialog({
          autoOpen: false,
          height: 600,
          width: 550,
          modal: true,
          buttons: {
            "Create/Edit": function() {
              if ($("#ch_name").val().trim() == '')
                alert("Field Name is not empty! Please input it first.");
              else {
                commit();
                $(this).dialog("close");
              }
            },
            Cancel: function() {
              $(this).dialog("close");
            }
          },
          close: function() {
            //allFields.val( "" ).removeClass( "ui-state-error" );
            ;
          }
        });

        if (!$('input[type=checkbox]', dialog_form).next().hasClass('tzCheckbox'))
          $('input[type=checkbox]', dialog_form).tzCheckbox({labels:['True','False']});

        $('li:odd', dialog_form).addClass('odd');

        $("#create-field")
          .button()
          .click(function() {
            dialog_open();
          });

        app.schema_update_template = template;   
        params.callback();
      }
    );
  }
);

sammy.bind(
  'schema_update_load_data',
  function(event, params) {
    var core_basepath = params.active_core.attr('data-basepath');
    var content_element = $('#content');
    var current_core_name = params.active_core[0].id;

    content_element
      .html('<div id="schema-update"><div class="loader">Loading ...</div></div>');

    $.ajax({
      url: app.config.url + app.config.solr_path + app.config.core_admin_path + "?wt=json&action=desc&name=" + current_core_name,
      dataType: 'json',
      beforeSend: function(xhr, settings) {},
      success: function(response, text_status, xhr) {
        app.schema_browser_data = {
          name: null,
          desc: null,
          fields: {}
        };

        app.schema_browser_data.name = response.data.cores[0].name;
        app.schema_browser_data.desc = response.data.cores[0].desc;
        app.schema_browser_data.fields = response.data.cores[0].fields;

        params.success( app.sources_data );
      },

      error : function( xhr, text_status, error_thrown)
      {
      },
      complete : function( xhr, text_status )
      {
      }
    });
  }
);          

sammy.bind(
  'schema_update_build_table',
  function(event, params) {
    var schema_table = params.schema_table;

    var tbody = '';

    var fields_name = ["indexed", "stored", "unique", "required", "combine", "multiValued", "type", "mode"];

    var flags_arr = [];
    for( var key in fields_name )
    {
      flags_arr.push( '<th data-key="' + key + '">' + fields_name[key] + '</th>' );
    }

    $( 'thead tr', schema_table )
      .append( flags_arr.join( "\n" ) );

    var flags_body = $( 'tbody', schema_table );
    flags_body.empty();

    for (var i = 0; i < app.schema_browser_data.fields.length; i++) {
      var field = app.schema_browser_data.fields[i];
      var flags_arr = generate_flags_row( field );

      flags_body
        .append( '<tr>' + flags_arr.join( "\n" ) + '</tr>' );

      var self = flags_body;
      $( '.trigger', self )
        .die( 'click' )
        .live
        (
          'click',
          function( event )
          {
            dialog_open(this);
            return false;
          }
        );

    }
    $(schema_table).addClass('tablesorter');
    $(schema_table).tablesorter({
        sortList: [[3,1], [0,0]] 
    }); 
  }
);

// #/:core/schema-update
sammy.get(
  new RegExp(app.core_regex_base + '\\/(schema-update)$'),

  function(context) {

    var content_element = $( '#content' );

    var core_basepath = this.active_core.attr('data-basepath');
    current_core = context.params.splat[0];

    delete app.schema_update_template;

    delete app.schema_browser_data;

    sammy.trigger(
      'schema_update_load_data',
      {
        active_core: this.active_core,
        error : function()
        {
          context.redirect( '#/' + current_core );
        },
        success : function( sources )
        {
          sammy.trigger(
            'schema_update_load_template',
            {
              content_element : content_element,
              callback : function()
              {
                var schema_update_element = $('#schema-update', content_element);
                var related_element = $('#related', schema_update_element);
                var related_select_element = $('select', related_element);
                var data_element = $('#data', schema_update_element);

                var schema_table = $('#schema', schema_update_element);

                sammy.trigger
                (
                  'schema_update_build_table',
                  {
                    schema_table : schema_table
                  }
                );
              }
            }
          );
        }
    });
  }
);

function generate_flags_row( field )
{
  var flags_arr = [ '<th><a href="#" class="trigger"><span class="name">' + field.name.esc() + '</span></a></th>' ];

  var flags_str = field.flag;

  var i = 0;
  for( var key = 0; key <= 5; key ++ )
  {
    var flag_match = !('-' === flags_str.charAt(key));

    var flag_cell = '<td '
                  + ' data-key="' + key + '"'
                  + ' class="' + ( flag_match ? 'check' : '' ) + '"'
                  + '>'
                  + ( flag_match ? '<span>√</span>' : '&nbsp;' )
                  + '</td>';

    flags_arr.push( flag_cell );
    i++;
  }

  var flag_cell = '<td '
                    + ' data-key="' + field.type + '"'
                    + ' class="type"'
                    + '>'
                    + '<span>' + field.type + '</span>'
                    + '</td>';

  flags_arr.push( flag_cell );

  flag_cell = '<td '
                    + ' data-key="' + field.mode + '"'
                    + ' class="' + ( 'rw' === field.mode ? 'mysql' : '' ) + '"'
                    + '>'
                    + ( 'rw' === field.mode ? '<span>mysql</span>' : '&nbsp;' )
                    + '</td>';

  flags_arr.push( flag_cell );

  return flags_arr;
};

function commit() {
  var fname = $("#ch_name").val();
  var indexed = $("#ch_indexed").attr('checked') == 'checked';
  var stored = $("#ch_stored").attr('checked') == 'checked';
  var unique = $("#ch_unique").attr('checked') == 'checked';
  var required = $("#ch_required").attr('checked') == 'checked';
  var combine = $("#ch_combine").attr('checked') == 'checked';
  var multiValued = $("#ch_multivalued").attr('checked') == 'checked';
  var type = $("#ch_type").val();
  var mode = ($("#ch_mode").attr('checked') == 'checked') ? 'ro' : 'rw';
  var rowindex = $("#rowindex");  
  var update = "<fields><field name='" + fname
            + "' type='" + type
            + "' indexed='" + indexed
            + "' stored='" + stored
            + "' multiValued='" + multiValued
            + "' unique='" + unique
            + "' combine='" + combine
            + "' required='" + required
            + "' mode='" + mode
            + "' ></field></fields>";
  $.ajax({
    url: app.config.url + app.config.solr_path + app.config.core_admin_path + '?wt=json&action=UPDATE&name=' + app.schema_browser_data.name + '&stream.body=' + update,
    dataType: 'json',
    context: $(this),
    beforeSend: function(xhr, settings) {
      $('span', this)
        .addClass('loader');
    },
    success: function(response, text_status, xhr) {
      if (response.responseHeader.status == 0) {
        //destory dialog for avoid memory leak
        $('#dialog-form').remove();

        sammy.refresh();
        /*
        if (rowindex.val() == "") { //新增
          $("#schema tbody").append(generate_flags_row(update, fname));
        } else { //修改
          var idx = rowindex.val();
          var tr = $("#schema>tbody>tr").eq(idx);
          tr.children().eq(0).text(name.val());
          tr.children().eq(1).text(indexed.val());
          tr.children().eq(2).text(stored.val());
          tr.children().eq(3).text(unique.val());
          tr.children().eq(4).text(required.val());
          tr.children().eq(5).text(combine.val());
          tr.children().eq(6).text(multiValued.val());
          tr.children().eq(7).text(type.val());
          tr.children().eq(8).text(mode.val());
        }*/
      } else if (response.responseHeader.status == 500) {
        alert(response.responseHeader.message);
      }

    },
    error: function(xhr, text_status, error_thrown) {

    },
    complete: function(xhr, text_status) {
      $('span', this)
        .removeClass('loader');
    }
  });
};

function dialog_open( record ) {

  var create_field = (record == undefined);
  var field_selector = $( '#field-selector' );
  field_selector.find( '#create-new-field' ).toggle( create_field );
  field_selector.find( '#modify-exist-field' ).toggle( !create_field );

  var name = $("#modify-exist-field span");
  var indexed = $("#ch_indexed");
  var stored = $("#ch_stored");
  var unique = $("#ch_unique");
  var required = $("#ch_required");
  var combine = $("#ch_combine");
  var multiValued = $("#ch_multiValued");
  var type = $("#ch_type");
  var mode = $("#ch_mode");
  var rowindex = $("#rowindex");


  if (!create_field) {
    var b = $(record);
    var tr = b.parents("tr");
    var tds = tr.children();

    name.text(tds.eq(0).text());

    //indexed.setValue($(tds.eq(1)).hasClass('check'));
    if ((indexed.attr('checked') == 'checked') != $(tds.eq(1)).hasClass('check'))
      indexed.trigger('change');

    if ((stored.attr('checked') == 'checked') != $(tds.eq(2)).hasClass('check'))
      stored.trigger('change');

    if ((unique.attr('checked') == 'checked') != $(tds.eq(3)).hasClass('check'))
      unique.trigger('change');

    if ((required.attr('checked') == 'checked') != $(tds.eq(4)).hasClass('check'))
      required.trigger('change');

    if ((combine.attr('checked') == 'checked') != $(tds.eq(5)).hasClass('check'))
      combine.trigger('change');

    if ((multiValued.attr('checked') == 'checked') != $(tds.eq(6)).hasClass('check'))
      multiValued.trigger('change');

    type.val(tds.eq(7).text());

    if ((mode.attr('checked') == 'checked') == $(tds.eq(8)).hasClass('mysql'))
      mode.trigger('change');

    var trs = b.parents("tbody").children();
    //设置行号，以行号为标识，进行修改。
    rowindex.val(trs.index(tr));
  }


  //打开对话框
  $("#dialog-form").dialog("open");

  return false;
};

/*

            //新增修改字段

              allFields = $([]).add(name).add(indexed).add(stored).add(unique).add(required).add(combine).add(multiValued).add(type).add(mode).add(rowindex);

*/
