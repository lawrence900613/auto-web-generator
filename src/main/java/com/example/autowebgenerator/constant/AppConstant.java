package com.example.autowebgenerator.constant;

public interface AppConstant {

    /** Priority value that marks an app as featured on the homepage. */
    Integer GOOD_APP_PRIORITY = 99;

    /** Default priority for newly created apps. */
    Integer DEFAULT_APP_PRIORITY = 0;

    /** Root directory for all AI-generated code output. */
    String CODE_OUTPUT_ROOT_DIR = System.getProperty("user.dir") + "/tmp/code_output";
}
