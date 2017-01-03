package org.iipg.hurricane.jmx.client;

/*
 * 
 * Copyright 2014 Roland Huss
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


import org.json.simple.JSONObject;

/**
 * An extractor can be used to add special behaviour to the extraction process before a {@link JMXResponse} is created.
 * This is especially useful for advanced error handling e.g. for bulk requests.
 *
 * @author roland
 * @since 23/12/14
 */
public interface JMXResponseExtractor {

    /**
     * Extract a response object for the given request and the returned JSON structure
     *
     * @param request the original request
     * @param jsonResp the response as obtained from the agent
     * @param <RESP> response typ
     * @param <REQ> request type
     * @return the created response
     * @throws JMXRemoteException if the response is in an error state this exception should be thrown.
     */
    <RESP extends JMXResponse<REQ>, REQ extends JMXRequest> RESP extract(REQ request, JSONObject jsonResp) throws JMXRemoteException;
}
