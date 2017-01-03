// **********************************************************************
//
// Copyright (c) 2009-2014 IIPG, Inc. All rights reserved.
//
// This copy of Ice is licensed to you under the terms described in the
// ICE_LICENSE file included in this distribution.
//
// **********************************************************************

#ifndef HMW_SERVICE_ICE
#define HMW_SERVICE_ICE

#include "HMWModule.ice"

module org {
  	module iipg	{
		module hurricane {
			module service {
				interface HBroker
				{
					// Insert
					model::HMWResponse add(model::HMWDocument doc) throws model::HMWConnException;
					model::HMWResponse addBatch(model::HMWDocumentSeq docs) throws model::HMWConnException;
					
					//update
					model::HMWResponse update(model::HMWDocument doc) throws model::HMWConnException;
					model::HMWResponse updateBatch(model::HMWDocumentSeq docs) throws model::HMWConnException;
					model::HMWResponse updateByQuery(model::HMWDocument doc, model::HMWQuery q) throws model::HMWConnException;
					
					//delete
					model::HMWResponse delete(model::HMWDocument doc) throws model::HMWConnException;
					model::HMWResponse deleteBatch(model::HMWDocumentSeq docs) throws model::HMWConnException;
					model::HMWResponse deleteByQuery(model::HMWQuery q) throws model::HMWConnException;
					
					// Query
					model::HMWResponse query(model::HMWQuery q) throws model::HMWConnException;
					model::HMWResponse reportQuery(model::HMWReportQuery q) throws model::HMWConnException;
					
					// Download
					model::HMWResponse getBinary(model::HMWQuery q) throws model::HMWConnException;
				};
			};		
		};
	};
};

#endif
