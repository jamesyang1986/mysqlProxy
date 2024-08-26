package com.qiezi.mysqlproxy.server;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;


class BackendConnectionTest {

    @Before
    void setUp() {
    }

    @After
    void tearDown() {
    }

    @Test
    void executeSql() {
        BackendConnection connection = new BackendConnection("127.0.0.1", 3306, "root", "123qweasd");
        connection.executeSql(" show databases; ");
    }
}