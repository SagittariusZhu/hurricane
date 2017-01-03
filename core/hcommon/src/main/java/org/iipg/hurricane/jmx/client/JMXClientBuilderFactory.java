package org.iipg.hurricane.jmx.client;

/*
 * Copyright 2009-2011 Roland Huss
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

import org.apache.http.client.CookieStore;
import org.jolokia.client.request.J4pResponseExtractor;

/**
 * Hook class for easily creating J4pClient with the a builder.
 *
 * @author roland
 * @since 23.09.11
 */
public abstract class JMXClientBuilderFactory {

    protected JMXClientBuilderFactory() {}

    /** See {@link JMXClientBuilder#url} */
    public static JMXClientBuilder url(String pUrl) {
        return new JMXClientBuilder().url(pUrl);
    }

    /** See {@link JMXClientBuilder#user} */
    public static JMXClientBuilder user(String pUser) {
        return new JMXClientBuilder().user(pUser);
    }

    /** See {@link JMXClientBuilder#password} */
    public static JMXClientBuilder password(String pPassword) {
        return new JMXClientBuilder().password(pPassword);
    }

    /** See {@link JMXClientBuilder#singleConnection()} */
    public static JMXClientBuilder singleConnection() {
        return new JMXClientBuilder().singleConnection();
    }

    /** See {@link JMXClientBuilder#pooledConnections()} */
    public static JMXClientBuilder pooledConnections() {
        return new JMXClientBuilder().pooledConnections();
    }

    /** See {@link JMXClientBuilder#connectionTimeout(int)} */
    public static JMXClientBuilder connectionTimeout(int pTimeOut) {
        return new JMXClientBuilder().connectionTimeout(pTimeOut);
    }

    /** See {@link JMXClientBuilder#socketTimeout(int)} */
    public static JMXClientBuilder socketTimeout(int pTimeOut) {
        return new JMXClientBuilder().socketTimeout(pTimeOut);
    }

    /** See {@link JMXClientBuilder#maxTotalConnections(int)} */
    public static JMXClientBuilder maxTotalConnections(int pConnections) {
        return new JMXClientBuilder().maxTotalConnections(pConnections);
    }

    /** See {@link JMXClientBuilder#maxConnectionPoolTimeout(int)} */
    public static JMXClientBuilder maxConnectionPoolTimeout(int pConnectionPoolTimeout) {
        return new JMXClientBuilder().maxConnectionPoolTimeout(pConnectionPoolTimeout);
    }

    /** See {@link JMXClientBuilder#contentCharset(String)} */
    public static JMXClientBuilder contentCharset(String pContentCharset) {
        return new JMXClientBuilder().contentCharset(pContentCharset);
    }

    /** See {@link JMXClientBuilder#expectContinue(boolean)} */
    public static JMXClientBuilder expectContinue(boolean pUse) {
        return new JMXClientBuilder().expectContinue(pUse);
    }

    /** See {@link JMXClientBuilder#tcpNoDelay(boolean)} */
    public static JMXClientBuilder tcpNoDelay(boolean pUse) {
        return new JMXClientBuilder().tcpNoDelay(pUse);
    }

    /** See {@link JMXClientBuilder#socketBufferSize(int)} */
    public static JMXClientBuilder socketBufferSize(int pSize) {
        return new JMXClientBuilder().socketBufferSize(pSize);
    }

    /** See {@link JMXClientBuilder#cookieStore(CookieStore)} */
    public static JMXClientBuilder cookieStore(CookieStore pStore) {
        return new JMXClientBuilder().cookieStore(pStore);
    }

    /** See {@link JMXClientBuilder#authenticator(J4pAuthenticator)} */
//    public static JMXClientBuilder authenticator(J4pAuthenticator pAuthenticator) {
//        return new JMXClientBuilder().authenticator(pAuthenticator);
//    }

    public static JMXClientBuilder responseExtractor(JMXResponseExtractor pExtractor) {
        return new JMXClientBuilder().responseExtractor(pExtractor);
    }
}
