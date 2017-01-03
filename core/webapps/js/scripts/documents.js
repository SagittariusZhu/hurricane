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
//helper for formatting JSON and others
var content_generator = {

  _default: function (toEsc) {
    return toEsc.esc();
  },

  json: function (toEsc) {
    return app.format_json(toEsc);
  }

};

//Utiltity function for turning on/off various elements
function toggles(documents_form, show_type, doc_text) {
  var update_delete_only = $('#update-delete-only');
  var add_only = $('#add-only');
  var the_document = $('#document', documents_form);
  var wizard = $('#wizard', documents_form);
  if (show_type=="addDoc") {
    the_document.val(doc_text);
    the_document.show();
    wizard.show();
    add_only.show();
    update_delete_only.hide();
  } else if(show_type=="update"){
    the_document.val(doc_text);
    the_document.show();
    wizard.show();
    the_document.show();
    update_delete_only.show();
    add_only.hide();
  } else if(show_type=="delete"){
    wizard.hide();
    the_document.hide();
    update_delete_only.show();
    add_only.hide();
  }
  
}
// #/:core/documents

//Utiltity function for setting up the wizard fields
function addWizardFields(active_core, wizard) {
  var core_name = active_core.attr("title");
  var select_options = "";
  //Populate the select options based off the Fields REST API
  $.getJSON(app.config.url+app.config.solr_path + app.config.core_admin_path+"?wt=json&action=desc&name="+core_name).done(
      //TODO: handle dynamic fields
      //TODO: get the unique key, too
      function (data) {
        var field_select = $("#wiz-field-select", wizard);
        field_select.empty();
        $.each(data.data.cores[0].fields,
            function (i, item) {
              //console.log("i[" + i + "]=" + item.name);
              if (item.name != "_version_"){
                select_options += '<option name="' + item.name + '">'
                  + item.name + '</option>';
              }
            });
        //console.log("select_options: " + select_options);
        //fill in the select options
        field_select.append(select_options);
      });
      var wizard_doc = $("#wizard-doc", wizard);
      wizard_doc.die('focusin')
      .live('focusin', function (event) {
        $("#wizard-doc", wizard).text("");
      }
      );
  //Add the click handler for the "Add Field" target, which
  //takes the field content and moves it into the document target
    var add_field = $("#add-field-href", wizard);
    add_field.die("click")
      .live("click",
      function (event) {
        //take the field and the contents and append it to the document
        var wiz_select = $("#wiz-field-select", wizard);
        var selected = $("option:selected", wiz_select);
        console.log("selected field: " + selected);
        var wiz_doc = $("#wizard-doc", wizard);
        var the_document = $("#document");
        var current_doc = the_document.val();
        console.log("current_text: " + current_doc + " wiz_doc: " + wiz_doc.val());
        var index = current_doc.lastIndexOf("}");
        var new_entry = '"' + selected.val() + '":"' + wiz_doc.val() + '"';
        if (index >= 0) {
          current_doc = current_doc.substring(0, index) + ', ' + new_entry + "}";
        } else {
          //we don't have a doc at all
          current_doc = "{" + new_entry + "}";
        }
        current_doc = content_generator['json'](current_doc);
        the_document.val(current_doc);
        //clear the wiz doc window
        wiz_doc.val("");
        return false;
      }
  );

  //console.log("adding " + i + " child: " + child);

}

