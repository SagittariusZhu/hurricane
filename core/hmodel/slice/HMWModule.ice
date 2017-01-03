// **********************************************************************
//
// Copyright (c) 2009-2014 IIPG, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

#ifndef HMW_MODULE_ICE
#define HMW_MODULE_ICE

module org {
  	module iipg	{
		module hurricane {
			module model {
				exception HMWConnException
				{
				    string reason;
				    int errorCode;
				};
				
				dictionary<string, string> PropertyDict;
				sequence<byte> HMWBlob;
				
				struct HMWDocument
				{
					string schema;
					PropertyDict dict;
					HMWBlob blob;	
				};
				
				sequence<HMWDocument> HMWDocumentSeq;
				
				struct HMWResponse
				{
					int errorCode;
					string reason;
					int totalCount;
					int used;
					string dataType;
					HMWDocumentSeq data;
					HMWBlob blob;
				};
				
				struct HMWQuery {
					string schema;
					string highlightMode;
					string selectFields; 
					string highlightFields; 
					string orderByStr;
					string qStr;
					string uuid;
					string item;
					int rowStart;
					int rowCount;
				};
				
				struct HMWReportQuery {
					string schema;
					string groupByField;
					string havingClause;
					string qStr;
					int rowStart;
					int rowCount;
				};
			};
		};
	};
};

#endif