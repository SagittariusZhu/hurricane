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

require
(
  [
    'lib/order!lib/console',
    'lib/order!jquery',
    'lib/order!lib/jquery-ui-1.8.21.custom',
    'lib/order!lib/jquery.cookie',
    'lib/order!lib/jquery.form',
    'lib/order!lib/jquery.jstree',
    'lib/order!lib/jquery.sammy',
    'lib/order!lib/jquery.prettyxml',
    'lib/order!lib/jquery.timeago',
    'lib/order!lib/jquery.ajaxfileupload',
    'lib/order!lib/jquery.blockUI',
    'lib/order!lib/jquery.jqplot',
    'lib/order!lib/jquery.tablesorter',
    'lib/order!lib/jquery.tzCheckbox',
    'lib/order!lib/highlight',
    'lib/order!lib/highcharts',
    'lib/order!lib/linker',
    'lib/order!lib/ZeroClipboard',
    'lib/order!lib/d3',
    'lib/order!lib/chosen',
    'lib/order!scripts/app',

    'lib/order!lib/plugins/jqplot.barRenderer',
    'lib/order!lib/plugins/jqplot.pieRenderer',

    'lib/order!scripts/analysis',
    'lib/order!scripts/cores',
    'lib/order!scripts/sources',
    'lib/order!scripts/documents',
    'lib/order!scripts/datasync',
    'lib/order!scripts/dataimport',
    'lib/order!scripts/dashboard',
    'lib/order!scripts/index',
//    'lib/order!scripts/instances',
    'lib/order!scripts/plugins',
    'lib/order!scripts/properties',
    'lib/order!scripts/query',
    'lib/order!scripts/schema-update',
    'lib/order!scripts/subscribe',
  ],
  function( $ )
  {
    app.run();
  }
);
