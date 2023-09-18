package com.cg.bms;

import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectClasses({com.cg.bms.BmsApplicationTests.class,
                com.cg.bms.QueryTests.class,
                com.cg.bms.MutationTests.class,
                com.cg.bms.PostServiceImplTest.class,
                com.cg.bms.PostGraphQLControllerIntTest.class})
public class BmsApplicationTestSuite {}
