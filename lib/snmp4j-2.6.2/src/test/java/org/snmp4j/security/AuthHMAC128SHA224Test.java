package org.snmp4j.security;

import junit.framework.TestCase;

public class AuthHMAC128SHA224Test extends TestCase {

    public void testAuthHMAC128SHA224() throws Exception {
        AuthHMAC128SHA224 authHMAC128SHA224 = new AuthHMAC128SHA224();
        assertTrue(authHMAC128SHA224.isSupported());
    }

}