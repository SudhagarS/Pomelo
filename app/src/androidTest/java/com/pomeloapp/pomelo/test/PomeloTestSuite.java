package com.pomeloapp.pomelo.test;

import android.test.suitebuilder.TestSuiteBuilder;

import junit.framework.Test;
import junit.framework.TestSuite;

public class PomeloTestSuite extends TestSuite
{

    public static Test suite()
    {
        return new TestSuiteBuilder(PomeloTestSuite.class)
                .includeAllPackagesUnderHere().build();
    }

    public PomeloTestSuite()
    {
        super();
    }
}