//The main program for adding the docs
sammy.get
(
    new RegExp(app.core_regex_base + '\\/(documents)$'),
    function (context) {
      var active_core = this.active_core;
      var core_basepath = active_core.attr('data-basepath');
      var content_element = $('#content');


      $.post
      (
          'tpl/documents.html',
          function (template) {

            content_element
                .html(template);
            var documents_element = $('#documents', content_element);
            var documents_form = $('#form form', documents_element);
            var url_element = $('#url', documents_element);
            var result_element = $('#result', documents_element);
            var response_element = $('#response', documents_element);
            var update_type_select = $('#update-type', documents_form);
            //Since we are showing "example" docs, when the area receives the focus
            // remove the example content.
            $('#document', documents_form).die('focusin')
                .live('focusin',
                function (event) {
                  var update_type = $('#update-type', documents_form).val();
                  if (update_type != "addDoc"){
                    //Don't clear the document when in wizard mode.
                    var the_document = $('#document', documents_form);
                    the_document.text("");
                  }
                }
            );

          update_type_select
                .die('change')
                .live
            (
                'change',
                function (event) {
                  var update_type = $('#update-type', documents_form).val();
                  //need to clear out any old file upload by forcing a redraw so that
                  //we don't try to upload an old file

                  if (update_type == "update") {
                    toggles(documents_form, "update", "");
                    $("#attribs").show();
                    $("#update-delete-only").show();
                  } else if (update_type == "delete") {
                    toggles(documents_form, "delete", "");
                     $("#attribs").show();
                    $("#update-delete-only").show();
                  }  else if (update_type == "addDoc") {
                    var wizard = $('#wizard', documents_form);
                    addWizardFields(active_core, wizard);
                    //$("#wizard-doc", wizard).text('Enter your field text here and then click "Add Field" to add the field to the document.');
                    toggles(documents_form, "addDoc","");
                    $("#attribs").show();
                    $("#add-only").show();
                    
                  } 
                  return false;
                }
            );
            update_type_select.chosen().trigger('change');
           
            //Setup the submit option handling.
            documents_form
                .die('submit')
                .live
            (
                'submit',
                function (event) {
                  var form_values = [];
                  var handler_path = $('#update-type', documents_form).val();
                  if ('/' !== handler_path[0]) {
                    handler_path = '/'+handler_path;
                  }

                  var document_url = app.config.url + core_basepath + handler_path;

                  url_element
                      .attr('href', document_url)
                      .text(document_url)
                      .trigger('change');
                  var the_document = $('#document', documents_form).val();
                  var wt = $('#wt', documents_form).val();
                  var autoUniqueKey = $('#autoUniqueKey', documents_form).val();
                  var q = $('#q', documents_form).val();
                  var update_type = $('#update-type', documents_form).val();

                  //Both JSON and Wizard use the same pathway for submission
                  //New entries primarily need to fill the_command and set the content_type
                  if (update_type == "addDoc" ) {
                    //create a JSON command
                   // document_url=document_url+"?wt="+wt+"&autoUniqueKey="+autoUniqueKey+"&data="+encodeURIComponent(the_document);
                   //Handle the submission of the form in the case where we are not uploading a file
                   $.ajax(
                        {
                          url: document_url,
                          //dataType : 'json',
                          processData: false,
                          type: 'POST',
                          contentType: "application/x-www-form-urlencoded",
                          data: "wt="+wt+"&autoUniqueKey="+autoUniqueKey+"&data="+encodeURIComponent(the_document),
                          context: response_element,
                          beforeSend: function (xhr, settings) {
                            console.log("beforeSend: Vals: " + document_url + " update-type: " + update_type );

                          },
                          success: function (response, text_status, xhr) {
                            console.log("success:  " + response + " status: " + text_status + " xhr: " + xhr.responseText);
                            result_element.html('<div><span class="description">Status</span>: ' + text_status + '</div>'
                                + '<div><span class="description">Response:</span>' + '<pre class="syntax language-json"><code>' + content_generator['json'](xhr.responseText) + "</code></pre></div>");
                            result_element.show();
                          },
                          error: function (xhr, text_status, error_thrown) {
                            console.log("error: " + text_status + " thrown: " + error_thrown);
                            result_element.html('<div><span class="description">Status</span>: ' + text_status + '</div><div><span class="description">Error:</span> '
                                + '' + error_thrown
                                + '</div>'
                                + '<div><span class="description">Error</span>:' + '<pre class="syntax language-json"><code>'
                                //+ content_generator['json'](xhr.responseText) +
                                + xhr.responseText +
                                '</code></pre></div>');
                            result_element.show();
                          },
                          complete: function (xhr, text_status) {
                            //console.log("complete: " + text_status + " xhr: " + xhr.responseText + " doc type: " + document_type);

                            //alert(text_status + ": " + xhr.responseText);
                            /*this
                             .removeClass( 'loader' );*/
                          }
                        }
                    );
                  } else if (update_type == "update") {

                   // document_url=document_url+"?wt="+wt+"&q="+q+"&data="+the_document;
 //Handle the submission of the form in the case where we are not uploading a file
                  $.ajax(
                        {
                          url: document_url,
                          //dataType : 'json',
                          processData: false,
                          type: 'POST',
                          contentType: "application/x-www-form-urlencoded",
                          data: "wt="+wt+"&q="+q+"&data="+encodeURIComponent(the_document),
                       
                          context: response_element,
                          beforeSend: function (xhr, settings) {
                            console.log("beforeSend: Vals: " + document_url + " update-type: " + update_type );

                          },
                          success: function (response, text_status, xhr) {
                            console.log("success:  " + response + " status: " + text_status + " xhr: " + xhr.responseText);
                            result_element.html('<div><span class="description">Status</span>: ' + text_status + '</div>'
                                + '<div><span class="description">Response:</span>' + '<pre class="syntax language-json"><code>' + content_generator['json'](xhr.responseText) + "</code></pre></div>");
                            result_element.show();
                          },
                          error: function (xhr, text_status, error_thrown) {
                            console.log("error: " + text_status + " thrown: " + error_thrown);
                            result_element.html('<div><span class="description">Status</span>: ' + text_status + '</div><div><span class="description">Error:</span> '
                                + '' + error_thrown
                                + '</div>'
                                + '<div><span class="description">Error</span>:' + '<pre class="syntax language-json"><code>'
                                //+ content_generator['json'](xhr.responseText) +
                                + xhr.responseText +
                                '</code></pre></div>');
                            result_element.show();
                          },
                          complete: function (xhr, text_status) {
                            //console.log("complete: " + text_status + " xhr: " + xhr.responseText + " doc type: " + document_type);

                            //alert(text_status + ": " + xhr.responseText);
                            /*this
                             .removeClass( 'loader' );*/
                          }
                        }
                    );
                  } else if (update_type == "delete") {

                    document_url=document_url+"?wt="+wt+"&q="+q;
 //Handle the submission of the form in the case where we are not uploading a file
                  $.ajax(
                        {
                          url: document_url,
                          //dataType : 'json',
                          processData: false,
                          type: 'GET',
                          contentType: "application/json",
                          //data: the_command,
                          context: response_element,
                          beforeSend: function (xhr, settings) {
                            console.log("beforeSend: Vals: " + document_url + " update-type: " + update_type );

                          },
                          success: function (response, text_status, xhr) {
                            console.log("success:  " + response + " status: " + text_status + " xhr: " + xhr.responseText);
                            result_element.html('<div><span class="description">Status</span>: ' + text_status + '</div>'
                                + '<div><span class="description">Response:</span>' + '<pre class="syntax language-json"><code>' + content_generator['json'](xhr.responseText) + "</code></pre></div>");
                            result_element.show();
                          },
                          error: function (xhr, text_status, error_thrown) {
                            console.log("error: " + text_status + " thrown: " + error_thrown);
                            result_element.html('<div><span class="description">Status</span>: ' + text_status + '</div><div><span class="description">Error:</span> '
                                + '' + error_thrown
                                + '</div>'
                                + '<div><span class="description">Error</span>:' + '<pre class="syntax language-json"><code>'
                                //+ content_generator['json'](xhr.responseText) +
                                + xhr.responseText +
                                '</code></pre></div>');
                            result_element.show();
                          },
                          complete: function (xhr, text_status) {
                            //console.log("complete: " + text_status + " xhr: " + xhr.responseText + " doc type: " + document_type);

                            //alert(text_status + ": " + xhr.responseText);
                            /*this
                             .removeClass( 'loader' );*/
                          }
                        }
                    );
                  }  else {
                    //How to handle other?
                  }

                    return false;
                }
            );
          }
      )
    }
)
